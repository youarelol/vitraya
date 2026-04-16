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

@Table("bill_category_savings_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillCategorySavingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id")
    private Long id;

    @Column("bill_category_saving_id")
    private Long billCatgorySavingId;

    @Column("bill_module_id")
    private Long billModuleId;

    @Column("category_name")
    private String categoryName;

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

    @Column("status")
    private String status;

    @Column("rerun_count")
    private Integer rerunCount = 0;

    @Column("change_code")
    private String changeCode;

    @Column("remark")
    private String remark;

    @Column("insurer_remark")
    private String insurerRemark;

    @Column("internal_remark")
    private String internalRemark;


    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;


    @Column("created_by")
    private String created_by;

    @Column("updated_by")
    private String updated_by;


    public static BillCategorySavingAudit from(BillCategorySaving source) {
        return BillCategorySavingAudit.builder()
                .categoryName(source.getCategoryName())
                .billCatgorySavingId(source.getId()) // set original ID to backup field
                .billModuleId(source.getBillModuleId())
                .categoryName(source.getCategoryName())
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
                .status(source.getStatus())
                .rerunCount(source.getRerunCount())
                .changeCode(source.getChangeCode())
                .remark(source.getRemark())
                .insurerRemark(source.getInsurerRemark())
                .internalRemark(source.getInternalRemark())
                .dateCreated(source.getDateCreated())
                .dateUpdated(source.getDateUpdated())
                .created_by(source.getCreated_by())
                .updated_by(source.getUpdated_by())
                .build();
    }

}
