package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.entity.Notice;
import com.project.yomozomo.service.CategoryService;
import com.project.yomozomo.service.NoticeService;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;
    private final ProductListService productListService;
    private final SubCategoryService subCategoryService;
    private final NoticeService noticeService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal User user) {
        // 카테고리 정보
        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);

        List<Map<String, String>> imageList = new ArrayList<>();
        imageList.add(Map.of("file", "banner1.png" +
                "" +
                "" +
                "" +
                "" +
                "" +
                "" +
                "", "url", "https://www.youtube.com/watch?v=c_l1ZwJbAnc"));
        imageList.add(Map.of("file", "banner2.png", "url", "https://www.youtube.com/watch?v=x9mLLDcBmU0"));
        imageList.add(Map.of("file", "banner3.png", "url", "https://www.youtube.com/watch?v=RudIRwJwdAg"));
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

    @GetMapping("/subcategory/{subCategoryId}")
    public String subCategoryRedirect(@PathVariable int subCategoryId) {
        SubCategory subCategory = subCategoryService.findById(subCategoryId);
        int categoryId = subCategory.getCategory().getCategoryId();

        return "redirect:/category/" + categoryId + "/subcategory/" + subCategoryId;
    }

    @GetMapping("/support")
    public String supportPage(Model model) {
        List<Notice> notices = noticeService.findAll(); // 최신순 정렬 원하면 정렬 쿼리 추가!
        model.addAttribute("notices", notices);
        return "support"; // 고객센터 메인 템플릿
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
