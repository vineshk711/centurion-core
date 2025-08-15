package com.stockmeds.centurion_core.product.repository;

import com.stockmeds.centurion_core.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    Page<ProductEntity> findByCategoryId(Integer categoryId, Pageable pageable);

}
