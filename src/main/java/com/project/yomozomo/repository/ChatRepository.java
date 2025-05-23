// src/main/java/com/chat/repository/ChatRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.Chat;
import com.project.yomozomo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom); // 특정 방의 메시지 시간순 조회
}