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

@Table("bill_line_item_saving_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillLineItemSavingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id")
    private Long id;

    @Column("bill_line_item_id")
    private Long billLineItemId;

    @Column("bill_module_id")
    private Long billModuleId;

    @Column("category_name")
    private String categoryName;

    @Column("category_id")
    private Long categoryId;

    @Column("row_id")
    private Integer rowId;

    @Column("line_item")
    private String lineItem;

    @Column("unit")
    private int unit;

    @Column("insurer_unit")
    private int insurerUnit;

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

    @Column("irdai_payable")
    private boolean irdaiPayable;

    @Column("insurer_irdai_payable")
    private boolean insurerIrdaiPayable;

    @Column("deleted")
    private Boolean deleted = false;

    @Column("added")
    private Boolean added = false;

    @Column("edited")
    private Boolean edited = false;

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

    public static BillLineItemSavingAudit from(BillLineItemSaving source) {
        return BillLineItemSavingAudit.builder()
                .billLineItemId(source.getId())
                .billModuleId(source.getBillModuleId())
                .categoryName(source.getCategoryName())
                .categoryId(source.getCategoryId())
                .rowId(source.getRowId())
                .lineItem(source.getLineItem())
                .unit(source.getUnit())
                .insurerUnit(source.getInsurerUnit())
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
                .irdaiPayable(source.isIrdaiPayable())
                .insurerIrdaiPayable(source.isInsurerIrdaiPayable())
                .deleted(source.isDeleted())
                .added(source.isAdded())
                .edited(source.isEdited())
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
