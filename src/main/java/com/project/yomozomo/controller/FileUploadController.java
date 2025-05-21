// src/main/java/com/chat/controller/FileUploadController.java
package com.chat.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload-dir}") // application.yml에서 설정한 경로 주입
    private String uploadDir;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 파일명 중복 방지를 위해 UUID 사용
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path copyLocation = Paths.get(uploadDir + "/" + fileName);

            // 파일 저장
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

            // 클라이언트가 접근할 수 있는 URL 생성
            // 실제 배포 시에는 Nginx 또는 웹 서버 설정에 따라 URL이 달라질 수 있습니다.
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/") // 서버에서 이미지에 접근할 수 있는 경로를 별도로 설정해야 합니다.
                    .path(fileName)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileUrl", fileDownloadUri);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Could not upload the file: " + e.getMessage()));
        }
    }
}