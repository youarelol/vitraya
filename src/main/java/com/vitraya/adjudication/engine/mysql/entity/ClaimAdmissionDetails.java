package com.vitraya.adjudication.engine.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "claim_admission_details")
@Builder
public class ClaimAdmissionDetails {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("admission_date")
    private Date admissionDate;

    @Column("discharge_date")
    private Date dischargeDate;

    @JsonIgnore
    @Column("cost_estimation")
    private String costEstimation;

    @Column("is_package")
    private boolean isPackage;

    @Column("package_amount")
    private BigDecimal packageAmount;

    @Column("txn_id")
    private String txnId;

    @Column("date_created")
    @JsonIgnore
    private Date dateCreated;

    @Column("date_updated")
    @JsonIgnore
    private Date dateUpdated;
}
