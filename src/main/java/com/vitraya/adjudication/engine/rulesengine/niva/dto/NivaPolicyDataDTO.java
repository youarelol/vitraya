package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Data
public class NivaPolicyDataDTO {
    private String Status;
    private String StatusMessage;
    private PolicyVariable policy;

    @Getter
    @Setter
    @ToString
    public static class PolicyVariable {
        private PolicyHolderDetailsDTO policyHolderDetails;
        private List<MemberDetailsDTO> MemberDetails;
        private List<NivaPolicyRenewalHistoryDTO> Policy_Renewal_History;
    }
}
