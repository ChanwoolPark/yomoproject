// src/main/java/com.project/yomozomo/repository/ChatbotAnswerRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotAnswerRepository extends JpaRepository<ChatbotAnswer, Long> {
}