package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotKeyword; // ★★★ 이 줄은 맞습니다.
// import com.project.yomozomo.entity.ChatbotResult; // ★★★ 이 줄을 삭제해야 합니다! ★★★
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotKeywordRepository extends JpaRepository<ChatbotKeyword, Long> {
    Optional<ChatbotKeyword> findByKeyword(String keyword);
}