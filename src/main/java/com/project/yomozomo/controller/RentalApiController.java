package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.User;
import com.project.yomozomo.dto.RentalRequestDto;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
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

    public RentalApiController(RentalRepository rentalRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository) {
        this.rentalRepository = rentalRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
                                          HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = 1L;
            session.setAttribute("userId", userId);
        }

        try {
            User user = userRepository.findById(userId).orElseThrow();
            Product product = productRepository.findById(requestDto.getProductId()).orElseThrow();

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

            rentalRepository.save(rental);
            return ResponseEntity.ok("예약 완료");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("예약 실패: " + e.getMessage());
        }
    }

}

