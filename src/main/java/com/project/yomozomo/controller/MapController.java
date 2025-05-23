package com.project.yomozomo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController { // 또는 기존 HomeController, ChatController
    // application.yml에 정의된 naver.map.client-id 값을 주입
    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    @GetMapping("/map") // 지도를 보여줄 URL
    public String showMapPage(Model model) {
        // 모델에 Client ID를 추가하여 Thymeleaf 템플릿으로 전달
        model.addAttribute("naverMapClientId", naverMapClientId);
        return "map"; // src/main/resources/templates/map.html 템플릿 반환
    }

//    // 만약 chat.html에서 지도를 보여준다면, ChatController에 추가하거나
//    // chat.html을 렌더링하는 컨트롤러에 이 로직을 추가합니다.
//    @GetMapping("/chat") // 이 URL로 접속했을 때 chat.html을 보여줍니다.
//    public String chatPage(Model model) {
//        model.addAttribute("naverMapsClientId", naverMapClientId);
//        return "chat"; // 여기서 "chat"은 src/main/resources/templates/chat.html을 의미합니다.
//    }
}