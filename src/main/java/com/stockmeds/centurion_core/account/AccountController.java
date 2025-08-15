package com.stockmeds.centurion_core.account;

import com.stockmeds.centurion_core.account.record.Account;
import com.stockmeds.centurion_core.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {


    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account")
    public ResponseEntity<Account> getAccount() {
        return ResponseEntity.ok(accountService.getAccount());
    }

}
