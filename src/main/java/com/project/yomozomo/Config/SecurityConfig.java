package com.project.yomozomo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()    // 이 경로들은 누구나
                        .anyRequest().authenticated()  // 나머진 로그인 필요
                )
                // 2) 로그인 페이지 설정
                .formLogin(form -> form
                        .loginPage("/login")          // 내가 만든 로그인 폼 매핑
                        .defaultSuccessUrl("/", true) // 로그인 성공 후 리다이렉트
                        .permitAll()                  // 로그인 페이지는 모두 허용
                )
                // 3) 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // 4) 필요하다면 CSRF 비활성화
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
