package com.project.yomozomo.controller;

import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.service.ProductFormService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductWriteController {

    private final ProductFormService productFormService;

    public ProductWriteController(ProductFormService productFormService) {
        this.productFormService = productFormService;
    }

    @GetMapping("/write")
    public String showWriteForm(Model model) {
        // 서브카테고리 목록을 모델에 추가
        List<SubCategory> subCategories = productFormService.getAllSubCategories();
        model.addAttribute("subCategories", subCategories);

        return "product/write";  // → templates/product/write.html
    }
}