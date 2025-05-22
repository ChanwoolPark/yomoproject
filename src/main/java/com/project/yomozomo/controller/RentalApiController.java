package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.repository.RentalRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rental")
public class RentalApiController {

    private final RentalRepository rentalRepository;

    public RentalApiController(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @GetMapping("/reserved/{productId}")
    public List<Map<String, String>> getReservedDates(@PathVariable int productId) {
        List<Rental> rentals = rentalRepository.findByProduct_ProductIdAndStatusIn(productId, List.of("예약", "대여중"));
        return rentals.stream()
                .map(r -> Map.of(
                        "start", r.getStartDate().toString(),
                        "end", r.getEndDate().toString()
                ))
                .toList();
    }
}

