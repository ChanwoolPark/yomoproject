package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductImageRepository;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.SubCategoryRepository;
import com.project.yomozomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductWriteService {

    private final ProductRepository productRepo;
    private final ProductImageRepository imageRepo;
    private final SubCategoryRepository subCategoryRepo;
    private final UserRepository userRepo;

    public void registerProduct(String title, String description, int price, int deposit,
                                User seller, SubCategory subCategory, List<MultipartFile> images) {

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setDeposit(deposit);
        product.setCount(0);
        product.setSeller(seller);
        product.setSubCategory(subCategory);
        product.setStatus("판매중");

        productRepo.save(product); // 먼저 product 저장 (ID 필요)

        if (images != null) {
            images.stream().limit(3).forEach(file -> {
                try {
                    // 실제 저장 경로 지정 (static/images 폴더)
                    String uploadDir = new File("src/main/resources/static/images").getAbsolutePath();

                    // 저장될 파일 객체 생성
                    File dest = new File(uploadDir, file.getOriginalFilename());

                    // 파일 복사
                    file.transferTo(dest);

                    // DB에는 상대 URL 경로만 저장
                    String imageUrl = "/images/" + file.getOriginalFilename();

                    ProductImage img = new ProductImage();
                    img.setProduct(product);
                    img.setImageUrl(imageUrl);
                    imageRepo.save(img);

                } catch (Exception e) {
                    throw new RuntimeException("이미지 저장 중 오류 발생: " + file.getOriginalFilename(), e);
                }
            });
        }
    }

    public SubCategory getSubCategory(int id) {
        return subCategoryRepo.findById(id).orElseThrow(() -> new RuntimeException("소카테고리 없음"));
    }

    public Product getProductById(int productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
    }

    @Transactional
    public void updateProduct(int productId, String title, String description, int price, int deposit,
                              String status,  User seller, SubCategory subCategory,  List<MultipartFile> images) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setDeposit(deposit);
        product.setSubCategory(subCategory);
        product.setStatus(status);

        if (images != null && images.stream().anyMatch(file -> !file.isEmpty())) {
            imageRepo.deleteByProduct(product);

            images.stream()
                    .filter(file -> !file.isEmpty())
                    .limit(3)
                    .forEach(file -> {
                        try {
                            String uploadDir = new File("src/main/resources/static/images").getAbsolutePath();
                            File dir = new File(uploadDir);
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }

                            File dest = new File(uploadDir, file.getOriginalFilename());
                            file.transferTo(dest);

                            String imageUrl = "/images/" + file.getOriginalFilename();
                            ProductImage img = new ProductImage();
                            img.setProduct(product);
                            img.setImageUrl(imageUrl);
                            imageRepo.save(img);
                        } catch (IOException e) {
                            throw new RuntimeException("이미지 저장 중 오류 발생", e);
                        }
                    });
        }
    }

    @Transactional
    public void deleteProduct(int productId, User currentUser) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        if (!product.getSeller().getId().equals(currentUser.getId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        product.delete();  // isDeleted = 'Y'
    }

}
