package com.centurion.centurion_core.service;

import com.centurion.centurion_core.dto.ExternalHttpRequest;

public interface GenericRestClientService {
    <T> T execute(ExternalHttpRequest requestDetails, Class<T> responseType);
}
