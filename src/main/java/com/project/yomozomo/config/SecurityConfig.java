package com.project.yomozomo.config;

import com.project.yomozomo.security.OAuth2LoginSuccessHandler;
import com.project.yomozomo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.HashMap;
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
        System.out.println("★ SecurityConfig 로드됨");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin1/**").hasAuthority("ADMIN")
                        .requestMatchers(
                                "/", "/index.html", "/login", "/login/form", "/login/**",
                                "/css/**", "/js/**", "/images/**",
                                "/charge", "/oauth2/**", "/signup", "/category",
                                "/category/**","/api/**", "/find-id.html", "/find-password",
                                "/find-id","/find-password.html", "/profile/**",
                                "/test-login", "/uploads/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")  // ★★★ 로그인 진입 선택화면 ("/login")으로 지정
                        .loginProcessingUrl("/login/form") // 실제 로그인 submit POST action
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login/form?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService((userRequest) -> {
                                    String registrationId = userRequest.getClientRegistration().getRegistrationId();
                                    if ("kakao".equals(registrationId)) {
                                        return kakaoOAuth2UserService().loadUser(userRequest);
                                    } else if ("naver".equals(registrationId)) {
                                        return naverOAuth2UserService().loadUser(userRequest);
                                    } else {
                                        return new DefaultOAuth2UserService().loadUser(userRequest);
                                    }
                                })
                        )
                        .successHandler(successHandler)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * 네이버가 반환하는 JSON 구조(response 안에 id, name, email 등 있음)를
     *
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

    OAuth2UserService<OAuth2UserRequest, OAuth2User> kakaoOAuth2UserService() {
        return userRequest -> {
            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();

            // 카카오는 "kakao_account" 안에 email, profile 등이 들어있음
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) profile.get("nickname");
            String profileImageUrl = (String) profile.get("profile_image_url");

            // 💥 null-safe 처리: 이메일이 null이면 임시로 fallback 값 지정
            if (email == null) {
                email = "kakao_" + attributes.get("id"); // or UUID.randomUUID().toString();
            }

            Map<String, Object> customAttributes = new HashMap<>();
            customAttributes.put("email", email);
            customAttributes.put("nickname", nickname);
            customAttributes.put("profile_image_url", profileImageUrl);

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    customAttributes,
                    "email"
            );
        };
    }


}