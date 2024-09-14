package com.centurion.centurion_core.service.impl;

import com.centurion.centurion_core.dto.ExternalHttpRequest;
import com.centurion.centurion_core.enums.ErrorCode;

import com.centurion.centurion_core.exception.ExternalCallException;
import com.centurion.centurion_core.service.GenericRestClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class GenericRestClientServiceImpl implements GenericRestClientService {

    @Override
    public <T> T execute(ExternalHttpRequest request, Class<T> responseType) {
        var url = appendQueryParams(request.getUrl(), request.getUriParams());
        var httpMethod = request.getHttpMethod();
        log.debug("Calling endpoint [{}] : [{}]", httpMethod, url);

        var httpEntity = prepareHTTPEntity(httpMethod, request.getData(), request.getHeaders());
        long startTime = 0;
        long endTime = 0;
        ResponseEntity<T> response = null;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        try {
            startTime = System.currentTimeMillis();
            response = new RestTemplate().exchange(new URI(url), httpMethod, httpEntity, responseType);
            endTime = System.currentTimeMillis();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            httpStatus = (HttpStatus) e.getStatusCode();
            throw new ExternalCallException(httpStatus, ErrorCode.EXTERNAL_CALL_FAILED, e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ExternalCallException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EXTERNAL_CALL_FAILED, e.getMessage());
        } finally {
            int statusCode = nonNull(response) ? response.getStatusCode().value() : httpStatus.value();
            logExecutionTime(httpMethod, url, startTime, endTime, statusCode);
        }
        return response.getBody();
    }

    private void logExecutionTime(HttpMethod httpMethod, String url, long startTime, long endTime, int statusCode) {
        log.info("Time taken to execute [{}]: {} is {} ms with status code: {}",
                httpMethod, url, endTime - startTime, statusCode);
    }

    private HttpEntity<?> prepareHTTPEntity(HttpMethod httpMethod, Serializable data, Map<String, String> headers) {
        if (HttpMethod.GET == httpMethod || HttpMethod.DELETE == httpMethod || isNull(data)) {
            return new HttpEntity<>(getHeaders(headers));
        }
        return new HttpEntity<>(data, getHeaders(headers));
    }

    private HttpHeaders getHeaders(Map<String, String> headerParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (MapUtils.isNotEmpty(headerParams)) {
            headerParams.forEach(headers::add);
        }
        return headers;
    }

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
