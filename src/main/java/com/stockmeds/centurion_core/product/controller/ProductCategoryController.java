package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.product.record.ProductCategory;
import com.stockmeds.centurion_core.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorie")
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
    public ResponseEntity<List<ProductCategory>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<ProductCategory>> getCategoriesByParentId(@PathVariable Integer parentId) {
        return ResponseEntity.ok(productService.getCategoriesByParentId(parentId));
    }

}