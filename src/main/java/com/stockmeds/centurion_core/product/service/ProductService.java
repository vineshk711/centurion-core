package com.stockmeds.centurion_core.product.service;


import com.stockmeds.centurion_core.product.entity.ProductCategoryEntity;
import com.stockmeds.centurion_core.product.entity.ProductEntity;
import com.stockmeds.centurion_core.product.record.ProductCategory;
import com.stockmeds.centurion_core.product.record.Product;
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
        return productRepository.findById(productId).map(ProductEntity::toProductDTO).orElse(null);
    }

    public Page<Product> getProductsByCategory(Integer categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(ProductEntity::toProductDTO);
    }


    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductEntity::toProductDTO);
    }

    public ProductCategory getProductCategory(Integer categoryId) {
        return productCategoryRepository.findById(categoryId)
                .map(ProductCategoryEntity::toProductCategoryDTO).orElse(null);
    }

    public List<ProductCategory> getAllCategories() {
        return productCategoryRepository.findByParentCategoryIsNull()
                .stream().map(category -> new ProductCategory(
                        category.getId(),
                        category.getName(),
                        null, // parentCategoryId is null for root categories
                        category.getDescription(),
                        category.getImageUrl(),
                        category.getSubcategories()
                ))
                .toList();
    }

    public List<ProductCategory> getCategoriesByParentId(Integer parentId) {
        return productCategoryRepository.findByParentCategoryId(parentId)
                .stream().map(category -> new ProductCategory(
                        category.getId(),
                        category.getName(),
                        parentId,
                        category.getDescription(),
                        category.getImageUrl(),
                        null // subcategories not needed for child categories
                ))
                .toList();
    }
}
