package com.vitraya.adjudication.engine.dto.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BenefitDTO {
    private String benefit_group;
    private String benefit_code;
    private BigDecimal benefit_group_claimed_amount;
    private List<BillItemDataDTO> bill_item_data;
}
