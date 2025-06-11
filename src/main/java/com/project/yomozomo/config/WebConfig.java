package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기존 /uploaded-files/** 매핑은 그대로 두거나 제거.

        // ⭐⭐⭐ 이 부분이 가장 중요합니다. ⭐⭐⭐
        // ChatController에서 파일을 저장하는 실제 경로와 정확히 일치해야 합니다.
        // 그리고 'file:///' 접두사 다음에 오는 경로 구분자는 항상 슬래시 '/'여야 합니다.
        registry.addResourceHandler("/uploaded-chat-images/**") // 클라이언트가 요청할 웹 URL (예: http://localhost:8080/uploaded-chat-images/my_image.png)
                .addResourceLocations("file:///C:/Users/soldesk/IdeaProjects/yomoproject/uploaded-files/image-chatimage/"); // 실제 파일 시스템 경로 (슬래시 주의!)
    }
}