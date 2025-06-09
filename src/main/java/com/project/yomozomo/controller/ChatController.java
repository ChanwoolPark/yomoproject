package com.project.yomozomo.controller;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.yomozomo.entity.ChatMessage; // ChatMessage 엔티티 사용
import com.project.yomozomo.dto.ChatMessageDTO; // ChatMessageDTO DTO

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;
    private final SimpMessagingTemplate messagingTemplate;

    // ====================================================================================
    // ⭐⭐ 웹소켓 메시지 처리 로직 ⭐⭐
    // ====================================================================================

    /**
     * 클라이언트에서 보낸 STOMP 메시지를 받아서 처리하는 메서드.
     * 클라이언트가 `/pub/chat.sendMessage/{chatRoomId}` 경로로 메시지를 보낼 때 호출됩니다.
     *
     * @param chatMessageDto 클라이언트로부터 받은 ChatMessageDTO 객체
     * @param chatRoomId 메시지가 전송된 채팅방 ID (경로 변수에서 추출)
     */
    @MessageMapping("/chat.sendMessage/{chatRoomId}")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDto,
                            @DestinationVariable Long chatRoomId) {

        System.out.println("서버: 클라이언트로부터 메시지 수신 - 룸ID(URL): " + chatRoomId +
                ", 발신자(DTO): " + chatMessageDto.getSender() +
                ", 메시지(DTO): " + chatMessageDto.getContent() +
                ", 타입(DTO): " + chatMessageDto.getType());

        try {
            Long senderId = Long.valueOf(chatMessageDto.getSender());
            String messageContent = chatMessageDto.getContent();
            String messageType = chatMessageDto.getType();
            String imgUrl = ""; // ChatMessageDTO에 imgUrl이 있다면 chatMessageDto.getImgUrl() 사용

            // ChatService의 saveChatMessage는 Long, Long, String, String, String을 받으며 Chat 엔티티를 반환
            // 현재 코드 흐름상 ChatService.saveChatMessage는 Chat 엔티티를 저장하는 것으로 보임
            // 따라서 ChatService.saveChatMessage의 매개변수에 맞춰 호출
            chatService.saveChatMessage(chatRoomId, senderId, messageContent, imgUrl, messageType);
            System.out.println("서버: 메시지 DB 저장 성공 (ChatService 호출)");

        } catch (NumberFormatException e) {
            System.err.println("서버: 발신자 ID 변환 중 오류 발생 (유효한 Long 형태의 senderId가 아닙니다): " + chatMessageDto.getSender());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("서버: 메시지 DB 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // TODO: 저장 실패 시 클라이언트에게 오류 알림 등 추가 처리
        }

        // 메시지를 채팅방의 모든 구독자에게 브로드캐스트 (클라이언트에게는 DTO 형태로 전달)
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        System.out.println("서버: 메시지 [" + chatMessageDto.getContent() + "]를 [" + destination + "] 경로로 브로드캐스트 완료.");
    }

    /**
     * 클라이언트가 채팅방에 입장했을 때 처리하는 메서드.
     *
     * @param chatMessageDto 입장 메시지 (ChatMessageDTO)
     * @param chatRoomId 입장한 채팅방 ID
     */
    @MessageMapping("/chat.addUser/{chatRoomId}")
    public void addUser(@Payload ChatMessageDTO chatMessageDto,
                        @DestinationVariable Long chatRoomId) {

        String senderDisplayName = chatMessageDto.getSender();

        System.out.println("서버: 사용자 입장 - 룸ID: " + chatRoomId +
                ", 발신자: " + chatMessageDto.getSender() +
                ", 닉네임: " + senderDisplayName);

        // 클라이언트에게 브로드캐스트
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        System.out.println("서버: 사용자 입장 메시지 브로드캐스트 완료.");
    }

    // ====================================================================================
    // ⭐⭐ 웹 페이지 (GetMapping) 로직 ⭐⭐
    // ====================================================================================

    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅을 시작하려면 로그인이 필요합니다.");
            return "redirect:/login";
        }

        Rental rental = rentalService.getRentalById(rentalId);
        User buyer = userService.getUserByUsername(principal.getName());
        User sellerUser = rental.getProduct().getSeller();

        if (sellerUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "렌탈 상품의 판매자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage";
        }

        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, sellerUser, rental.getRentalId());
        return "redirect:/chat/" + chatRoomId;
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

        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        boolean isBuyer = currentUser.getId().equals(chatRoom.getBuyer().getId());
        boolean isSeller = currentUser.getId().equals(chatRoom.getSeller().getId());

        if (!isBuyer && !isSeller) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/access-denied";
        }

        String chatPartnerNickname;
        if (isBuyer) {
            chatPartnerNickname = chatRoom.getSeller().getNickname();
        } else {
            chatPartnerNickname = chatRoom.getBuyer().getNickname();
        }

        model.addAttribute("currentUserId", currentUser.getId());
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
            model.addAttribute("productTitle", "상품 정보 없음");
        }

        String userProfileImageUrl = currentUser.getProfileImageUrl();
        if (userProfileImageUrl == null || userProfileImageUrl.isEmpty()) {
            userProfileImageUrl = "/images/default-profile.png";
        }
        model.addAttribute("currentUserProfileImage", userProfileImageUrl);

        // ⭐⭐⭐ 과거 채팅 메시지 로딩 로직 ⭐⭐⭐
        try {
            List<ChatMessage> chatHistoryEntities = chatService.getChatMessagesByRoomId(roomId);

            List<ChatMessageDTO> chatHistoryDtos = chatHistoryEntities.stream().map(entity -> {
                ChatMessageDTO dto = new ChatMessageDTO();
                dto.setDbChatRoomId(entity.getRoomId()); // ChatMessage 엔티티에 roomId 필드가 있다고 가정

                // ⭐⭐ 이 부분 수정: ifPresent 대신 User 객체를 직접 사용 ⭐⭐
                try {
                    User user = userService.getUserById(entity.getSenderId());
                    dto.setSender(user.getNickname());
                } catch (IllegalArgumentException e) {
                    System.err.println("경고: 메시지 ID " + entity.getMessageId() + "의 발신자 (" + entity.getSenderId() + ")를 찾을 수 없습니다. 기본값으로 설정.");
                    dto.setSender("알 수 없는 사용자");
                }


                dto.setContent(entity.getMessage());
                dto.setType(entity.getMessageType());
                dto.setTime(entity.getSendTime().toString()); // LocalDateTime -> String

                // (옵션) imgUrl 필드도 DTO에 있다면 설정
                // dto.setImgUrl(entity.getImgUrl()); // ChatMessage 엔티티에 imgUrl 필드가 있다면

                return dto;
            }).toList();
            model.addAttribute("chatHistory", chatHistoryDtos);
            System.out.println("DEBUG: 채팅방 " + roomId + "의 기존 메시지 " + chatHistoryDtos.size() + "개 로드 완료.");
        } catch (Exception e) {
            System.err.println("DEBUG: 기존 채팅 메시지 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("chatHistory", new ArrayList<>());
        }

        return "chat";
    }


    @GetMapping("/reportForm")
    public String showReportForm(Model model, Principal principal) {
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            model.addAttribute("reporterId", currentUser.getId());
            model.addAttribute("reporterNickname", currentUser.getNickname());
        }
        return "report";
    }
}