package com.stockmeds.centurion_core.account.service;

import com.stockmeds.centurion_core.account.entity.AccountEntity;
import com.stockmeds.centurion_core.account.record.Account;
import com.stockmeds.centurion_core.account.repository.AccountRepository;
import com.stockmeds.centurion_core.auth.record.UserAccountAttributes;
import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public Account getAccount() {
        UserAccountAttributes userAccountAttributes = CenturionThreadLocal.getUserAccountAttributes();
        return accountRepository.findById(userAccountAttributes.getAccountId()).map(AccountEntity::toAccountDTO).orElse(null);
    }
}