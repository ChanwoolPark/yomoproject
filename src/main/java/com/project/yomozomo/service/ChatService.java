package com.project.yomozomo.service;

// import com.project.yomozomo.entity.Chat; // Chat 엔티티는 더 이상 사용하지 않는다면 제거
import com.project.yomozomo.entity.ChatMessage; // ChatMessage 엔티티 사용
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // Rental의 실제 위치가 domain이라면 유지
// import com.project.yomozomo.repository.ChatRepository; // ChatRepository는 더 이상 사용하지 않는다면 제거
import com.project.yomozomo.repository.ChatMessageRepository; // ⭐ChatMessageRepository 사용⭐
import com.project.yomozomo.repository.ChatRoomRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Slf4j // 로그 사용을 위해 추가
@RequiredArgsConstructor
public class ChatService {

    // ⭐ ChatRepository 대신 ChatMessageRepository를 사용합니다. ⭐
    private final ChatMessageRepository chatMessageRepository; // 메시지 저장을 위한 레포지토리
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
            return existingRoom.get().getRoomId(); // ChatRoom 엔티티의 PK 필드 이름이 roomId라면
        } else {
            // 3. 존재하지 않으면 새 채팅방 생성
            String productName = "알 수 없는 상품"; // 기본값
            if (rental.getProduct() != null && rental.getProduct().getTitle() != null) {
                productName = rental.getProduct().getTitle();
            }

            // ChatRoom 엔티티에 roomName 필드가 있다고 가정
            String roomName = productName + " 대여 채팅 (" + buyer.getNickname() + " - " + seller.getNickname() + ")";

            ChatRoom newChatRoom = ChatRoom.builder()
                    .roomName(roomName)
                    .buyer(buyer)
                    .seller(seller)
                    .rental(rental)
                    .build();

            ChatRoom savedRoom = chatRoomRepository.save(newChatRoom);
            return savedRoom.getRoomId(); // ChatRoom 엔티티의 PK 필드 이름이 roomId라면
        }
    }


    // --- 메시지 관리 ---

    @Transactional
    // ⭐ 반환 타입을 ChatMessage로 변경하고, ChatRepository 대신 ChatMessageRepository를 사용합니다. ⭐
    public ChatMessage saveChatMessage(Long chatRoomId, Long senderUserId, String messageContent,
                                       String imgUrl, String messageType) {
        log.info("DEBUG: ChatService.saveChatMessage 호출됨. chatRoomId: {}, senderUserId: {}", chatRoomId, senderUserId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("ERROR: 채팅방 찾기 실패. ID: {}", chatRoomId);
                    return new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId);
                });
        log.info("DEBUG: 채팅방 찾음: {}", chatRoom.getRoomId());

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> {
                    log.error("ERROR: 발신자 사용자 찾기 실패. ID: {}", senderUserId);
                    return new IllegalArgumentException("발신자 사용자를 찾을 수 없습니다. ID: " + senderUserId);
                });
        log.info("DEBUG: 발신자 찾음: {} (ID: {})", sender.getNickname(), sender.getId());

        // ⭐ ChatMessage 엔티티를 생성하고 필드를 설정합니다. ⭐
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(chatRoomId);    // ChatMessage 엔티티에 roomId 필드가 있다고 가정
        chatMessage.setSenderId(senderUserId); // ChatMessage 엔티티에 senderId 필드가 있다고 가정
        chatMessage.setMessage(messageContent);
//        chatMessage.setImgUrl(imgUrl);        // ChatMessage 엔티티에 imgUrl 필드가 있다면 설정
        chatMessage.setMessageType(messageType);
        chatMessage.setSendTime(LocalDateTime.now()); // 또는 ChatMessage 엔티티의 @PrePersist에서 설정

        log.info("DEBUG: ChatMessage 엔티티 생성 완료. chatRoomId: {}, senderId: {}, message: {}",
                chatMessage.getRoomId(), chatMessage.getSenderId(), chatMessage.getMessage());

        // ⭐ ChatMessageRepository를 사용하여 저장합니다. ⭐
        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
        log.info("DEBUG: ChatMessage 엔티티 DB 저장 완료. Message ID: {}", savedChatMessage.getMessageId());

        return savedChatMessage;
    }

    // ⭐⭐⭐ ChatController에서 호출하는 getChatMessagesByRoomId 메서드 추가 (List<ChatMessage> 반환) ⭐⭐⭐
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessagesByRoomId(Long roomId) {
        // ChatMessageRepository에 findByRoomIdOrderBySendTimeAsc 메서드가 정의되어 있어야 합니다.
        return chatMessageRepository.findByRoomIdOrderBySendTimeAsc(roomId);
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