// src/main/java/com/project/yomozomo/repository/ChatRoomRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatRoom; // 여러분의 ChatRoom 엔티티 경로
import com.project.yomozomo.entity.User;   // User 엔티티 경로 (필요 시)
import com.project.yomozomo.domain.Rental; // Rental 엔티티 경로
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> { // roomId가 Long 타입이므로 <ChatRoom, Long>
    // 특정 Rental, 판매자, 구매자에 해당하는 채팅방을 찾는 쿼리
    // User 객체를 직접 파라미터로 받습니다.
    Optional<ChatRoom> findByRentalAndSellerAndBuyer(Rental rental, User seller, User buyer);

    // 필요하다면 특정 판매자, 구매자 간의 채팅방을 찾는 쿼리 (Rental 없이)
    Optional<ChatRoom> findByBuyerAndSellerAndRental(User buyer, User seller, Rental rental);

    // roomId로 채팅방을 찾는 것은 JpaRepository의 findById가 처리
}