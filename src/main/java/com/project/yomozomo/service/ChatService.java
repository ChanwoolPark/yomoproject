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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional 사용
import java.util.List;
import java.util.Optional;
import java.util.Date; // Date 대신 LocalDateTime 사용 권장 (JPA 최신 버전에서 선호)
import java.time.LocalDateTime; // 필요한 경우 추가

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository usersRepository; // 이름 일관성을 위해 userRepository로 변경을 고려할 수 있음
    private final RentalRepository rentalRepository;

    @Autowired // 생성자 주입 방식 (Spring 4.3부터는 단일 생성자라면 @Autowired 생략 가능)
    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, UserRepository usersRepository, RentalRepository rentalRepository) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.usersRepository = usersRepository;
        this.rentalRepository = rentalRepository;
    }

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

    @Transactional(readOnly = true)
    public ChatRoom getChatRoomById(Long roomId) {
        // ChatRoom 엔티티의 ID 필드가 'roomId'로 정의되어 있다고 가정합니다.
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
    @Transactional
    public Long findOrCreateChatRoom(User buyer, User seller, Rental rental) {
        // 1. 기존 채팅방이 있는지 확인
        // Repository 메서드 이름(findByRentalAndSellerAndBuyer)과 인자 순서가 Repository 인터페이스와 일치해야 합니다.
        return chatRoomRepository.findByRentalAndSellerAndBuyer(rental, seller, buyer)
                .map(ChatRoom::getRoomId) // 기존 방이 있으면 해당 roomId(Long) 반환
                .orElseGet(() -> {
                    // 2. 없으면 새로운 채팅방 생성 및 저장
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setBuyer(buyer);
                    newRoom.setSeller(seller);
                    newRoom.setRental(rental);
                    // roomName 설정: 렌탈 상품의 제목을 활용 (null 체크 필수)
                    // rental.getProduct() 접근을 위해 Rental 엔티티와 Product 엔티티 간의 매핑 확인 필수
                    String roomName = "새로운 대화"; // 기본값
                    if (rental != null && rental.getProduct() != null && rental.getProduct().getTitle() != null) {
                        roomName = rental.getProduct().getTitle() + " 대여 문의";
                    } else if (rental != null) {
                        roomName = rental.getRentalId() + "번 렌탈 문의";
                    }
                    newRoom.setRoomName(roomName);

                    // createdAt은 @PrePersist (JPA Lifecycle Callback) 또는 @CreationTimestamp (Spring Data JPA)가
                    // ChatRoom 엔티티에 설정되어 있다면 여기서 직접 설정할 필요가 없습니다.
                    // newRoom.setCreatedAt(LocalDateTime.now()); // 필요 시 주석 해제

                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);
                    return savedRoom.getRoomId(); // 새로 생성된 roomId(Long) 반환
                });
    }

    // 이 getter는 테스트 또는 특정 상황에서 필요할 수 있으나, 일반적으로 서비스 계층에서 Repository를 직접 노출하지 않습니다.
    public RentalRepository getRentalRepository() {
        return rentalRepository;
    }
}