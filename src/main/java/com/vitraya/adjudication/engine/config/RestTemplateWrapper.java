package com.vitraya.adjudication.engine.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateWrapper {
    private static final Integer restTemplate_ConnectionRequestTimeout=10 * 1000; // 10 sec

    private static final Integer restTemplate_SocketTimeout=30 * 60000; // 15 min

    private static final Integer restTemplate_ConnectionTimeout= 10 * 1000; // 10 sec
    private static final Integer maxTotal=100;
    private static final Integer defaultMaxPerRoute=20;

    static RestTemplate restTemplate  ;

    private static final RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
    private static Integer flag=0;


    public static RestTemplate getRestTemplate() {

        if (flag == 0) {
            flag++;
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(maxTotal);
            connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setConnectionRequestTimeout(Timeout.ofSeconds(restTemplate_ConnectionRequestTimeout)) // timeout to get connection from pool
                    .setConnectionRequestTimeout(Timeout.ofSeconds(restTemplate_SocketTimeout)) // standard connection timeout
                    .build();

            HttpClient httpClient = HttpClientBuilder.create()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig).build();

            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

            RestTemplate resttemplate = restTemplateBuilder.build();
            resttemplate.setRequestFactory(requestFactory);
            restTemplate = resttemplate;
            return restTemplate;

        }
        else{
            return restTemplate;
        }
    }
}
