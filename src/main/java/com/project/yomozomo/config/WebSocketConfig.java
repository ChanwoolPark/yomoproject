package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// ⭐ 추가: CORS 설정을 위한 어노테이션 임포트 ⭐
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;

@Configuration // Spring 설정 클래스임을 명시
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 메시징 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        // 이 줄은 이미 주석 처리되어 있어야 합니다.
        // config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ⭐ 이 부분을 수정합니다. ⭐
        registry.addEndpoint("/ws")
                .withSockJS();
        // .setAllowedOrigins("*") 부분을 제거합니다. (위 오류 때문에)
    }

    // ⭐ CORS 필터를 빈(Bean)으로 추가하여 웹소켓 CORS 문제를 해결합니다. ⭐
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // 모든 오리진 허용 (개발 단계에서)
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용 (GET, POST 등)
        config.setAllowCredentials(true); // 쿠키 및 인증 정보 허용

        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 CORS 설정 적용
        return new CorsFilter(source);
    }
}