package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.ClaimFlowType;
import com.vitraya.adjudication.engine.mysql.entity.InsurerFetchResponses;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ClaimRequestDTO {
    private ClaimFlowType claimFlowType;
    private ClaimDTO claim;
    private ClaimIllnessTreatmentDetailsDTO claimIllnessTreatmentDetails;
    private ClaimAdmissionDetailsDTO claimAdmissionDetails;
    private HospitalServiceTypeDTO hospitalServiceType;
    private ProcedureDTO procedure;
    private ProcedureMethodDTO procedureMethod;
    private List<DocumentMasterListItem> documentMasterList;
    private IllnessDTO illness;
    private AdjudicationResultDTO adjudicationResult;
    private BigDecimal requestedAmount;
    private InsurerFetchResponses insurerFetchResponses;
    private boolean verifiedClaim ;
    private boolean billPresentInPreAuth;

    @Data
    public static class AdjudicationResultDTO {
        private String AdjudicationCategory;
        private String AdjudicationDecision;
        private String AdjudicationCategoryRemarks;
        private String AdjudicationRemarks;
        private String AdjudicationApprovedAmount;
    }
}