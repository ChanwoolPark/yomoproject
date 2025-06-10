package com.project.yomozomo.repository;

import com.project.yomozomo.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // 필요하면 커스텀 쿼리 추가 가능
}
