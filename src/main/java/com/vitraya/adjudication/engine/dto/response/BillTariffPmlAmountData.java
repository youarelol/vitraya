package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillTariffPmlAmountData {
    private String category_name;
    private BigDecimal bill_requested_amount;
    private BigDecimal tariff_admissible_amount;
    private BigDecimal tariff_admissible_amount_without_procedure_construct;
    private BigDecimal benefit_group_admissible_amount;
    private BigDecimal benefit_group_claimed_amount;
    private boolean benefit_covered;
    private BigDecimal savings;
    private BigDecimal summarySavings;
    private List<BillItemResultDto> billItemList;
    private BigDecimal tariffAmount;
    private BigDecimal mouApplicable;
    private BigDecimal amountAfterMou;
    private BigDecimal roomProportionalDiscount;
    private BigDecimal amountAfterRoomProportionalDiscount;
    private BigDecimal limit;
    private BigDecimal amountBeforeCopay;
    private BigDecimal finalAmount;
    private BigDecimal amountAfterDrop;
    private String remarks;
    private BigDecimal mou;
    private BigDecimal mouAmountApplied;
    private BigDecimal copay;
    private BigDecimal copayAmountApplied;
    private BigDecimal proportionalDiscount;
    private BigDecimal proportionateDiscountApplied;
    private BigDecimal hospitalPayableDeduction;
    private BigDecimal patientPayableDeduction;
    private boolean updated;

    public static BillTariffPmlAmountData prepareDto(CategorySummaryItem categorySummary, BenefitDataDto benefitDataDto) {
        BillTariffPmlAmountData tariffPmlAmountData = null;
        if (categorySummary != null) {
            tariffPmlAmountData = new BillTariffPmlAmountData();
            tariffPmlAmountData.setCategory_name(categorySummary.getCategory_name());
            tariffPmlAmountData.setBill_requested_amount(BillTariffPmlDto.roundOff(categorySummary.getRequested_amount()));
            tariffPmlAmountData.setTariff_admissible_amount(BillTariffPmlDto.roundOff(categorySummary.getAdmissible_amount()));
            tariffPmlAmountData.setTariff_admissible_amount_without_procedure_construct(BillTariffPmlDto.roundOff(categorySummary.getAdmissible_amount_without_procedure_construct()));
            if (benefitDataDto != null) {
                BenefitResult benefitResult = benefitDataDto.getBenefitResult();
                if (benefitResult != null) {
                    BigDecimal benefitGroupAdmissibleAmount = benefitResult.getBenefit_group_admissible_amount();
                    tariffPmlAmountData.setBenefit_group_admissible_amount(BillTariffPmlDto.roundOff(benefitGroupAdmissibleAmount));
                    tariffPmlAmountData.setBenefit_group_claimed_amount(BillTariffPmlDto.roundOff(benefitResult.getBenefit_group_claimed_amount()));
                    tariffPmlAmountData.setBenefit_covered(benefitResult.isBenefit_covered());
                    BigDecimal savings = BigDecimal.ZERO;
                    if (categorySummary.getRequested_amount() != null && benefitGroupAdmissibleAmount != null) {
                        savings = categorySummary.getRequested_amount().subtract(benefitGroupAdmissibleAmount);
                    }
                    tariffPmlAmountData.setSavings(BillTariffPmlDto.roundOff(savings.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : savings));
                }
                tariffPmlAmountData.setBillItemList(benefitDataDto.getBillItemResultDto());
                BillTariffPmlAmountData billItemListCalculations = benefitDataDto.getBillTariffPmlAmountData();
                if (billItemListCalculations != null) {
                    tariffPmlAmountData.setTariffAmount(billItemListCalculations.getTariffAmount());
                    tariffPmlAmountData.setMouApplicable(billItemListCalculations.getMouApplicable());
                    tariffPmlAmountData.setAmountAfterMou(billItemListCalculations.getAmountAfterMou());
                    tariffPmlAmountData.setRoomProportionalDiscount(billItemListCalculations.getRoomProportionalDiscount());
                    tariffPmlAmountData.setAmountAfterRoomProportionalDiscount(billItemListCalculations.getAmountAfterRoomProportionalDiscount());
                    tariffPmlAmountData.setLimit(billItemListCalculations.getLimit());
                    tariffPmlAmountData.setCopay(billItemListCalculations.getCopay());
                    tariffPmlAmountData.setFinalAmount(billItemListCalculations.getFinalAmount());
                    tariffPmlAmountData.setAmountBeforeCopay(billItemListCalculations.getAmountBeforeCopay());
                    tariffPmlAmountData.setAmountAfterDrop(billItemListCalculations.getAmountAfterDrop());
                    tariffPmlAmountData.setRemarks(billItemListCalculations.getRemarks());
                }
            }
        }
        return tariffPmlAmountData;
    }

    public void setBillSavingsComponents() {
        if (this.billItemList != null) {
            BigDecimal mouSavings = BigDecimal.ZERO;
            BigDecimal mou = BigDecimal.ZERO;
            BigDecimal copaySavings = BigDecimal.ZERO;
            BigDecimal copay = BigDecimal.ZERO;
            BigDecimal proportionateSavings = BigDecimal.ZERO;
            BigDecimal proportionateDiscount = BigDecimal.ZERO;


            for (BillItemResultDto billItemResultDto : this.billItemList) {
                mou = billItemResultDto.getMou_discount() != null ? billItemResultDto.getMou_discount() : BigDecimal.ZERO;
                mouSavings = mouSavings.add(billItemResultDto.getMou_discount_applied() != null
                        ? billItemResultDto.getMou_discount_applied() : BigDecimal.ZERO);
                copay = billItemResultDto.getCopay() != null ? billItemResultDto.getCopay() : BigDecimal.ZERO;
                copaySavings = copaySavings.add(billItemResultDto.getCopay_applied() != null
                        ? billItemResultDto.getCopay_applied() : BigDecimal.ZERO);
                proportionateDiscount = billItemResultDto.getRoom_proportional_discount() != null
                        ? billItemResultDto.getRoom_proportional_discount() : BigDecimal.ZERO;
                proportionateSavings = proportionateSavings.add(billItemResultDto.getRoom_proportional_discount_applied() != null
                        ? billItemResultDto.getRoom_proportional_discount_applied() : BigDecimal.ZERO);
            }

            this.mou = mou;
            this.copay = copay;
            this.proportionalDiscount = proportionateDiscount;
            this.mouAmountApplied = mouSavings;
            this.copayAmountApplied = copaySavings;
            this.proportionateDiscountApplied = proportionateSavings;
        }
    }
}
