package com.vitraya.adjudication.engine.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillItemResultDto {
    private int row_id;
    private String master_category;
    private String bill_item_name;
    private BigDecimal actual_amount;
    private BigDecimal amount_after_mou_discount;
    private BigDecimal amount_after_room_proportional_discount;
    private BigDecimal limit;
    private BigDecimal admissible_amount;
    private BigDecimal final_amount;
    private BigDecimal amountBeforeCopay;
    private boolean irdaiPayable;
    private boolean procedureConstructPayable;
    private String remarks;
    private boolean isUpdated;
    private BigDecimal final_amount_after_drop;
    private boolean si_sublimit_drop;
    private BigDecimal requested_amount;
    private int requested_units;
    private boolean isProcedureConstructPayable;
    private BigDecimal mou_discount;
    private BigDecimal mou_discount_applied;
    private BigDecimal copay;
    private BigDecimal copay_applied;
    private BigDecimal room_proportional_discount;
    private BigDecimal room_proportional_discount_applied;
}
