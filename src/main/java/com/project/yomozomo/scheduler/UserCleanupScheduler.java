package com.project.yomozomo.scheduler;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private final UserService userService;
    private final UserRepository userRepository;
    // 매일 새벽 2시에 30일 이상 지난 탈퇴자 삭제
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteOldWithdrawnUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> users = userRepository.findByIsWithdrawnAndWithdrawnAtBefore("Y", threshold);
        System.out.println("삭제대상: " + users.size() + "명");
        for (User user : users) {
            System.out.println("삭제대상 ID: " + user.getId() + ", 탈퇴일: " + user.getWithdrawnAt());
        }
        userService.anonymizeWithdrawnUsersOlderThan(threshold);
    }

    // 매일 새벽 3시마다 1년 미접속자 휴면 처리
    @Scheduled(cron = "0 0 3 * * *")
    public void batchDormant() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        userService.markDormantUsersOlderThan(oneYearAgo);
    }
}
