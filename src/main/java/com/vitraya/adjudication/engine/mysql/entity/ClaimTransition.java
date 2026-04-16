package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "claim_transition")
public class ClaimTransition {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("status")
    private String status;

    @Column("txn_id")
    private String txnId;

    @Column("date_created")
    private Date dateCreated;
}
