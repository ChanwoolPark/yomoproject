// src/main/java/com/project/yomozomo/controller/ChatController.java
package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller; // 여기는 @Controller
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.service.ChatService;

@Controller // 뷰를 반환하는 컨트롤러
@RequestMapping("/chat") // 일반 웹 페이지 URL 프리픽스
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{roomId}") // 최종 URL: /chat/{roomId}
    public String chatRoom(@PathVariable Long roomId,
                           @RequestParam(required = false) String targetUser,
                           Principal principal,
                           Model model) {

        String currentUsername = principal.getName();

        // 1. 채팅방 정보 조회
        ChatRoom chatRoom = chatService.getChatRoomById(roomId);

        // 2. 대화 상대방 사용자 이름 결정 (로직 유지)
        String resolvedTargetUsername;
        if (targetUser != null && !targetUser.isEmpty()) {
            resolvedTargetUsername = targetUser;
        } else {
            if (currentUsername.equals(chatRoom.getBuyer().getUsername())) {
                resolvedTargetUsername = chatRoom.getSeller().getUsername();
            } else if (currentUsername.equals(chatRoom.getSeller().getUsername())) {
                resolvedTargetUsername = chatRoom.getBuyer().getUsername();
            } else {
                throw new IllegalArgumentException("현재 사용자는 이 채팅방의 유효한 참여자가 아닙니다.");
            }
        }

        // 3. Model에 필요한 데이터 추가
        model.addAttribute("username", currentUsername);
        model.addAttribute("targetUsername", resolvedTargetUsername);
        model.addAttribute("roomId", roomId);

        // 4. 렌탈 정보 추가
        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRental", chatRoom.getRental());
        } else {
            model.addAttribute("currentRental", null);
        }

        return "chat"; // "chat.html" 템플릿을 렌더링
    }
}