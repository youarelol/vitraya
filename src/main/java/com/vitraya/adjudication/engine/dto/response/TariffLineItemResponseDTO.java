package com.vitraya.adjudication.engine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TariffLineItemResponseDTO {
    private int row_id;
    private String bill_line_item;
    private String master_category;
    private String level2CategoryName;
    private double unit;
    private BigDecimal unit_amount;
    private double allowed_units_by_proc_construct;
    private BigDecimal total_bill_amount;
    private BigDecimal deduction;
    private BigDecimal tariff_rate;
    private BigDecimal tariff_rate_for_additional_room_type;
    private boolean cost_depends_on_room_type;
    private BigDecimal tariff_amount;
    private BigDecimal tariff_amount_actual;
    private BigDecimal admissible_amount;
    private String tariff_application_reference_text;
    private boolean irdai_payable;
    private boolean procedure_construct_payable;
    private String procedure_construct_payable_text;
    private String room_type;
    private String color;
    private BigDecimal savings;
    private BigDecimal savingsAfterModification;
    private BigDecimal admissible_amount_without_procedure_construct;
    private BigDecimal insurerAmount;
    private int insurer_unit;
    private String remarks;
    private boolean deleted;
    private boolean added;
    private boolean updated;
    private Coordinates coordinates;
    private String matched_document;
    private int confidence;
    private boolean tariff_matched;
    private String construct_remarks;
    private boolean included_in_package;
    private boolean included_in_procedure;
    private String construct_matched_item;
    private boolean percentage_adjudicated;
    private BigDecimal tariff_per_unit_amount;
    private int procedure_construct_savings;
    private BigDecimal allowed_quantity_by_construct;
    private boolean procedure_construct_payable_boolean;
    private BigDecimal insurer_amount;
    private String mapped_hospital_room_type;
    private Tariff.LineItemCoordinates line_item_coordinates;
    private Tariff.BillLineItemCoordinates bill_line_item_coordinates;
    private String page_number;
    private boolean insurer_irdai_payable;
    private boolean insurer_procedure_construct_payable;
    private double insurer_unit_actual;
    private BigDecimal insurer_unit_amount;
    private BigDecimal insurer_bill_amount;
    private BigDecimal insurer_tariff_rate;
    private BigDecimal insurer_tariff_amount;
    private BigDecimal insurer_savings;
    private String insurer_remarks;
    private BigDecimal insurer_amount_actual;

    public void setUnit(String unit) {
        this.unit = unit != null ? Double.parseDouble(unit) : 0;
    }

    public void setUnit_amount(String unit_amount) {
        this.unit_amount = unit_amount != null && !unit_amount.equalsIgnoreCase("null")
                ? BigDecimal.valueOf(Double.parseDouble(unit_amount)) : BigDecimal.ZERO;
    }

    public void setTotal_bill_amount(String total_bill_amount) {
        this.total_bill_amount = total_bill_amount != null && !total_bill_amount.equalsIgnoreCase("null")
                ? BigDecimal.valueOf(Double.parseDouble(total_bill_amount)) : BigDecimal.ZERO;
    }

    public static TariffLineItemResponseDTO populateLineItemInformation(LineItemsItem lineItem, String roomType) {
        TariffLineItemResponseDTO lineItemData = null;
        if (lineItem.getData() != null) {
            lineItemData = new TariffLineItemResponseDTO();
            lineItemData.setBill_line_item(lineItem.getData().getDescription() != null
                    && lineItem.getData().getDescription().getValue() != null
                    ? lineItem.getData().getDescription().getValue().trim() : null); // ToDo: Need to check why we are not receiving the value
            lineItemData.setMaster_category(lineItem.getData().getVitraya_master_category().getValue());
            // lineItemData.setLevel2CategoryName(lineItem.getData().getVitraya_master_category().getLevel2_category());
            lineItemData.setUnit(lineItem.getData().getQuantity() != null ? lineItem.getData().getQuantity().getValue() : "1"); // ToDo: Need to check why we are not receiving the value
            lineItemData.setTotal_bill_amount(lineItem.getData().getAmount() != null ? lineItem.getData().getAmount().getValue() : "0"); // ToDo: Need to check why we are not receiving the value
            BigDecimal unitRequested = new BigDecimal(Double.toString(lineItemData.getUnit()).equals("0.0") ? "1"
                    : Double.toString(lineItemData.getUnit()));
            if (unitRequested.equals(BigDecimal.ZERO)) {
                unitRequested = BigDecimal.ONE; // Default to 1 if unit is zero
            }
//            BigDecimal unitRequested = new BigDecimal(lineItemData.getUnit());
            BigDecimal totalBillAmount = lineItemData.getTotal_bill_amount();
            BigDecimal unitAmount = totalBillAmount.divide(unitRequested, RoundingMode.HALF_UP);
            lineItemData.setUnit_amount(unitAmount.toString());
            lineItemData.setRoom_type(roomType);
            lineItemData.setRow_id(lineItem.getData().getRow_id());

            if (lineItem.getData().getTariff() != null) {
                lineItemData.setMatched_document(lineItem.getData().getTariff().getMatched_document());
                lineItemData.setRow_id(lineItem.getData().getTariff().getRow_id());
                lineItemData.setAllowed_units_by_proc_construct(lineItem.getData().getTariff()
                        .getAllowed_units_by_proc_construct() != null ? lineItem.getData().getTariff()
                        .getAllowed_units_by_proc_construct().doubleValue() : 1); // ToDo: Need to check why we are not receiving the value
                lineItemData.setTariff_rate(lineItem.getData().getTariff().getTariff_per_unit_amount());
                lineItemData.setTariff_rate_for_additional_room_type(lineItem.getData().getTariff().getTariff_amount());
                lineItemData.setCost_depends_on_room_type(lineItem.getData().getTariff().isCost_depends_on_room_type());
                lineItemData.setTariff_amount(lineItem.getData().getTariff().getAdmissible_amount());
                lineItemData.setTariff_amount_actual(lineItem.getData().getTariff().getTariff_amount());
                lineItemData.setAdmissible_amount(lineItem.getData().getTariff().getAdmissible_amount());
                lineItemData.setTariff_application_reference_text(lineItem.getData().getTariff().getTariff_line_item());
                lineItemData.setIrdai_payable(lineItem.getData().getTariff().isIrdai_payable());
                lineItemData.setProcedure_construct_payable_text(lineItem.getData().getTariff().getProcedure_construct_payable());
                lineItemData.setProcedure_construct_payable(lineItem.getData().getTariff().isProcedure_construct_payable_boolean());
                lineItemData.setAdmissible_amount_without_procedure_construct(lineItem.getData().getTariff()
                        .getAdmissible_amount_without_procedure_construct());
                lineItemData.setRemarks(lineItem.getData().getTariff().getRemarks());
                if (lineItem.getData().getTariff().getInsurer_amount() == null) {
                    lineItem.getData().getTariff().setInsurer_amount(lineItem.getData().getTariff().getAdmissible_amount());
                }
                lineItemData.setInsurerAmount(lineItem.getData().getTariff().getInsurer_amount());
                lineItemData.setInsurer_unit(lineItem.getData().getTariff().getInsurer_unit());
                BigDecimal savings = lineItemData.getTotal_bill_amount().subtract(lineItemData.getTariff_amount() == null
                        ? BigDecimal.ZERO : lineItemData.getTariff_amount());
                BigDecimal savingsAfterModification = lineItemData.getTotal_bill_amount().subtract(lineItemData.getInsurer_amount() != null
                        ? lineItemData.getInsurer_amount() : BigDecimal.ZERO);
                lineItemData.setSavings(savings);
                lineItemData.setSavingsAfterModification(savingsAfterModification);
                lineItemData.setDeduction(savings);
                lineItemData.setDeleted(lineItem.getData().getTariff().isDeleted());
                lineItemData.setAdded(lineItem.getData().getTariff().isAdded());
                lineItemData.setUpdated(lineItem.getData().getTariff().isEdited());
                lineItemData.setTariff_matched(lineItem.getData().getTariff().isTariff_matched());
                lineItemData.setConstruct_remarks(lineItem.getData().getTariff().getConstruct_remarks());
                lineItemData.setIncluded_in_package(lineItem.getData().getTariff().isIncluded_in_package());
                lineItemData.setIncluded_in_procedure(lineItem.getData().getTariff().isIncluded_in_procedure());
                lineItemData.setConstruct_matched_item(lineItem.getData().getTariff().getConstruct_matched_item());
                lineItemData.setPercentage_adjudicated(lineItem.getData().getTariff().isPercentage_adjudicated());
                lineItemData.setTariff_per_unit_amount(lineItem.getData().getTariff().getTariff_per_unit_amount());
                lineItemData.setProcedure_construct_savings(lineItem.getData().getTariff().getProcedure_construct_savings());
                lineItemData.setAllowed_quantity_by_construct(lineItem.getData().getTariff().getAllowed_quantity_by_construct());
                lineItemData.setProcedure_construct_payable_boolean(lineItem.getData().getTariff().isProcedure_construct_payable_boolean());
                lineItemData.setInsurer_amount(lineItem.getData().getTariff().getInsurer_amount());
                lineItemData.setMapped_hospital_room_type(lineItem.getData().getTariff().getMapped_hospital_room_type());
                lineItemData.setLine_item_coordinates(lineItem.getData().getTariff().getLine_item_coordinates());
                lineItemData.setBill_line_item_coordinates(lineItem.getData().getTariff().getBill_line_item_coordinates());
                lineItemData.setPage_number(lineItem.getData().getTariff().getPage_number());
                lineItemData.setInsurer_irdai_payable(lineItem.getData().getTariff().isInsurer_irdai_payable());
                lineItemData.setInsurer_procedure_construct_payable(lineItem.getData().getTariff().isInsurer_procedure_construct_payable());
                lineItemData.setInsurer_unit_actual(lineItem.getData().getTariff().getInsurer_unit_actual());
                lineItemData.setInsurer_unit_amount(lineItem.getData().getTariff().getInsurer_unit_amount());
                lineItemData.setInsurer_bill_amount(lineItem.getData().getTariff().getInsurer_bill_amount());
                lineItemData.setInsurer_tariff_rate(lineItem.getData().getTariff().getInsurer_tariff_rate());
                lineItemData.setInsurer_tariff_amount(lineItem.getData().getTariff().getInsurer_tariff_amount());
                lineItemData.setInsurer_savings(lineItem.getData().getTariff().getInsurer_savings());
                lineItemData.setInsurer_remarks(lineItem.getData().getTariff().getInsurer_remarks());
            } else {
                BigDecimal savings = lineItemData.getTotal_bill_amount();
                lineItemData.setSavings(savings);
                lineItemData.setDeduction(savings);
                lineItemData.setProcedure_construct_payable_text("Tariff not Applied");
            }
        }
        return lineItemData;
    }
}
