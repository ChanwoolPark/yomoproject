// ReportController.java 예시
package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    @GetMapping("/report")
    public String showReportPage(@RequestParam(required = false) String roomId,
                                 @RequestParam(required = false) String targetUser,
                                 Model model) {
        if (roomId != null) {
            model.addAttribute("reportedRoomId", roomId);
        }
        if (targetUser != null) {
            model.addAttribute("reportedTargetUser", targetUser);
        }
        // 필요한 경우 추가적인 데이터를 모델에 담을 수 있습니다.
        return "report"; // src/main/resources/templates/report.html 렌더링
    }
}