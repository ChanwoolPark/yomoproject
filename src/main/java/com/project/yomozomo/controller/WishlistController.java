package com.project.yomozomo.controller;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.UserService;
import com.project.yomozomo.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;

    public WishlistController(WishlistService wishlistService, UserService userService) {
        this.wishlistService = wishlistService;
        this.userService = userService;
    }

    @PostMapping("/wishlist")
    public ResponseEntity<?> addToWishlist(@RequestBody Map<String, Integer> body, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        String username = principal.getName(); // 로그인된 사용자의 username
        User user = userService.findByUsername(username); // 유저 엔티티 조회
        Long userId = user.getId();

        int productId = body.get("productId");

        boolean added = wishlistService.addToWishlist(userId, productId);
        if (added) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "이미 찜한 상품입니다."));
        }
    }

    @DeleteMapping("/wishlist/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable int productId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();

        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("success", true));
    }

}