// src/main/java/com/project/yomozomo/repository/ChatRoomRepository.java (예시)

package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.Rental; // ⭐ Rental 엔티티 임포트 ⭐
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 구매자, 판매자, 렌탈 ID로 채팅방을 찾는 메서드
    Optional<ChatRoom> findByBuyerAndSellerAndRental(User buyer, User seller, Rental rental);

    List<ChatRoom> findByRoomNameContainingIgnoreCase(String roomName);
    // 참고: buyer와 seller의 순서에 상관없이 찾고 싶다면, 두 가지 경우를 모두 고려해야 할 수 있습니다.
    // 또는 buyer_id와 seller_id를 채팅방 생성 시 항상 정해진 순서로 저장하도록 강제할 수 있습니다.

    // 렌탈 상품과 연결되지 않은 일반 1:1 채팅방을 찾을 때 (rental 필드가 null인 경우)
    Optional<ChatRoom> findByBuyerAndSellerAndRentalIsNull(User buyer, User seller);

    // 특정 사용자가 참여한 모든 채팅방을 찾을 때 (필요하다면 추가)
    // List<ChatRoom> findByBuyerOrSeller(User user1, User user2);
}