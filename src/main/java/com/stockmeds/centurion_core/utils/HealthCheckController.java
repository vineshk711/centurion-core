package com.stockmeds.centurion_core.utils;

import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HealthCheckController {

    private final Environment env;

    HealthCheckController(Environment env) {
        this.env = env;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE} )
    public ResponseEntity<Map<String, Object>> getHealthCheckInfo() {
        return ResponseEntity.ok(Map.of(
                "env", Arrays.toString(env.getActiveProfiles()),
                "status", "UP"
        ));
    }

}