package com.vitraya.adjudication.engine.dto.response;

import com.vitraya.adjudication.engine.dto.enums.PMLStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Data
public class BillTariffPmlDto {
    private String bill_tariff_status;
    private String pml_status;
    private BigDecimal total_tariff_admissible_amount;
    private BigDecimal total_pml_admissible_amount;
    private BigDecimal total_deduction;
    private BigDecimal total_bill_amount;
    private BigDecimal nmeTariffDeductionAmount;
    private BigDecimal irdaiNonPayableDeductionAmount;
    private List<BillTariffPmlAmountData> billTariffPmlAmountList;
    private boolean roomProportionalDiscountNonZero;
    private BigDecimal claim_approved_amount_after_ucr_application;

    public BillTariffPmlDto() {
        this.total_tariff_admissible_amount = BigDecimal.ZERO;
        this.total_pml_admissible_amount = BigDecimal.ZERO;
        this.total_deduction = BigDecimal.ZERO;
        this.total_bill_amount = BigDecimal.ZERO;
    }

    public static BigDecimal roundOff(BigDecimal value) {
        return (value == null) ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String roundOffStr(BigDecimal value) {
        return (value == null) ? "0" : value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static String getPercent(BigDecimal numerator, BigDecimal denominator) {
        return (numerator == null || denominator == null
                || numerator.compareTo(BigDecimal.ZERO) == 0 || denominator.compareTo(BigDecimal.ZERO) == 0)
                ? "-" : (numerator.multiply(BigDecimal.valueOf(100))).divide(denominator, 2, RoundingMode.HALF_UP).toString();
    }

    public static double getPercentage(BigDecimal numerator, BigDecimal denominator) {
        return (numerator == null || denominator == null
                || numerator.compareTo(BigDecimal.ZERO) == 0 || denominator.compareTo(BigDecimal.ZERO) == 0)
                ? 0.0 : (numerator.multiply(BigDecimal.valueOf(100))).divide(denominator, 2, RoundingMode.HALF_UP).doubleValue();
    }

    public void addBillTariffPmlAmountList(CategorySummaryItem categorySummary, BenefitDataDto benefitDataDto) {
        if (this.billTariffPmlAmountList == null) {
            this.billTariffPmlAmountList = new ArrayList<>();
        }
        BillTariffPmlAmountData billTariffPmlAmountData = BillTariffPmlAmountData.prepareDto(categorySummary, benefitDataDto);
        this.billTariffPmlAmountList.add(billTariffPmlAmountData);
        if (billTariffPmlAmountData.getSavings() != null)
            this.total_deduction = this.total_deduction.add(roundOff(billTariffPmlAmountData.getSavings()));
        if (billTariffPmlAmountData.getBill_requested_amount() != null)
            this.total_bill_amount = this.total_bill_amount.add(roundOff(billTariffPmlAmountData.getBill_requested_amount()));
        if (billTariffPmlAmountData.getTariff_admissible_amount() != null)
            this.total_tariff_admissible_amount = this.total_tariff_admissible_amount.add(roundOff(billTariffPmlAmountData.getTariff_admissible_amount()));
        if (billTariffPmlAmountData.getBenefit_group_admissible_amount() != null) {
            this.total_pml_admissible_amount = this.total_pml_admissible_amount
                    .add(roundOff(billTariffPmlAmountData.getBenefit_group_admissible_amount()));
            this.pml_status = PMLStatusEnum.SUCCESSFUL.name();
        }
        if (billTariffPmlAmountData.getRoomProportionalDiscount() != null)
            this.roomProportionalDiscountNonZero = this.roomProportionalDiscountNonZero
                    || billTariffPmlAmountData.getRoomProportionalDiscount().compareTo(BigDecimal.ZERO) > 0;
    }
}
