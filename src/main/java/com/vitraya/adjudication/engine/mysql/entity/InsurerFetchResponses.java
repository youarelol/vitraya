package com.vitraya.adjudication.engine.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Table("insurer_fetch_responses")
public class InsurerFetchResponses {
    @Id
    private long id;

    @JsonIgnore
    @Column("claim_data_id")
    private long claimDataId;

    @Column("claim_intimation_number")
    private String claimIntimationNumber;

    @Column("policy_data")
    private String policyData;

    @Column("claim_history")
    private String claimHistory;

    @CreationTimestamp
    @Column("date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column("date_updated")
    private Date dateUpdated;
}
