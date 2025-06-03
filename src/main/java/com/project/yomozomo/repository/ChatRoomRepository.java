// src/main/java/com/project/yomozomo/repository/ChatRoomRepository.java
package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.User; // User 엔티티 필요
import com.project.yomozomo.domain.Rental; // Rental 엔티티 필요
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 기존 메서드 유지
    Optional<ChatRoom> findByBuyerAndSellerAndRental(User buyer, User seller, Rental rental);

    // ⭐⭐ 새로운 검색 메서드 추가 ⭐⭐
    List<ChatRoom> findByRoomNameContainingIgnoreCase(String roomName);

    // 사용자 이름으로 검색하기 위한 JpaRepository 쿼리 메서드 (선택 사항, 필요시 추가)
    // User 엔티티에 username 필드가 있고, ChatRoom 엔티티에 buyer/seller 관계가 설정되어 있다고 가정
    /*
    List<ChatRoom> findByBuyer_UsernameContainingIgnoreCase(String username);
    List<ChatRoom> findBySeller_UsernameContainingIgnoreCase(String username);
    */

    // JPQL을 사용하여 복잡한 검색 조건 (예: buyerName, sellerName 동시 검색) 구현 가능
    /*
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.buyer b JOIN cr.seller s WHERE " +
           "(:roomName IS NULL OR LOWER(cr.roomName) LIKE LOWER(CONCAT('%', :roomName, '%'))) AND " +
           "(:buyerName IS NULL OR LOWER(b.username) LIKE LOWER(CONCAT('%', :buyerName, '%'))) AND " +
           "(:sellerName IS NULL OR LOWER(s.username) LIKE LOWER(CONCAT('%', :sellerName, '%')))")
    List<ChatRoom> searchChatRooms(
            @Param("roomName") String roomName,
            @Param("buyerName") String buyerName,
            @Param("sellerName") String sellerName
    );
    */
}