// src/main/java/com/project/yomozomo/repository/ChatbotOptionRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatbotOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotOptionRepository extends JpaRepository<ChatbotOption, Long> {
}