package com.stockmeds.centurion_core.service.impl;

import com.stockmeds.centurion_core.dto.ExternalHttpRequest;
import com.stockmeds.centurion_core.enums.ErrorCode;

import com.stockmeds.centurion_core.exception.ExternalCallException;
import com.stockmeds.centurion_core.service.RestRestClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Map;


import static com.stockmeds.centurion_core.utils.UrlUtils.appendQueryParams;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class RestClientServiceImpl implements RestRestClientService {

    @Override
    public <T> T execute(ExternalHttpRequest request, Class<T> responseType) {
        var url = appendQueryParams(request.getUrl(), request.getUriParams());
        var httpMethod = request.getHttpMethod();
        log.debug("Calling endpoint [{}] : [{}]", httpMethod, url);

        var httpEntity = prepareHTTPEntity(httpMethod, request.getBody(), request.getHeaders());
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
        log.info("Time taken to execute external API call [{}]: {} is {} ms with status code: {}",
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
}
