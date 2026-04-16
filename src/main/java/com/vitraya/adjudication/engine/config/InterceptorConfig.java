package com.vitraya.adjudication.engine.config;

import com.vitraya.adjudication.engine.config.Interceptor.ExternalInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    ExternalInterceptor externalInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(externalInterceptor)
                .addPathPatterns("/api/v1/external/**", "/api/v1/claim", "/api/v1/bill", "/api/v1/claim/demo");
    }

}
