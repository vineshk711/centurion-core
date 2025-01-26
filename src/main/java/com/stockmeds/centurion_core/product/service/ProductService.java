package com.stockmeds.centurion_core.product.service;


import com.stockmeds.centurion_core.product.entity.Product;
import com.stockmeds.centurion_core.product.repository.ProductCategoryRepository;
import com.stockmeds.centurion_core.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    // Full-text search
    public List<Product> searchProducts(String searchTerm) {
//        return productRepository.searchProducts(searchTerm);
        return null;
    }

    // Find products by category
    public List<Product> findProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product getProduct(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        return productOptional.orElse(null);
    }
}
