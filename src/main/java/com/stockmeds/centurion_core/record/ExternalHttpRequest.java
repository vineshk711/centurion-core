package com.stockmeds.centurion_core.record;

import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.Map;

public record ExternalHttpRequest(
    Serializable body,
    String url,
    HttpMethod httpMethod,
    Map<String, Object> uriParams,
    Map<String, String> headers
) {
}
