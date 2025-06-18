package com.project.yomozomo.controller.user;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserPageController {
    CategoryService categoryService;

    @GetMapping("/find-id")
    public String showFindIdPage() {
        return "find-id";
    }
    @GetMapping("/find-password")
    public String showFindPasswordPage() {

        return "find-password";
    }
}
