// src/main/java/com/chat/controller/ChatRoomController.java
package com.project.yomozomo.controller;

import org.springframework.ui.Model; // Correct Model import
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;     // User 엔티티 경로
import com.project.yomozomo.domain.Rental; // Rental 엔티티 경로
import com.project.yomozomo.service.ChatService; // For createRoom and getAllRooms


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor; // For constructor injection

import java.security.Principal;
import java.util.List;

@RestController // RESTful API를 제공하는 컨트롤러
@RequestMapping("/api/chatrooms") // 모든 엔드포인트에 /api/chatrooms 프리픽스 적용
@RequiredArgsConstructor // Lombok: final 필드에 대한 생성자를 자동으로 생성
public class ChatRoomController {

    private final ChatService chatService; // 기존 ChatService
    private final ChatService chatRoomService; // 새로 추가된 ChatRoomService

    // @Autowired는 @RequiredArgsConstructor를 사용하면 필요 없습니다.
    // Lombok이 final 필드인 chatService와 chatRoomService를 인자로 받는 생성자를 자동으로 만들어 줍니다.
    /*
    @Autowired
    public ChatRoomController(ChatService chatService) {
        this.chatService = chatService;
    }
    // 만약 chatRoomService도 @Autowired로 주입하려면 아래와 같이 변경해야 합니다.
    @Autowired
    public ChatRoomController(ChatService chatService, ChatRoomService chatRoomService) {
        this.chatService = chatService;
        this.chatRoomService = chatRoomService;
    }
    */


    @PostMapping // /api/chatrooms 에 대한 POST 요청
    public ResponseEntity<ChatRoom> createRoom(@RequestParam String roomName) {
        ChatRoom newRoom = chatService.createChatRoom(roomName); // chatService가 createChatRoom을 가지고 있다고 가정
        return ResponseEntity.ok(newRoom);
    }

    // 기존의 /chat/{roomId} 매핑 (web page return)
    // 이 매핑은 @RequestMapping("/api/chatrooms") 밖에 있어야 할 가능성이 높습니다.
    // 만약 이 매핑이 /api/chatrooms/chat/{roomId} 로 작동하길 원한다면 이 위치에 둡니다.
    // 아니라면 이 메서드를 @Controller가 붙은 다른 클래스(예: ChatController)로 옮겨야 합니다.
    // 현재는 @RestController이므로, View를 반환하려면 별도의 @Controller 클래스가 더 적합합니다.
    // 일단은 @RequestMapping("/api/chatrooms") 안에 있는 것으로 처리합니다.
    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable Long roomId, // String -> Long으로 변경 (필수!)
                           @RequestParam(required = false) String targetUser, // @RequestParam은 필수가 아닐 수도 있으니 required=false 추가 고려
                           Principal principal, // 현재 로그인 사용자
                           Model model) { // org.springframework.ui.Model 사용

        String currentUsername = principal.getName(); // 현재 로그인된 사용자 이름 (내 이름)

        // 1. 채팅방 정보 조회
        ChatRoom chatRoom = chatService.getChatRoomById(roomId); // roomId가 Long 타입이므로, 서비스 메서드도 Long을 받도록

        // 2. 대화 상대방 사용자 이름 결정 (targetUser가 없으면 chatRoom에서 찾기)
        String resolvedTargetUsername;
        if (targetUser != null && !targetUser.isEmpty()) {
            resolvedTargetUsername = targetUser;
        } else {
            // targetUser가 명시되지 않았다면, 현재 사용자가 구매자/판매자인지 확인하여 상대방 결정
            if (currentUsername.equals(chatRoom.getBuyer().getUsername())) {
                resolvedTargetUsername = chatRoom.getSeller().getUsername();
            } else if (currentUsername.equals(chatRoom.getSeller().getUsername())) {
                resolvedTargetUsername = chatRoom.getBuyer().getUsername();
            } else {
                // 이 채팅방의 유효한 참여자가 아님
                throw new IllegalArgumentException("현재 사용자는 이 채팅방의 유효한 참여자가 아닙니다.");
            }
        }


        // 3. Model에 필요한 데이터 추가
        model.addAttribute("username", currentUsername);     // 내 사용자 이름
        model.addAttribute("targetUsername", resolvedTargetUsername); // 상대방 사용자 이름
        model.addAttribute("roomId", roomId);                // 채팅방 고유 ID (Long 타입)


        // 4. 렌탈 정보 추가 (이전 오류의 원인: currentRental 누락)
        if (chatRoom.getRental() != null) {
            model.addAttribute("currentRental", chatRoom.getRental());
        } else {
            // 렌탈 정보가 없을 경우를 대비하여 null을 명시적으로 추가
            // 이렇게 하면 chat.html에서 ${currentRental}이 null인지 체크할 수 있습니다.
            model.addAttribute("currentRental", null);
        }


        return "chat"; // "chat.html" 템플릿을 렌더링
    }
    // **1. 채팅방 시작/생성 요청을 처리하는 새로운 엔드포인트**
    // 이 엔드포인트는 /api/chatrooms/start 로 매핑됩니다.
    @PostMapping("/start") // @RequestMapping("/api/chatrooms") 있으므로, /api/chatrooms/start
    @ResponseBody // 이 메서드는 View를 반환하는 것이 아니라 JSON 데이터를 반환
    public Long startChat(@RequestParam Long rentalId, // 상품 ID 대신 Rental ID (Long)
                          @RequestParam Long sellerId, // 판매자 ID (Long)
                          Principal principal) { // 현재 로그인한 사용자 (구매자)

        // TODO: 실제 UserService와 RentalService를 통해 User와 Rental 엔티티를 조회하는 로직을 구현해야 합니다.
        // 예시: 현재는 임시 User 객체를 만듭니다. 실제 서비스에서는 User와 Rental을 DB에서 찾아야 합니다.
        // 이 부분은 UserService와 RentalService를 실제 주입받아 사용해야 합니다.
        User buyer = new User(); // 실제 로그인 유저의 User 엔티티로 변경 필요
        // buyer.setId(로그인 유저의 ID);
        // buyer.setUsername(principal.getName());

        User seller = new User();
        seller.setId(sellerId);

        Rental rental = new Rental();
        rental.setRentalId(rentalId);

        Long roomId = chatService.findOrCreateChatRoom(buyer, seller, rental);
        return roomId; // 생성되거나 찾아진 roomId(Long)를 클라이언트에 반환
    }

    // **2. 채팅방 진입 엔드포인트**
    // 이 엔드포인트는 /api/chatrooms/chat/{roomId} 로 매핑됩니다.
    // 위와 동일한 @GetMapping("/chat/{roomId}") 매핑이므로 충돌 가능성이 있습니다.
    // 만약 chat.html을 반환하는 기능과 API 기능을 분리하고 싶다면,
    // 이 둘 중 하나는 @Controller로 분리하거나, 매핑 경로를 다르게 해야 합니다.
    // 현재는 @RestController이므로 JSON/데이터 반환에 더 적합합니다.
    // 뷰를 반환하는 @GetMapping("/chat/{roomId}")는 별도의 @Controller 클래스에 두는 것이 좋습니다.
    // 여기서는 일단 JSON 응답으로 변경하여 API 성격을 유지합니다.
    @GetMapping("/{roomId}") // /api/chatrooms/{roomId}
    public ResponseEntity<ChatRoom> getRoomDetails(@PathVariable Long roomId) { // roomId 타입 Long으로 변경
        ChatRoom chatRoom = chatService.getChatRoomById(roomId);
        return ResponseEntity.ok(chatRoom);
    }


    // 모든 채팅방 목록을 가져오는 엔드포인트
    // 이 매핑은 /api/chatrooms 로 매핑됩니다.
    @GetMapping // /api/chatrooms 에 대한 GET 요청
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        List<ChatRoom> rooms = chatService.getAllChatRooms(); // chatService가 getAllChatRooms를 가지고 있다고 가정
        return ResponseEntity.ok(rooms);
    }
} // <--- 이 닫는 중괄호가 ChatRoomController 클래스의 유일한 끝 중괄호여야 합니다.