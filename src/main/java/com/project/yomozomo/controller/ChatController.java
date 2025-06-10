package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.dto.ChatMessageDTO;

import java.security.Principal;
import java.time.LocalDateTime; // 필요한 경우 유지 (날짜/시간 DTO/엔티티 매핑에 따라)
import java.util.ArrayList;
import java.util.List;
// import java.util.Optional; // 이제 사용되지 않으면 제거

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat") // 이 컨트롤러는 /chat 경로의 웹 페이지를 담당합니다.
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;
    // private final SimpMessagingTemplate messagingTemplate; // 웹소켓 관련이므로 제거

    // ====================================================================================
    // ⭐⭐ 웹 페이지 (GetMapping) 로직만 남김 ⭐⭐
    // ====================================================================================

    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅을 시작하려면 로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null || rental.getProduct() == null || rental.getProduct().getSeller() == null) {
            log.warn("렌탈 상품 또는 판매자 정보를 찾을 수 없음 - rentalId: {}", rentalId);
            redirectAttributes.addFlashAttribute("errorMessage", "렌탈 상품 또는 판매자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage";
        }

        User buyer = userService.getUserByUsername(principal.getName());
        User sellerUser = rental.getProduct().getSeller();

        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, sellerUser, rental.getRentalId());
        log.info("렌탈 상품 채팅방 시작 - rentalId: {}, chatRoomId: {}", rentalId, chatRoomId);
        return "redirect:/chat/" + chatRoomId; // 이 컨트롤러의 /chat/{roomId} 매핑으로 리다이렉트
    }

    @GetMapping("/{roomId}")
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
            log.error("현재 로그인된 사용자({})를 DB에서 찾을 수 없음.", currentUsername);
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage";
        }

        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> {
                    log.warn("채팅방을 찾을 수 없습니다. ID: {}", roomId);
                    return new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId);
                });

        boolean isParticipant = (currentUser.getId().equals(chatRoom.getBuyer().getId()) ||
                currentUser.getId().equals(chatRoom.getSeller().getId()));

        if (!isParticipant) {
            log.warn("사용자({})가 채팅방({})에 접근할 권한이 없음.", currentUsername, roomId);
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/access-denied";
        }

        User chatPartnerUser;
        if (currentUser.getId().equals(chatRoom.getBuyer().getId())) {
            chatPartnerUser = chatRoom.getSeller();
        } else {
            chatPartnerUser = chatRoom.getBuyer();
        }
        String chatPartnerNickname = chatPartnerUser.getNickname();
        Long chatPartnerId = chatPartnerUser.getId();

        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUserName", currentUser.getNickname());
        model.addAttribute("chatPartnerId", chatPartnerId);
        model.addAttribute("chatPartnerNickname", chatPartnerNickname);
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

        String currentUserProfileImageUrl = currentUser.getProfileImageUrl();
        if (currentUserProfileImageUrl == null || currentUserProfileImageUrl.isEmpty()) {
            currentUserProfileImageUrl = "/images/default-profile.png";
        }
        model.addAttribute("currentUserProfileImage", currentUserProfileImageUrl);

        String chatPartnerProfileImageUrl = chatPartnerUser.getProfileImageUrl();
        if (chatPartnerProfileImageUrl == null || chatPartnerProfileImageUrl.isEmpty()) {
            chatPartnerProfileImageUrl = "/images/default-profile.png";
        }
        model.addAttribute("chatPartnerProfileImage", chatPartnerProfileImageUrl);

        try {
            List<ChatMessage> chatHistoryEntities = chatService.getChatMessagesByRoomId(roomId);
            List<ChatMessageDTO> chatHistoryDtos = chatHistoryEntities.stream().map(entity -> {
                ChatMessageDTO dto = new ChatMessageDTO();
                dto.setRoomId(entity.getRoomId());
                dto.setSenderId(entity.getSenderId());

                User senderOfPastMessage = userService.getUserById(entity.getSenderId());
                if (senderOfPastMessage != null) {
                    dto.setSenderName(senderOfPastMessage.getNickname());
                } else {
                    dto.setSenderName("알 수 없는 사용자");
                    log.warn("경고: 과거 메시지의 발신자 ID {}를 찾을 수 없음.", entity.getSenderId());
                }

                dto.setMessage(entity.getMessage());
                dto.setImgUrl(entity.getImgUrl());

                try {
                    dto.setMessageType(ChatMessageDTO.MessageType.valueOf(entity.getMessageType()));
                } catch (IllegalArgumentException e) {
                    log.warn("경고: 알 수 없는 messageType '{}'가 DB에서 감지되었습니다. TALK로 기본 설정합니다. 메시지 ID: {}", entity.getMessageType(), entity.getMessageId());
                    dto.setMessageType(ChatMessageDTO.MessageType.TALK);
                }

                dto.setSendTime(entity.getSendTime());
                return dto;
            }).toList();

            model.addAttribute("chatHistory", chatHistoryDtos);
            log.info("채팅방 {}의 기존 메시지 {}개 로드 완료.", roomId, chatHistoryDtos.size());
        } catch (Exception e) {
            log.error("기존 채팅 메시지 로드 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("chatHistory", new ArrayList<>());
        }

        return "chat";
    }

    @GetMapping("/reportForm")
    public String showReportForm(Model model, Principal principal) {
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            if (currentUser != null) {
                model.addAttribute("reporterId", currentUser.getId());
                model.addAttribute("reporterNickname", currentUser.getNickname());
            } else {
                log.warn("신고 폼 접근 시 현재 사용자({})를 DB에서 찾을 수 없음.", currentUsername);
            }
        }
        return "report";
    }
}