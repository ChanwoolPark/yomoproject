// src/main/java/com/project/yomozomo/service/ChatService.java
package com.project.yomozomo.service;

import com.project.yomozomo.entity.Chat;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // Rental 도메인 (혹은 엔티티)
// Product 엔티티를 사용할 예정이 없다면 import 제거
// import com.project.yomozomo.domain.Product;

import com.project.yomozomo.repository.ChatRepository;
import com.project.yomozomo.repository.ChatRoomRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository; // UserRepository로 변경 고려

import lombok.Data; // 필드에 @Data 어노테이션 사용 시 필요
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired; // @RequiredArgsConstructor 사용 시 필요 없음
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime; // Date 대신 LocalDateTime 사용을 강력히 권장

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository usersRepository; // usersRepository로 유지하거나 일관성을 위해 userRepository로 변경
    private final RentalRepository rentalRepository;

    // --- 채팅방 관리 ---

    @Transactional
    public ChatRoom createChatRoom(String roomName) {
        // 이 메서드는 roomName만으로 채팅방을 생성하는데, buyer, seller, rental 정보가 없으므로
        // 현재 DB 스키마상 rental_id가 NOT NULL 이므로, 이 메서드로는 채팅방을 생성할 수 없습니다.
        // 이 메서드를 호출하려면 rental, buyer, seller도 Builder로 설정해야 합니다.
        // 관리 목적으로 "빈" 채팅방을 만들 필요가 없다면 이 메서드는 제거하거나,
        // 예외를 던지도록 명시적으로 변경해야 합니다.
        log.warn("ChatRoom createChatRoom(String roomName) 호출: 이 메서드는 rental, buyer, seller 정보 없이 채팅방을 생성할 수 없습니다. 적절한 사용 여부 확인 필요.");
        throw new UnsupportedOperationException("채팅방은 구매자, 판매자, 렌탈 정보와 함께 생성되어야 합니다.");
        // ChatRoom chatRoom = new ChatRoom();
        // chatRoom.setRoomName(roomName);
        // return chatRoomRepository.save(chatRoom);
    }

    @Transactional(readOnly = true)
    public ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    // ⭐⭐ 여기 아래에 searchChatRoomsByRoomName 메서드를 추가합니다. ⭐⭐
    @Transactional(readOnly = true)
    public List<ChatRoom> searchChatRoomsByRoomName(String roomName) {
        // ChatRoomRepository에 findByRoomNameContainingIgnoreCase 메서드가 있어야 합니다.
        return chatRoomRepository.findByRoomNameContainingIgnoreCase(roomName);
    }

    // 사용자 이름으로 검색하는 메서드는 ChatRoomRepository에 적절한 쿼리 메서드가 필요합니다.
    // ChatRoomAdminController에서 buyerName, sellerName으로 검색하는 로직을 사용하려면
    // 아래와 같은 메서드가 ChatService에 필요하며, ChatRoomRepository에도 해당 쿼리 메서드가 정의되어야 합니다.
    @Transactional(readOnly = true)
    public List<ChatRoom> searchChatRooms(String roomName, String buyerName, String sellerName) {
        // 복합 검색 로직 (ChatRoomRepository에 searchChatRooms(String, String, String) 같은 메서드가 있다면 사용)
        // 현재 ChatRoomRepository에는 해당 메서드가 없으므로 아래 코드는 예시입니다.
        // 필요하다면 ChatRoomRepository에 해당 JPQL 쿼리 메서드를 추가해야 합니다.
        // List<ChatRoom> result = chatRoomRepository.searchChatRooms(roomName, buyerName, sellerName);
        // return result;

        // 임시 방편으로 모든 채팅방을 가져와서 필터링 (비효율적이지만 빠른 테스트 가능)
        List<ChatRoom> allChatRooms = chatRoomRepository.findAll();
        return allChatRooms.stream()
                .filter(room -> {
                    boolean matches = true;
                    if (roomName != null && !roomName.isEmpty()) {
                        if (room.getRoomName() == null || !room.getRoomName().toLowerCase().contains(roomName.toLowerCase())) {
                            matches = false;
                        }
                    }
                    if (matches && buyerName != null && !buyerName.isEmpty()) {
                        if (room.getBuyer() == null || room.getBuyer().getUsername() == null || !room.getBuyer().getUsername().toLowerCase().contains(buyerName.toLowerCase())) {
                            matches = false;
                        }
                    }
                    if (matches && sellerName != null && !sellerName.isEmpty()) {
                        if (room.getSeller() == null || room.getSeller().getUsername() == null || !room.getSeller().getUsername().toLowerCase().contains(sellerName.toLowerCase())) {
                            matches = false;
                        }
                    }
                    return matches;
                })
                .collect(java.util.stream.Collectors.toList());
    }


    // --- 메시지 관리 ---

    @Transactional
    public Chat saveChatMessage(Long roomId, Long userId, String messageContent, String imgUrl, String messageType) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Chat chat = new Chat();
        chat.setChatRoom(chatRoom);
        chat.setUser(user);
        chat.setMessage(messageContent);
        chat.setImgUrl(imgUrl);
        chat.setHasImage(imgUrl != null && !imgUrl.isEmpty() ? 'Y' : 'N');
        chat.setMessageType(messageType != null ? messageType : "TEXT");

        return chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public List<Chat> getChatMessagesByRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));
        return chatRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }

    @Transactional
    public User createUser(String username) {
        // 이 메서드는 ChatService보다는 UserService에 있는 것이 적절합니다.
        log.warn("ChatService.createUser(String username) 호출: 사용자 생성 로직은 UserService에 있어야 합니다.");
        User user = new User();
        user.setUsername(username);
        // User 엔티티의 다른 필수 필드들도 설정해야 할 수 있습니다.
        // 예를 들어, password, email 등. DDL을 다시 확인하세요.
        user.setName("Default Name"); // 임시 값
        user.setEmail(username + "@example.com"); // 임시 값
        user.setPassword("default_password"); // 임시 값 (실제로는 인코딩 필요)
        return usersRepository.save(user);
    }

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
                            // Rental 엔티티에 getTitle() 메서드가 있는지 확인하거나
                            // roomName 생성 로직을 변경해야 합니다.
                            .roomName(rental.getRentalId() != null ? rental.getRentalId() + " 관련 채팅" : "렌탈 채팅")
                            .createdAt(LocalDateTime.now()) // LocalDateTime으로 변경 권장
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