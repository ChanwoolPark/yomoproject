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
import java.util.*;

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
                               UserRepository userRepository,
                               UserService userService,
                               ChatService chatService) {
        this.rentalRepository = rentalRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.chatService = chatService;
    }

    // 날짜 포맷
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    // +1일(캘린더 달력용)
    private Date plusOneDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    // 1. 예약된 날짜 리스트 조회
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

    // 2. 대여 생성(예약 + 자동 채팅방 생성)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRental(@RequestBody RentalRequestDto requestDto, Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            // 1. 로그인 확인
            if (principal == null) {
                resp.put("message", "로그인이 필요합니다.");
                resp.put("chatRoomId", -1L);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            String username = principal.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                resp.put("message", "유저 정보 없음.");
                resp.put("chatRoomId", -1L);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            // 2. 상품 확인
            Product product = productRepository.findById(requestDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 정보를 찾을 수 없습니다: " + requestDto.getProductId()));
            // 3. 날짜 파싱
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(requestDto.getStartDate());
            Date end = sdf.parse(requestDto.getEndDate());
            long days = ChronoUnit.DAYS.between(start.toInstant(), end.toInstant()) + 1;
            int totalPrice = (int) days * product.getPrice();

            // 4. Rental 저장
            Rental rental = new Rental();
            rental.setUser(user);
            rental.setProduct(product);
            rental.setStartDate(start);
            rental.setEndDate(end);
            rental.setTotalPrice(totalPrice);
            rental.setStatus("예약");
            rental.setCreatedAt(new Date());
            Rental savedRental = rentalRepository.save(rental);

            // 5. 채팅방 자동 생성
            Long chatRoomId = chatService.findOrCreateChatRoom(user, product.getSeller(), savedRental);

            resp.put("message", "예약 완료");
            resp.put("chatRoomId", chatRoomId);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("message", "예약 실패: " + e.getMessage());
            resp.put("chatRoomId", -1L);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    // 3. 금액 변경(판매자만)
    @PostMapping("/{rentalId}/price")
    public ResponseEntity<Map<String, Object>> updateRentalPrice(
            @PathVariable Long rentalId,
            @RequestBody Map<String, Integer> req,
            Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (principal == null) {
                resp.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
            }
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            Rental rental = rentalRepository.findById(rentalId).orElse(null);
            if (rental == null) {
                resp.put("message", "렌탈 정보 없음");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }
            // **판매자만 금액 변경**
            if (!rental.getProduct().getSeller().getId().equals(user.getId())) {
                resp.put("message", "판매자만 금액을 변경할 수 있습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
            }
            int newPrice = req.getOrDefault("newPrice", 0);
            if (newPrice <= 0) {
                resp.put("message", "유효하지 않은 금액입니다.");
                return ResponseEntity.badRequest().body(resp);
            }
            rental.setTotalPrice(newPrice);
            rentalRepository.save(rental);
            resp.put("success", true);
            resp.put("newPrice", newPrice);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("message", "금액 변경 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }
}
