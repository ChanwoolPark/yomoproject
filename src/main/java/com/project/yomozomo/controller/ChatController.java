package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // 혹은 com.project.yomozomo.entity.Rental
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor; // 만약 @RequiredArgsConstructor 사용한다면
import org.springframework.beans.factory.annotation.Autowired; // 만약 @Autowired 생성자 주입 사용한다면
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/chat") // 이 컨트롤러의 기본 경로는 "/chat" 입니다.
// @RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;

    @Autowired
    public ChatController(ChatService chatService, UserService userService, RentalService rentalService) {
        this.chatService = chatService;
        this.userService = userService;
        this.rentalService = rentalService;
    }


    @GetMapping("/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           Principal principal,
                           Model model) {

        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername);

        ChatRoom chatRoom = chatService.getChatRoomById(roomId);

        boolean isBuyer = currentUser.getId().equals(chatRoom.getBuyer().getId());
        boolean isSeller = currentUser.getId().equals(chatRoom.getSeller().getId());

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("현재 사용자는 이 채팅방에 접근할 권한이 없습니다.");
        }

        String chatPartnerNickname;
        if (isBuyer) {
            chatPartnerNickname = chatRoom.getSeller().getNickname();
        } else {
            chatPartnerNickname = chatRoom.getBuyer().getNickname();
        }

        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("chatPartnerNickname", chatPartnerNickname);
        model.addAttribute("chatRoomId", roomId);

        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRentalId", chatRoom.getRental().getRentalId());
        } else {
            model.addAttribute("currentRentalId", null);
        }

        // ⭐ 프로필 이미지 URL 추가 로직 (이전 대화에서 논의했던 부분)
        String userProfileImageUrl = currentUser.getProfileImageUrl();
        if (userProfileImageUrl == null || userProfileImageUrl.isEmpty()) {
            userProfileImageUrl = "/images/default-profile.png";
        }
        model.addAttribute("currentUserprofile_image", userProfileImageUrl);
        // ⭐ 여기까지 프로필 이미지 URL 추가 로직 ⭐

        return "chat";
    }

    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {

        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername);

        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("렌탈 정보를 찾을 수 없습니다: " + rentalId);
        }

        User seller = rental.getProduct().getSeller();
        if (currentUser.getId().equals(seller.getId())) {
            return "redirect:/product/" + rental.getProduct().getProductId();
        }

        Long chatRoomId = chatService.findOrCreateChatRoom(currentUser, seller, rental);
        return "redirect:/chat/" + chatRoomId;
    }

    // ⭐ 여기에 report.html 템플릿을 반환하는 새로운 메서드를 추가합니다. ⭐
    @GetMapping("/reportForm") // 이 엔드포인트로 접근하면 리포트 폼 페이지를 보여줍니다.
    public String showReportForm(Model model, Principal principal) {
        // 채팅방 내부에서 리포트를 하는 경우, 필요한 정보(예: 현재 사용자 ID, 신고 대상 ID 등)를
        // 이 메서드의 Model에 담아서 report.html로 전달할 수 있습니다.
        // 예를 들어, 현재 로그인된 유저 정보를 넘겨줄 수 있습니다.
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            model.addAttribute("reporterId", currentUser.getId()); // 신고자 ID 전달
            model.addAttribute("reporterNickname", currentUser.getNickname()); // 신고자 닉네임 전달
        }

        // report.html 템플릿을 반환합니다. (src/main/resources/templates/report.html)
        return "report";
    }
    // ⭐ 새로운 메서드 추가 끝 ⭐
}