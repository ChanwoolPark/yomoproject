package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal User user) {
        // 카테고리 정보
        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);

        // 배너 이미지 리스트
        List<String> imageList = List.of(
                "banner1.png",
                "banner2.png",
                "banner3.png"
        );
        model.addAttribute("imageList", imageList);

        return "index";
    }


    @GetMapping("/category")
    public String category() {
        return "redirect:/category.html"; // 해당 html이 static에 존재할 경우
    }

    @GetMapping("/logout")
    public String logout() {
        // 세션 만료 등 처리
        return "redirect:/";
    }

/*    @GetMapping("/my")
    public String myPage() {
        return "redirect:/my.html";
    }*/

//    @GetMapping("/chatbot")
//    public String chatbot() {
//        return "redirect:/chatbot.html";
//    }

    @GetMapping("/support")
    public String support() {
        return "redirect:/support.html";
    }

    @GetMapping("/explore")
    public String explore() {
        return "redirect:/explore.html";
    }

    @GetMapping("/ads")
    public String ads() {
        return "redirect:/ads.html";
    }


}
