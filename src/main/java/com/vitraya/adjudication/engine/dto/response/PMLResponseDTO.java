package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PMLResponseDTO {
    private String product_code;
    private BigDecimal total_claim_amount;
    private String procedure_name;
    private List<BenefitResult> benefit_results;
    private ClaimResult claim_result;
    private List<BillItemResultDto> bill_item_result;
    private List<String> errors;
    private String message;

    public static List<BillItemResultDto> resetBillItemResult(List<BillItemResultDto> billItemResultList) {
        billItemResultList.forEach(billItemResultDto -> billItemResultDto.setUpdated(false));
        return billItemResultList;
    }
}
