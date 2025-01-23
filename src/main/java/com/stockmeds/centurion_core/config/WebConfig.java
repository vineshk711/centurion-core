package com.stockmeds.centurion_core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CenturionInterceptor centurionInterceptor;

    public WebConfig(CenturionInterceptor centurionInterceptor) {
        this.centurionInterceptor = centurionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(centurionInterceptor).addPathPatterns("/**").order(0);
    }

}