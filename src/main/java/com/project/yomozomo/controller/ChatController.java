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
@RequestMapping("/chat")
// @RequiredArgsConstructor // 만약 이걸 사용한다면 아래 @Autowired 생성자는 필요 없음
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;

    // @RequiredArgsConstructor를 사용하지 않고 @Autowired로 명시적 주입할 경우
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

        // 현재 사용자가 채팅방의 유효한 참여자인지 확인
        boolean isBuyer = currentUser.getId().equals(chatRoom.getBuyer().getId()); // User 엔티티의 ID 필드가 userId라고 가정
        boolean isSeller = currentUser.getId().equals(chatRoom.getSeller().getId()); // User 엔티티의 ID 필드가 userId라고 가정

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("현재 사용자는 이 채팅방에 접근할 권한이 없습니다.");
        }

        String chatPartnerNickname;
        if (isBuyer) {
            chatPartnerNickname = chatRoom.getSeller().getNickname();
        } else { // isSeller
            chatPartnerNickname = chatRoom.getBuyer().getNickname();
        }

        model.addAttribute("currentUserId", currentUser.getId()); // User 엔티티의 ID 필드가 userId라고 가정
        model.addAttribute("chatPartnerNickname", chatPartnerNickname);
        model.addAttribute("chatRoomId", roomId);

        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRentalId", chatRoom.getRental().getRentalId());
            // 렌탈 상품의 제목 등 추가 정보도 필요할 수 있습니다.
            // model.addAttribute("rentalProductTitle", chatRoom.getRental().getProduct().getTitle());
        } else {
            model.addAttribute("currentRentalId", null);
        }

        return "chat";
    }

    // 새롭게 추가 (이 부분이 없었다면 추가해야 합니다!)
    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {

        // 1. 현재 로그인한 사용자 (채팅을 시작하려는 사람)
        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername); // 이 메서드가 User 객체를 반환한다고 가정

        // 2. 렌탈 정보 조회
        // com.project.yomozomo.domain.Rental로 임포트했으므로, 서비스에서 Rental 객체를 반환해야 합니다.
        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException("렌탈 정보를 찾을 수 없습니다: " + rentalId);
        }

        // 3. 렌탈에 관련된 판매자 (상품 등록자) 확인
        // Rental 엔티티에 Product 엔티티가 @ManyToOne으로 연결되어 있고,
        // Product 엔티티에 seller 필드(User 타입)가 연결되어 있다는 전제
        User seller = rental.getProduct().getSeller(); // 이 부분이 NullPointerException을 일으킬 수 있으니 주의.
        // rental.getProduct() 또는 rental.getProduct().getSeller()가 null일 수 있습니다.

        // 4. 자신과 채팅 시작 방지 (현재 사용자가 판매자 자신인 경우)
        if (currentUser.getId().equals(seller.getId())) { // User 엔티티의 ID 필드가 userId라고 가정
            // TODO: 사용자에게 "자신과의 채팅은 불가능합니다." 등의 메시지를 보여주는 로직 추가
            // 예: redirectAttributes.addFlashAttribute("errorMessage", "자신이 등록한 상품에는 채팅할 수 없습니다.");
            return "redirect:/product/" + rental.getProduct().getProductId(); // 상품 상세 페이지로 다시 리다이렉트
        }

        // 5. 채팅방을 찾거나 생성합니다.
        // findOrCreateChatRoom 메서드가 buyerUser, sellerUser, rentalEntity를 직접 인자로 받도록 수정되어야 합니다.
        Long chatRoomId = chatService.findOrCreateChatRoom(currentUser, seller, rental);

        // 6. 생성되거나 찾아진 채팅방으로 리다이렉트 (roomId를 전달)
        return "redirect:/chat/" + chatRoomId;
    }
}