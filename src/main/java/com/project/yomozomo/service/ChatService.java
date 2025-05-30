// src/main/java/com/chat/service/ChatService.java
package com.project.yomozomo.service;

import com.project.yomozomo.entity.Chat;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ChatRepository;       // 변경
import com.project.yomozomo.repository.ChatRoomRepository;   // 변경
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import com.project.yomozomo.domain.Rental;


@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository usersRepository;
    private final RentalRepository rentalRepository;
    @Autowired
    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, UserRepository usersRepository, RentalRepository rentalRepository) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.usersRepository = usersRepository;
        this.rentalRepository = rentalRepository;
    }

    // --- 채팅방 관리 ---
    @Transactional
    public ChatRoom createChatRoom(String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        // TODO: 채팅방 타입 (1:1, 그룹 등) 설정 로직 추가
        return chatRoomRepository.save(chatRoom);
    }

    @Transactional(readOnly = true)
    public ChatRoom getChatRoomById(Long roomId) { // roomId가 Long 타입
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
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

    // TODO: 유저 생성/조회 (인증/인가 미포함, 단순 CRUD)
    @Transactional
    public User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        return usersRepository.save(user);
    }
    @Transactional
    public Long findOrCreateChatRoom(User buyer, User seller, Rental rental) { // User, Rental 객체를 파라미터로 받음
        // 1. 기존 채팅방이 있는지 확인
        return chatRoomRepository.findByRentalAndSellerAndBuyer(rental, seller, buyer)
                .map(ChatRoom::getRoomId) // 기존 방이 있으면 해당 roomId(Long) 반환
                .orElseGet(() -> {
                    // 2. 없으면 새로운 채팅방 생성 및 저장
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setBuyer(buyer);
                    newRoom.setSeller(seller);
                    newRoom.setRental(rental);
                    newRoom.setRoomName(rental.getRentalId() + "에 대한 채팅"); // 예시: roomName 설정
                    // createdAt은 @PrePersist에서 자동 설정

                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);
                    return savedRoom.getRoomId(); // 새로 생성된 roomId(Long) 반환
                });
    }


}