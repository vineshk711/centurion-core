package com.stockmeds.centurion_core.product.service;


import com.stockmeds.centurion_core.product.record.Product;
import com.stockmeds.centurion_core.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {


    private final ProductRepository productRepository;

    public ProductService(
            ProductRepository productRepository
    ) {
        this.productRepository = productRepository;
    }

    //------------------------------------------------------------------------------------------------------------------

    public Product getProduct(Integer productId) {
        return productRepository.findById(productId).map(Product::fromProductEntity).orElse(null);
    }


    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(Product::fromProductEntity);
    }
}
