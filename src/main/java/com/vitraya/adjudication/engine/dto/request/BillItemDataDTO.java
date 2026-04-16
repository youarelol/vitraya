package com.vitraya.adjudication.engine.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillItemDataDTO {
    private int row_id;
    private String bill_item;
    private BigDecimal bill_rate;
    private BigDecimal requested_amount;
    private BigDecimal tariff_amount_actual_room;
    private String cost_depends_on_room_type;
    private double allowed_units_by_proc_construct;
    private int requested_units;
    @JsonIgnore
    private String mapped_hospital_room_type;
    private BigDecimal tariff_rate_actual_room;
    private boolean isIrdaiPayable;
    private boolean isProcedureConstructPayable;
}
