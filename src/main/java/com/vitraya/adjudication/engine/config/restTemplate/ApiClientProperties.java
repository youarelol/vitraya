package com.vitraya.adjudication.engine.config.restTemplate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "api.client")
public class ApiClientProperties {
    private int connectionTimeout = 1; // Default 1 minute
    private int readTimeout = 360000;     // Default 10 seconds
    private int maxAttempts = 3; // Default 3 retries
    private long backoffDelay = 5000; // Default 1 second backoff
}
