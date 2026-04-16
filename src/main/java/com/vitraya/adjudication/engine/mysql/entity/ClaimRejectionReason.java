package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.ClaimType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "claim_rejection_reason")
public class ClaimRejectionReason {

    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("claim_stage")
    private String claimStage;

    @Column("claim_status")
    private String claimStatus;

    @Column("reason")
    private String reason;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

}
