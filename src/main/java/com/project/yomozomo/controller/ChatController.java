package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User; // User 엔티티
import com.project.yomozomo.domain.Rental; // Rental 도메인 (혹은 엔티티)
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;
// import org.springframework.beans.factory.annotation.Autowired; // @RequiredArgsConstructor 사용 시 불필요
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import lombok.RequiredArgsConstructor; // ⭐ 추가: Lombok @RequiredArgsConstructor 사용 ⭐

@Controller
@RequestMapping("/chat") // 이 컨트롤러의 기본 경로는 "/chat" 입니다.
@RequiredArgsConstructor // ⭐ 추가: final 필드에 대한 생성자 자동 생성 ⭐
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;

    // ⭐ @Autowired 생성자 삭제 (lombok @RequiredArgsConstructor가 대체) ⭐
    // @Autowired
    // public ChatController(ChatService chatService, UserService userService, RentalService rentalService) {
    //     this.chatService = chatService;
    //     this.userService = userService;
    //     this.rentalService = rentalService;
    // }

    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            // 로그인하지 않은 사용자 처리 (로그인 페이지로 리다이렉트 등)
            redirectAttributes.addFlashAttribute("errorMessage", "채팅을 시작하려면 로그인이 필요합니다.");
            return "redirect:/login";
        }

        // 1. rentalId로 Rental 엔티티를 조회합니다.
        // RentalService.getRentalById()가 Optional<Rental>을 반환한다면 .orElseThrow() 추가
        Rental rental = rentalService.getRentalById(rentalId);

        // 2. 구매자 (현재 로그인한 사용자) 정보를 가져옵니다.
        // UserService.getUserByUsername()이 Optional<User>를 반환한다면 .orElseThrow() 추가
        User buyer = userService.getUserByUsername(principal.getName());


        // 3. 판매자 정보를 가져옵니다. (⭐ 이 부분이 중요! Product 엔티티를 통해 판매자 User를 가져옵니다 ⭐)
        User sellerUser = rental.getProduct().getSeller(); // Product 엔티티에 getSeller()가 있다면 사용

        if (sellerUser == null) {
            // 판매자 정보가 없는 경우 처리
            redirectAttributes.addFlashAttribute("errorMessage", "렌탈 상품의 판매자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage"; // 적절한 에러 페이지로 리다이렉트
        }

        // 4. 채팅방을 찾거나 생성합니다.
        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, sellerUser, rental.getRentalId());

        // 바로 해당 채팅방으로 리다이렉트
        return "redirect:/chat/" + chatRoomId;
    }

    @GetMapping("/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) { // ⭐ RedirectAttributes 추가 ⭐

        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅방에 접근하려면 로그인이 필요합니다.");
            return "redirect:/login";
        }

        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername); // UserService에서 Optional 반환 여부 확인 필요

        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId)); // Optional 반환 시

        // 현재 사용자가 채팅방에 접근 권한이 있는지 확인
        boolean isBuyer = currentUser.getId().equals(chatRoom.getBuyer().getId());
        boolean isSeller = currentUser.getId().equals(chatRoom.getSeller().getId());

        if (!isBuyer && !isSeller) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/access-denied"; // 적절한 접근 거부 페이지로 리다이렉트
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

        // System.out.println("채팅방 ID: " + roomId + ", rental: " + chatRoom.getRental());
        // System.out.println("rentalId: " + (chatRoom.getRental() != null ? chatRoom.getRental().getRentalId() : "null"));

        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRentalId", chatRoom.getRental().getRentalId());
            // 상품 타이틀도 추가하면 유용합니다.
            if (chatRoom.getRental().getProduct() != null) {
                model.addAttribute("productTitle", chatRoom.getRental().getProduct().getTitle());
            } else {
                model.addAttribute("productTitle", "상품 정보 없음");
            }
        } else {
            model.addAttribute("currentRentalId", null);
            model.addAttribute("productTitle", "상품 정보 없음");
        }

        String userProfileImageUrl = currentUser.getProfileImageUrl();
        if (userProfileImageUrl == null || userProfileImageUrl.isEmpty()) {
            userProfileImageUrl = "/images/default-profile.png";
        }
        model.addAttribute("currentUserProfileImage", userProfileImageUrl); // ⭐ 오타 수정: currentUserprofile_image -> currentUserProfileImage ⭐

        return "chat"; // ⭐ 실제 템플릿 파일명에 따라 "chat/chat-room" 등으로 변경 필요 ⭐
    }


    @GetMapping("/reportForm") // 이 엔드포인트로 접근하면 리포트 폼 페이지를 보여줍니다.
    public String showReportForm(Model model, Principal principal) {
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            model.addAttribute("reporterId", currentUser.getId()); // 신고자 ID 전달
            model.addAttribute("reporterNickname", currentUser.getNickname()); // 신고자 닉네임 전달
        }

        return "report";
    }
}