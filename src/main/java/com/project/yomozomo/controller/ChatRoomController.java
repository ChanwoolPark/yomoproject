// src/main/java/com/chat/controller/ChatRoomController.java
package com.project.yomozomo.controller;

import ch.qos.logback.core.model.Model;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    private final ChatService chatService;

    @Autowired
    public ChatRoomController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
        ChatRoom newRoom = chatService.createChatRoom(roomName);
        return ResponseEntity.ok(newRoom);
    }

    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable String roomId,
                           @RequestParam String targetUser, // 상대방 사용자 이름
                           Principal principal, // 현재 로그인 사용자
                           Model model) {

        String username = principal.getName(); // 현재 로그인된 사용자 이름

        model.addText("username"); // 내 사용자 이름
        model.addText("targetUsername"); // 상대방 사용자 이름
        model.addText("roomId"); // 채팅방 고유 ID

        // 옵션 1을 선택했다면, 아래 headerTitlePrefix는 더 이상 필요 없습니다.
        // model.addAttribute("headerTitlePrefix", "채팅방"); // 또는 특정 상품명 등

        // 여기에 렌탈 정보 등 추가 데이터 로직을 넣을 수 있습니다.
        // 예를 들어, roomId를 사용하여 렌탈 정보를 조회하고 model에 추가:
        // Rental rental = rentalService.getRentalByRoomId(roomId);
        // model.addAttribute("targetrental", rental);

        return "chat"; // chat.html 템플릿 반환
    }

    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(rooms);
    }
}