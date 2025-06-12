package com.project.yomozomo.controller.chat; // 패키지명을 최상위로 조정하거나 필요에 따라 변경

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.dto.ChatMessageDTO;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Optional은 사용되지 않으므로 제거 가능
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/chat") // 통합된 컨트롤러의 기본 경로는 "/chat"으로 설정합니다.
@RequiredArgsConstructor // final 필드들을 위한 생성자 자동 생성
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final RentalService rentalService;
    private final SimpMessagingTemplate messagingTemplate; // 웹소켓 메시지 전송용

    // ====================================================================================
    // ⭐⭐ 파일 업로드 로직 (HTTP POST) ⭐⭐
    // ====================================================================================

    /**
     * 클라이언트로부터 이미지 파일을 업로드 받아 서버에 저장하고, 저장된 이미지의 URL을 반환합니다.
     * 이 URL은 채팅 메시지로 클라이언트에 전송됩니다.
     *
     * @param file 업로드할 MultipartFile 객체 (이미지 파일)
     * @param roomId 파일이 업로드될 채팅방의 ID
     * @param senderId 파일을 업로드한 사용자의 ID
     * @return 성공 시 이미지 URL과 메시지 타입을 포함하는 ResponseEntity, 실패 시 오류 메시지
     */
    @PostMapping("/uploadFile")
    @ResponseBody // HTTP 응답 본문에 직접 데이터를 직렬화하여 반환
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("roomId") Long roomId,
            @RequestParam("senderId") Long senderId) {

        if (file.isEmpty()) {
            log.warn("파일 업로드 요청: 파일이 비어 있습니다. RoomID: {}, SenderID: {}", roomId, senderId);
            return ResponseEntity.badRequest().body(Map.of("error", "업로드할 파일이 없습니다."));
        }

        try {
            // 프로젝트 기준 경로: C:/Users/soldesk/IdeaProjects/yomoproject
            // 실제 배포 환경에서는 이 경로를 외부 설정(application.properties)으로 관리하거나,
            // 상대 경로를 사용하는 것이 좋습니다.
            String baseUploadDir = "C:" + File.separator + "Users" + File.separator + "soldesk" +
                    File.separator + "IdeaProjects" + File.separator + "yomoproject" +
                    File.separator + "uploaded-files"; // 'uploaded-files'까지의 기본 경로

            String specificUploadPathStr = Paths.get(baseUploadDir, "image-chatimage").toString();
            File uploadPath = new File(specificUploadPathStr);

            // 업로드 디렉토리가 없으면 생성
            if (!uploadPath.exists()) {
                Files.createDirectories(uploadPath.toPath());
                log.info("업로드 디렉토리 생성 완료: {}", uploadPath.getAbsolutePath());
            }

            String originalFileName = file.getOriginalFilename();
            // 파일 확장자 추출
            String fileExtension = "";
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                fileExtension = originalFileName.substring(dotIndex); // .png, .jpg 등 확장자
            }
            // UUID를 사용하여 고유한 파일명 생성 (파일명 중복 방지)
            String storedFileName = UUID.randomUUID().toString() + fileExtension;
            File dest = new File(uploadPath, storedFileName);

            // 파일 복사 (실제로 파일 저장)
            Files.copy(file.getInputStream(), dest.toPath());

            // 클라이언트에서 접근할 수 있는 URL 생성
            // WebConfig에서 /uploaded-chat-images/** 로 매핑되어야 합니다.
            String fileUrl = "/uploaded-chat-images/" + storedFileName;

            log.info("파일 업로드 성공: originalFileName={}, storedFileName={}, roomId={}, senderId={}, fileUrl={}",
                    originalFileName, storedFileName, roomId, senderId, fileUrl);

            // 클라이언트에 반환할 응답 데이터
            Map<String, String> response = new HashMap<>();
            response.put("imgUrl", fileUrl);
            response.put("messageType", "IMAGE"); // 클라이언트가 IMAGE 타입 메시지로 처리하도록 지시

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("파일 업로드 중 IO 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 저장 중 오류가 발생했습니다. 서버 경로 및 권한을 확인하십시오."));
        } catch (Exception e) {
            log.error("파일 업로드 중 예기치 않은 오류 발생: {}. 상세 스택 트레이스:", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드 처리 중 예기치 않은 오류가 발생했습니다."));
        }
    }

    // ====================================================================================
    // ⭐⭐ 웹소켓 메시지 처리 로직 (@MessageMapping) ⭐⭐
    // ====================================================================================

    /**
     * 클라이언트에서 보낸 STOMP 메시지를 받아서 처리하는 메서드.
     * 클라이언트가 `/app/pub/chat.sendMessage/{chatRoomId}` 경로로 메시지를 보낼 때 호출됩니다.
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

        // DTO의 chatRoomId가 null이거나 URL의 roomId와 다를 경우, URL의 ID를 사용
        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            log.warn("경고: DTO의 chatRoomId({})와 URL의 chatRoomId({})가 일치하지 않습니다. URL의 ID를 사용합니다.",
                    chatMessageDto.getRoomId(), chatRoomId);
            chatMessageDto.setRoomId(chatRoomId);
        }

        try {
            // 발신자 정보 가져오기 (메시지 브로드캐스트 시 닉네임 필요)
            User senderUser = userService.getUserById(chatMessageDto.getSenderId());
            String senderNickname;

            if (senderUser != null) {
                senderNickname = senderUser.getNickname();
                chatMessageDto.setSenderName(senderNickname); // DTO에 닉네임 설정
            } else {
                senderNickname = "알 수 없는 사용자";
                chatMessageDto.setSenderName(senderNickname); // DTO에 닉네임 설정
                log.error("오류: 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다. 메시지 저장 및 브로드캐스트를 건너뜜.", chatMessageDto.getSenderId());
                return; // 사용자 정보 없으면 처리 중단
            }

            // DB에 저장할 messageType 결정 (DB의 CHECK 제약조건 고려)
            // ChatMessageDTO.MessageType의 ENUM 이름을 그대로 사용하거나, 필요한 경우 변환
            String dbMessageType = chatMessageDto.getMessageType() != null ?
                    chatMessageDto.getMessageType().name() : ChatMessageDTO.MessageType.TALK.name(); // 기본값 TALK

            // DB에 메시지 저장
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    chatMessageDto.getImgUrl(), // imgUrl이 null이면 ChatService에서 null로 처리됨
                    dbMessageType // DB에 저장할 타입
            );
            log.info("메시지 DB 저장 성공 (ChatService 호출) - RoomID: {}", chatMessageDto.getRoomId());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            // 클라이언트에게 오류를 알리는 별도 로직을 추가할 수 있습니다.
            return;
        }

        // DB 저장 후, 메시지를 해당 채팅방을 구독하는 모든 클라이언트에게 브로드캐스트
        // 클라이언트가 /sub/chat/room/{chatRoomId}를 구독하고 있어야 합니다.
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto); // 저장된 DTO 객체 그대로 전송
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
        chatMessageDto.setSenderName(senderNickname); // DTO에 닉네임 설정

        // 입장 메시지 내용 설정 (클라이언트가 보낸 메시지가 없으면 기본 문구 사용)
        String joinMessage = chatMessageDto.getMessage();
        if (joinMessage == null || joinMessage.trim().isEmpty()) {
            joinMessage = senderNickname + "님이 입장하셨습니다.";
        } else {
            // 클라이언트가 미리 메시지를 구성해서 보낸 경우 (예: "님이 입장했습니다.")
            // 이 경우 senderNickname을 앞에 붙일지 말지 결정해야 함.
            // joinMessage = senderNickname + joinMessage; // 필요하면 주석 해제
        }
        chatMessageDto.setMessage(joinMessage); // 최종 입장 메시지 설정

        // 입장 메시지는 SYSTEM 타입으로 DB에 저장되고 브로드캐스트
        chatMessageDto.setMessageType(ChatMessageDTO.MessageType.SYSTEM); // 브로드캐스트용 DTO 타입 설정
        chatMessageDto.setSendTime(LocalDateTime.now()); // 서버 시간으로 설정

        try {
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    null, // 입장 메시지는 imgUrl이 없으므로 null 전달 (혹은 빈 문자열 ""로 변경 가능)
                    chatMessageDto.getMessageType().name() // DB에 저장될 타입은 'SYSTEM'으로 명시
            );
            log.info("입장 메시지 DB 저장 성공 - RoomID: {}, Sender: {}", chatRoomId, senderNickname);
        } catch (Exception e) {
            log.error("입장 메시지 DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }

        // 입장 메시지를 해당 채팅방을 구독하는 모든 클라이언트에게 브로드캐스트
        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto); // DTO 객체 그대로 전송
        log.info("사용자 입장 메시지 브로드캐스트 완료 - User: {}, Room: {}", senderNickname, chatRoomId);
    }

    // ====================================================================================
    // ⭐⭐ 웹 페이지 (GetMapping) 로직 ⭐⭐
    // ====================================================================================

    /**
     * 특정 렌탈 상품과 관련된 채팅을 시작하는 요청을 처리합니다.
     * 채팅방을 찾거나 새로 생성하고 해당 채팅방으로 리다이렉트합니다.
     *
     * @param rentalId 채팅을 시작할 렌탈 상품의 ID
     * @param principal 현재 로그인된 사용자 정보
     * @param redirectAttributes 리다이렉트 시 메시지 전달을 위한 객체
     * @return 리다이렉트할 채팅방 URL 또는 에러 페이지 URL
     */
    @GetMapping("/start/{rentalId}")
    public String startChatWithRental(@PathVariable Long rentalId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅을 시작하려면 로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        Rental rental = rentalService.getRentalById(rentalId);
        // 렌탈 상품 정보 및 판매자 정보 유효성 검사
        if (rental == null || rental.getProduct() == null || rental.getProduct().getSeller() == null) {
            log.warn("렌탈 상품 또는 판매자 정보를 찾을 수 없음 - rentalId: {}", rentalId);
            redirectAttributes.addFlashAttribute("errorMessage", "렌탈 상품 또는 판매자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage"; // 에러 페이지로 리다이렉트
        }

        User buyer = userService.getUserByUsername(principal.getName());
        User sellerUser = rental.getProduct().getSeller(); // 렌탈 상품의 판매자 정보

        // 채팅방을 찾거나 새로 생성
        // findOrCreateChatRoomForRental 메서드의 인자 순서와 타입 확인 필요
        Long chatRoomId = chatService.findOrCreateChatRoomForRental(buyer, sellerUser, rental.getRentalId());
        log.info("렌탈 상품 채팅방 시작 - rentalId: {}, chatRoomId: {}", rentalId, chatRoomId);

        // 생성되거나 찾아진 채팅방으로 리다이렉트
        return "redirect:/chat/" + chatRoomId;
    }

    /**
     * 특정 채팅방의 웹 페이지를 표시하고, 초기 데이터(사용자 정보, 과거 메시지)를 로드합니다.
     *
     * @param roomId 접근할 채팅방의 ID
     * @param principal 현재 로그인된 사용자 정보
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @param redirectAttributes 리다이렉트 시 메시지 전달을 위한 객체
     * @return 채팅방 템플릿의 이름 또는 리다이렉트 URL
     */
    @GetMapping("/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "채팅방에 접근하려면 로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        String currentUsername = principal.getName();
        User currentUser = userService.getUserByUsername(currentUsername);

        if (currentUser == null) {
            log.error("현재 로그인된 사용자({})를 DB에서 찾을 수 없음.", currentUsername);
            redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/errorPage"; // 에러 페이지로 리다이렉트
        }

        // 채팅방 정보 조회. 없으면 예외 발생 (404 처리 또는 에러 페이지로 리다이렉트)
        ChatRoom chatRoom = chatService.getChatRoomById(roomId)
                .orElseThrow(() -> {
                    log.warn("채팅방을 찾을 수 없습니다. ID: {}", roomId);
                    redirectAttributes.addFlashAttribute("errorMessage", "채팅방을 찾을 수 없습니다.");
                    // 리다이렉트가 throw new Exception() 보다 우선하므로, 예외 대신 리다이렉트를 고려.
                    // 이 경우, 예외를 발생시키기보다 String 리턴으로 리다이렉트 하는 것이 일반적입니다.
                    // throw new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId);
                    return new IllegalStateException("채팅방을 찾을 수 없습니다. ID: " + roomId); // 예외 타입을 변경
                });

        // 현재 사용자가 채팅방 참여자인지 확인 (권한 검증)
        boolean isParticipant = (currentUser.getId().equals(chatRoom.getBuyer().getId()) ||
                currentUser.getId().equals(chatRoom.getSeller().getId()));

        if (!isParticipant) {
            log.warn("사용자({})가 채팅방({})에 접근할 권한이 없음.", currentUsername, roomId);
            redirectAttributes.addFlashAttribute("errorMessage", "이 채팅방에 접근할 권한이 없습니다.");
            return "redirect:/access-denied"; // 권한 없음 페이지로 리다이렉트
        }

        // 채팅 파트너 정보 설정
        User chatPartnerUser;
        if (currentUser.getId().equals(chatRoom.getBuyer().getId())) {
            chatPartnerUser = chatRoom.getSeller(); // 현재 사용자가 구매자면, 판매자가 파트너
        } else {
            chatPartnerUser = chatRoom.getBuyer(); // 현재 사용자가 판매자면, 구매자가 파트너
        }
        String chatPartnerNickname = chatPartnerUser.getNickname();
        Long chatPartnerId = chatPartnerUser.getId();

        // Model에 채팅방 관련 정보 추가 (뷰로 전달)
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUserName", currentUser.getNickname());
        model.addAttribute("chatPartnerId", chatPartnerId);
        model.addAttribute("chatPartnerNickname", chatPartnerNickname);
        model.addAttribute("chatRoomId", roomId);

        // 렌탈 상품 정보 추가 (채팅방이 렌탈과 연관되어 있을 경우)
        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRentalId", chatRoom.getRental().getRentalId());
            if (chatRoom.getRental().getProduct() != null) {
                model.addAttribute("productTitle", chatRoom.getRental().getProduct().getTitle());
            } else {
                model.addAttribute("productTitle", "상품 정보 없음");
            }
            // 렌탈 상태 및 기타 렌탈 정보도 필요하다면 추가
             model.addAttribute("rental", chatRoom.getRental());
             System.out.println("렌탈 status: [" + chatRoom.getRental().getStatus() + "]");
        } else {
            model.addAttribute("currentRentalId", null);
            model.addAttribute("productTitle", "일반 채팅"); // 렌탈과 무관한 일반 채팅방
        }


        // 사용자 프로필 이미지 URL 추가
        String currentUserProfileImageUrl = currentUser.getProfileImageUrl();
        if (currentUserProfileImageUrl == null || currentUserProfileImageUrl.isEmpty()) {
            currentUserProfileImageUrl = "/images/default-profile.png"; // 기본 이미지 경로
        }
        model.addAttribute("currentUserProfileImage", currentUserProfileImageUrl);

        // 채팅 파트너 프로필 이미지 URL 추가
        String chatPartnerProfileImageUrl = chatPartnerUser.getProfileImageUrl();
        if (chatPartnerProfileImageUrl == null || chatPartnerProfileImageUrl.isEmpty()) {
            chatPartnerProfileImageUrl = "/images/default-profile.png"; // 기본 이미지 경로
        }
        model.addAttribute("chatPartnerProfileImage", chatPartnerProfileImageUrl);

        // 과거 채팅 메시지 로딩
        try {
            List<ChatMessage> chatHistoryEntities = chatService.getChatMessagesByRoomId(roomId);
            List<ChatMessageDTO> chatHistoryDtos = chatHistoryEntities.stream().map(entity -> {
                ChatMessageDTO dto = new ChatMessageDTO();
                dto.setRoomId(entity.getRoomId());
                dto.setSenderId(entity.getSenderId());

                // 과거 메시지의 발신자 닉네임 설정
                User senderOfPastMessage = userService.getUserById(entity.getSenderId());
                if (senderOfPastMessage != null) {
                    dto.setSenderName(senderOfPastMessage.getNickname());
                } else {
                    dto.setSenderName("알 수 없는 사용자");
                    log.warn("경고: 과거 메시지의 발신자 ID {}를 찾을 수 없음.", entity.getSenderId());
                }

                dto.setMessage(entity.getMessage());
                dto.setImgUrl(entity.getImgUrl());

                // DB에서 읽은 messageType을 DTO의 MessageType enum으로 변환
                try {
                    dto.setMessageType(ChatMessageDTO.MessageType.valueOf(entity.getMessageType()));
                } catch (IllegalArgumentException e) {
                    log.warn("경고: 알 수 없는 messageType '{}'가 DB에서 감지되었습니다. TALK로 기본 설정합니다. 메시지 ID: {}", entity.getMessageType(), entity.getMessageId());
                    dto.setMessageType(ChatMessageDTO.MessageType.TALK); // DB 타입에 없는 경우 TALK로
                }

                dto.setSendTime(entity.getSendTime());
                return dto;
            }).toList();

            model.addAttribute("chatHistory", chatHistoryDtos);
            log.info("채팅방 {}의 기존 메시지 {}개 로드 완료.", roomId, chatHistoryDtos.size());
        } catch (Exception e) {
            log.error("기존 채팅 메시지 로드 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("chatHistory", new ArrayList<>()); // 오류 발생 시 빈 리스트 반환
        }

        return "chat"; // chat.html 템플릿 반환
    }

    /**
     * 채팅 신고 폼 페이지를 표시합니다.
     *
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @param principal 현재 로그인된 사용자 정보
     * @return 신고 폼 템플릿의 이름
     */
    @GetMapping("/reportForm")
    public String showReportForm(Model model, Principal principal) {
        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            if (currentUser != null) {
                model.addAttribute("reporterId", currentUser.getId()); // 신고자 ID 전달
                model.addAttribute("reporterNickname", currentUser.getNickname()); // 신고자 닉네임 전달
            } else {
                log.warn("신고 폼 접근 시 현재 사용자({})를 DB에서 찾을 수 없음.", currentUsername);
            }
        }
        return "report"; // report.html 템플릿 반환
    }
}