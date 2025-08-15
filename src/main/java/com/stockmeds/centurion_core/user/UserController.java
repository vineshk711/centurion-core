package com.stockmeds.centurion_core.user;

import com.stockmeds.centurion_core.user.record.User;
import com.stockmeds.centurion_core.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<User> getUser() {
        return ResponseEntity.ok(userService.getUser());
    }
}


