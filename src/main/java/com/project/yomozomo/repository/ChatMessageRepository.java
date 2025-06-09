// src/main/java/com/project/yomozomo/repository/ChatMessageRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}