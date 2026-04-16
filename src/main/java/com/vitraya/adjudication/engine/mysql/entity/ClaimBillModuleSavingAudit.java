package com.vitraya.adjudication.engine.mysql.entity;

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

@Table("bill_module_saving_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimBillModuleSavingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id")
    private long id;

    @Column("bill_module_saving_id")
    private long billModuleSavingId;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("is_package_claim")
    private boolean isPackageClaim;

    @Column("no_of_line_items")
    private int noOfLineItems;

    @Column("requested_amount")
    private BigDecimal requestedAmount;

    @Column("insurer_requested_amount")
    private BigDecimal insurerRequestedAmount;

    @Column("approved_amount")
    private BigDecimal approvedAmount;

    @Column("insurer_approved_amount")
    private BigDecimal insurerApprovedAmount;

    @Column("tariff_savings")
    private BigDecimal tariffSavings;

    @Column("non_payable_savings")
    private BigDecimal nonPayableSavings;

    @Column("construct_savings")
    private BigDecimal constructSavings;

    @Column("pharmacy_savings")
    private BigDecimal pharmacySavings;

    @Column("vneuron_savings")
    private BigDecimal vneuronSavings;

    @Column("insurer_tariff_savings")
    private BigDecimal insurerTariffSavings;

    @Column("insurer_non_payable_savings")
    private BigDecimal insurerNonPayableSavings;

    @Column("insurer_construct_savings")
    private BigDecimal insurerConstructSavings;

    @Column("insurer_pharmacy_savings")
    private BigDecimal insurerPharmacySavings;

    @Column("insurer_vneuron_savings")
    private BigDecimal insurerVneuronSavings;

    @Column("mou_savings")
    private BigDecimal mouSavings;

    @Column("sublimit_savings")
    private BigDecimal sublimitSavings;

    @Column("copay_savings")
    private BigDecimal copaySavings;

    @Column("proportional_discount")
    private BigDecimal proportionalDiscount;

    @Column("stage")
    private String stage;

    @Column("pml_review_reason")
    private String pmlReviewReason;

    @Column("vneuron_review_reason")
    private String vneuronReviewReason;

    @Column("is_insurer_edit")
    private boolean isInsurerEdit;

    @Column("insurer_remark")
    private String insurerRemark;

    @Column("parsed_percentage")
    private BigDecimal parsedPercentage;

    @Column("rerun_count")
    private int rerunCount = 0;

    @Column("change_code")
    private String changeCode;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

    @Column("created_by")
    private String created_by;

    @Column("updated_by")
    private String updated_by;

    public static ClaimBillModuleSavingAudit from(ClaimBillModuleSaving source) {
        return ClaimBillModuleSavingAudit.builder()
                .billModuleSavingId(source.getId())
                .claimDataId(source.getClaimDataId())
                .isPackageClaim(source.getIsPackageClaim() != null && source.getIsPackageClaim())
                .noOfLineItems(source.getNoOfLineItems())
                .requestedAmount(source.getRequestedAmount())
                .insurerRequestedAmount(source.getInsurerRequestedAmount())
                .approvedAmount(source.getApprovedAmount())
                .insurerApprovedAmount(source.getInsurerApprovedAmount())
                .tariffSavings(source.getTariffSavings())
                .nonPayableSavings(source.getNonPayableSavings())
                .constructSavings(source.getConstructSavings())
                .pharmacySavings(source.getPharmacySavings())
                .vneuronSavings(source.getVneuronSavings())
                .insurerTariffSavings(source.getInsurerTariffSavings())
                .insurerNonPayableSavings(source.getInsurerNonPayableSavings())
                .insurerConstructSavings(source.getInsurerConstructSavings())
                .insurerPharmacySavings(source.getInsurerPharmacySavings())
                .insurerVneuronSavings(source.getInsurerVneuronSavings())
                .mouSavings(source.getMouSavings())
                .sublimitSavings(source.getSublimitSavings())
                .copaySavings(source.getCopaySavings())
                .proportionalDiscount(source.getProportionalDiscount())
                .stage(source.getStage())
                .pmlReviewReason(source.getPmlReviewReason())
                .vneuronReviewReason(source.getVneuronReviewReason())
                .isInsurerEdit(source.isInsurerEdit())
                .insurerRemark(source.getInsurerRemark())
                .parsedPercentage(source.getParsedPercentage())
                .rerunCount(source.getRerunCount())
                .changeCode(source.getChangeCode())
                .dateCreated(source.getDateCreated())
                .dateUpdated(source.getDateUpdated())
                .created_by(source.getCreated_by())
                .updated_by(source.getUpdated_by())
                .build();
    }
}
