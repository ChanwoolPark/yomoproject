// src/main/java/com.project/yomozomo/repository/ChatbotOptionRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotOptionRepository extends JpaRepository<ChatbotOption, Long> {
    // Option ID로 Option을 찾을 때, Question과 Answer (및 nextQuestion)를 즉시 로딩하도록 쿼리 정의
    @EntityGraph(attributePaths = {"question", "answer", "nextQuestion"})
    Optional<ChatbotOption> findById(Long id);
}