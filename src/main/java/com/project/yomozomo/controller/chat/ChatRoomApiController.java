package com.project.yomozomo.controller.chat;

import com.project.yomozomo.dto.ChatStartRequest;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomApiController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    // [채팅방 시작/생성]
    @PostMapping("/start")
    public ResponseEntity<Long> startChat(@RequestBody ChatStartRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(-1L);
        }
        Long rentalId = request.getRentalId();
        if (rentalId == null) {
            return ResponseEntity.badRequest().body(-1L);
        }
        User buyer = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("구매자 정보를 찾을 수 없습니다."));

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("렌탈 정보를 찾을 수 없습니다. ID: " + rentalId));
        User seller = rental.getUser(); // Rental 객체에 연결된 판매자

        if (seller == null) {
            return ResponseEntity.badRequest().body(-1L);
        }
        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, seller, rental.getRentalId());
        return ResponseEntity.ok(chatRoomId);
    }

    // [채팅방 상세 조회]
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getRoomDetails(@PathVariable Long roomId) {
        Optional<ChatRoom> chatRoomOpt = chatService.getChatRoomById(roomId);
        if (chatRoomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chatRoomOpt.get());
    }

    // [전체 채팅방 목록]
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(rooms);
    }
}
