package com.vitraya.adjudication.engine.aspect;

import com.vitraya.adjudication.engine.service.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class RequestResponseLoggingAspect {
    private final RequestLogService requestLogService;
    private final HttpServletRequest httpServletRequest;

    public RequestResponseLoggingAspect(RequestLogService requestLogService, HttpServletRequest httpServletRequest) {
        this.requestLogService = requestLogService;
        this.httpServletRequest = httpServletRequest;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String uniqueId = java.util.UUID.randomUUID().toString(); // Unique identifier for the request

        String url = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();
        String sourceIp = httpServletRequest.getRemoteAddr();
        Map<String, String> requestParams = getRequestParameters();

        try {
            log.info("Request Received [{}]: Method={}, URL={}, Params={}", uniqueId, method, url, requestParams);

            // Proceed with method execution
            Object result = joinPoint.proceed();

            // Capture response details
            long responseTime = System.currentTimeMillis() - startTime;
            // log.info("Response Sent [{}]: Result={}, TimeTaken={}ms", uniqueId, result, responseTime);
            log.info("Response Sent [{}]: TimeTaken={}ms", uniqueId, responseTime);

            // Save log to MongoDB
            requestLogService.saveLog(method, url, requestParams.toString(),
                    (result != null ? result.toString() : "null"), responseTime, sourceIp, null);
            return result;
        } catch (Exception ex) {
            // Capture exception details
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Exception [{}]: Message={}, TimeTaken={}ms", uniqueId, ex.getMessage(), responseTime);

            // Save exception log to MongoDB
            requestLogService.saveLog(method, url, requestParams.toString(), null,
                    responseTime, sourceIp, ex.getMessage());
            throw ex;
        }
    }

    private Map<String, String> getRequestParameters() {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, httpServletRequest.getParameter(paramName));
        }
        return params;
    }
}
