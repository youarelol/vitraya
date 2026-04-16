package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Builder
@Table("error_message_logs")
public class ErrorMessageLogs {

    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("error_reason")
    private String errorReason;

    @Column("error_message")
    private String errorMessage;

    @Column("failure_engine")
    private String failureEngine;

    @Column("date_created")
    private Date date_created;

    @Column("claim_stage")
    private String claimStage;

}
