// src/main/java/com/project/yomozomo/controller/ChatRoomApiController.java
package com.project.yomozomo.controller;

import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.security.Principal;
import com.project.yomozomo.dto.ChatMessage;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // domain 패키지라면 유지
import com.project.yomozomo.service.ChatService;
import org.springframework.http.HttpStatus; // HttpStatus import
@RestController // 여전히 RESTful API 컨트롤러
@RequestMapping("/api/chatrooms") // API 프리픽스 유지
@RequiredArgsConstructor
public class ChatRoomApiController { // 이름을 좀 더 명확하게 변경 (선택 사항)

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    @PostMapping("/start")
    public ResponseEntity<Long> startChat(@RequestBody ChatMessage.ChatStartRequest request, // ⭐ @RequestBody로 변경 및 DTO 사용 ⭐
                                          Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // DTO에서 rentalId를 가져옴
        Long rentalId = request.getRentalId();
        if (rentalId == null) { // rentalId가 필수라면 null 체크
            return ResponseEntity.badRequest().body(-1L); // 또는 적절한 에러 응답
        }

        User buyer = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("구매자 정보를 찾을 수 없습니다."));

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("렌탈 정보를 찾을 수 없습니다. ID: " + rentalId));

        User seller = rental.getUser(); // ⭐ Rental 엔티티에 판매자 User가 매핑되어 있다고 가정 ⭐
        if (seller == null) {
            throw new IllegalArgumentException("렌탈 상품의 판매자 정보를 찾을 수 없습니다.");
        }

        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, seller, rental.getRentalId());

        return ResponseEntity.ok(chatRoomId);
    }

    @GetMapping("/{roomId}") // /api/chatrooms/{roomId} (채팅방 상세 정보 조회 API)
    public ResponseEntity<ChatRoom> getRoomDetails(@PathVariable Long roomId) {
        // ⭐ 수정: Optional<ChatRoom>을 풀어줘야 합니다. ⭐
        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));
        return ResponseEntity.ok(chatRoom);
    }

    @GetMapping // /api/chatrooms (모든 채팅방 목록 조회 API)
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        // ⭐ 추가: ChatService에 getAllChatRooms() 메서드가 없었다면 추가해야 합니다. ⭐
        List<ChatRoom> rooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(rooms);
    }
}