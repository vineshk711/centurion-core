package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.product.record.Product;
import com.stockmeds.centurion_core.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product")
public class ProductController {


    private final ProductService productService;


    public ProductController (
            ProductService productService
    ) {
        this.productService = productService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") Integer productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }
}

