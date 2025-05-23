package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration // Spring 설정 클래스임을 명시
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 메시징 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. 메시지 브로커 설정: 클라이언트에게 메시지를 전달할 때 사용할 Prefix
        //    "/topic"으로 시작하는 경로로 메시지가 전송되면, 해당 경로를 구독한 모든 클라이언트에게 메시지를 전달합니다.
        //    주로 1:N (pub-sub) 통신에 사용됩니다.
        config.enableSimpleBroker("/topic", "/queue"); // /queue는 1:1 통신에 주로 사용 (선택 사항)

        // 2. 애플리케이션 목적지 Prefix 설정: 클라이언트가 서버의 @MessageMapping으로 메시지를 보낼 때 사용할 Prefix
        //    클라이언트가 "/app"으로 시작하는 경로로 메시지를 보내면, Spring의 @MessageMapping이 붙은 컨트롤러 메서드로 라우팅됩니다.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결을 위한 STOMP 엔드포인트 등록
        // 클라이언트는 "/ws" 엔드포인트로 WebSocket 핸드셰이크를 시작합니다.
        // 예를 들어, 클라이언트에서 `new SockJS('/ws')` 와 같이 연결합니다.
        registry.addEndpoint("/ws").withSockJS(); // SockJS 지원 활성화 (하위 호환성을 위해 권장)
        // .setAllowedOrigins("*") // CORS 문제 해결 (필요시 추가, 실제 운영에서는 특정 도메인만 허용)
    }
}