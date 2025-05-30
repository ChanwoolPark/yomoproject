package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.domain.Rental; // 추가
import com.project.yomozomo.entity.User;   // 추가
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService; // 추가
import com.project.yomozomo.service.UserService;   // 추가


@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;     // 추가
    private final RentalService rentalService; // 추가

    // 기존: 특정 roomId로 바로 채팅방 접속 (채팅방이 없으면 오류 발생)
    @GetMapping("/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           @RequestParam(required = false) String targetUser,
                           Principal principal,
                           Model model) {

        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername); // 현재 로그인한 사용자 정보 조회

        ChatRoom chatRoom = chatService.getChatRoomById(roomId); // 채팅방이 없으면 IllegalArgumentException 발생

        // 대화 상대방 사용자 이름 결정 로직 (기존과 동일)
        String resolvedTargetUsername;
        if (targetUser != null && !targetUser.isEmpty()) {
            resolvedTargetUsername = targetUser;
        } else {
            // 현재 사용자가 채팅방의 구매자이면 상대는 판매자, 아니면 구매자 (혹은 오류)
            if (currentUser.getId().equals(chatRoom.getBuyer().getId())) { // ID 비교로 변경
                resolvedTargetUsername = chatRoom.getSeller().getUsername();
            } else if (currentUser.getId().equals(chatRoom.getSeller().getId())) { // ID 비교로 변경
                resolvedTargetUsername = chatRoom.getBuyer().getUsername();
            } else {
                throw new IllegalArgumentException("현재 사용자는 이 채팅방의 유효한 참여자가 아닙니다.");
            }
        }

        model.addAttribute("username", currentUsername);
        model.addAttribute("targetUsername", resolvedTargetUsername);
        model.addAttribute("roomId", roomId);

        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRental", chatRoom.getRental());
        } else {
            model.addAttribute("currentRental", null);
        }

        return "chat";
    }

    // 새롭게 추가: rentalId를 기반으로 채팅방을 조회하거나 생성하는 엔드포인트
    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId,
                                      Principal principal,
                                      Model model) {
        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername); // 현재 로그인한 사용자 (구매자 역할)

        // 렌탈 정보 조회
        Rental rental = rentalService.getRentalById(rentalId);
        User seller = rental.getUser(); // 렌탈 상품을 등록한 사용자 (판매자 역할)

        // 현재 사용자가 렌탈 상품의 판매자 본인인 경우 (자신과 채팅할 수 없으므로 예외 처리)
        if (currentUser.getId().equals(seller.getId())) {
            throw new IllegalArgumentException("자신이 등록한 상품과 채팅을 시작할 수 없습니다.");
        }

        // 채팅방을 찾거나 생성합니다.
        // buyer는 현재 로그인한 사용자, seller는 렌탈 상품의 사용자
        Long chatRoomId = chatService.findOrCreateChatRoom(currentUser, seller, rental);

        // 생성되거나 찾아진 채팅방으로 리다이렉트
        // 리다이렉트 시에는 @RequestParam 대신 @PathVariable을 사용하는 기존 엔드포인트로 이동
        return "redirect:/chat/" + chatRoomId;
    }
}