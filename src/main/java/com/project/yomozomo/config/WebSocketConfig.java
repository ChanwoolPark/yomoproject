package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub", "/topic");
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // "*" 대신 클라이언트가 실행되는 실제 오리진을 명시합니다.
                // 보통 개발 중에는 프론트엔드가 실행되는 주소 (예: http://localhost:8080)입니다.
                // 또는 `allowedOriginPatterns`를 사용합니다 (더 유연함).
                // 이 예시에서는 명시적인 오리진을 사용합니다.
                .setAllowedOrigins("http://localhost:8080") // 또는 클라이언트가 실행되는 실제 주소
                // 만약 클라이언트가 http://localhost:3000 에서 실행된다면: .setAllowedOrigins("http://localhost:3000")
                // 여러 오리진을 허용해야 한다면 쉼표로 구분하여 나열할 수 있습니다.
                // 예: .setAllowedOrigins("http://localhost:8080", "http://yourfrontend.com", "http://anotherdomain.com")
                .withSockJS();
    }

    // CorsFilter는 이 문제와 직접 관련이 없으므로, 현재로서는 제거하거나 주석 처리하는 것이 좋습니다.
    // 다른 HTTP 요청에서 CORS 문제가 없다면 그대로 두어도 무방합니다.
    /*
    @Bean
    public CorsFilter corsFilter() {
        // ... (기존 CorsFilter 코드 유지 또는 제거) ...
    }
    */
}