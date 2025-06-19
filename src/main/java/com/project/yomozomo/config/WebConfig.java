package com.project.yomozomo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.yml 등에서 세팅된 파일 업로드 디렉토리 (프로필용)
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 프로필 이미지 (ex: /uploads/** → uploadDir로 매핑)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");

        // 2. 채팅 이미지 (ex: /uploaded-chat-images/** → 실제 경로로 매핑)
        registry.addResourceHandler("/uploaded-chat-images/**")
                .addResourceLocations("file:/C:/Users/dbseh/OneDrive/문서/GitHub/yomoProject/uploaded-files/image-chatimage/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // 3. 기존 정적 리소스 (static, public 등)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/");
    }
}
