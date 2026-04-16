package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table("niva_request_data")
public class NivaRequestData {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("request_type")
    private String requestType;

    @Column("wdms_unique_no")
    private String wdmsUniqueNo;

    @Column("request_data")
    private String requestData;

    @Column("is_success")
    private boolean isSuccess;

    @Column("error_msg")
    private String errorMsg;

    @Column("create_date")
    private Date dateCreated;
}
