package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;

@Configuration // Spring 설정 클래스임을 명시
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 메시징 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 서버로 메시지를 보낼 때 사용할 경로의 프리픽스.
        // @MessageMapping이 붙은 컨트롤러 메서드를 찾을 때 사용됩니다.
        // 클라이언트에서 stompClient.send("/app/...") 와 같이 사용합니다.
        config.setApplicationDestinationPrefixes("/app"); // ⭐ 이 줄의 주석을 해제하거나 추가합니다. ⭐

        // 서버에서 클라이언트로 메시지를 브로드캐스트할 때 사용할 경로의 프리픽스.
        // 클라이언트에서 stompClient.subscribe("/sub/...") 와 같이 구독할 때 사용합니다.
        // 현재 ChatController에서 /sub/chat/room/{chatRoomId}를 사용하고 있으므로,
        // 이 부분은 /sub가 되어야 합니다.
        config.enableSimpleBroker("/sub"); // ⭐ "/topic" 대신 "/sub"로 변경합니다. (또는 둘 다 유지) ⭐
        // /topic 이 필요하다면 config.enableSimpleBroker("/sub", "/topic");
        // 하지만 현재 채팅 로직에서는 /sub만 필요해 보입니다.
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .withSockJS();
        // .setAllowedOrigins("*")는 개발 단계에서 유용하지만, 보안상 운영에서는 특정 도메인으로 제한해야 합니다.
        // 현재 코드에서는 이 부분이 이미 제거되어 있으므로 그대로 둡니다.
    }

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