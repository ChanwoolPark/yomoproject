package com.project.yomozomo.service;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.dto.SignupForm;
import com.project.yomozomo.repository.ReviewRepository;
import com.project.yomozomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final ReviewRepository reviewRepository;

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepo.existsByPhone(phone);
    }


    // 아이디 찾기 (이름+이메일)
    public Optional<String> findUsernameByNameAndEmail(String name, String email) {
        return userRepo.findByNameAndEmail(name, email)
                .map(User::getUsername);
    }

    // 비밀번호 찾기 (아이디+이메일)
    public boolean existsByUsernameAndEmail(String username, String email) {
        return userRepo.existsByUsernameAndEmail(username, email);
    }

    @Transactional
    public void updatePassword(String username, String newPassword) {
        userRepo.findByUsername(username).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword)); // 반드시 암호화!
            userRepo.save(user);
            userRepo.flush();
        });
    }

    public boolean existsByNickname(String nickname) {
        return userRepo.existsByNickname(nickname);
    }
    @Transactional
    public User registerNewUser(SignupForm form) {
        User u = new User();
        u.setUsername(form.getUsername());
        u.setNickname(form.getNickname());
        u.setEmail(form.getEmail());
        u.setName(form.getName());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.setGender(form.getGender());
        u.setBirthdate(form.getBirthdate());
        u.setPhone(form.getPhone());
        u.setReferral(form.getReferral());
        u.setProfileImageUrl(form.getProfileImageUrl());

        // ↓ 추가된 주소 필드들
        u.setZipNo(form.getZipNo());                 // 우편번호
        u.setAddress(form.getAddress());             // 도로명 주소
        u.setAddressDetail(form.getAddressDetail()); // 상세 주소

        u.setRole("ROLE_USER");
        userRepo.save(u);

        // --- 여기서 바로 로그인 처리 추가 ---
        UserDetails userDetails = userDetailsService.loadUserByUsername(u.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return u;
    }


    @Transactional
    public void updateLastLoginDate(User user) {
        System.out.println("로그인 날짜 갱신! " + user.getUsername());
        user.setLastLoginDate(LocalDate.now());
        System.out.println("user.getLastLoginDate() = " + user.getLastLoginDate());
        userRepo.save(user);
    }

    // 현재 로그인한 user 아이디로 User 조회
    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    public User getUserById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
    }

    // 평점, 리뷰 수 집계해서 유저에 저장
    public void updateUserRatingAndCount(Long targetUserId) {
        Double avg = reviewRepository.findAverageRatingByTargetId(targetUserId).orElse(0.0);
        int cnt = reviewRepository.countByTarget_Id(targetUserId);

        User user = userRepo.findById(targetUserId).orElseThrow();
        user.setRating(BigDecimal.valueOf(avg));// 평점 저장 (user_rating 컬럼)
        user.setReviewCount(cnt);   // 리뷰 개수 저장 (review_count 컬럼)
        userRepo.save(user);
    }
    // 휴면 처리
    @Transactional
    public void markDormant(User user) {
        user.setIsDormant("Y");
        user.setDormantDate(LocalDate.now());
        userRepo.save(user);
    }

    // 휴면 해제 (로그인 시 처리)
    @Transactional
    public void clearDormant(User user) {
        user.setIsDormant("N");
        user.setDormantDate(null);
        userRepo.save(user);
    }

    // 회원 탈퇴(soft delete)
    @Transactional
    public void withdraw(User user) {
        user.setIsWithdrawn("Y");
        user.setWithdrawnAt(LocalDateTime.now());
        userRepo.save(user);
    }

    // 탈퇴 복구 (30일 이내 로그인)
    @Transactional
    public void recoverWithdrawn(User user) {
        user.setIsWithdrawn("N");
        user.setWithdrawnAt(null);
        userRepo.save(user);
    }

    // 30일 지난 탈퇴자 개인정보 초기화
    @Transactional
    public void anonymizeWithdrawnUsersOlderThan(LocalDateTime threshold) {
        List<User> users = userRepo.findByIsWithdrawnAndWithdrawnAtBefore("Y", threshold);
        for (User user : users) {
            user.setName("탈퇴회원");
            user.setEmail("withdrawn_" + user.getUsername() + "@deleted.com");
            user.setNickname("탈퇴회원" + user.getUsername());
            user.setPhone(null);
            user.setProfileImageUrl(null);
            user.setAddress(null);
            user.setAddressDetail(null);
            user.setReferral(null);
            user.setUsername("탈퇴한 사용자");
            user.setBirthdate(null);
            user.setGender(null);
            user.setGrade(null);
            user.setZipNo(null);
        }
        userRepo.saveAll(users);
    }

    public List<User> getDormantUsers() {
        return userRepo.findByIsDormant("Y");
    }

    @Transactional
    public void markDormantUsersOlderThan(LocalDate standardDate) {
        List<User> users = userRepo.findByIsDormantAndLastLoginDateBefore("N", standardDate);
        for (User user : users) {
            user.setIsDormant("Y");
            user.setDormantDate(LocalDate.now());
        }
        userRepo.saveAll(users);
    }


    public Optional<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}
