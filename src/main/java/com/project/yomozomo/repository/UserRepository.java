package com.project.yomozomo.repository;

import com.project.yomozomo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    // OAuth2 는 email 기준으로 조회하거나, provider+id 복합 키 사용 가능
}
