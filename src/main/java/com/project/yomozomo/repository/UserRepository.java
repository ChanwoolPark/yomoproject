package com.project.yomozomo.repository;

import com.project.yomozomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findByNameAndEmail(String name, String email);
    boolean existsByUsernameAndEmail(String username, String email);
    boolean existsByNickname(String nickname);
    Optional<User> findById(Long id);
}