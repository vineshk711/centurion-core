package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.product.entity.ProductCategory;
import com.stockmeds.centurion_core.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/category")
public class ProductCategoryController {


    private final ProductService productService;


    public ProductCategoryController (
            ProductService productService
    ) {
        this.productService = productService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getProductCategory(@PathVariable("id") Integer categoryId) {
        return ResponseEntity.ok(productService.getProductCategory(categoryId));
    }


    @GetMapping
    public ResponseEntity<Page<ProductCategory>> getAllCategories(@PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllCategories(pageable));
    }

}