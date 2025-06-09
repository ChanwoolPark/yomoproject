// src/main/java/com/project/yomozomo/controller/RentalApiController.java

package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.dto.RentalRequestDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rental")
public class RentalApiController {

    private final RentalRepository rentalRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ChatService chatService;

    public RentalApiController(RentalRepository rentalRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository, UserService userService, ChatService chatService) {
        this.rentalRepository = rentalRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.chatService = chatService;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    @GetMapping("/reserved/{productId}")
    public List<Map<String, String>> getReservedDates(@PathVariable int productId) {
        List<Rental> rentals = rentalRepository.findByProduct_ProductIdAndStatusIn(productId, List.of("예약", "대여중"));
        return rentals.stream()
                .map(r -> Map.of(
                        "start", formatDate(r.getStartDate()),
                        "end", formatDate(plusOneDay(r.getEndDate()))
                ))
                .toList();
    }

    private Date plusOneDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }


    // RentalApiController.java 내부에 추가
    @PostMapping
    public ResponseEntity<?> createRental(@RequestBody RentalRequestDto requestDto,
                                          Principal principal) {
        // 로그인 ID 가져오기
        String username = principal.getName(); // 로그인된 사용자의 username(email, 아이디 등)

        // userService.findByUsername()의 반환 타입에 따라 .orElseThrow() 사용 여부 결정
        User user = userService.findByUsername(username);


        Long userId = user.getId(); // 실제 user_id 추출

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            // 이 user는 위에서 이미 검증되었으므로, 다시 userRepository.findById로 찾을 필요는 없습니다.
            // 하지만 일관성 유지 차원에서 남겨둘 수도 있습니다.
            // user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다: " + userId));
            Product product = productRepository.findById(requestDto.getProductId()).orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다: " + requestDto.getProductId()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(requestDto.getStartDate());
            Date end = sdf.parse(requestDto.getEndDate());

            long days = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant()) + 1;
            int totalPrice = (int) days * product.getPrice();

            Rental rental = new Rental();
            rental.setUser(user);
            rental.setProduct(product);
            rental.setStartDate(start);
            rental.setEndDate(end);
            rental.setTotalPrice(totalPrice);
            rental.setStatus("예약");
            rental.setCreatedAt(new Date());

            Rental savedRental = rentalRepository.save(rental);

            // ⭐ 이 부분이 핵심입니다: chatService의 메서드를 호출합니다. ⭐
            // product.getUser()가 판매자 User 객체를 반환하는지 다시 한번 확인해주세요.
            Long chatRoomId = chatService.findOrCreateChatRoomForRental(user, product.getSeller(), savedRental.getRentalId());

            // ⭐ (선택 사항) 응답을 JSON 형태로 변경하여 chatRoomId를 클라이언트에 전달 ⭐
            // 이 부분을 사용하려면 product-detail.html의 submitReservation 함수도 수정해야 합니다.
            return ResponseEntity.ok(Map.of("message", "예약 완료", "chatRoomId", chatRoomId));

        } catch (Exception e) {
            // 에러 메시지도 JSON 형태로 반환하는 것이 좋습니다.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "예약 실패: " + e.getMessage()));
        }
    }
}