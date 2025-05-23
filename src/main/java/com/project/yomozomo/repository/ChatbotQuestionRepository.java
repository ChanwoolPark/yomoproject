// src/main/java/com/project/yomozomo/repository/ChatbotQuestionRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotQuestionRepository extends JpaRepository<ChatbotQuestion, Long> {
    // 필요한 경우 특정 질문을 찾는 메서드 추가
}