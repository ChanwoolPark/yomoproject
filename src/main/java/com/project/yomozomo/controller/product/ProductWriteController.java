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
    
    // 수정시 데이터 불러오기
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") int productId, Model model, Principal principal) {
        Product product = productWriteService.getProductById(productId); // 상품 불러오기
        String username = principal.getName(); // 로그인 사용자 확인
        User user = userService.findByUsername(username);

        if (!product.getSeller().getId().equals(user.getId())) {
            return "redirect:/"; // 권한 없는 사용자는 리다이렉트
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
        User seller = userService.findByUsername(username);

        productWriteService.deleteProduct(productId, seller);

        redirectAttributes.addFlashAttribute("message", "상품이 삭제되었습니다.");
        return "redirect:/";
    }


    
}

