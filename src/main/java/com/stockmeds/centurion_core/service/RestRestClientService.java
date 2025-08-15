package com.stockmeds.centurion_core.service;

import com.stockmeds.centurion_core.record.ExternalHttpRequest;

public interface RestRestClientService {
    <T> T execute(ExternalHttpRequest requestDetails, Class<T> responseType);
}
