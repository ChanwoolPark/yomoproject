package com.project.yomozomo.controller;

import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ProductFormService;
import com.project.yomozomo.service.ProductWriteService;
import com.project.yomozomo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductWriteController {

    private final ProductFormService productFormService;
    private final ProductWriteService productWriteService;
    private final UserService userService; // 로그인 유저 정보 조회 용도

    public ProductWriteController(ProductFormService productFormService, ProductWriteService productWriteService, UserService userService) {
        this.productFormService = productFormService;
        this.userService = userService;
        this.productWriteService = productWriteService;
    }

    @GetMapping("/write")
    public String showWriteForm(@RequestParam("categoryId") int categoryId, Model model) {
        model.addAttribute("subCategories", productFormService.getAllSubCategories(categoryId));
        return "product/write";
    }

    @PostMapping("/write")
    public String writeProduct(@RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam("price") int price,
                               @RequestParam("deposit") int deposit,
                               @RequestParam("subCategoryId") int subCategoryId,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               RedirectAttributes redirectAttributes) {

        // 로그인 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // username → user_id로 변환
        User seller = userService.findByUsername(username);  // 여기서 user_id 포함된 User 객체 획득
        Long userId = seller.getId(); // 나중에 user_id가 필요할 경우를 대비

        // 상품 등록
        SubCategory subCategory = productWriteService.getSubCategory(subCategoryId);
        productWriteService.registerProduct(title, description, price, deposit, seller, subCategory, images);

        redirectAttributes.addFlashAttribute("message", "상품 등록이 완료되었습니다!");

        return "redirect:/category/" + subCategory.getCategory().getCategoryId();
    }

}
