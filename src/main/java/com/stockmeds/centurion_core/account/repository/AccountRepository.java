package com.stockmeds.centurion_core.account.repository;

import com.stockmeds.centurion_core.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {
}
