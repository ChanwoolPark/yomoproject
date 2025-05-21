// src/main/java/com/project/yomozomo/config/SecurityConfig.java
package com.project.yomozomo.config;

import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.security.OAuth2LoginSuccessHandler;
import com.project.yomozomo.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;

    public SecurityConfig(OAuth2LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index.html", "/login",
                                "/css/**", "/js/**", "/images/**",
                                "/charge", "/oauth2/**", "/signup", "/category",
                                "/category/**",
                                "/test-login"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        // --- 여기에 userInfoEndpoint 추가 ---
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(naverOAuth2UserService())
                        )
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * 네이버가 반환하는 JSON 구조(response 안에 id, name, email 등 있음)를
     * 언팩해서 DefaultOAuth2User를 만들어줍니다.
     */
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> naverOAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return userRequest -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = oauth2User.getAttribute("response");
            return new DefaultOAuth2User(
                    oauth2User.getAuthorities(),
                    resp,
                    "id"    // 이제 이 “id”가 네이버 실제 사용자 ID
            );
        };
    }
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }
}