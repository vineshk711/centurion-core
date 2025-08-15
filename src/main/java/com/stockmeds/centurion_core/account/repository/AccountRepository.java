package com.stockmeds.centurion_core.account.repository;

import com.stockmeds.centurion_core.account.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
}
