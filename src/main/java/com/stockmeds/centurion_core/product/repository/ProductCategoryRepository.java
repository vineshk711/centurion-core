package com.stockmeds.centurion_core.product.repository;

import com.stockmeds.centurion_core.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
    //TODO return only relevant data
    List<ProductCategory> findByParentCategoryIsNull();

    List<ProductCategory> findByParentCategoryId(Integer parentId);
}

