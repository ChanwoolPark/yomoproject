// src/main/java/com/project/yomozomo/dto/ChatStartRequest.java
package com.project.yomozomo.dto; // 이 패키지 경로가 중요합니다!

import lombok.Getter; // ⭐ Lombok Getter 임포트 필수 ⭐
import lombok.Setter; // ⭐ Lombok Setter 임포트 필수 ⭐

@Getter // ⭐ 이 어노테이션이 getRentalId() 메서드를 자동으로 생성합니다 ⭐
@Setter // ⭐ 이 어노테이션이 setRentalId() 메서드를 자동으로 생성합니다 ⭐
public class ChatStartRequest {
    private Long rentalId; // ⭐ 이 필드가 존재하고 이름이 'rentalId'인지 확인 ⭐
    // 다른 필드가 있었다면 여기서 제거되었는지, 혹은 필요하다면 여기에만 남아있는지 확인
}