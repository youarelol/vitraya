package com.vitraya.adjudication.engine.mongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document(collection = "request_logs")
@Getter
@Setter
public class RequestLogDocument {
    @Id
    private String id;

    private String method;
    private String url;
    private String request;
    private String response;
    private Long responseTime;
    private String sourceIp;
    private Date createdAt;
    private String exception;

    public RequestLogDocument() {
        this.createdAt = new Date();
    }


}
