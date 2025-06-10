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
    @MessageMapping("/pub/chat.sendMessage/{chatRoomId}")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDto,
                            @DestinationVariable Long chatRoomId) {

        // DTO 필드명에 맞게 변경 (getSender() -> getSenderId(), getContent() -> getMessage(), getType() -> getMessageType())
        System.out.println("서버: 클라이언트로부터 메시지 수신 - 룸ID(URL): " + chatRoomId +
                ", 발신자(DTO): " + chatMessageDto.getSenderId() + // ⭐ getSenderId() ⭐
                ", 메시지(DTO): " + chatMessageDto.getMessage() +  // ⭐ getMessage() ⭐
                ", 타입(DTO): " + chatMessageDto.getMessageType());// ⭐ getMessageType() ⭐

        try {
            // chatMessageDto.getSenderId()가 이미 Long 타입이므로, Long.valueOf()가 필요 없습니다.
            // String messageType = chatMessageDto.getMessageType(); // enum 타입이므로 String으로 변환 필요
            // ChatService의 saveChatMessage는 String을 받으므로, enum을 String으로 변환해서 전달
            String messageTypeString = chatMessageDto.getMessageType().name(); // Enum을 String으로 변환

            chatService.saveChatMessage(
                    chatRoomId,
                    chatMessageDto.getSenderId(),      // ⭐ 이미 Long이므로 바로 사용 ⭐
                    chatMessageDto.getMessage(),       // ⭐ DTO에서 바로 가져옴 ⭐
                    "", // imgUrl은 ChatMessageDTO에서 직접 가져오도록 하거나, 별도 처리 필요
                    messageTypeString                  // ⭐ Enum을 String으로 변환하여 전달 ⭐
            );
            System.out.println("서버: 메시지 DB 저장 성공 (ChatService 호출)");

        } catch (Exception e) { // NumberFormatException이 아니라 일반 Exception으로 변경
            System.err.println("서버: 메시지 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // TODO: 저장 실패 시 클라이언트에게 오류 알림 등 추가 처리
        }

        // 메시지를 채팅방의 모든 구독자에게 브로드캐스트 (클라이언트에게는 DTO 형태로 전달)
        String destination = "/sub/chat/room/" + chatRoomId;
        // 브로드캐스트하는 DTO도 클라이언트가 기대하는 필드명을 가진 DTO여야 합니다.
        // 현재 chatMessageDto는 이미 올바른 형태이므로 그대로 보냅니다.
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        System.out.println("서버: 메시지 [" + chatMessageDto.getMessage() + "]를 [" + destination + "] 경로로 브로드캐스트 완료.");
    }

    /**
     * 클라이언트가 채팅방에 입장했을 때 처리하는 메서드.
     *
     * @param chatMessageDto 입장 메시지 (ChatMessageDTO)
     * @param chatRoomId 입장한 채팅방 ID
     */
    @MessageMapping("/pub/chat.addUser/{chatRoomId}")
    public void addUser(@Payload ChatMessageDTO chatMessageDto,
                        @DestinationVariable Long chatRoomId) {

        // chatMessageDto.getSenderId()는 Long 타입이므로, 직접 String으로 변환하거나
        // userService.getUserById(chatMessageDto.getSenderId()).getNickname()을 통해 닉네임을 가져와야 합니다.
        // 여기서는 간단하게 ID를 String으로 변환하여 로그에 사용합니다.
        String senderDisplayName = String.valueOf(chatMessageDto.getSenderId());

        System.out.println("서버: 사용자 입장 - 룸ID: " + chatRoomId +
                ", 발신자 ID: " + chatMessageDto.getSenderId() + // ⭐ senderId 사용 ⭐
                ", 닉네임 (추정): " + senderDisplayName); // 닉네임을 가져오려면 User 서비스 필요

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
                dto.setRoomId(entity.getRoomId());

                // ⭐ dto.setSenderId()에는 Long 타입의 senderId를 넣어야 합니다. ⭐
                dto.setSenderId(entity.getSenderId());

                // ⭐ 닉네임은 별도의 필드에 넣어주거나, 클라이언트에서 senderId로 조회해야 합니다. ⭐
                // 여기서는 임시로 닉네임 대신 ID를 String으로 변환해서 사용합니다.
                // (실제 닉네임이 필요하면 User 객체를 통해 가져와야 함)
                try {
                    User user = userService.getUserById(entity.getSenderId());
                    // 만약 ChatMessageDTO에 senderNickname이라는 필드가 있다면
                    // dto.setSenderNickname(user.getNickname());
                } catch (IllegalArgumentException e) {
                    System.err.println("경고: 메시지 ID " + entity.getMessageId() + "의 발신자 (" + entity.getSenderId() + ")를 찾을 수 없습니다. 기본값으로 설정.");
                    // dto.setSenderNickname("알 수 없는 사용자");
                }

                dto.setMessage(entity.getMessage()); // ⭐ setContent()가 아니라 setMessage() ⭐
                // entity.getMessageType()은 String이므로, DTO의 MessageType enum으로 변환하거나 String으로 직접 설정
                dto.setMessageType(ChatMessageDTO.MessageType.valueOf(entity.getMessageType())); // ⭐ enum 변환 ⭐

                dto.setSendTime(entity.getSendTime().toString()); // ⭐ LocalDateTime -> String 변환 ⭐

                // (옵션) imgUrl 필드도 DTO에 있다면 설정
                // dto.setImgUrl(entity.getImgUrl());

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