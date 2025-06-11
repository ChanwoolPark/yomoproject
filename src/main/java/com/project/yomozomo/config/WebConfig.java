package com.project.yomozomo.config;// WebConfig.java (예시)
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 'uploads' 대신 'uploaded-files'로 변경
        registry.addResourceHandler("/uploaded-files/**")
                .addResourceLocations("file:///C:/Users/soldesk/IdeaProjects/yomoproject/uploaded-files/");
        // 또는 로컬 개발 환경에서 상대 경로를 사용하고 싶다면
        // .addResourceLocations("file:./uploaded-files/");
        // 하지만 절대 경로를 사용하는 것이 더 안정적입니다.
    }
}