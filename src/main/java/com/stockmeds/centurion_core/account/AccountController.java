package com.stockmeds.centurion_core.account;

import com.stockmeds.centurion_core.account.service.AccountService;
import com.stockmeds.centurion_core.user.dto.UserDTO;
import com.stockmeds.centurion_core.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {


    private final AccountService accountService;

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account")
    public ResponseEntity<AccountDTO> getAccount() {
        return ResponseEntity.ok(accountService.getAccount());
    }

}
