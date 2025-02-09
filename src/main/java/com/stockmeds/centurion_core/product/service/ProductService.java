package com.stockmeds.centurion_core.product.service;


import com.stockmeds.centurion_core.product.dto.ProductCategoryDTO;
import com.stockmeds.centurion_core.product.dto.ProductDTO;
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

    public ProductDTO getProduct(Integer productId) {
        return productRepository.findById(productId).map(Product::toProductDTO).orElse(null);
    }

    public Page<ProductDTO> getProductsByCategory(Integer categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(Product::toProductDTO);
    }


    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(Product::toProductDTO);
    }

    public ProductCategoryDTO getProductCategory(Integer categoryId) {
        return productCategoryRepository.findById(categoryId)
                .map(ProductCategory::toProductCategoryDTO).orElse(null);
    }

    public List<ProductCategoryDTO> getAllCategories() {
        return productCategoryRepository.findByParentCategoryIsNull()
                .stream().map(category -> ProductCategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .imageUrl(category.getImageUrl())
                        .subcategories(category.getSubcategories())
                        .build())
                .toList();
    }

    public List<ProductCategoryDTO> getCategoriesByParentId(Integer parentId) {
        return productCategoryRepository.findByParentCategoryId(parentId)
                .stream().map(category -> ProductCategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .imageUrl(category.getImageUrl())
                        .build())
                .toList();
    }
}
