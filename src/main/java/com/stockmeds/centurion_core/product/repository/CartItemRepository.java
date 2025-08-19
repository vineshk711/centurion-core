package com.stockmeds.centurion_core.product.repository;

import com.stockmeds.centurion_core.product.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find all cart items for an account (replaces findByCartId)
    List<CartItem> findByAccountId(Integer accountId);

    // Find specific cart item by account and product
    Optional<CartItem> findByAccountIdAndProductId(Integer accountId, Integer productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.accountId = :accountId")
    void deleteByAccountId(@Param("accountId") Integer accountId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.accountId = :accountId AND ci.product.id = :productId")
    void deleteByAccountIdAndProductId(@Param("accountId") Integer accountId, @Param("productId") Integer productId);
}
