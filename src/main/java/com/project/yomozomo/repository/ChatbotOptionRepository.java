// src/main/java/com/project/yomozomo/repository/ChatbotOptionRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatbotOptionRepository extends JpaRepository<ChatbotOption, Long> {
    Optional<ChatbotOption> findByContent(String content); // 옵션 내용으로 조회
    List<ChatbotOption> findByQuestionId(Long questionId); // 특정 질문 ID에 속한 옵션 목록 조회
}