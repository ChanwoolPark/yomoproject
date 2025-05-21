// src/main/java/com/chat/repository/UsersRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUserId(Long userId);
    Optional<Users> findByUsername(String username);
}