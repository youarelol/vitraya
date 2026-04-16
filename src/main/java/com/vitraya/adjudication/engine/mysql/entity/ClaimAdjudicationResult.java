package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.AdjudicationStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "claim_adjudication_result")
public class ClaimAdjudicationResult {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("claim_decision")
    private String claimDecision;

    @Column("claim_stage")
    private String claimStage;

    @Column("remarks")
    private String remarks;

    @Column("pre_auth_bill_amount")
    private BigDecimal preAuthBillAmount;

    @Column("pre_auth_identifier")
    private String preAuthIdentifier;

    @Column("pre_auth_amount_approved")
    private BigDecimal preAuthAmountApproved;

    @Column("pre_auth_decision")
    private String preAuthDecision;

    @Column("pre_auth_insurer_decision")
    private String preAuthInsurerDecision;

    @Column("pre_auth_insurer_amount_approved")
    private BigDecimal preAuthInsurerAmountApproved;

    @Column("pre_auth_savings")
    private BigDecimal preAuthSavings;

    @Column("discharge_bill_amount")
    private BigDecimal dischargeBillAmount;

    @Column("discharge_amount_approved")
    private BigDecimal dischargeAmountApproved;

    @Column("discharge_claim_decision")
    private String dischargeClaimDecision;

    @Column("discharge_savings")
    private BigDecimal dischargeSavings;

    @Column("discharge_insurer_decision")
    private String dischargeInsurerDecision;

    @Column("insurer_discharge_amount_approved")
    private BigDecimal insurerDischargeAmountApproved;

    @Column("final_insurer_decision_reverse_feed")
    private String finalInsurerDecisionReversefeed;

    @Column("final_insurer_amount_reverse_feed")
    private BigDecimal finalInsurerAmountReversefeed;

    @Column("query_text")
    private String queryText;

    @Column("txn_id")
    private String txnId;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

    public boolean isClaimRejected(boolean isPreAuth, boolean isDischarge) {
        if (isPreAuth) {
            if (this.getPreAuthInsurerDecision() != null) {
                return this.getPreAuthInsurerDecision().equalsIgnoreCase(AdjudicationStatus.REJECTED.name());
            } else if (this.getPreAuthDecision() != null) {
                return this.getPreAuthDecision().equalsIgnoreCase(AdjudicationStatus.REJECTED.name());
            }
        } else if (isDischarge) {
            if (this.getDischargeInsurerDecision() != null) {
                return this.getDischargeInsurerDecision().equalsIgnoreCase(AdjudicationStatus.REJECTED.name());
            } else if (this.getDischargeClaimDecision() != null) {
                return this.getDischargeClaimDecision().equalsIgnoreCase(AdjudicationStatus.REJECTED.name());
            }
        }

        return false;
    }
}
