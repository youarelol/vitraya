package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Data
public class BenefitDataDto {
    private List<BillItemResultDto> billItemResultDto;
    private BenefitResult benefitResult;
    private BillTariffPmlAmountData billTariffPmlAmountData;

    public BenefitDataDto(BenefitResult benefitResult) {
        this.benefitResult = benefitResult;
    }

    public void addBillItemResultData(BillItemResultDto billItemResultDto) {
        if (this.billItemResultDto == null || this.billItemResultDto.isEmpty()) {
            this.billItemResultDto = new ArrayList<>();
        }
        this.billItemResultDto.add(BillItemResultDto.builder()
                .row_id(billItemResultDto.getRow_id())
                .master_category(billItemResultDto.getMaster_category())
                .bill_item_name(billItemResultDto.getBill_item_name())
                .actual_amount(roundUp(billItemResultDto.getActual_amount()))
                .mou_discount(roundUp(billItemResultDto.getMou_discount()))
                .room_proportional_discount(billItemResultDto.getRoom_proportional_discount())
                .amount_after_mou_discount(roundUp(billItemResultDto.getAmount_after_mou_discount()))
                .amount_after_room_proportional_discount(roundUp(billItemResultDto.getAmount_after_room_proportional_discount()))
                .limit(roundUp(billItemResultDto.getLimit()))
                .admissible_amount(roundUp(billItemResultDto.getAdmissible_amount()))
                .copay(roundUp(billItemResultDto.getCopay()))
                .final_amount(roundUp(billItemResultDto.getFinal_amount()))
                .amountBeforeCopay(roundUp(billItemResultDto.getAdmissible_amount()))
                .irdaiPayable(billItemResultDto.isIrdaiPayable())
                .procedureConstructPayable(billItemResultDto.isProcedureConstructPayable())
                .remarks(billItemResultDto.getRemarks())
                .final_amount_after_drop(billItemResultDto.getFinal_amount_after_drop())
                .si_sublimit_drop(billItemResultDto.isSi_sublimit_drop())
                .build());
    }

    public void addBillTariffPmlAmountData(BillItemResultDto billItemResultDto, BenefitResult benefitResult) {
        if (billTariffPmlAmountData == null) {
            billTariffPmlAmountData = new BillTariffPmlAmountData();
        }
        billTariffPmlAmountData.setTariffAmount(addValue(billTariffPmlAmountData.getTariffAmount(), billItemResultDto.getActual_amount()));
        billTariffPmlAmountData.setMouApplicable(findMax(billTariffPmlAmountData.getMouApplicable(), billItemResultDto.getMou_discount()));
        billTariffPmlAmountData.setAmountAfterMou(addValue(billTariffPmlAmountData.getAmountAfterMou(), billItemResultDto.getAmount_after_mou_discount()));
        billTariffPmlAmountData.setRoomProportionalDiscount(findMax(billTariffPmlAmountData.getRoomProportionalDiscount(),
                billItemResultDto.getRoom_proportional_discount()));
        billTariffPmlAmountData.setAmountAfterRoomProportionalDiscount(addValue(
                billTariffPmlAmountData.getAmountAfterRoomProportionalDiscount(), billItemResultDto.getAmount_after_room_proportional_discount()));
        billTariffPmlAmountData.setFinalAmount(roundUp(benefitResult.getBenefit_group_admissible_amount()));
        billTariffPmlAmountData.setLimit(roundUp(benefitResult.getLimitValue()));
        billTariffPmlAmountData.setCopay(roundUp(benefitResult.getCopay()));
        billTariffPmlAmountData.setAmountBeforeCopay(roundUp(benefitResult.getAmount_before_copay()));
        billTariffPmlAmountData.setAmountAfterDrop(addValue(billTariffPmlAmountData.getAmountAfterDrop(), billItemResultDto.getFinal_amount_after_drop()));
    }

    private BigDecimal getCopay(BigDecimal finalAmount, BigDecimal copayPercentage) {
        BigDecimal copayAmount = BigDecimal.ZERO, admissibleAmount = BigDecimal.ZERO;
        if (finalAmount != null && copayPercentage != null
                && finalAmount.compareTo(BigDecimal.ZERO) > 0
                && copayPercentage.compareTo(BigDecimal.ZERO) > 0) {
            admissibleAmount = finalAmount;
            copayAmount = admissibleAmount.multiply(copayPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return admissibleAmount.subtract(copayAmount);
    }

    public static BigDecimal addValue(BigDecimal base, BigDecimal toBeAdded) {
        toBeAdded = toBeAdded == null ? BigDecimal.ZERO : toBeAdded;
        return roundUp(base == null ? toBeAdded : base.add(toBeAdded));
    }

    public static BigDecimal subtract(BigDecimal bigValue, BigDecimal smallValue) {
        smallValue = smallValue == null ? BigDecimal.ZERO : smallValue;
        return roundUp(bigValue == null ? (BigDecimal.ZERO.subtract(smallValue)) : bigValue.subtract(smallValue));
    }

    public static BigDecimal multiply(BigDecimal first, BigDecimal second) {
        second = second == null ? BigDecimal.ZERO : second;
        return roundUp(first == null ? (BigDecimal.ZERO.multiply(second)) : first.multiply(second));
    }

    public static BigDecimal getPercent(BigDecimal numerator, BigDecimal denominator) {
        return (numerator == null || denominator == null
                || numerator.compareTo(BigDecimal.ZERO) == 0 || denominator.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO : (numerator.multiply(BigDecimal.valueOf(100))).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal findMax(BigDecimal base, BigDecimal value) {
        value = value == null ? BigDecimal.ZERO : value;
        return roundUp((base == null) ? value : base.max(value));
    }

    public static BigDecimal roundUp(BigDecimal value) {
        return (value == null) ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundUp(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal getAmtByConcession(BigDecimal value, boolean isConcession) {
        return isConcession ? BigDecimal.ZERO : value;
    }

    public static BigDecimal getNumber(String value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public static boolean isNotNull(BigDecimal value) {
        return value != null;
    }
}
