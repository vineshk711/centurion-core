package com.stockmeds.centurion_core.account.service;

import com.stockmeds.centurion_core.account.entity.AccountEntity;
import com.stockmeds.centurion_core.account.enums.AccountStatus;
import com.stockmeds.centurion_core.account.record.Account;
import com.stockmeds.centurion_core.account.repository.AccountRepository;
import com.stockmeds.centurion_core.auth.record.UserAccountAttributes;
import com.stockmeds.centurion_core.config.CenturionThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public Account getAccount(Integer accountId) {
        accountId = Optional.ofNullable(accountId).orElse(CenturionThreadLocal.getUserAccountAttributes().getAccountId());
        return accountRepository.findById(accountId).map(Account::fromAccountEntity).orElse(null);
    }

    public void createAccount(Integer ownerId) {
        AccountEntity account = new AccountEntity();
        account.setName(String.format("Account of %s", ownerId));
        account.setOwnerId(ownerId);
        account.setAccountStatus(AccountStatus.INCOMPLETE);
        accountRepository.save(account);
    }

    public Account createAccount(Account account) {
        UserAccountAttributes userAccountAttributes = CenturionThreadLocal.getUserAccountAttributes();
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setName(account.name());
        accountEntity.setOwnerId(userAccountAttributes.getUserId());
        accountEntity = accountRepository.save(accountEntity);
        return Account.fromAccountEntity(accountEntity);
    }

    public Account getAccountByOwnerId(Integer id) {
        return Optional.ofNullable(accountRepository.findByOwnerId(id))
                .map(Account::fromAccountEntity)
                .orElse(null);
    }
}