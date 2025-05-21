package com.project.yomozomo.service;

import com.project.yomozomo.domain.User;
import com.project.yomozomo.dto.SignupForm;
import com.project.yomozomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepo.existsByPhone(phone);
    }

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

        u.setReferral(form.getReferral());
        u.setProfileImageUrl(form.getProfileImageUrl());

        userRepo.save(u);

        // --- 여기서 바로 로그인 처리 추가 ---
        UserDetails userDetails = userDetailsService.loadUserByUsername(u.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return u;
    }
}
