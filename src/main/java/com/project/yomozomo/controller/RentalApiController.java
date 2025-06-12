package com.project.yomozomo.controller.chat; // 적절한 패키지 경로로 변경해주세요.

import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.security.Principal;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // domain 패키지의 Rental 임포트
import com.project.yomozomo.service.ChatService;

@RestController // RESTful API 컨트롤러임을 명시
@RequestMapping("/api/chatrooms") // API 기본 경로 설정
@RequiredArgsConstructor // final 필드들을 위한 생성자 자동 생성 (Lombok)
public class ChatRoomApiController { // API 컨트롤러임을 명확히 하는 이름 (선택 사항)

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    /**
     * 특정 렌탈 상품에 대한 채팅방을 시작하거나 기존 채팅방을 반환합니다.
     * 구매자, 판매자, 렌탈 상품 정보를 기반으로 채팅방을 찾거나 새로 생성합니다.
     *
     * @param rentalId 채팅을 시작할 렌탈 상품의 ID
     * @param sellerId 해당 렌탈 상품 판매자의 ID
     * @param principal 현재 로그인된 사용자(구매자) 정보
     * @return 생성되거나 찾아진 채팅방의 ID (Long)
     * @throws IllegalArgumentException 구매자, 판매자, 또는 렌탈 상품 정보를 찾을 수 없을 경우 발생
     */
    @PostMapping("/start")
    @ResponseBody // @RestController에 포함되어 있지만 명시적으로 추가해도 무방
    public Long startChat(@RequestParam Long rentalId,
                          @RequestParam Long sellerId,
                          Principal principal) {
        // 현재 로그인된 구매자 정보 조회
        User buyer = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("구매자 정보를 찾을 수 없습니다."));
        // 판매자 정보 조회
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));
        // 렌탈 상품 정보 조회
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("렌탈 상품 정보를 찾을 수 없습니다."));

        // 채팅방을 찾거나 새로 생성하고 해당 채팅방의 ID를 반환
        return chatService.findOrCreateChatRoom(buyer, seller, rental);
    }

    /**
     * 특정 채팅방의 상세 정보를 조회합니다.
     *
     * @param roomId 조회할 채팅방의 ID
     * @return 해당 채팅방 정보 (ChatRoom 객체)를 포함하는 ResponseEntity
     * @throws IllegalArgumentException 채팅방을 찾을 수 없을 경우 발생 (ChatService에서 처리)
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoom> getRoomDetails(@PathVariable Long roomId) {
        // ChatService를 통해 채팅방 정보를 조회 (서비스 계층에서 Optional 처리 또는 예외 던지기)
        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 모든 채팅방의 목록을 조회합니다.
     * (이 API는 관리자용 또는 특정 목적에 따라 제한될 수 있음)
     *
     * @return 모든 채팅방 목록 (List<ChatRoom>)을 포함하는 ResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatService.getAllChatRooms();
        return ResponseEntity.ok(rooms);
    }
}