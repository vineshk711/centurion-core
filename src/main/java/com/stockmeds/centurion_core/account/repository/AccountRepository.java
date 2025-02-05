package com.stockmeds.centurion_core.account.repository;

import com.stockmeds.centurion_core.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.owner WHERE a.id = :id")
    Optional<Account> findByIdWithOwner(Integer id);
}
