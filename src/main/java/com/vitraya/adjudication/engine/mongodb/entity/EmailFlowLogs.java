package com.vitraya.adjudication.engine.mongodb.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "email_flow_logs")
@Getter
@Setter
public class EmailFlowLogs {
    @Id
    private String id;

    private String intiationNumber;
    private String preauthId;
    private String subject;
    private String emailBody;
    private String claimFlowType;
    private String filesSent;
    private boolean isEmailSent;
    private String emailSentTime;

}
