package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.enums.ClaimRequestTypeEnum;
import com.vitraya.adjudication.engine.dto.enums.ClaimStatus;
import com.vitraya.adjudication.engine.dto.enums.ErrorMsgType;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.repository.ClaimRidersRepository;
import com.vitraya.adjudication.engine.mysql.repository.ErrorMessageLogRepository;
import com.vitraya.adjudication.engine.mysql.repository.InsurerFetchResponsesRepository;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.NivaPolicyDataDTO;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PolicyHolderDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class ErrorMessageLogsService {

    private final ErrorMessageLogRepository errorMessageLogRepository;
    private final ClaimRidersRepository claimRidersRepository;
    private final InsurerFetchResponsesRepository insurerFetchResponsesRepository;

    public ErrorMessageLogsService(ErrorMessageLogRepository errorMessageLogRepository, ClaimRidersRepository claimRidersRepository, InsurerFetchResponsesRepository insurerFetchResponsesRepository) {
        this.errorMessageLogRepository = errorMessageLogRepository;
        this.claimRidersRepository = claimRidersRepository;
        this.insurerFetchResponsesRepository = insurerFetchResponsesRepository;
    }

    public void saveErrorMessages(ClaimData claimData, ErrorMsgType error) {
        try {
            String claimStage = getClaimStage(claimData.getClaimStatus());
            log.info("claimStage:{}", claimStage);
            ErrorMessageLogs existingLog = errorMessageLogRepository.findByClaimIdErrorReasonClaimStage(claimData.getId(), error.getFailureEngine(), claimStage).orElse(null);
            if (existingLog == null) {
                ErrorMessageLogs errorMessageLogs = ErrorMessageLogs.builder()
                        .claimDataId(claimData.getId())
                        .errorReason(error.getReason())
                        .errorMessage(error.getMessage())
                        .failureEngine(error.getFailureEngine())
                        .claimStage(claimStage)
                        .date_created(new Date())
                        .build();
                errorMessageLogRepository.save(errorMessageLogs);
                log.info("Error Message Logs saved successfully for intimation number: {}", claimData.getIntimationNumber());
            } else {
                log.info("Error Message Logs already exists for intimation number: {} with failure engine: {} and claim stage: {}", claimData.getIntimationNumber(), error.getFailureEngine(), claimStage);
            }
        } catch (Exception e) {
            log.error("Error :{}  while saving error message logs in saveErrorMessages for intimation number : {}", e.getMessage(), claimData.getIntimationNumber());
        }
    }

    public void saveRidersFlag(String intimationNumber, ClaimData claimData) {
        try {
            ClaimRiderDetails claimRiderDetails = claimRidersRepository.findByClaimDataId(claimData.getId());
            if (claimRiderDetails == null) {
                claimRiderDetails = new ClaimRiderDetails();
            } else {
                log.info("Rider details already exist for intimation number: {}", intimationNumber);
                return;
            }

            InsurerFetchResponses insurerFetchResponses =
                    insurerFetchResponsesRepository.getInsurerFetchResponsesByClaimIntimationNumber(intimationNumber);

            if (insurerFetchResponses == null ||
                    insurerFetchResponses.getPolicyData() == null ||
                    insurerFetchResponses.getPolicyData().isEmpty()) {
                log.error("insurerFetchResponses is empty for intimationNumber: {}", intimationNumber);
                return;
            }

            NivaPolicyDataDTO nivaPolicyDataDTO = new Gson()
                    .fromJson(insurerFetchResponses.getPolicyData(), NivaPolicyDataDTO.class);

            if (nivaPolicyDataDTO == null ||
                    nivaPolicyDataDTO.getPolicy() == null ||
                    nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails() == null ||
                    nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getRiders() == null) {
                log.error("nivaPolicyDataDTO is empty for intimationNumber: {}", intimationNumber);
                return;
            }

            PolicyHolderDetailsDTO policyHolderDetails = nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails();

            boolean safeguard = false;
            boolean safeguardPlus = false;

            for (PolicyHolderDetailsDTO.Riders rider : policyHolderDetails.getRiders()) {
                String benefitName = rider.getBenefitName();
                if (benefitName == null) continue;

                if (benefitName.equalsIgnoreCase(PolicyHolderDetailsDTO.RidersEnum.SAFEGUARD.name())) {
                    safeguard = true;
                }

                if (benefitName.equalsIgnoreCase(PolicyHolderDetailsDTO.RidersEnum.SAFEGUARD_PLUS.name())
                        || benefitName.equalsIgnoreCase("Safeguard Plus")
                        || benefitName.equalsIgnoreCase("Safeguard+")) {
                    safeguardPlus = true;
                }
            }
            claimRiderDetails.setClaimDataId(claimData.getId());
            claimRiderDetails.setIntimationNumber(intimationNumber);
            claimRiderDetails.setSafeguard(safeguard);
            claimRiderDetails.setSafeguardPlus(safeguardPlus);
            claimRiderDetails.setRefillFlagPolicy("Y".equalsIgnoreCase(policyHolderDetails.getRefill_Flag_Policy()));
            claimRiderDetails.setReassureBenefitAmount("Y".equalsIgnoreCase(policyHolderDetails.getReassure_Benefit_Amount()));
            claimRiderDetails.setPolicyPorted("Y".equalsIgnoreCase(policyHolderDetails.getPolicy_Ported()));
            claimRidersRepository.save(claimRiderDetails);

        } catch (Exception e) {
            log.error("Exception in saveRidersFlag for intimationNumber: {}. Message: {}", intimationNumber, e.getMessage(), e);
        }
    }

    public String getClaimStage(ClaimStatus claimStatus) {

        if (ClaimStatus.isPreAuthStage(claimStatus))
            return ClaimRequestTypeEnum.preauth_request.name();
        else if (ClaimStatus.isInterimStage(claimStatus))
            return ClaimRequestTypeEnum.interim_enhancement_request.name();
        else if (ClaimStatus.isDischargeStage(claimStatus))
            return ClaimRequestTypeEnum.final_enhancement_request.name();
        else if (ClaimStatus.isQueryStage(claimStatus))
            return ClaimRequestTypeEnum.query_response.name();
        else if (ClaimStatus.isSettlement(claimStatus))
            return ClaimRequestTypeEnum.settlement_request.name();
        else if (ClaimStatus.isReconsideration(claimStatus))
            return ClaimRequestTypeEnum.reconsideration_request.name();
        return "";
    }
}
