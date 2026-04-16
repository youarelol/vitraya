package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.BillSavingStatus;
import com.vitraya.adjudication.engine.dto.response.LineItemData;
import com.vitraya.adjudication.engine.dto.response.LineItemsItem;
import com.vitraya.adjudication.engine.dto.response.Tariff;
import com.vitraya.adjudication.engine.dto.response.ValueConfidenceDTO;
import com.vitraya.adjudication.engine.service.HelperService;
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
import java.util.Optional;

@Table("bill_line_item_saving")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillLineItemSaving {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id")
    private long id;

    @Column("bill_module_id")
    private long billModuleId;

    @Column("category_id")
    private long categoryId;

    @Column("category_name")
    private String categoryName;

    @Column("row_id")
    private int rowId;

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
    private boolean deleted = false;

    @Column("added")
    private boolean added = false;

    @Column("edited")
    private boolean edited = false;

    @Column("status")
    private String status;

    @Column("rerun_count")
    private int rerunCount = 0;

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

    public BillLineItemSaving(LineItemsItem lineItem, int rerunCount, String changeCode, String userId, boolean isEditFlow) {
        this.categoryName = lineItem.getData().getVitraya_master_category().getValue();
        this.rowId = lineItem.getData().getRow_id();
        this.lineItem = lineItem.getData().getDescription().getValue();

        if (lineItem != null && lineItem.getData() != null && lineItem.getData().getAmount() != null && lineItem.getData().getAmount().getValue() != null) {
            this.requestedAmount = new BigDecimal(lineItem.getData().getAmount().getValue());
        } else {
            this.requestedAmount = BigDecimal.ZERO;
        }

        this.insurerRequestedAmount = lineItem.getData().getTariff().getInsurer_bill_amount();
        this.approvedAmount = lineItem.getData().getTariff().getAdmissible_amount();
        this.insurerApprovedAmount = lineItem.getData().getTariff().getInsurer_tariff_amount();
        this.setDefaultLineSavings();
        this.updateLineSavings(lineItem.getData(), isEditFlow);
        this.deleted = lineItem.getData().getTariff().isDeleted();
        this.added = lineItem.getData().getTariff().isAdded();
        this.edited = lineItem.getData().getTariff().isEdited();
        this.status = BillSavingStatus.ADDED.toString();
        this.rerunCount = rerunCount;
        this.unit = (int) Float.parseFloat(lineItem.getData().getQuantity().getValue());
        this.insurerUnit = lineItem.getData().getTariff().getInsurer_unit();
        this.irdaiPayable = lineItem.getData().getTariff().isIrdai_payable();
        this.insurerIrdaiPayable = lineItem.getData().getTariff().isInsurer_irdai_payable();
        this.changeCode = changeCode;
        this.remark = lineItem.getData().getTariff().getRemarks();
        this.insurerRemark = lineItem.getData().getTariff().getInsurer_remarks();
        if(isEditFlow){
            this.internalRemark = "Line Item Added";
        }
        this.dateCreated = new Date();
        this.created_by = userId;
    }

    public void updateLineItemDetails(LineItemData lineItemData, int rerunCount, String changeCode, String userId, boolean isEditFlow,String internalRemark) {
        Tariff tariff = lineItemData.getTariff();

        this.categoryName = lineItemData.getVitraya_master_category().getValue();
        if (lineItemData != null && lineItemData.getAmount() != null && lineItemData.getAmount().getValue() != null) {
            this.requestedAmount = new BigDecimal(lineItemData.getAmount().getValue());
        } else {
            this.requestedAmount = BigDecimal.ZERO;
        }

        this.insurerRequestedAmount = tariff.getInsurer_bill_amount();
        this.approvedAmount = tariff.getAdmissible_amount();
        this.insurerApprovedAmount = tariff.getInsurer_tariff_amount();
        this.setDefaultLineSavings();
        this.updateLineSavings(lineItemData, isEditFlow);
        this.deleted = tariff.isDeleted();
        this.added = tariff.isAdded();
        this.edited = tariff.isEdited();
        this.status = rerunCount == 0 ? BillSavingStatus.ADDED.toString() : BillSavingStatus.UPDATED.toString(); // This should be updated with the status of the line item
        this.rerunCount = rerunCount;
        this.unit = (int) Float.parseFloat(lineItemData.getQuantity().getValue());
        this.insurerUnit = tariff.getInsurer_unit();
        this.irdaiPayable = tariff.isIrdai_payable();
        this.insurerIrdaiPayable = tariff.isInsurer_irdai_payable();
        this.changeCode = changeCode;
        this.remark = tariff.getRemarks();
        this.insurerRemark = tariff.getInsurer_remarks();
        if(internalRemark!=null) this.internalRemark = internalRemark;
        this.dateUpdated = new Date();
        this.updated_by = userId;
    }
    private void updateLineSavings(LineItemData lineItemData, boolean isEditFlow) {
        if (BigDecimal.ZERO.compareTo(new BigDecimal(lineItemData.getAmount().getValue())) < 0) {
            Tariff tariff = lineItemData.getTariff();
            if (!tariff.isIrdai_payable()
                    || (isEditFlow && tariff.isIrdai_payable() != tariff.isInsurer_irdai_payable() && !tariff.isInsurer_irdai_payable())) {
                if (!tariff.isIrdai_payable()) {
                    this.nonPayableSavings = new BigDecimal(lineItemData.getAmount().getValue());
                }
                if (!tariff.isInsurer_irdai_payable()) {
                    this.insurerNonPayableSavings = tariff.getInsurer_bill_amount();
                }
            } else if (!tariff.isProcedure_construct_payable_boolean()) {
                this.constructSavings = new BigDecimal(tariff.getProcedure_construct_savings());
            } else if (lineItemData.getVitraya_master_category().getValue().trim()
                    .equalsIgnoreCase("Medicine & Consumables")) {
                this.pharmacySavings = new BigDecimal(lineItemData.getAmount().getValue())
                        .subtract(tariff.getAdmissible_amount());
            } else {
                this.tariffSavings = HelperService.safeSubtract(this.requestedAmount, this.approvedAmount);
                this.insurerTariffSavings = HelperService.safeSubtract(this.insurerRequestedAmount, this.insurerApprovedAmount);
            }
        }
    }

    private void setDefaultLineSavings() {
        this.nonPayableSavings = BigDecimal.ZERO;
        this.insurerNonPayableSavings = BigDecimal.ZERO;
        this.constructSavings = BigDecimal.ZERO;
        this.pharmacySavings = BigDecimal.ZERO;
        this.tariffSavings = BigDecimal.ZERO;
        this.insurerTariffSavings = BigDecimal.ZERO;
    }
}
