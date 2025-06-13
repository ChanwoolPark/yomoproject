/*
package com.project.yomozomo.controller;

import com.project.yomozomo.dto.ChatMessageDTO;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/websocket")
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;
    private final SimpMessagingTemplate messagingTemplate;


    // 📢 2. 채팅방 입장 (JOIN 메시지)
    @MessageMapping("/pub/chat.addUser/{chatRoomId}")
    public void addUser(@Payload ChatMessageDTO chatMessageDto,
                        @DestinationVariable Long chatRoomId) {
        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            chatMessageDto.setRoomId(chatRoomId);
        }
        User senderUser = userService.getUserById(chatMessageDto.getSenderId());
        String senderNickname = senderUser != null ? senderUser.getNickname() : "알 수 없는 사용자";
        chatMessageDto.setSenderName(senderNickname);

        String joinMsg = senderNickname + "님이 입장하셨습니다.";
        chatMessageDto.setMessage(joinMsg);
        chatMessageDto.setMessageType(ChatMessageDTO.MessageType.SYSTEM);
        chatMessageDto.setSendTime(LocalDateTime.now());

        try {
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    null,
                    "SYSTEM"
            );
            log.info("입장 메시지 저장 성공(RoomID: {}, Sender: {})", chatRoomId, senderNickname);
        } catch (Exception e) {
            log.error("입장 메시지 저장 오류: {}", e.getMessage(), e);
        }
        messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoomId, chatMessageDto);
        log.info("입장 메시지 브로드캐스트 완료(RoomID: {})", chatRoomId);
    }

    // 📄 3. 채팅 시작(예약 상품 기준)
    @GetMapping("/chat/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅을 시작하려면 로그인이 필요합니다.");
            return "redirect:/login";
        }
        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null || rental.getProduct() == null || rental.getProduct().getSeller() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "렌탈 상품 또는 판매자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage";
        }
        User buyer = userService.getUserByUsername(principal.getName());
        User sellerUser = rental.getProduct().getSeller();
        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, sellerUser, rental.getRentalId());
        return "redirect:/websocket/chat/" + chatRoomId;
    }

    // 📄 4. 채팅방 페이지 진입
    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅방에 접근하려면 로그인이 필요합니다.");
            return "redirect:/login";
        }
        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage";
        }
        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));
        boolean isParticipant = (currentUser.getId().equals(chatRoom.getBuyer().getId()) ||
                currentUser.getId().equals(chatRoom.getSeller().getId()));
        if (!isParticipant) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/access-denied";
        }
        User chatPartnerUser = currentUser.getId().equals(chatRoom.getBuyer().getId())
                ? chatRoom.getSeller() : chatRoom.getBuyer();
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUserName", currentUser.getNickname());
        model.addAttribute("chatPartnerId", chatPartnerUser.getId());
        model.addAttribute("chatPartnerNickname", chatPartnerUser.getNickname());
        model.addAttribute("chatRoomId", roomId);

        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRentalId", chatRoom.getRental().getRentalId());
            if (chatRoom.getRental().getProduct() != null) {
                model.addAttribute("productTitle", chatRoom.getRental().getProduct().getTitle());
            } else {
                model.addAttribute("productTitle", "상품 정보 없음");
            }
        } else {
            model.addAttribute("currentRentalId", null);
            model.addAttribute("productTitle", "일반 채팅");
        }
        // 프로필 이미지 세팅
        String curUserImg = currentUser.getProfileImageUrl();
        if (curUserImg == null || curUserImg.isEmpty()) curUserImg = "/images/default-profile.png";
        String partnerImg = chatPartnerUser.getProfileImageUrl();
        if (partnerImg == null || partnerImg.isEmpty()) partnerImg = "/images/default-profile.png";
        model.addAttribute("currentUserProfileImage", curUserImg);
        model.addAttribute("chatPartnerProfileImage", partnerImg);

        // 채팅 내역
        try {
            List<com.project.yomozomo.entity.ChatMessage> chatHistoryEntities = chatService.getChatMessagesByRoomId(roomId);
            List<ChatMessageDTO> chatHistoryDtos = chatHistoryEntities.stream().map(entity -> {
                ChatMessageDTO dto = new ChatMessageDTO();
                dto.setRoomId(entity.getRoomId());
                dto.setSenderId(entity.getSenderId());
                User sender = userService.getUserById(entity.getSenderId());
                dto.setSenderName(sender != null ? sender.getNickname() : "알 수 없는 사용자");
                dto.setMessage(entity.getMessage());
                dto.setImgUrl(entity.getImgUrl());
                try {
                    dto.setMessageType(ChatMessageDTO.MessageType.valueOf(entity.getMessageType()));
                } catch (IllegalArgumentException e) {
                    dto.setMessageType(ChatMessageDTO.MessageType.TEXT);
                }
                dto.setSendTime(entity.getSendTime());
                return dto;
            }).toList();
            model.addAttribute("chatHistory", chatHistoryDtos);
        } catch (Exception e) {
            model.addAttribute("chatHistory", new ArrayList<>());
        }
        return "chat"; // chat.html 반환
    }

    // 📄 5. 신고 폼
    @GetMapping("/chat/reportForm")
    public String showReportForm(Model model, Principal principal) {
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            if (currentUser != null) {
                model.addAttribute("reporterId", currentUser.getId());
                model.addAttribute("reporterNickname", currentUser.getNickname());
            }
        }
        return "report";
    }
}
*/
