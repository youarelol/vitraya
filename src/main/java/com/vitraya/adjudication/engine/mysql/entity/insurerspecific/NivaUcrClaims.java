package com.vitraya.adjudication.engine.mysql.entity.insurerspecific;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("niva_ucr_claims")
public class NivaUcrClaims {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("pml_approved_amount")
    private BigDecimal pmlApprovedAmount;

    @Column("ucr_amount")
    private BigDecimal ucrAmount;

    @Column("final_approved_amount")
    private BigDecimal finalApprovedAmount;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

}
