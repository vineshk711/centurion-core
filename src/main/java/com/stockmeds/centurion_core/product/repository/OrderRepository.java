package com.stockmeds.centurion_core.product.repository;

import com.stockmeds.centurion_core.product.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAccountIdOrderByCreatedAtDesc(Integer accountId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdAndAccountId(Long id, Integer accountId);

    @Query("SELECT o FROM Order o WHERE o.accountId = :accountId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByAccountIdAndStatus(@Param("accountId") Integer accountId, @Param("status") String status);
}
