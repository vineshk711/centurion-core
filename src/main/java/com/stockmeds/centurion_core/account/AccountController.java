package com.stockmeds.centurion_core.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {


    @GetMapping("/{account-id}")
    public ResponseEntity<Object> getAccountInfo(@PathVariable("account-id") Integer accountId) {
        return ResponseEntity.ok(Map.of("name", "Test account name",
                                        "accountId", accountId));
    }

}
