package com.vitraya.adjudication.engine.mongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "integration_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationLogDocument {

    @Id
    private String id;

    private Long pushId;
    private String identifier;
    private String entity;
    private String url;
    private String request;
    private String response;
    private Long timeTakenMs;
    private String notes;
    private Date dateCreated;
    private Date dateUpdated;

    public void markCreated() {
        Date now = new Date();
        this.dateCreated = now;
        this.dateUpdated = now;
    }

    public void markUpdated() {
        this.dateUpdated = new Date();
    }
}

