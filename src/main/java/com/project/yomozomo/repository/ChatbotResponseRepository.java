package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotResponseRepository extends JpaRepository<ChatbotResponse, String> {
    // JpaRepository<엔티티 타입, Primary Key 타입>
    // Spring Data JPA가 이 인터페이스를 기반으로 자동으로 데이터베이스 연동 코드를 생성해 줍니다.
    // 별도의 Hibernate 코드를 직접 작성할 필요가 없습니다.

    // 필요한 경우, 특정 조건에 맞는 데이터를 조회하는 메서드를 추가할 수 있습니다.
    // 예: List<ChatbotResponse> findByResponseContaining(String text);
}