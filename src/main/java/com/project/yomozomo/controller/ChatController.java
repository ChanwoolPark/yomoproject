// ChatController.java (수정된 uploadFile 메서드)
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
import java.util.Optional;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/uploadFile")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("roomId") Long roomId,
            @RequestParam("senderId") Long senderId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "업로드할 파일이 없습니다."));
        }

        try {
            // ⭐⭐⭐ 변경된 부분 시작 ⭐⭐⭐
            // 현재 이미지들이 실제로 저장되고 있는 경로로 설정합니다.
            // C:\Users\soldesk\IdeaProjects\yomoproject\\uploaded-files\image-chatimage
            String baseUploadDir = "C:" + File.separator + "Users" + File.separator + "soldesk" +
                    File.separator + "IdeaProjects" + File.separator + "yomoproject" +
                    File.separator + "uploaded-files"; // 'uploaded-files'까지의 기본 경로

            String specificUploadPathStr = Paths.get(baseUploadDir, "image-chatimage").toString(); // 'image-chatimage' 하위 폴더

            File uploadPath = new File(specificUploadPathStr);

            // 디렉토리 생성 확인 (없으면 생성)
            if (!uploadPath.exists()) {
                Files.createDirectories(uploadPath.toPath());
                log.info("업로드 디렉토리 생성 완료: {}", uploadPath.getAbsolutePath());
            }

            String originalFileName = file.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
            File dest = new File(uploadPath, storedFileName);

            log.info("DEBUG: final destination path for transfer = {}", dest.getAbsolutePath());

            // 파일 복사 (저장)
            Files.copy(file.getInputStream(), dest.toPath());

            // ⭐ 클라이언트에서 접근할 수 있는 파일의 웹 URL 생성 ⭐
            // WebConfig에서 '/uploaded-chat-images/**' 로 매핑할 것이므로, 이에 맞춰 URL 생성
            String fileUrl = "/uploaded-chat-images/" + storedFileName;
            // ⭐⭐⭐ 변경된 부분 끝 ⭐⭐⭐

            log.info("파일 업로드 성공: originalFileName={}, storedFileName={}, roomId={}, senderId={}, fileUrl={}",
                    originalFileName, storedFileName, roomId, senderId, fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("imgUrl", fileUrl);
            response.put("messageType", "IMAGE");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("파일 업로드 중 IO 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 저장 중 오류가 발생했습니다. 저장 경로를 확인하거나 권한을 부여하십시오."));
        } catch (Exception e) {
            log.error("파일 업로드 중 예기치 않은 오류 발생: {}. 상세 스택 트레이스:", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드 처리 중 오류가 발생했습니다."));
        }
    }

    // ====================================================================================
    // ⭐⭐ 웹소켓 메시지 처리 로직 ⭐⭐
    // 이 두 메서드의 주석을 해제해야 합니다.
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

        log.info("클라이언트로부터 메시지 수신 - 룸ID(URL): {}, DTO RoomID: {}, 발신자(DTO ID): {}, 메시지(DTO): {}, 타입(DTO): {}, 전송 시간(DTO): {}, 이미지 URL(DTO): {}",
                chatRoomId, chatMessageDto.getRoomId(), chatMessageDto.getSenderId(),
                chatMessageDto.getMessage(), chatMessageDto.getMessageType(), chatMessageDto.getSendTime(), chatMessageDto.getImgUrl());


        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            log.warn("경고: DTO의 chatRoomId({})와 URL의 chatRoomId({})가 일치하지 않습니다. URL의 ID를 사용합니다.",
                    chatMessageDto.getRoomId(), chatRoomId);
            chatMessageDto.setRoomId(chatRoomId);
        }

        try {
            User senderUser = userService.getUserById(chatMessageDto.getSenderId());
            String senderNickname;

            if (senderUser != null) {
                senderNickname = senderUser.getNickname();
                chatMessageDto.setSenderName(senderNickname);
            } else {
                senderNickname = "알 수 없는 사용자";
                chatMessageDto.setSenderName(senderNickname);
                log.error("오류: 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다. 메시지 저장 및 브로드캐스트를 건너뜜.", chatMessageDto.getSenderId());
                return;
            }

            // ChatService의 saveChatMessage 호출 시 DTO의 데이터 사용
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    chatMessageDto.getImgUrl(), // imgUrl이 null일 수 있으므로 그대로 전달
                    chatMessageDto.getMessageType() != null ? chatMessageDto.getMessageType().name() : ChatMessageDTO.MessageType.TALK.name()
            );
            log.info("메시지 DB 저장 성공 (ChatService 호출) - RoomID: {}", chatMessageDto.getRoomId());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            return;
        }

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

        User senderUser = userService.getUserById(chatMessageDto.getSenderId());
        String senderNickname;

        if (senderUser != null) {
            senderNickname = senderUser.getNickname();
        } else {
            senderNickname = "알 수 없는 사용자";
            log.error("오류: 입장 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다.", chatMessageDto.getSenderId());
        }
        chatMessageDto.setSenderName(senderNickname);

        String joinMessage = chatMessageDto.getMessage();
        if (joinMessage == null || joinMessage.trim().isEmpty()) {
            joinMessage = senderNickname + "님이 입장하셨습니다.";
        } else {
            joinMessage = senderNickname + joinMessage;
        }
        chatMessageDto.setMessage(joinMessage);

        chatMessageDto.setMessageType(ChatMessageDTO.MessageType.JOIN);
        chatMessageDto.setSendTime(LocalDateTime.now());

        try {
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    "", // 입장 메시지에는 imgUrl이 없으므로 빈 문자열 전달
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
    // ====================================================================================

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