// src/main/java/com/project/yomozomo/controller/ChatRoomAdminController.java
package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.UserService; // 사용자 이름 검색을 위해 필요할 수 있음
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/chatrooms") // 관리자 페이지 URL 프리픽스
@RequiredArgsConstructor
public class ChatRoomAdminController {

    private final ChatService chatService;
    private final UserService userService; // User 엔티티에서 이름으로 검색하려면 필요

    @GetMapping
    public String getChatRoomAdminPage(
            @RequestParam(value = "roomName", required = false) String roomName,
            @RequestParam(value = "buyerName", required = false) String buyerName,
            @RequestParam(value = "sellerName", required = false) String sellerName,
            Model model) {

        List<ChatRoom> chatRooms;

        // 검색 조건이 있다면 해당 조건으로 검색
        if (roomName != null && !roomName.isEmpty()) {
            chatRooms = chatService.searchChatRoomsByRoomName(roomName);
        } else if (buyerName != null && !buyerName.isEmpty()) {
            // 사용자 이름으로 검색하려면 UserService에 해당 기능이 있어야 합니다.
            // 예시: User buyer = userService.getUserByUsername(buyerName);
            //       if (buyer != null) chatRooms = chatService.getChatRoomsByBuyer(buyer);
            // 현재는 간단히 모든 채팅방을 가져옵니다. 이 부분은 실제 User 검색 로직으로 교체해야 합니다.
            chatRooms = chatService.getAllChatRooms(); // 임시: 검색 로직 미구현 시
        } else if (sellerName != null && !sellerName.isEmpty()) {
            chatRooms = chatService.getAllChatRooms(); // 임시: 검색 로직 미구현 시
        }
        else {
            chatRooms = chatService.getAllChatRooms(); // 모든 채팅방 조회
        }

        model.addAttribute("chatRooms", chatRooms);
        return "chat_room_admin"; // chat_room_admin.html 템플릿 반환
    }

    // ⭐ 추가: 특정 채팅방 상세 보기 페이지 (필요하다면) ⭐
    // @GetMapping("/{roomId}")
    // public String getChatRoomDetails(@PathVariable Long roomId, Model model) {
    //     ChatRoom chatRoom = chatService.getChatRoomById(roomId);
    //     model.addAttribute("chatRoom", chatRoom);
    //     // 해당 채팅방의 메시지들도 가져와야 한다면 ChatMessageService 등을 이용
    //     // List<ChatMessage> messages = chatMessageService.getMessagesByRoomId(roomId);
    //     // model.addAttribute("messages", messages);
    //     return "chat_room_detail"; // 상세 보기 템플릿
    // }
}