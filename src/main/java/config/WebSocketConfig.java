// src/main/java/com/chat/config/WebSocketConfig.java
package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 발행 요청 URL (클라이언트 -> 서버)
        config.setApplicationDestinationPrefixes("/app");
        // 메시지 구독 요청 URL (서버 -> 클라이언트)
        // Simple Message Broker (메모리 기반) 사용
        config.enableSimpleBroker("/topic", "/queue");
        // 외부 메시지 브로커 (RabbitMQ, Kafka 등) 사용 시 config.enableStompBrokerRelay()
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket Handshake를 위한 Endpoint 설정
        // 클라이언트에서 ws://localhost:8080/ws-chat 로 연결
        registry.addEndpoint("/ws-chat").withSockJS(); // SockJS 지원
        // SockJS 없이 순수 WebSocket만 사용하려면 .withSockJS() 제거
    }
}