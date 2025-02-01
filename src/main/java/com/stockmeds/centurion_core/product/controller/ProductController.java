package com.stockmeds.centurion_core.product.controller;

import com.stockmeds.centurion_core.product.entity.Product;
import com.stockmeds.centurion_core.product.service.ProductService;
import com.twilio.http.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
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


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(productService.findProductsByCategory(categoryId));
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(@PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }
}

