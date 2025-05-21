package com.project.yomozomo.repository;

import com.project.yomozomo.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

    // 양방향 유저 간 채팅방 존재 여부
    boolean existsByBuyer_IdAndSeller_IdOrBuyer_IdAndSeller_Id(
            Long buyerId, Long sellerId, Long sellerIdAlt, Long buyerIdAlt
    );
}