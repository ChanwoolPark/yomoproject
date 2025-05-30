// src/main/java/com/project/yomozomo/controller/ChatRoomApiController.java (이름 변경 추천)
package com.project.yomozomo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.security.Principal;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.service.ChatService;

@RestController // 여전히 RESTful API 컨트롤러
@RequestMapping("/api/chatrooms") // API 프리픽스 유지
@RequiredArgsConstructor
public class ChatRoomApiController { // 이름을 좀 더 명확하게 변경 (선택 사항)

    private final ChatService chatService;
    // private final ChatService chatRoomService; // 필요 없으면 제거

    @PostMapping // /api/chatrooms 에 대한 POST 요청 (새 채팅방 생성 API)
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
        ChatRoom newRoom = chatService.createChatRoom(roomName);
        return ResponseEntity.ok(newRoom);
    }

    @PostMapping("/start") // /api/chatrooms/start (채팅 시작/생성 API)
    @ResponseBody
    public Long startChat(@RequestParam Long rentalId,
                          @RequestParam Long sellerId,
                          Principal principal) {
        // ... (실제 User/Rental 조회 로직 구현 필요)
        User buyer = new User(); buyer.setUsername(principal.getName()); // 임시
        User seller = new User(); seller.setId(sellerId); // 임시
        Rental rental = new Rental(); rental.setRentalId(rentalId); // 임시

        Long roomId = chatService.findOrCreateChatRoom(buyer, seller, rental);
        return roomId;
    }

    @GetMapping("/{roomId}") // /api/chatrooms/{roomId} (채팅방 상세 정보 조회 API)
    public ResponseEntity<ChatRoom> getRoomDetails(@PathVariable Long roomId) {
        ChatRoom chatRoom = chatService.getChatRoomById(roomId);
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping // /api/chatrooms (모든 채팅방 목록 조회 API)
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(rooms);
    }
}