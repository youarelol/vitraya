package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import lombok.Data;

import java.util.List;

@Data
public class MemberDetailsDTO {
    private String Insured_First_Name;
    private String Insured_Name;
    private String DOB;
    private String Gender;
    private String memberId;
    private String Previous_Member_ID;
    private List<PEDDetails> Pre_Existing_Diseases;
    private String Available_SI;
    private String Member_SI;
    private List<MemberRiders> MemberRider;
    private String NoClaimBonus;

    @Data
    public static class PEDDetails {
        private String ICDCODE;
        private String ExclusionType;
        private String EffectiveDate;
        private String TerminationDate;
    }

    @Data
    public static class MemberRiders {
        private String BenefitName;
        private String BenefitValue;
        private String BenifitRemarks;

    }
}
