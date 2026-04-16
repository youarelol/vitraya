package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "claim_module_stats")
public class ClaimModuleStats {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("bill_identifier")
    private String billIdentifier;

    @Column("pml_identifier")
    private String pmlIdentifier;

    @Column("medical_identifier")
    private String medicalIdentifier;

    @Column("bill_tariff_tat")
    private int billTariffTat;

    @Column("pml_tat")
    private int pmlTat;

    @Column("medical_tat")
    private int medicalTat;

    @Column("claim_tat")
    private int claimTat;

    @Column("claim_start_time")
    private Date claimStartTime;

    @Column("claim_end_time")
    private Date claimEndTime;

    @Column("claim_stage")
    private String claimStage;

    @Column("txn_id")
    private String txnId;

    @CreationTimestamp
    @Column("date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column("date_updated")
    private Date dateUpdated;

    public boolean isClaimModuleProcessingDone() {
        return StringUtils.isNotEmpty(billIdentifier)
                && StringUtils.isNotEmpty(pmlIdentifier)
                && StringUtils.isNotEmpty(medicalIdentifier);
    }

    public boolean isUnderProcessing() {
        return !isClaimModuleProcessingDone();
    }
}
