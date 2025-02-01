package com.stockmeds.centurion_core.product.service;


import com.stockmeds.centurion_core.product.entity.Product;
import com.stockmeds.centurion_core.product.entity.ProductCategory;
import com.stockmeds.centurion_core.product.repository.ProductCategoryRepository;
import com.stockmeds.centurion_core.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {


    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductService(
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository
    ) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    //------------------------------------------------------------------------------------------------------------------

    public Product getProduct(Integer productId) {
        return productRepository.findById(productId)
                .orElse(null);
    }

    public List<Product> findProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(product -> Product.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .brand(product.getBrand())
                        .strength(product.getStrength())
                        .unitOfMeasure(product.getUnitOfMeasure())
                        .categoryId(product.getCategoryId())
                        .build());
    }

    public ProductCategory getProductCategory(Integer categoryId) {
        return productCategoryRepository.findById(categoryId)
                .orElse(null);
    }

    public List<ProductCategory> getAllCategories() {
        return productCategoryRepository.findByParentCategoryIsNull();
    }
}
