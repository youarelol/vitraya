package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Tariff {
    private String remarks;
    private int row_id;
    private int confidence;
    private boolean irdai_payable;
    private BigDecimal tariff_amount;
    private boolean tariff_matched;
    private String matched_document;
    private String tariff_line_item;
    private BigDecimal admissible_amount;
    private String construct_remarks;
    private boolean included_in_package;
    private boolean included_in_procedure;
    private String construct_matched_item;
    private boolean percentage_adjudicated;
    private BigDecimal tariff_per_unit_amount;
    private String procedure_construct_payable;
    private int procedure_construct_savings;
    private BigDecimal allowed_quantity_by_construct;
    private BigDecimal allowed_units_by_proc_construct;
    private boolean procedure_construct_payable_boolean;
    private BigDecimal admissible_amount_without_procedure_construct;
    private TariffMatchedItemCoordinates tariff_matched_item_coordinates;
    private boolean deleted;
    private BigDecimal insurer_amount;
    private int insurer_unit;
    private boolean cost_depends_on_room_type;
    private String mapped_hospital_room_type;
    private LineItemCoordinates line_item_coordinates;
    private BillLineItemCoordinates bill_line_item_coordinates;
    private String page_number;
    private boolean edited;
    private boolean added;
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

    @Data
    public static class LineItemCoordinates {
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal left;
        private BigDecimal top;
    }

    @Data
    public static class BillLineItemCoordinates {
        private BigDecimal top;
        private BigDecimal bottom;
        private BigDecimal left;
        private BigDecimal right;
        private int page_number;
    }
}