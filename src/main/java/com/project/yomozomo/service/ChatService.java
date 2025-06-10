package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.repository.ChatMessageRepository;
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
import java.util.Comparator;
import java.util.TreeSet;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    // --- 채팅방 관리 ---

    /**
     * 특정 렌탈에 대한 구매자와 판매자 간의 채팅방을 찾거나 생성합니다.
     * 이 메서드는 항상 rentalId를 기반으로 채팅방을 찾거나 생성합니다.
     *
     * @param buyer    채팅을 시작하는 구매자 User 객체
     * @param seller   판매자 User 객체 (Rental에 연결된 Product의 User)
     * @param rentalId 채팅방과 연결될 Rental의 ID (필수, null이면 에러 발생)
     * @return 생성되거나 찾아진 ChatRoom의 ID
     */
    @Transactional
    public Long findOrCreateChatRoomForRental(User buyer, User seller, Long rentalId) {
        // rentalId가 null인 경우를 허용하지 않도록 수정합니다.
        // 이 메서드는 'for Rental'이므로 rentalId는 항상 존재해야 합니다.
        if (rentalId == null) {
            throw new IllegalArgumentException("렌탈 ID는 필수입니다. 이 메서드는 렌탈 상품과 연결된 채팅방을 위한 것입니다.");
        }

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 렌탈 ID입니다: " + rentalId));

        // buyer와 seller ID를 기준으로 정규화하여 일관된 검색을 수행합니다.
        User participant1 = buyer.getId() < seller.getId() ? buyer : seller;
        User participant2 = buyer.getId() < seller.getId() ? seller : buyer;

        // 렌탈 ID가 있는 경우: rental까지 포함하여 채팅방을 찾습니다.
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByBuyerAndSellerAndRental(participant1, participant2, rental);

        /*
         * ⭐ 사용자의 요청에 따라 이 블록은 필요 없습니다. ⭐
         * 렌탈 ID가 없는 일반 1:1 채팅방을 찾는 로직은 이 메서드에서 제외합니다.
         * 필요한 경우, 별도의 findOrCreateGeneralChatRoom 같은 메서드를 구현해야 합니다.
         */
        // else {
        //    existingRoom = chatRoomRepository.findByBuyerAndSellerAndRentalIsNull(participant1, participant2);
        // }


        if (existingRoom.isPresent()) {
            log.info("기존 렌탈 채팅방 발견: Room ID {}", existingRoom.get().getRoomId());
            return existingRoom.get().getRoomId();
        } else {
            // 존재하지 않으면 새 채팅방 생성
            String roomName;
            if (rental.getProduct() != null && rental.getProduct().getTitle() != null) {
                roomName = rental.getProduct().getTitle() + " 대여 채팅";
            } else {
                roomName = "렌탈 상품 채팅"; // 상품 정보가 없을 경우 대비
            }
            roomName += " (" + buyer.getNickname() + " - " + seller.getNickname() + ")";


            ChatRoom newChatRoom = ChatRoom.builder()
                    .roomName(roomName)
                    .buyer(participant1)
                    .seller(participant2)
                    .rental(rental)
                    .build();

            ChatRoom savedRoom = chatRoomRepository.save(newChatRoom);
            log.info("새 렌탈 채팅방 생성: Room ID {}, Room Name: {}", savedRoom.getRoomId(), savedRoom.getRoomName());
            return savedRoom.getRoomId();
        }
    }

    /**
     * 현재 이 메서드는 findOrCreateChatRoomForRental을 호출하도록 되어 있습니다.
     * 만약 일반 1:1 채팅방(렌탈과 무관)을 생성하는 별도의 로직이 필요하다면, 이 메서드를 수정해야 합니다.
     * 현재는 `rentalId`를 필수로 받지 않는다면 이 메서드의 활용도가 애매해집니다.
     * @param user1 채팅 참여자 User 1
     * @param user2 채팅 참여자 User 2
     * @param rentalId 이 메서드에서 사용되지 않거나 (null 허용), 필요시 특정 용도로 사용될 수 있습니다.
     * @return 생성되거나 찾아진 ChatRoom의 ID
     */
    @Transactional
    public Long findOrCreateChatRoom(User user1, User user2, Long rentalId) { // rentalId를 받을 수 있도록 변경
        // 이 메서드가 렌탈과 무관한 일반 채팅을 생성하도록 의도되었다면, findOrCreateChatRoomForRental을 호출하지 않거나
        // findOrCreateChatRoomForRental의 rentalId 인자에 null을 넘기는 것은 허용되지 않으므로,
        // 이 메서드의 로직을 재검토해야 합니다.
        // 현재는 `findOrCreateChatRoomForRental`이 rentalId를 필수로 요구하므로,
        // 이 메서드를 통해 일반 채팅방을 생성할 수 없습니다.
        // 만약 일반 채팅방이 필요하다면 아래의 주석 처리된 코드를 활성화하고, ChatRoomRepository에 적절한 메서드를 추가해야 합니다.

        // ID가 작은 사용자를 buyer, 큰 사용자를 seller로 하여 일관된 순서 유지
        User participant1 = user1.getId() < user2.getId() ? user1 : user2;
        User participant2 = user1.getId() < user2.getId() ? user2 : user1;

        // ⭐⭐ 렌탈 아이디가 없는 채팅방을 찾거나 생성하는 로직을 이곳에 추가합니다. ⭐⭐
        // findByBuyerAndSellerAndRentalIsNull 메서드가 ChatRoomRepository에 정의되어 있어야 합니다.
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByBuyerAndSellerAndRentalIsNull(participant1, participant2);

        if (existingRoom.isPresent()) {
            log.info("기존 일반 채팅방 발견: Room ID {}", existingRoom.get().getRoomId());
            return existingRoom.get().getRoomId();
        } else {
            // 존재하지 않으면 새 일반 채팅방 생성
            String roomName = "일반 채팅 (" + user1.getNickname() + " - " + user2.getNickname() + ")";

            ChatRoom newChatRoom = ChatRoom.builder()
                    .roomName(roomName)
                    .buyer(participant1)
                    .seller(participant2)
                    .rental(null) // 렌탈 아이디가 없으므로 null로 설정
                    .build();

            ChatRoom savedRoom = chatRoomRepository.save(newChatRoom);
            log.info("새 일반 채팅방 생성: Room ID {}, Room Name: {}", savedRoom.getRoomId(), savedRoom.getRoomName());
            return savedRoom.getRoomId();
        }
    }


    // --- 메시지 관리 ---

    /**
     * 채팅 메시지를 DB에 저장합니다.
     * @param chatRoomId 메시지가 속한 채팅방 ID
     * @param senderUserId 메시지를 보낸 사용자 ID
     * @param messageContent 메시지 내용
     * @param imgUrl 이미지 URL (옵션)
     * @param messageType 메시지 타입 (String 형태, 예: "TALK", "SYSTEM", "PAYMENT_REQUEST" 등)
     * @return 저장된 ChatMessage 엔티티
     */
    @Transactional
    public ChatMessage saveChatMessage(Long chatRoomId, Long senderUserId, String messageContent,
                                       String imgUrl, String messageType) {
        log.info("ChatService.saveChatMessage 호출됨. chatRoomId: {}, senderUserId: {}", chatRoomId, senderUserId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> {
                    log.error("채팅방 찾기 실패. ID: {}", chatRoomId);
                    return new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId);
                });
        log.debug("채팅방 찾음: {}", chatRoom.getRoomId());

        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> {
                    log.error("발신자 사용자 찾기 실패. ID: {}", senderUserId);
                    return new IllegalArgumentException("발신자 사용자를 찾을 수 없습니다. ID: " + senderUserId);
                });
        log.debug("발신자 찾음: {} (ID: {})", sender.getNickname(), sender.getId());

        String finalMessageType = messageType != null ? messageType.toUpperCase() : "TALK";
        boolean isValidType = false;
        for (com.project.yomozomo.dto.ChatMessageDTO.MessageType type : com.project.yomozomo.dto.ChatMessageDTO.MessageType.values()) {
            if (type.name().equals(finalMessageType)) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            log.warn("알 수 없는 messageType '{}'가 감지되었습니다. 'TALK'로 기본 설정합니다.", finalMessageType);
            finalMessageType = "TALK";
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(chatRoomId);
        chatMessage.setSenderId(senderUserId);
        chatMessage.setMessage(messageContent);
        chatMessage.setImgUrl(imgUrl != null ? imgUrl : "");
        chatMessage.setMessageType(finalMessageType);
        chatMessage.setSendTime(LocalDateTime.now());

        log.debug("ChatMessage 엔티티 생성 완료. chatRoomId: {}, senderId: {}, message: {}, imgUrl: {}, messageType: {}, sendTime: {}",
                chatMessage.getRoomId(), chatMessage.getSenderId(), chatMessage.getMessage(), chatMessage.getImgUrl(), chatMessage.getMessageType(), chatMessage.getSendTime());

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);
        log.info("ChatMessage 엔티티 DB 저장 완료. Message ID: {}", savedChatMessage.getMessageId());

        return savedChatMessage;
    }

    /**
     * 특정 채팅방의 모든 메시지를 전송 시간 오름차순으로 가져옵니다.
     * @param roomId 채팅방 ID
     * @return 해당 채팅방의 메시지 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySendTimeAsc(roomId);
    }

    /**
     * 채팅방 ID로 채팅방 정보를 가져옵니다.
     * @param roomId 채팅방 ID
     * @return ChatRoom Optional 객체
     */
    @Transactional(readOnly = true)
    public Optional<ChatRoom> getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }

    /**
     * 채팅방 이름으로 채팅방을 검색합니다 (부분 일치, 대소문자 무시).
     * @param roomName 검색할 채팅방 이름
     * @return 검색된 채팅방 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> searchChatRoomsByRoomName(String roomName) {
        return chatRoomRepository.findByRoomNameContainingIgnoreCase(roomName);
    }

    /**
     * 모든 채팅방을 가져옵니다.
     * @return 모든 채팅방 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }
}