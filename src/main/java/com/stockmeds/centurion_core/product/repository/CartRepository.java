package com.stockmeds.centurion_core.product.repository;

import com.stockmeds.centurion_core.product.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByAccountId(Integer accountId);

    boolean existsByAccountId(Integer accountId);
}
