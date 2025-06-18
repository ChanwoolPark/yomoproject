package com.project.yomozomo.controller.product;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ProductFormService;
import com.project.yomozomo.service.ProductWriteService;
import com.project.yomozomo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
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
        model.addAttribute("categoryId", categoryId);
        return "product/write";
    }

    @PostMapping("/write")
    public String writeProduct(@RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam("price") int price,
                               @RequestParam("deposit") int deposit,
                               @RequestParam("subCategoryId") int subCategoryId,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        // 1. 제목 길이 제한 검사
        if (title.length() > 20) {
            model.addAttribute("errorMessage", "상품 제목이 너무 깁니다. 20자 이하로 입력해주세요.");
            model.addAttribute("subCategories", productFormService.getAllSubCategories(subCategoryId));
            model.addAttribute("categoryId", subCategoryId); // 뒤로가기 버튼용
            return "product/write";
        }

        // 2. 이미지 첨부 여부 검사
        if (images == null || images.isEmpty() || images.get(0).isEmpty()) {
            model.addAttribute("errorMessage", "상품 이미지는 필수입니다.");
            model.addAttribute("subCategories", productFormService.getAllSubCategories(subCategoryId));
            model.addAttribute("categoryId", subCategoryId); // 뒤로가기 버튼용
            return "product/write";
        }

        // 로그인 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User seller = userService.findByUsername(username);
        Long userId = seller.getId();

        // 상품 등록
        SubCategory subCategory = productWriteService.getSubCategory(subCategoryId);
        productWriteService.registerProduct(title, description, price, deposit, seller, subCategory, images);

        redirectAttributes.addFlashAttribute("message", "상품 등록이 완료되었습니다!");

        return "redirect:/category/" + subCategory.getCategory().getCategoryId();
    }
    // 수정시 데이터 불러오기
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int productId, Model model, Principal principal) {
        Product product = productWriteService.getProductById(productId); // 상품 불러오기
        String username = principal.getName(); // 로그인 사용자 확인
        User user = userService.findByUsername(username);

        if (!product.getSeller().getId().equals(user.getId()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }

        model.addAttribute("product", product);
        model.addAttribute("subCategories", productFormService.getAllSubCategories(product.getSubCategory().getCategory().getCategoryId()));
        return "product/edit"; // 수정 페이지
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable("id") int productId,
                                @RequestParam("title") String title,
                                @RequestParam("description") String description,
                                @RequestParam("price") int price,
                                @RequestParam("deposit") int deposit,
                                @RequestParam("subCategoryId") int subCategoryId,
                                @RequestParam("status") String status,
                                @RequestParam(value = "images", required = false) List<MultipartFile> images,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        User seller = userService.findByUsername(username);
        SubCategory subCategory = productWriteService.getSubCategory(subCategoryId);

        productWriteService.updateProduct(productId, title, description, price, deposit,status,  seller, subCategory, images);
        redirectAttributes.addFlashAttribute("message", "상품이 수정되었습니다.");
        return "redirect:/product/" + productId;
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") int productId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        String username = principal.getName();
        User user = userService.findByUsername(username);

        // 상품 정보 조회 (카테고리 ID 추출용)
        Product product = productWriteService.getProductById(productId);
        int categoryId = product.getSubCategory().getCategory().getCategoryId();

        // 실제 삭제 로직
        productWriteService.deleteProduct(productId, user);

        // 메시지 전달
        redirectAttributes.addFlashAttribute("message", "상품이 삭제되었습니다.");

        // 삭제 후 해당 카테고리 리스트로 이동
        return "redirect:/category/" + categoryId;
    }


    
}

