package com.project.yomozomo.controller.chat;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.dto.ChatMessageDTO;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.RentalService;
import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;
    private final SimpMessagingTemplate messagingTemplate;

    // ========== [1] 파일 업로드 (이미지 전송) ==========
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
            // ⭐⭐⭐ 이 부분을 새 경로의 '프로젝트 루트'로 변경 ⭐⭐⭐
            // Windows 경로의 역슬래시를 Java 문자열에서는 두 개로 표현해야 합니다.
            // 또는 Path.of를 사용하여 운영체제에 독립적인 경로를 만들 수도 있습니다.
            String baseUploadDir = "C:\\Users\\soldesk\\IdeaProjects\\yomoproject"; // <-- 이 부분을 변경했습니다.

            // Paths.get을 사용하면 운영체제에 맞게 경로를 결합해줍니다.
            // "uploaded-files", "image-chatimage"는 하위 디렉토리입니다.
            String specificUploadPathStr = Paths.get(baseUploadDir, "uploaded-files", "image-chatimage").toString();

            File uploadPath = new File(specificUploadPathStr);

            if (!uploadPath.exists()) {
                Files.createDirectories(uploadPath.toPath()); // 디렉토리가 없으면 생성
                log.info("DEBUG: 생성된 업로드 디렉토리: " + uploadPath.getAbsolutePath()); // 로그 추가
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                fileExtension = originalFileName.substring(dotIndex);
            }
            String storedFileName = UUID.randomUUID().toString() + fileExtension;
            File dest = new File(uploadPath, storedFileName);

            Files.copy(file.getInputStream(), dest.toPath()); // ⭐ 여기서 IOException 발생 가능성 높음 ⭐

            String fileUrl = "/uploaded-chat-images/" + storedFileName; // WebConfig와 매핑되는 URL
            Map<String, String> response = new HashMap<>();
            response.put("imgUrl", fileUrl);
            response.put("messageType", "IMAGE");
            log.info("DEBUG: 파일 업로드 성공: " + dest.getAbsolutePath()); // 성공 로그 추가
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: " + e.getMessage(), e); // 에러 로그에 스택 트레이스 추가
            return ResponseEntity.status(500)
                    .body(Map.of("error", "파일 저장 중 오류가 발생했습니다. 서버 로그를 확인하세요."));
        } catch (Exception e) { // 기타 예상치 못한 예외 처리
            log.error("알 수 없는 파일 업로드 오류 발생: " + e.getMessage(), e); // 에러 로그에 스택 트레이스 추가
            return ResponseEntity.status(500)
                    .body(Map.of("error", "알 수 없는 파일 업로드 오류가 발생했습니다. 서버 로그를 확인하세요."));
        }
    }

    // ========== [2] 웹소켓 채팅 메시지 송수신 ==========
    @MessageMapping("/pub/chat.sendMessage/{chatRoomId}")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDto, @DestinationVariable Long chatRoomId) {
        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            chatMessageDto.setRoomId(chatRoomId);
        }
        try {
            User senderUser = userService.getUserById(chatMessageDto.getSenderId());
            chatMessageDto.setSenderName(senderUser != null ? senderUser.getNickname() : "알 수 없는 사용자");
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    chatMessageDto.getImgUrl(),
                    chatMessageDto.getMessageType() != null ? chatMessageDto.getMessageType().name() : ChatMessageDTO.MessageType.TALK.name()
            );
        } catch (Exception e) { return; }
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
    }

    @MessageMapping("/pub/chat.addUser/{chatRoomId}")
    public void addUser(@Payload ChatMessageDTO chatMessageDto, @DestinationVariable Long chatRoomId) {
        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            chatMessageDto.setRoomId(chatRoomId);
        }
        User senderUser = userService.getUserById(chatMessageDto.getSenderId());
        String senderNickname = senderUser != null ? senderUser.getNickname() : "알 수 없는 사용자";
        chatMessageDto.setSenderName(senderNickname);
        String joinMessage = chatMessageDto.getMessage();
        if (joinMessage == null || joinMessage.trim().isEmpty()) {
            joinMessage = senderNickname + "님이 입장하셨습니다.";
        }
        chatMessageDto.setMessage(joinMessage);
        chatMessageDto.setMessageType(ChatMessageDTO.MessageType.JOIN);
        chatMessageDto.setSendTime(LocalDateTime.now());
        try {
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    "",
                    chatMessageDto.getMessageType().name()
            );
        } catch (Exception e) {}
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
    }

    // ========== [3] 채팅방 진입(렌탈상품 채팅 시작) ==========
    @GetMapping("/start/{rentalId}")
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
        return "redirect:/chat/" + chatRoomId;
    }

    // ========== [4] 채팅방 웹화면 ==========
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
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage";
        }
        Optional<ChatRoom> optionalChatRoom = chatService.getChatRoomById(roomId);
        if (optionalChatRoom.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅방을 찾을 수 없습니다. ID: " + roomId);
            return "redirect:/errorPage";
        }
        ChatRoom chatRoom = optionalChatRoom.get();

        boolean isParticipant = (currentUser.getId().equals(chatRoom.getBuyer().getId()) ||
                currentUser.getId().equals(chatRoom.getSeller().getId()));
        if (!isParticipant) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/access-denied";
        }

        User chatPartnerUser = currentUser.getId().equals(chatRoom.getBuyer().getId()) ? chatRoom.getSeller() : chatRoom.getBuyer();
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

        // 프로필 이미지 URL(기본값 처리)
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

        // 기존 채팅 메시지
        try {
            List<ChatMessage> chatHistoryEntities = chatService.getChatMessagesByRoomId(roomId);

            List<ChatMessageDTO> chatHistoryDtos = chatHistoryEntities.stream().map(entity -> {
                ChatMessageDTO dto = new ChatMessageDTO();
                dto.setRoomId(entity.getRoomId());
                dto.setSenderId(entity.getSenderId());
                User senderOfPastMessage = userService.getUserById(entity.getSenderId());
                dto.setSenderName(senderOfPastMessage != null ? senderOfPastMessage.getNickname() : "알 수 없는 사용자");
                dto.setMessage(entity.getMessage());
                dto.setImgUrl(entity.getImgUrl());
                try {
                    dto.setMessageType(ChatMessageDTO.MessageType.valueOf(entity.getMessageType()));
                } catch (Exception e) {
                    dto.setMessageType(ChatMessageDTO.MessageType.TALK);
                }
                dto.setSendTime(entity.getSendTime());
                return dto;
            }).toList();
            model.addAttribute("chatHistory", chatHistoryDtos);
        } catch (Exception e) {
            model.addAttribute("chatHistory", new ArrayList<>());
        }

        // isSeller 등 기타 Thymeleaf 변수
        model.addAttribute("isSeller", currentUser.getId().equals(chatRoom.getSeller().getId()));
        model.addAttribute("rental", chatRoom.getRental());

        return "chat";
    }

    // ========== [5] 신고 폼 ==========
    @GetMapping("/reportForm")
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