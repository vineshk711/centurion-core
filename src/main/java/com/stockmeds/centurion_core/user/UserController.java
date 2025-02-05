package com.stockmeds.centurion_core.user;

import com.stockmeds.centurion_core.product.entity.Product;
import com.stockmeds.centurion_core.user.dto.UserDTO;
import com.stockmeds.centurion_core.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserByPhoneNumber(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }
}


