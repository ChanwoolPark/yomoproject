package com.project.yomozomo.service;

import com.project.yomozomo.domain.Rental; // Rental 엔티티 클래스 임포트
import com.project.yomozomo.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentalService {
    private final RentalRepository rentalRepository;

    public Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("렌탈 정보를 찾을 수 없습니다: " + rentalId));
    }


    // 필요한 경우 여기에 추가적인 렌탈 관련 비즈니스 로직 추가
}