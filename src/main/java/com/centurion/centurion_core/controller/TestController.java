package com.centurion.centurion_core.controller;

import com.centurion.centurion_core.dto.ExternalHttpRequest;
import com.centurion.centurion_core.service.GenericRestClientService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
public class TestController {

    private final GenericRestClientService restClientService;

    public TestController(GenericRestClientService restClientService) {
        this.restClientService = restClientService;
    }

    @PostMapping("/test")
    public ResponseEntity<Object> test(@RequestBody Object data) {
        ExternalHttpRequest httpRequest = ExternalHttpRequest.builder()
                .url("http://localhost:8081/ambassador/user/login")
                .body((Serializable) data)
                .httpMethod(HttpMethod.POST)
                .build();
        Object response = restClientService.execute(httpRequest, Object.class);

        return ResponseEntity.ok(response);
    }
}
