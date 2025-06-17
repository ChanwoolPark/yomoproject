package com.project.yomozomo.controller.chat;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.CategoryService;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/chatadmin/chatrooms")
@RequiredArgsConstructor
public class ChatRoomAdminController {

    private final ChatService chatService;
    private final UserService userService;
    private final CategoryService categoryService;


    @GetMapping
    public String getChatRoomAdminPage(
            @RequestParam(value = "roomName", required = false) String roomName,
            @RequestParam(value = "buyerName", required = false) String buyerName,
            @RequestParam(value = "sellerName", required = false) String sellerName,
            Principal principal,
            Model model) {

        // 로그인한 사용자 정보
        User currentUser = userService.getUserByUsername(principal.getName());

        // 채팅방 검색 결과
        List<ChatRoom> chatRooms = chatService.searchChatRoomsFiltered(
                currentUser.getId(), roomName, buyerName, sellerName
        );

        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);

        model.addAttribute("chatRooms", chatRooms);
        return "chat_room_admin"; // templates/chat_room_admin.html
    }
}
