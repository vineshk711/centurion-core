package com.stockmeds.centurion_core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

import static com.stockmeds.centurion_core.constants.Constants.START_TIME;
import static org.springframework.http.HttpHeaders.COOKIE;


@Component
@Slf4j
public class CenturionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        Map<String, String > headers = extractRequestHeaderValues(request);
        log.info("Executing incoming request [{}]: {} with query params: {} and headers: {}", request.getMethod(),
                request.getRequestURI(), request.getQueryString(), headers);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long timeTaken = fetchRequestExecutionTime(request);
        log.info("Time taken to execute [{}]: {} is {} ms with status code: {}", request.getMethod(), request.getRequestURI(),
                timeTaken, response.getStatus());
    }

    private long fetchRequestExecutionTime(HttpServletRequest httpServletRequest) {
        return System.currentTimeMillis() - (Long) httpServletRequest.getAttribute(START_TIME);
    }

    private Map<String, String> extractRequestHeaderValues(HttpServletRequest httpServletRequest) {
        Map<String, String> headerMap = new HashMap<>();
        List<String> ignoredHeaders = List.of(COOKIE);
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        if (Objects.nonNull(headerNames)) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (ignoredHeaders.stream().noneMatch(headerName::equalsIgnoreCase)) {
                    headerMap.put(headerName, httpServletRequest.getHeader(headerName));
                }
            }
        }
        return headerMap;
    }

}
