package com.project.yomozomo.controller.chat;
import com.project.yomozomo.dto.ChatroomReportForm;
import com.project.yomozomo.service.ChatroomReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ChatroomReportController {

    private final ChatroomReportService chatroomReportService;

    @GetMapping("/new/{chatRoomId}")
    public String showReportForm(@PathVariable Long chatRoomId,
                                 @RequestParam Long reporterId,
                                 @RequestParam Long reportedId,
                                 Model model) {
        ChatroomReportForm form = new ChatroomReportForm();
        form.setChatRoomId(chatRoomId);
        form.setReporterId(reporterId);
        form.setReportedId(reportedId);
        model.addAttribute("reportForm", form);

        model.addAttribute("chatRoomId", chatRoomId);
        model.addAttribute("reporterId", reporterId);
        model.addAttribute("reportedId", reportedId);
        return "report/new";
    }

    @PostMapping("/new")
    public String submitReport(@ModelAttribute("reportForm") ChatroomReportForm form) {
        chatroomReportService.submitReport(
                form.getChatRoomId(),
                form.getReporterId(),
                form.getReportedId(),
                form.getTitle(),
                form.getContent()
        );
        return "redirect:/report/thanks";
    }

    @GetMapping("/thanks")
    public String thanks() {
        return "report/thanks";
    }
}