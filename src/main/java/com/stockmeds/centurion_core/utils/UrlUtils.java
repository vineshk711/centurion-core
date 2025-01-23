package com.stockmeds.centurion_core.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.Objects.isNull;

public class UrlUtils {

    private UrlUtils() { }

    public static String appendQueryParams(String url, Map<String, Object> parametersMap) {
        StringBuilder uriBuilder = new StringBuilder(url);
        if (parametersMap == null || parametersMap.isEmpty()) {
            return uriBuilder.toString();
        }

        String prefix = uriBuilder.toString().contains("?") ? "&" : "?";

        String queryString = parametersMap.entrySet()
                .stream()
                .map(entry -> {
                    var key = entry.getKey();
                    var value = entry.getValue();
                    return "%s=%s".formatted(
                            URLEncoder.encode(key, StandardCharsets.UTF_8),
                            URLEncoder.encode(isNull(value) ? "" : value.toString(), StandardCharsets.UTF_8)
                    );
                })
                .reduce("%s&%s"::formatted)
                .orElse("");

        uriBuilder.append(prefix).append(queryString);
        return uriBuilder.toString();
    }
}
