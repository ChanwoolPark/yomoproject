// src/main/java/com/project/yomozomo/repository/ChatMessageRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 이 메서드가 ChatService에서 호출되므로 반드시 존재해야 합니다.
    List<ChatMessage> findByRoomIdOrderBySendTimeAsc(Long roomId);
}