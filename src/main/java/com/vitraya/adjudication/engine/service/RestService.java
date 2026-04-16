package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import com.vitraya.adjudication.engine.config.RestTemplateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableRetry
@Slf4j
public class RestService {
    private final RestTemplate restTemplate;
    // private final RetryTemplate retryTemplate;

    public RestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public <T> T get(String url, Class<T> responseType, Map<String, String> headers, Map<String, String> queryParams) {
        return exchange(url, HttpMethod.GET, null, responseType, headers, queryParams);
    }

    public <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, String> headers) {
        log.info("Sending request to url {} and request method {}", url, requestBody);
        return exchange(url, HttpMethod.POST, requestBody, responseType, headers, null);
    }

    public ResponseEntity<ByteArrayResource> downloadFile(String url, String requestBody, Map<String, String> headers) {
        log.debug("Downloading file: Args {}, {}, {}", url, requestBody, headers);
        return exchangeForFile(url, requestBody, headers);
    }

    /*private <T> T executeWithRetry(java.util.function.Supplier<T> supplier) {

        return retryTemplate.execute(context -> {
            log.info("Response from supplier {}, {}", supplier.get(), context);
            log.info("Retry count: " + context.getRetryCount());
            return supplier.get();
        });
    }*/

    // ... (exchange and buildRequestEntity methods remain the same)

    private <T> T exchange(String url, HttpMethod method, Object requestBody, Class<T> responseType, Map<String, String> headers, Map<String, String> queryParams) {
        HttpEntity<?> requestEntity = buildRequestEntity(requestBody, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        URI uri = builder.build().toUri();

        ResponseEntity<T> response = restTemplate.exchange(uri, method, requestEntity, responseType);
        // log.info("Response Received for url {} as {} with response code {}", uri, response, response.getStatusCode());
        return response.getBody();
    }

    private ResponseEntity<ByteArrayResource> exchangeForFile(String url, Object requestBody, Map<String, String> headers) {
        HttpEntity<?> requestEntity = buildRequestEntity(requestBody, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        URI uri = builder.build().toUri();

        return restTemplate.exchange(uri, HttpMethod.GET, requestEntity, ByteArrayResource.class);
    }

    private HttpEntity<?> buildRequestEntity(Object requestBody, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        if (requestBody != null) {
            return new HttpEntity<>(requestBody, httpHeaders);
        } else {
            return new HttpEntity<>(httpHeaders);
        }
    }


    public <T> T executePostWithFile(String url, HashMap<String, Object> requestBody, Class<T> responseType, Map<String, String> headers) {
        return exchangeWithFile(url, requestBody, HttpMethod.POST, headers, responseType);
    }

    /*public <T> T executePutWithFile(String url, Map<String, String> headers, Class<T> responseType) {
        return executeWithRetry(() -> exchangeWithFile(url, HttpMethod.PUT, headers, responseType));
    }*/

    private <T> T exchangeWithFile(String url, HashMap<String, Object> requestBody, HttpMethod method, Map<String, String> headers, Class<T> responseType) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA); // Important: Set content type

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (String key : requestBody.keySet()) {
            if (key.equalsIgnoreCase("documents")) {
                List<FileSystemResource> documentList;
                if (requestBody.get(key) instanceof FileSystemResource) {
                    // Now get the key value as a list of FileSystemResource
                    documentList = List.of((FileSystemResource) requestBody.get(key));
                } else {
                    documentList = (List<FileSystemResource>) requestBody.get(key);
                }

                for (FileSystemResource fileSystemResource : documentList) {
                    body.add(key, fileSystemResource);
                }
            } else {
                body.add(key, requestBody.get(key));
            }

        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

        log.info("Sending request to url {}, request method {} and requestEntity: {}", url, method, requestBody);
        ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
        // log.debug("Response received: {}", response);
        return response.getBody();
    }

    @SkipResponseLogging
    public byte[] fetchFileFromS3(String s3Url) {
        return restTemplate.getForObject(s3Url, byte[].class);
    }
}
