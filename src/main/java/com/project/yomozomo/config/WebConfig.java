package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 기존 /uploaded-files/** 매핑은 그대로 두거나 제거할 수 있습니다.
        // 현재 이미지 파일은 이 경로에 저장되지 않으므로, 이 매핑이 필요 없으면 제거해도 됩니다.
        registry.addResourceHandler("/uploaded-files/**")
                .addResourceLocations("file:///C:/Users/soldesk/IdeaProjects/yomoproject/uploaded-files/");

        // ⭐⭐⭐ 새로 추가되는 이미지 파일 매핑 ⭐⭐⭐
        // ChatController에서 파일을 저장할 경로와 동일하게 설정합니다.
        // C:/Users/soldesk/IdeaProjects/yomoproject/uploads/image-chatimage
        registry.addResourceHandler("/uploaded-chat-images/**") // 웹에서 접근할 URL 경로
                .addResourceLocations("file:///C:/Users/soldesk/IdeaProjects/yomoproject/uploads/image-chatimage/"); // 실제 파일 시스템 경로
    }
}