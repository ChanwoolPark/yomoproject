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

import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.dto.ChatMessageDTO;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // 이 Optional 임포트는 이제 더 이상 사용되지 않을 수도 있습니다. (getChatRoomById 등에서 Optional을 사용하는 경우 제외)

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserService userService; // UserService는 User를 직접 반환하는 것으로 가정
    private final RentalService rentalService;
    private final SimpMessagingTemplate messagingTemplate;

//    // ====================================================================================
//    // ⭐⭐ 웹소켓 메시지 처리 로직 ⭐⭐
//    // ====================================================================================
//
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

        log.info("클라이언트로부터 메시지 수신 - 룸ID(URL): {}, DTO RoomID: {}, 발신자(DTO ID): {}, 메시지(DTO): {}, 타입(DTO): {}, 전송 시간(DTO): {}",
                chatRoomId, chatMessageDto.getRoomId(), chatMessageDto.getSenderId(),
                chatMessageDto.getMessage(), chatMessageDto.getMessageType(), chatMessageDto.getSendTime());

        // DTO의 roomId가 경로 변수의 chatRoomId와 일치하는지 확인 (클라이언트 측 오류 방지)
        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            log.warn("경고: DTO의 chatRoomId({})와 URL의 chatRoomId({})가 일치하지 않습니다. URL의 ID를 사용합니다.",
                    chatMessageDto.getRoomId(), chatRoomId);
            chatMessageDto.setRoomId(chatRoomId); // URL의 ID를 기준으로 설정
        }

        try {
            // ⭐ 1. ChatMessageDTO에 senderName 필드 설정 (userService.getUserById()가 User를 직접 반환하는 경우) ⭐
            // userService.getUserById()가 Optional<User>가 아닌 User를 직접 반환한다고 가정
            User senderUser = userService.getUserById(chatMessageDto.getSenderId());
            String senderNickname;

            if (senderUser != null) { // User 객체가 null이 아닌지 확인
                senderNickname = senderUser.getNickname();
                chatMessageDto.setSenderName(senderNickname); // DTO에 닉네임 설정
            } else {
                senderNickname = "알 수 없는 사용자";
                chatMessageDto.setSenderName(senderNickname); // DTO에 닉네임 설정
                log.error("오류: 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다. 메시지 저장 및 브로드캐스트를 건너뜜.", chatMessageDto.getSenderId());
                return; // 사용자를 찾지 못하면 처리 중단
            }

            // ⭐ 2. ChatService의 saveChatMessage 호출 시 DTO의 데이터 사용 ⭐
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(), // Long 타입의 senderId 그대로 전달
                    chatMessageDto.getMessage(),
                    chatMessageDto.getImgUrl() != null ? chatMessageDto.getImgUrl() : "", // imgUrl이 null이면 빈 문자열
                    chatMessageDto.getMessageType() != null ? chatMessageDto.getMessageType().name() : ChatMessageDTO.MessageType.TALK.name() // Enum을 String으로 변환, null 방지
            );
            log.info("메시지 DB 저장 성공 (ChatService 호출) - RoomID: {}", chatMessageDto.getRoomId());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            return;
        }

        // ⭐ 3. 클라이언트에게 브로드캐스트할 DTO 준비 ⭐
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        log.info("메시지 [{}]를 [{}] 경로로 브로드캐스트 완료. 발신자: {}, RoomID: {}",
                chatMessageDto.getMessage(), destination, chatMessageDto.getSenderName(), chatRoomId);
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

        log.info("사용자 입장 요청 수신 - 룸ID(URL): {}, DTO RoomID: {}, 발신자 ID: {}",
                chatRoomId, chatMessageDto.getRoomId(), chatMessageDto.getSenderId());

        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            log.warn("경고: DTO의 chatRoomId({})와 URL의 chatRoomId({})가 일치하지 않습니다. URL의 ID를 사용합니다.",
                    chatMessageDto.getRoomId(), chatRoomId);
            chatMessageDto.setRoomId(chatRoomId);
        }

        // 입장 메시지도 발신자의 닉네임을 포함하여 클라이언트에게 전송
        User senderUser = userService.getUserById(chatMessageDto.getSenderId()); // User를 직접 반환한다고 가정
        String senderNickname;

        if (senderUser != null) {
            senderNickname = senderUser.getNickname();
        } else {
            senderNickname = "알 수 없는 사용자";
            log.error("오류: 입장 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다.", chatMessageDto.getSenderId());
        }
        chatMessageDto.setSenderName(senderNickname); // DTO에 발신자 닉네임 설정

        String joinMessage = chatMessageDto.getMessage();
        if (joinMessage == null || joinMessage.trim().isEmpty()) {
            joinMessage = senderNickname + "님이 입장하셨습니다.";
        } else {
            joinMessage = senderNickname + joinMessage;
        }
        chatMessageDto.setMessage(joinMessage);

        chatMessageDto.setMessageType(ChatMessageDTO.MessageType.JOIN);
        chatMessageDto.setSendTime(LocalDateTime.now()); // 서버 시간으로 설정

        try {
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    "",
                    chatMessageDto.getMessageType().name()
            );
            log.info("입장 메시지 DB 저장 성공 - RoomID: {}, Sender: {}", chatRoomId, senderNickname);
        } catch (Exception e) {
            log.error("입장 메시지 DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }

        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        log.info("사용자 입장 메시지 브로드캐스트 완료 - User: {}, Room: {}", senderNickname, chatRoomId);
    }

    // ====================================================================================
    // ⭐⭐ 웹 페이지 (GetMapping) 로직 ⭐⭐
     //====================================================================================

    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅을 시작하려면 로그인이 필요합니다.");
            return "redirect:/login";
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

        // ⭐⭐⭐ 과거 채팅 메시지 로딩 로직 개선 ⭐⭐⭐
        try {
            List<ChatMessage> chatHistoryEntities = chatService.getChatMessagesByRoomId(roomId);
            List<ChatMessageDTO> chatHistoryDtos = chatHistoryEntities.stream().map(entity -> {
                ChatMessageDTO dto = new ChatMessageDTO();
                dto.setRoomId(entity.getRoomId());
                dto.setSenderId(entity.getSenderId());

                // ⭐ userService.getUserById()가 User를 직접 반환한다고 가정 ⭐
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