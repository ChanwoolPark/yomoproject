package com.project.yomozomo.config;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.security.CustomAuthenticationEntryPoint;
import com.project.yomozomo.security.OAuth2LoginSuccessHandler;
import com.project.yomozomo.service.CustomUserDetailsService;
import com.project.yomozomo.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    private final CustomAuthenticationEntryPoint customEntryPoint;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    // 필요한 서비스들을 생성자를 통해 주입받습니다.
    public SecurityConfig(OAuth2LoginSuccessHandler successHandler,
                          CustomAuthenticationEntryPoint customEntryPoint,
                          CustomUserDetailsService userDetailsService,
                          UserService userService) {
        this.successHandler = successHandler;
        this.customEntryPoint = customEntryPoint;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("★ SecurityConfig 로드됨"); // 로드 확인용 로그

        http
                // 예외 처리 설정: 인증되지 않은 접근에 대한 커스텀 진입점 지정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customEntryPoint)
                )
                // HTTP 요청에 대한 인가(Authorization) 설정
                .authorizeHttpRequests(auth -> auth
                        // "/admin/**" 경로는 ADMIN 권한을 가진 사용자만 접근 허용
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        // 아래 나열된 경로들은 모든 사용자에게 접근 허용 (permitAll)
                        .requestMatchers(
                                "/", "/index.html", "/login", "/login/form", "/login/**",
                                "/login-page", // 로그인 페이지 관련 경로
                                "/css/**", "/js/**", "/images/**", // 정적 리소스
                                "/oauth2/**", // OAuth2 로그인 관련 경로
                                "/signup", "/category", "/category/**", // 회원가입, 카테고리
                                "/api/**", // API 엔드포인트
                                "/find-id.html", "/find-password", "/find-id", "/find-password.html", // 아이디/비밀번호 찾기
                                "/test-login", "/uploads/**", // 테스트, 업로드 파일 접근
                                "/login-required", "/subcategory/**", "/product/**", // 로그인 요구 페이지, 상품 관련
                                "/mypage/profile/{username}" // 특정 프로필 페이지
                        ).permitAll()
                        // 위에 명시되지 않은 모든 다른 요청은 인증된 사용자만 접근 허용
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")  // 로그인 폼 페이지 URL
                        .loginProcessingUrl("/login/form") // 로그인 폼 제출(POST) URL
                        // 로그인 성공 시 실행될 커스텀 핸들러 (람다 표현식)
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName(); // 로그인 성공한 사용자 이름(ID) 가져오기
                            User user = userService.getUserByUsername(username); // 사용자 정보 조회

                            // 마지막 로그인 날짜 업데이트
                            userService.updateLastLoginDate(user);

                            // 탈퇴 계정인 경우 복구
                            if (user != null && "Y".equals(user.getIsWithdrawn())) {
                                userService.recoverWithdrawn(user);
                                System.out.println("★ 탈퇴 계정 복구 완료: " + username); // 복구 로그
                            }

                            // 휴면 계정인 경우 휴면 안내 페이지로 리다이렉트
                            if (user != null && "Y".equals(user.getIsDormant())) {
                                response.sendRedirect("/dormant-info");
                                System.out.println("★ 휴면 계정 로그인: /dormant-info로 리다이렉트 - " + username); // 휴면 계정 로그
                                return; // 리다이렉트 후 종료
                            }

                            // 로그인 전 접근하려던 URL이 있다면 그곳으로, 없으면 루트("/")로 리다이렉트
                            String redirectUrl = request.getParameter("redirect");
                            if (redirectUrl != null && redirectUrl.startsWith("/")) {
                                response.sendRedirect(redirectUrl);
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .failureUrl("/login/form?error=true") // 로그인 실패 시 리다이렉트 URL
                        .permitAll() // 로그인 관련 모든 페이지 허용
                )
                // OAuth2 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // OAuth2 로그인 시작 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                // 사용자 정보 서비스 커스터마이징 (네이버/카카오 특화 처리)
                                .userService((userRequest) -> {
                                    String registrationId = userRequest.getClientRegistration().getRegistrationId();
                                    if ("kakao".equals(registrationId)) {
                                        return kakaoOAuth2UserService().loadUser(userRequest);
                                    } else if ("naver".equals(registrationId)) {
                                        return naverOAuth2UserService().loadUser(userRequest);
                                    } else {
                                        // 다른 OAuth2 제공자는 기본 서비스 사용
                                        return new DefaultOAuth2UserService().loadUser(userRequest);
                                    }
                                })
                        )
                        .successHandler(successHandler) // OAuth2 로그인 성공 시 커스텀 핸들러 사용
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 처리 URL
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트 URL
                        .permitAll() // 로그아웃 관련 모든 페이지 허용
                )
                // Remember Me (로그인 유지) 기능 설정
                .rememberMe(remember -> remember
                        .key("yomozomo-remember-me-key") // 토큰 생성에 사용될 고유 키
                        .tokenValiditySeconds(60 * 60 * 24 * 14) // 토큰 유효 기간 (14일)
                        .rememberMeParameter("remember-me") // HTML 폼에서 리멤버 미 체크박스 이름
                        .userDetailsService(userDetailsService) // 리멤버 미 기능이 사용자 정보를 로드할 서비스
                )
                // CSRF (Cross-Site Request Forgery) 보호 비활성화 (개발 편의를 위해, 운영 시 재고)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * 네이버 OAuth2 사용자 정보 처리 서비스
     * 네이버 API 응답의 'response' 객체에서 실제 사용자 정보를 추출하여 DefaultOAuth2User를 생성합니다.
     */
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> naverOAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return userRequest -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            // 네이버 응답은 "response" 키 안에 실제 사용자 정보가 중첩되어 있습니다.
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = oauth2User.getAttribute("response");
            return new DefaultOAuth2User(
                    oauth2User.getAuthorities(), // 권한
                    resp, // 실제 사용자 속성
                    "id"    // 네이버 사용자 ID를 주 식별자로 사용
            );
        };
    }

    /**
     * 카카오 OAuth2 사용자 정보 처리 서비스
     * 카카오 API 응답의 'kakao_account' 및 'profile' 객체에서 사용자 정보를 추출합니다.
     */
    OAuth2UserService<OAuth2UserRequest, OAuth2User> kakaoOAuth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return userRequest -> {
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();

            // 카카오 사용자 정보는 "kakao_account" 내에 이메일, 프로필 등이 들어있습니다.
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) profile.get("nickname");
            String profileImageUrl = (String) profile.get("profile_image_url");

            // 이메일이 null인 경우 (사용자가 이메일 제공에 동의하지 않은 경우) 대체 값 설정
            if (email == null) {
                email = "kakao_" + attributes.get("id"); // 카카오 고유 ID를 기반으로 대체 이메일 생성
            }

            // 필요한 속성들을 새로운 맵에 담아 전달 (DefaultOAuth2User 생성 시 사용)
            Map<String, Object> customAttributes = new HashMap<>();
            customAttributes.put("email", email);
            customAttributes.put("nickname", nickname);
            customAttributes.put("profile_image_url", profileImageUrl);

            return new DefaultOAuth2User(
                    // 기본적으로 "ROLE_USER" 권한 부여
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    customAttributes, // 커스텀 속성
                    "email" // "email"을 주 식별자로 사용 (Primary Key 역할)
            );
        };
    }
}