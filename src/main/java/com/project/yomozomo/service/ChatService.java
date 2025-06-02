// src/main/java/com/project/yomozomo/service/ChatService.java
package com.project.yomozomo.service;

import com.project.yomozomo.entity.Chat; // 채팅 메시지 엔티티
import com.project.yomozomo.entity.ChatRoom; // 채팅방 엔티티
import com.project.yomozomo.entity.User;     // 사용자 엔티티
import com.project.yomozomo.domain.Rental;   // 렌탈 도메인 (혹은 엔티티)

import com.project.yomozomo.repository.ChatRepository;       // Chat 메시지 저장/조회용
import com.project.yomozomo.repository.ChatRoomRepository;   // ChatRoom 저장/조회용
import com.project.yomozomo.repository.RentalRepository;     // Rental 조회용
import com.project.yomozomo.repository.UserRepository;       // User 조회용 (usersRepository로 사용됨)
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional 사용
import java.util.List;
import java.util.Optional;
import java.util.Date; // Date 대신 LocalDateTime 사용 권장 (JPA 최신 버전에서 선호)
import java.time.LocalDateTime; // 필요한 경우 추가

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository usersRepository; // 이름 일관성을 위해 userRepository로 변경을 고려할 수 있음
    private final RentalRepository rentalRepository;



    // --- 채팅방 관리 ---

    // 이 메서드는 외부에서 직접 호출되기보다는, findOrCreateChatRoom 내부에서 사용되는 것이 일반적입니다.
    // 만약 RoomName만으로 방을 생성할 필요가 없다면 제거하거나 private으로 변경하는 것을 고려하세요.
    @Transactional
    public ChatRoom createChatRoom(String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        // TODO: 채팅방 타입 (1:1, 그룹 등) 설정 로직 추가 (필요하다면)
        // newRoom.setCreatedAt(LocalDateTime.now()); // @PrePersist가 있다면 필요 없음
        return chatRoomRepository.save(chatRoom);
    }


    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    // --- 메시지 관리 ---

    /**
     * 특정 채팅방에 새 메시지를 저장합니다.
     * @param roomId 메시지가 속할 채팅방의 ID
     * @param userId 메시지를 보낸 사용자의 ID
     * @param messageContent 메시지 내용
     * @param imgUrl 첨부 이미지 URL (선택 사항)
     * @param messageType 메시지 타입 (예: "TEXT", "IMAGE", "STATUS")
     * @return 저장된 Chat 메시지 엔티티
     */
    @Transactional
    public Chat saveChatMessage(Long roomId, Long userId, String messageContent, String imgUrl, String messageType) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Chat chat = new Chat();
        chat.setChatRoom(chatRoom); // ChatRoom 엔티티 설정
        chat.setUser(user);         // User 엔티티 설정
        chat.setMessage(messageContent);
        chat.setImgUrl(imgUrl);
        // 'Y'/'N' char 타입이라면
        chat.setHasImage(imgUrl != null && !imgUrl.isEmpty() ? 'Y' : 'N');
        // 'createdAt' 필드는 Chat 엔티티에서 @CreationTimestamp 또는 @PrePersist로 자동 설정되는 것이 일반적입니다.
        chat.setMessageType(messageType != null ? messageType : "TEXT"); // 메시지 타입 기본값 설정

        return chatRepository.save(chat);
    }

    /**
     * 특정 채팅방의 모든 메시지를 최신순으로 가져옵니다. (OrderByCreatedAtAsc 확인)
     * @param roomId 메시지를 가져올 채팅방의 ID
     * @return 해당 채팅방의 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<Chat> getChatMessagesByRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));
        // Repository 메서드 이름과 Chat 엔티티의 createdAt 필드가 정확해야 합니다.
        // 예를 들어, Chat 엔티티에 'createdAt' 필드가 있고 오름차순으로 정렬하려면 `findByChatRoomOrderByCreatedAtAsc`가 맞습니다.
        return chatRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }

    // TODO: 유저 생성/조회 (인증/인가 미포함, 단순 CRUD)
    // 이 메서드는 ChatService보다는 UserService에 있는 것이 적절합니다.
    @Transactional
    public User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        // user.setCreatedAt(LocalDateTime.now()); // User 엔티티에 @PrePersist 또는 @CreationTimestamp 있다면 필요 없음
        return usersRepository.save(user);
    }

    /**
     * 구매자, 판매자, 렌탈 정보를 바탕으로 채팅방을 찾아 반환하거나, 없으면 새로 생성하여 ID를 반환합니다.
     * chatRoomRepository.findByRentalAndSellerAndBuyer 메서드의 인자 순서에 주의하세요.
     * @param buyer 구매자 User 엔티티
     * @param seller 판매자 User 엔티티
     * @param rental 관련 Rental 엔티티
     * @return 생성되거나 찾아진 채팅방의 ID (Long)
     */
    // ChatService.java 예시 (findOrCreateChatRoom 메서드)
    @Transactional // 트랜잭션이 걸려있어야 save가 정상 작동하고 롤백 가능
    public Long findOrCreateChatRoom(User buyer, User seller, Rental rental) {
        // 1. 기존 채팅방 찾기 시도
        return chatRoomRepository.findByBuyerAndSellerAndRental(buyer, seller, rental)
                .map(ChatRoom::getRoomId)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatRoom.builder()
                            .buyer(buyer)
                            .seller(seller)
                            .rental(rental)
                            .createdAt(LocalDateTime.now())
                            .build();
                    try {
                        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
                        log.info("새로운 채팅방 생성됨: RoomId = {}", savedChatRoom.getRoomId());
                        return savedChatRoom.getRoomId();
                    } catch (Exception e) {
                        log.error("채팅방 생성 중 오류 발생: {}", e.getMessage(), e);
                        throw new RuntimeException("채팅방 생성에 실패했습니다.", e);
                    }
                });
    }
}
