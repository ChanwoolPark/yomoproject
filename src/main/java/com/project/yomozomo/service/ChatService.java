package com.project.yomozomo.service;

import com.project.yomozomo.entity.Chat;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // Rental의 실제 위치가 domain이라면 유지
import com.project.yomozomo.repository.ChatRepository;
import com.project.yomozomo.repository.ChatRoomRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional; // Optional 임포트는 한 번만 필요합니다.
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    // --- 채팅방 관리 ---

    /**
     * 특정 렌탈에 대한 구매자와 판매자 간의 채팅방을 찾거나 생성합니다.
     *
     * @param buyer    채팅을 시작하는 구매자 User 객체
     * @param seller   판매자 User 객체 (Rental에 연결된 Product의 User)
     * @param rentalId 채팅방과 연결될 Rental의 ID
     * @return 생성되거나 찾아진 ChatRoom의 ID
     */
    @Transactional // 채팅방 생성/저장이 트랜잭션 내에서 이루어지도록 합니다.
    public Long findOrCreateChatRoomForRental(User buyer, User seller, Long rentalId) {
        // 1. rentalId로 Rental 엔티티를 찾습니다.
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 렌탈 ID입니다: " + rentalId));

        // 2. buyer, seller, rental 기준으로 채팅방을 찾습니다.
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByBuyerAndSellerAndRental(buyer, seller, rental);

        if (existingRoom.isPresent()) {
            // 기존 채팅방이 존재하면 해당 채팅방의 ID를 반환
            return existingRoom.get().getRoomId(); // ⭐ getId()로 수정 (ChatRoom 엔티티의 PK 필드 이름에 따름) ⭐
        } else {
            // 3. 존재하지 않으면 새 채팅방 생성
            // roomName은 "상품명_구매자_판매자" 형식으로 생성
            String productName = "알 수 없는 상품"; // 기본값
            if (rental.getProduct() != null && rental.getProduct().getTitle() != null) {
                // Product 엔티티에 getTitle() 메서드가 있는지 확인 필수
                productName = rental.getProduct().getTitle();
            }

            // ⭐ roomName 필드가 ChatRoom 엔티티에 있어야 합니다. ⭐
            String roomName = productName + " 대여 채팅 (" + buyer.getNickname() + " - " + seller.getNickname() + ")";

            ChatRoom newChatRoom = ChatRoom.builder()
                    .roomName(roomName) // ⭐ roomName 필드 설정 (ChatRoom 엔티티에 존재해야 함) ⭐
                    .buyer(buyer)
                    .seller(seller)
                    .rental(rental)
                    .build();
                    /*.updatedAt(LocalDateTime.now())  최신 업데이트 시간을 명시적으로 설정     builde 위로*/

            ChatRoom savedRoom = chatRoomRepository.save(newChatRoom);
            return savedRoom.getRoomId(); // ⭐ getId()로 수정 (ChatRoom 엔티티의 PK 필드 이름에 따름) ⭐
        }
    }


    // --- 메시지 관리 ---

    @Transactional
    public Chat saveChatMessage(Long chatRoomId, Long senderUserId, String messageContent,
                                String imgUrl, String messageType) {
        // 1. chatRoomId로 ChatRoom 엔티티를 찾습니다.
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));

        // 2. senderUserId로 User 엔티티를 찾습니다.
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new IllegalArgumentException("발신자 사용자를 찾을 수 없습니다. ID: " + senderUserId));

        // 3. Chat 엔티티 생성 및 저장
        Chat chat = new Chat(); // Chat 엔티티에 @Builder가 없다면 이 방식이 맞습니다.
        chat.setChatRoom(chatRoom);
        chat.setUser(sender);
        chat.setMessage(messageContent);
        chat.setImgUrl(imgUrl);
        // ⭐ 아래 두 줄은 Chat 엔티티의 @PrePersist가 처리한다면 제거해도 됩니다. ⭐
        // 만약 @PrePersist가 없다면 유지해야 합니다.
        // chat.setHasImage(imgUrl != null && !imgUrl.isEmpty() ? 'Y' : 'N');
        // chat.setMessageType(messageType != null ? messageType : "TEXT");

        return chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public List<Chat> getChatMessagesByRoom(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));
        return chatRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public Optional<ChatRoom> getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }

    //채팅방 이름으로 검색
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public List<ChatRoom> searchChatRoomsByRoomName(String roomName) {
        // chatRoomRepository에 findByRoomNameContainingIgnoreCase 메서드가 있어야 합니다.
        // 이 메서드는 대소문자 구분 없이 roomName을 포함하는 모든 채팅방을 검색합니다.
        return chatRoomRepository.findByRoomNameContainingIgnoreCase(roomName);
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }
}