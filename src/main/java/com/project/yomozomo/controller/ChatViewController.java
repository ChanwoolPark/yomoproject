// src/main/java/com/project/yomozomo/controller/ChatViewController.java
package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // Rental 엔티티 경로
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.UserService;
import org.springframework.stereotype.Controller; // View를 반환하므로 @Controller 사용
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Controller // 이 컨트롤러는 View (HTML 템플릿)를 반환합니다.
@RequiredArgsConstructor
public class ChatViewController { // 채팅방 관련 뷰를 담당하는 컨트롤러

    private final ChatService chatService;
    private final UserService userService;
    // private final RentalService rentalService; // 렌탈 정보가 필요하면 주입


    // 채팅방 웹 페이지 진입 엔드포인트
    // URL: /chat/{roomId}
    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           @RequestParam(required = false) String targetUser,
                           Principal principal,
                           Model model) {

        String currentUsername = principal.getName(); // 현재 로그인된 사용자 이름

        // 1. 채팅방 정보 조회 (Null 체크 강화)
        ChatRoom chatRoom = chatService.getChatRoomById(roomId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("존재하지 않는 채팅방입니다: " + roomId);
        }

        // 2. 대화 상대방 사용자 이름 결정
        String resolvedTargetUsername;
        if (targetUser != null && !targetUser.isEmpty()) {
            resolvedTargetUsername = targetUser;
        } else {
            // targetUser가 명시되지 않았다면, 현재 사용자가 구매자/판매자인지 확인하여 상대방 결정
            // 구매자/판매자 User 객체가 chatRoom에 포함되어 있어야 합니다.
            if (chatRoom.getBuyer() != null && currentUsername.equals(chatRoom.getBuyer().getUsername())) {
                resolvedTargetUsername = chatRoom.getSeller() != null ? chatRoom.getSeller().getUsername() : "알 수 없는 상대";
            } else if (chatRoom.getSeller() != null && currentUsername.equals(chatRoom.getSeller().getUsername())) {
                resolvedTargetUsername = chatRoom.getBuyer() != null ? chatRoom.getBuyer().getUsername() : "알 수 없는 상대";
            } else {
                throw new IllegalArgumentException("현재 사용자는 이 채팅방의 유효한 참여자가 아닙니다.");
            }
        }

        // 3. Model에 필요한 데이터 추가
        model.addAttribute("username", currentUsername);
        model.addAttribute("targetUsername", resolvedTargetUsername);
        model.addAttribute("roomId", roomId); // 채팅방 고유 ID

        // 4. 렌탈 정보 추가 (이전 오류의 원인: currentRental 누락 해결)
        // ChatRoom 엔티티에 getRental() 메서드가 있다면 사용합니다.
        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRental", chatRoom.getRental());
            // ★ 여기에 'rental_Id'를 추가해야 합니다.
            // currentRental 객체에서 ID를 가져와서 model에 추가합니다.
            // rental 객체의 ID 필드 이름이 'rentalId'라면:
            model.addAttribute("rental_Id", chatRoom.getRental().getRentalId()); //
            // 만약 rental_Id 라는 필드명이 아니라면, 해당 엔티티의 실제 ID 필드명으로 변경해야 합니다.
        } else {
            model.addAttribute("currentRental", null);
            model.addAttribute("rental_Id", null); // 렌탈 정보가 없을 경우 null
        }

        return "chat"; // "chat.html" 템플릿 렌더링
    }

    // 렌탈 상세 템플릿을 위한 임시 엔드포인트 (만약 필요하다면)
    // 이 메서드는 ChatRoomController에 있던 showRentalDetailByParam을 View Controller로 옮긴 것입니다.
    // 이 메서드도 특정 View를 렌더링할 때 'rental_Id'를 model에 추가해줘야 합니다.
    @GetMapping("/rentals/detail")
    public String showRentalDetailByParam(@RequestParam("id") Long rentalId, Model model) {
        model.addAttribute("rental_Id", rentalId); //
        // 필요한 경우, 렌탈 객체 자체를 모델에 추가할 수도 있습니다.
        // Rental rental = rentalService.getRentalById(rentalId).orElse(null);
        // model.addAttribute("rentalObject", rental);
        return "your-rental-detail-template"; // 이 템플릿에서 ${rental_Id}를 사용
    }
}