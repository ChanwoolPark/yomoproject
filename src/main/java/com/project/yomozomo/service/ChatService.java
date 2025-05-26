// src/main/java/com/chat/service/ChatService.java
package com.project.yomozomo.service;

import com.project.yomozomo.entity.Chat;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ChatRepository;       // 변경
import com.project.yomozomo.repository.ChatRoomRepository;   // 변경
import com.project.yomozomo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository usersRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, UserRepository usersRepository) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.usersRepository = usersRepository;
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
    public Optional<ChatRoom> getChatRoomById(Long roomId) {
        return chatRoomRepository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    // --- 메시지 관리 ---
    @Transactional
    public Chat saveChatMessage(Long roomId, Long userId, String messageContent, String imgUrl, String messageType) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
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
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
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

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return usersRepository.findById(userId);
    }
}