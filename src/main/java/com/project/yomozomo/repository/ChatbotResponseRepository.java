package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotResponseRepository extends JpaRepository<ChatbotResponse, String> {
    // JpaRepository는 기본적인 CRUD (Create, Read, Update, Delete) 기능을 제공합니다.
    // Primary Key의 데이터 타입이 String (keyword)이므로 <ChatbotResponse, String>으로 지정합니다.

    // 필요하다면 특정 키워드를 포함하는 응답을 찾는 메서드 등을 추가할 수 있습니다.
    // 예: List<ChatbotResponse> findByKeywordContaining(String keyword);
}