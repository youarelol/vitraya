package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.mongodb.entity.RequestLogDocument;
import com.vitraya.adjudication.engine.mongodb.repository.RequestLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequestLogService {
    private final RequestLogRepository requestLogRepository;  // MySQL Repository

    public RequestLogService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    public void saveLog(String method, String url, String requestBody, String responseBody, Long responseTime,
                        String sourceIp, String exception) {
        // Save to MongoDB
        log.info("Inside saving log information for url {}", url);
        RequestLogDocument requestLogDocument = new RequestLogDocument();
        requestLogDocument.setMethod(method);
        requestLogDocument.setUrl(url);
        requestLogDocument.setRequest(requestBody);
        requestLogDocument.setResponse(responseBody);
        requestLogDocument.setResponseTime(responseTime);
        requestLogDocument.setSourceIp(sourceIp);
        requestLogDocument.setSourceIp(sourceIp);
        requestLogDocument.setException(exception);

        requestLogRepository.save(requestLogDocument);
    }
}
