package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User; // com.project.yomozomo.entity.User 임을 명확히
import com.project.yomozomo.domain.Rental; // com.project.yomozomo.domain.Rental 임을 명확히
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional; // ⭐ @Transactional 임포트 추가 ⭐

import java.security.Principal;

@Controller
@RequestMapping("/chat")
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

    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) { // ⭐ RedirectAttributes 추가 ⭐
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "렌탈 정보를 찾을 수 없습니다.");
            return "redirect:/error"; // 또는 적절한 에러 페이지
        }

        User buyer = userService.getUserByUsername(principal.getName());
        if (buyer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인된 사용자 정보를 찾을 수 없습니다.");
            return "redirect:/login";
        }

        // ⭐ 판매자는 렌탈 객체에서 가져와야 합니다. ⭐
        User seller = rental.getUser(); // rental 객체에 연결된 User (판매자)를 가져옴
        if (seller == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "판매자 정보를 찾을 수 없습니다.");
            return "redirect:/error";
        }

        // 자신과의 채팅방 생성 방지 (이전 로직에서 이미 있었던 부분)
        if (buyer.getId().equals(seller.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "자신과의 채팅방은 생성할 수 없습니다.");
            return "redirect:/rental/detail/" + rentalId; // 렌탈 상세 페이지로 돌아가기
        }

        Long chatRoomId = chatService.findOrCreateChatRoom(buyer, seller, rental);
        return "redirect:/chat/" + chatRoomId;
    }

    @GetMapping("/{roomId}")
    @Transactional // ⭐ JPA 지연 로딩 문제 해결을 위해 @Transactional 추가 ⭐
    public String chatRoom(@PathVariable Long roomId,
                           Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) { // ⭐ RedirectAttributes 추가 ⭐

        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 정보가 없습니다.");
            return "redirect:/login";
        }

        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "현재 사용자 정보를 찾을 수 없습니다.");
            return "redirect:/login";
        }

        ChatRoom chatRoom = chatService.getChatRoomById(roomId);
        if (chatRoom == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 채팅방입니다.");
            return "redirect:/error"; // 적절한 에러 페이지로 리다이렉트
        }

        // 채팅방 접근 권한 확인
        // chatRoom.getBuyer()와 chatRoom.getSeller()는 @Transactional 덕분에 정상 로드됩니다.
        boolean isBuyer = currentUser.getId().equals(chatRoom.getBuyer().getId());
        boolean isSeller = currentUser.getId().equals(chatRoom.getSeller().getId());

        if (!isBuyer && !isSeller) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/"; // 홈 또는 다른 적절한 페이지로 리다이렉트
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

        // ⭐ 삼항 연산자로 간결화 ⭐
        model.addAttribute("currentRentalId", chatRoom.getRental() != null ? chatRoom.getRental().getRentalId() : null);

        // 프로필 이미지 URL 추가 로직
        String userProfileImageUrl = currentUser.getProfileImageUrl();
        if (userProfileImageUrl == null || userProfileImageUrl.isEmpty()) {
            userProfileImageUrl = "/images/default-profile.png";
        }
        model.addAttribute("currentUserprofile_image", userProfileImageUrl);

        return "chat";
    }

    @GetMapping("/reportForm")
    public String showReportForm(Model model, Principal principal) {
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            model.addAttribute("reporterId", currentUser.getId());
            model.addAttribute("reporterNickname", currentUser.getNickname());
        }
        return "report";
    }
}