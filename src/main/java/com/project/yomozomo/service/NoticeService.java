package com.project.yomozomo.service;

import com.project.yomozomo.entity.Notice;
import com.project.yomozomo.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepo;

    public List<Notice> findAll() {
        return noticeRepo.findAll();
    }

    public Notice findById(Long id) {
        return noticeRepo.findById(id).orElseThrow();
    }

    @Transactional
    public void createNotice(String title, String content) {
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        noticeRepo.save(notice);
    }
}
