// src/main/java/com/project/yomozomo/repository/ChatbotAnswerRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotAnswerRepository extends JpaRepository<ChatbotAnswer, Long> {
    // 키워드 검색을 위한 쿼리 메서드 (예시)
    // 실제 구현에서는 LIKE, INSTR 등을 활용하여 더 복잡한 검색 로직이 필요할 수 있습니다.
    List<ChatbotAnswer> findByRelatedKeywordsContaining(String keyword);
}