package com.stockmeds.centurion_core.account.service;

import com.stockmeds.centurion_core.account.AccountDTO;
import com.stockmeds.centurion_core.account.entity.Account;
import com.stockmeds.centurion_core.account.repository.AccountRepository;
import com.stockmeds.centurion_core.auth.dto.UserAccountAttributes;
import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import com.stockmeds.centurion_core.user.entity.User;
import com.stockmeds.centurion_core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public AccountDTO getAccount() {
        UserAccountAttributes userAccountAttributes = CenturionThreadLocal.getUserAccountAttributes();
        return accountRepository.findById(userAccountAttributes.getAccountId()).map(Account::toAccountDTO).orElse(null);
    }
}