package com.stockmeds.centurion_core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExternalHttpRequest {

    private Serializable body;
    private String url;
    private HttpMethod httpMethod;
    private Map<String, Object> uriParams;
    private Map<String, String> headers;
}
