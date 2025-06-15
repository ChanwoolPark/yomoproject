package com.project.yomozomo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 클라이언트가 요청할 웹 경로: /uploaded-chat-images/**
        // ChatController에서 파일을 저장하는 실제 절대 경로: C:\Users\김승주\IdeaProjects\yomoproject\|uploaded-files\image-chatimage\
        registry.addResourceHandler("/uploaded-chat-images/**")
                // ⭐⭐⭐ 이 경로를 새 경로로 변경 ⭐⭐⭐
                .addResourceLocations("file:///C:/Users/soldesk/IdeaProjects/yomoproject/uploaded-files/image-chatimage/")// <--- 주의: 슬래시 방향
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());


        // Spring Boot의 기본 정적 자원 핸들러 (기존 static 폴더 리소스용)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
    }
}