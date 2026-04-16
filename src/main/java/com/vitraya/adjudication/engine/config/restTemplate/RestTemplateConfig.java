package com.vitraya.adjudication.engine.config.restTemplate;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.config.RequestConfig;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${rest.template.connection.request.timeout.second}")
    private int connectionRequestTimeout;

    @Value("${rest.template.connection.response.timeout.minute}")
    private int connectionResponseTimeout;

    @Value("${rest.template.connection.connect.timeout.second}")
    private int connectionConnectTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .requestFactory(this::httpComponentsClientHttpRequestFactory)
                .setConnectTimeout(Duration.ofSeconds(5)) // Connection timeout
//                 .setReadTimeout(Duration.ofSeconds(600))  // Read timeout
                .setConnectTimeout(Duration.ofSeconds(connectionConnectTimeout))
                .build();
    }

    private HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(connectionConnectTimeout))
                .setConnectionRequestTimeout(Timeout.ofSeconds(connectionRequestTimeout))
                .setResponseTimeout(Timeout.ofMinutes(connectionResponseTimeout))
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager())
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private PoolingHttpClientConnectionManager poolingConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200); // Maximum total connections
        connectionManager.setDefaultMaxPerRoute(50); // Maximum connections per route
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(2)); // Validate connection after 2 seconds of inactivity
        return connectionManager;
    }
}