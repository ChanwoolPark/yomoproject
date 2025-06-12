package com.project.yomozomo.repository;

import com.project.yomozomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    // OAuth2 는 email 기준으로 조회하거나, provider+id 복합 키 사용 가능
    Optional<User> findByRole(String role);
    // 탈퇴 상태 아닌 사용자 (isWithdrawn = 'N')
    List<User> findByIsWithdrawn(String isWithdrawn);


    // 탈퇴 요청 후 30일 경과된 사용자 (삭제 후보)
    List<User> findByIsWithdrawnAndWithdrawnAtBefore(String isWithdrawn, LocalDateTime deadline);

    List<User> findByIsDormant(String isDormant);

    List<User> findByIsDormantAndLastLoginDateBefore(String isDormant, LocalDate lastLoginDate);



}
