// src/main/java/com/chat/repository/ChatRoomRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomId(Long roomId); // roomId로 채팅방 조회
}