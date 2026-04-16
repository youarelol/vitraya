package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
public class PolicyHolderDetailsDTO {
    private String PH_FName;
    private String PH_MName;
    private String PH_LName;
    private String Address;
    private String City;
    private String Dist;
    private String State;
    private String Pin_code;
    private String Inception_Date;
    private String Policy_Number;
    private String Policy_Ported;
    private String Policy_Start_Date;
    private String Policy_End_Date;
    private String Policy_Type;
    private String Policy_Type2;
    private String Product_Code;
    private String Product_Name;
    private String Reassure_Benefit_Amount;
    private String Refill_Flag_Policy;
    private String Base_SumInsured;
    private String FAMILY_First_SumInsured;
    private String Variant_Name;
    private List<ZoneWiseCopay> Zone_Wise_Copay;
    private String deductible;
    private String Refill_Benefit_Amount;
    private String COPay_Optional;
    private String COPay_Total_Percentage;
    private String NoClaimBonus;
    private List<Riders> Riders;
    private String PlanID;
    private String Bed_Type;

    @Getter
    @Setter
    @ToString
    public static class Riders {
        private String BenefitName;
        private String BenefitValue;
        private String BenifitRemarks;
    }

    @Getter
    @Setter
    @ToString
    public static class ZoneWiseCopay {
        private String ZoneName;
        private String Copay;
    }

    @Getter
    public enum RidersEnum {
        PERSONAL_ACCIDENT("Personal Accident PA"),
//        SAFEGUARD("Safeguard SG"),
        HOSPITAL_DAILY_CASH("Hospital Daily Cash HC"),
        ANNUAL_AGGREGATE_DEDUCTIBLE("Annual aggregate Deductible"),
        TREATMENT_ONLY_IN_TIERED_NETWORK("Treatment only in Tiered Network(Renewal Only)"),
        ADD_ON_SAFEGUARD("Add On (Safeguard)"),
        PERSONAL_ACCIDENT_COVER("Personal Accident cover"),
        HOSPITAL_CASH("Hospital Cash"),
        ENHANCED_GEOGRAPHICAL_SCOPE_FOR_INTERNATIONAL_COVERAGE("Enhanced Geographical Scope for International coverage"),
        CRITICAL_ILLNESS_COVER("Critical illness cover"),
        E_CONSULTATION("e-consultation"),
        PREMIUM_WAIVER("Premium Waiver"),
        DEDUCTIBLE("Deductible"),
        CO_PAYMENT("Co-payment"),
        INTERNATIONAL_COVERAGE_EXTENSION("International coverage extension"),
        SAFEGUARD("Safeguard"),
        SAFEGUARD_PLUS("Safeguard Plus"),
        SAFEGUARD_PS("Safeguard+"),
        REFILL("Refill Flag Policy"),
        REASSURE("Reassure"),
        PORTED_POLICY("Ported Policy");

        private final String riderName;

        RidersEnum(String riderName) {
            this.riderName = riderName;
        }

    }
}
