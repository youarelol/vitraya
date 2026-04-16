package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.dto.response.BillTariffResponseDTO;
import com.vitraya.adjudication.engine.dto.response.PMLResponseDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrClaims;
import com.vitraya.adjudication.engine.mysql.repository.*;
import com.vitraya.adjudication.engine.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClaimCommonService {
    private final ClaimAdjudicationService claimAdjudicationService;
    private final ClaimRejectionReasonRepo claimRejectionReasonRepo;
    private final ClaimTransitionService claimTransitionService;
    private final NivaUcrClaimsRepo nivaUcrClaimsRepo;
    private final ErrorMessageLogRepository errorMessageLogRepository;
    private final ErrorMessageLogsService errorMessageLogsService;
    @Value("${bill.tariff.response.threshold}")
    private int BILL_TARIFF_RESPONSE_THRESHOLD;

    @Value("${claim.module.response.wait.threshold}")
    private int CLAIM_MODULE_RESPONSE_WAIT_THRESHOLD;

    @Value("${vneuron.result.binded}")
    private boolean vneuronResultBinded;

    private final ClaimModuleStatsRepository claimModuleStatsRepository;
    private final ClaimDataRepository claimDataRepository;
    private final ClaimAdjudicationRepository claimAdjudicationResultRepository;
    private final BillTariffResponseRepository billTariffResponseRepository;
    private final PMLResponseRepository pmlResponseRepository;
    private final VneuronResponseRepository vneuronResponseRepository;

    public ClaimCommonService(ClaimModuleStatsRepository claimModuleStatsRepository, ClaimDataRepository claimDataRepository,
                              ClaimAdjudicationRepository claimAdjudicationResultRepository, BillTariffResponseRepository billTariffResponseRepository,
                              PMLResponseRepository pmlResponseRepository, VneuronResponseRepository vneuronResponseRepository,
                              ClaimAdjudicationService claimAdjudicationService, ClaimRejectionReasonRepo claimRejectionReasonRepo,
                              ClaimTransitionService claimTransitionService, NivaUcrClaimsRepo nivaUcrClaimsRepo, ErrorMessageLogRepository errorMessageLogRepository, ErrorMessageLogsService errorMessageLogsService) {
        this.claimModuleStatsRepository = claimModuleStatsRepository;
        this.claimDataRepository = claimDataRepository;
        this.claimAdjudicationResultRepository = claimAdjudicationResultRepository;
        this.billTariffResponseRepository = billTariffResponseRepository;
        this.pmlResponseRepository = pmlResponseRepository;
        this.vneuronResponseRepository = vneuronResponseRepository;
        this.claimAdjudicationService = claimAdjudicationService;
        this.claimRejectionReasonRepo = claimRejectionReasonRepo;
        this.claimTransitionService = claimTransitionService;
        this.nivaUcrClaimsRepo = nivaUcrClaimsRepo;
        this.errorMessageLogRepository = errorMessageLogRepository;
        this.errorMessageLogsService = errorMessageLogsService;
    }

    public ClaimModuleStats createClaimModuleStats(long claimDataId, ClaimRequestTypeEnum requestType, String txnId) {
        ClaimModuleStats claimModuleStats = new ClaimModuleStats();
        claimModuleStats.setClaimDataId(claimDataId);
        claimModuleStats.setBillIdentifier(null);
        claimModuleStats.setPmlIdentifier(null);
        claimModuleStats.setMedicalIdentifier(null);
        claimModuleStats.setBillTariffTat(0);
        claimModuleStats.setPmlTat(0);
        claimModuleStats.setMedicalTat(0);
        claimModuleStats.setClaimTat(0);
        claimModuleStats.setClaimStartTime(new Date());
        claimModuleStats.setDateCreated(new Date());
        claimModuleStats.setClaimStage(requestType.name());
        claimModuleStats.setTxnId(txnId);

        return claimModuleStatsRepository.save(claimModuleStats);
    }

    public ClaimModuleStats recordClaimModuleStats(ClaimData claimData, ClaimModuleStats claimModuleStats,
                                                   ClaimModulesEnum claimModulesEnum, String identifier, int tat) {
        log.info("Recording claim module stats for claim id: {} and claim module: {}", claimData.getId(), claimModulesEnum);

        if (claimModuleStats == null) {
            log.error("Claim module stats not found for claim id: {}", claimData.getId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_MODULE_FOUND);
        }

        return updateClaimModuleStats(claimModuleStats, claimModulesEnum, identifier, tat);
    }

    private ClaimModuleStats updateClaimModuleStats(ClaimModuleStats claimModuleStats, ClaimModulesEnum claimModulesEnum,
                                                    String identifier, int tat) {
        int rowUpdated = 0;
        switch (claimModulesEnum) {
            case BILL_TARIFF:
                log.info("Updating bill tariff stats for id {} :- identifier {} and tat is {}", claimModuleStats.getId(),
                        identifier, tat);
                rowUpdated = claimModuleStatsRepository.updateBillStats(identifier, tat, claimModuleStats.getId());
                if (rowUpdated > 0) {
                    claimModuleStats.setBillIdentifier(identifier);
                    claimModuleStats.setBillTariffTat(tat);
                }
                return claimModuleStats;
            case PML:
                log.info("Updating PML stats for id {} :- identifier {} and tat is {}", claimModuleStats.getId(),
                        identifier, tat);
                rowUpdated = claimModuleStatsRepository.updatePMLStats(identifier, tat, claimModuleStats.getId());
                if (rowUpdated > 0) {
                    claimModuleStats.setPmlIdentifier(identifier);
                    claimModuleStats.setPmlTat(tat);
                }
                return claimModuleStats;
            case MEDICAL_ADMISSIBILITY:
                log.info("Updating Medical Admissibility stats for id {} :- identifier {} and tat is {}", claimModuleStats.getId(),
                        identifier, tat);
                rowUpdated = claimModuleStatsRepository.updateVneuronStats(identifier, tat, claimModuleStats.getId());
                if (rowUpdated > 0) {
                    claimModuleStats.setMedicalIdentifier(identifier);
                    claimModuleStats.setMedicalTat(tat);
                }
                return claimModuleStats;
            default:
                throw new IllegalArgumentException("Unknown claim module: " + claimModulesEnum);
        }

    }

    public ClaimModuleStats getLatestClaimModuleStats(long claimDataId) {
        return claimModuleStatsRepository.findLatestByClaimDataId(claimDataId);
    }

    public ClaimModuleStats getLatestClaimModuleStatsByClaimIdAndClaimStage(long claimDataId, String claimStage) {
        return claimModuleStatsRepository.findLatestByClaimStageAndClaimDataId(claimDataId, claimStage);
    }

    public boolean isClaimAdmissionDetailsUse(ClaimData claimData, BillTariffResponse billTariffResponse) {
        /*
         Now the condition needs to be when we have hit the threshold and bill not found / no response received
         from bill tariff and claim is in preauth or interim stage
         */

        if (billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null) {
            if (ClaimStatus.isPreAuthStage(claimData.getClaimStatus()) || ClaimStatus.isInterimStage(claimData.getClaimStatus())) {
                if (isBillOpenForEditing(billTariffResponse.getBillTariffResponseDTO())) {
                    return isBillTariffResponseHitThreshold(billTariffResponse);
                } else if (!claimData.isPreauthBillPresentFlag() && ClaimStatus.isPreAuthStageWithoutRepush(claimData.getClaimStatus())) {
                    return true;
                } else {
                    return isBillNotFound(billTariffResponse);
                }
            }
        }

        return false;
    }

    public boolean isBillNotFound(BillTariffResponse billTariffResponse) {
        return "BILL_FAILED".equalsIgnoreCase(billTariffResponse.getBillTariffResponseDTO().getResponse_code())
                && BillCodeEnum.BILL_NOT_FOUND.toString().equalsIgnoreCase(billTariffResponse.getBillTariffResponseDTO().getBill_code());
    }

    public boolean isBillOpenForEditing(BillTariffResponseDTO billTariffResponseDTO) {
        return "BILL_FAILED_OPEN_FOR_EDITING".equalsIgnoreCase(billTariffResponseDTO.getResponse_code())
                && BillCodeEnum.BILL_NOT_FOUND.toString().equalsIgnoreCase(billTariffResponseDTO.getBill_code());
    }

    private boolean isBillTariffResponseHitThreshold(BillTariffResponse billTariffResponse) {
        if (billTariffResponse.getDateCreated() == null) {
            billTariffResponseRepository.updateDateCreated(billTariffResponse.getId(), new Date());
            return true;
        }

        return new Date().after(DateUtil.getPastOrFutureDateMinuteBased(billTariffResponse.getDateCreated(),
                BILL_TARIFF_RESPONSE_THRESHOLD));

    }

    public void updateClaimModuleStatsProcessed(ClaimModuleStats claimModuleStats) {
        int tat = CLAIM_MODULE_RESPONSE_WAIT_THRESHOLD * 60 * 1000;
        if (claimModuleStats.getBillIdentifier() == null) {
            claimModuleStats.setBillIdentifier("Run Force Failed");
            claimModuleStats.setBillTariffTat(0);
        }

        if (claimModuleStats.getPmlIdentifier() == null) {
            claimModuleStats.setPmlIdentifier("Run Force Failed");
            claimModuleStats.setPmlTat(0);
        }

        if (claimModuleStats.getMedicalIdentifier() == null) {
            claimModuleStats.setMedicalIdentifier("Run Force Failed");
            claimModuleStats.setMedicalTat(0);
        }

        claimModuleStats.setClaimTat(tat);
        claimModuleStats.setClaimEndTime(new Date());
        claimModuleStats.setDateUpdated(new Date());

        claimModuleStatsRepository.save(claimModuleStats);
    }

    public void recordClaimAdjudicationResult(ClaimData claimData, VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        log.info("inside recordClaimAdjudicationResult method...");
        ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationResultRepository.findByClaimDataId(claimData.getId());

        if (claimAdjudicationResult == null) {
            ClaimAdjudicationResult newClaimAdjudicationResult = new ClaimAdjudicationResult();
            newClaimAdjudicationResult.setClaimDataId(claimData.getId());
            newClaimAdjudicationResult.setClaimDecision(null);
            newClaimAdjudicationResult.setPreAuthInsurerDecision(null);
            newClaimAdjudicationResult.setClaimStage(vitrayaInsurerClaimData.getRequestType().toString());
            newClaimAdjudicationResult.setRemarks(null);
            newClaimAdjudicationResult.setPreAuthDecision(vitrayaInsurerClaimData.getRequest().getAdjudicationResult() == null
                    ? null : vitrayaInsurerClaimData.getRequest().getAdjudicationResult().getAdjudicationDecision());
            newClaimAdjudicationResult.setFinalInsurerDecisionReversefeed(null);
            newClaimAdjudicationResult.setDateCreated(new Date());
            newClaimAdjudicationResult.setDateUpdated(new Date());
            claimAdjudicationResult = newClaimAdjudicationResult;
        } else {
            claimAdjudicationResult.setClaimDecision(null);
            claimAdjudicationResult.setPreAuthInsurerDecision(null);
            claimAdjudicationResult.setFinalInsurerDecisionReversefeed(null);
            claimAdjudicationResult.setDateUpdated(new Date());
        }

        claimAdjudicationResult.setTxnId(vitrayaInsurerClaimData.getTxnId());
        claimAdjudicationResultRepository.save(claimAdjudicationResult);
    }

    public ClaimData checkClaimModuleCompletion(ClaimData claimData, boolean moduleExecuted) {
         /*
            We need to do the following action.
            1. Based on the given hours we need to fetch the details from the claim data which are in pending state.
            2. We need to check the claim adjudication result and based on the result we need to update the claim data.
            3. We need to update the claim module stats, claim adjudication result.
             */
        log.info("Came to check for the claim module completion for claim id: {}", claimData.getId());
        ClaimModuleStats claimModuleStats = claimModuleStatsRepository.findLatestByClaimDataId(claimData.getId());
        boolean isClaimProcessed = false;
        // claimModuleStats.isClaimProcessingDone()
        BillTariffResponse billTariffResponse = billTariffResponseRepository.getBillTariffResponseByClaimDataIdOrderByIdDesc(claimData.getId());
        if (billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null) {
            ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationService.getClaimAdjudicationResult(claimData.getId());
            if (isClaimProcessingDone(claimModuleStats, billTariffResponse.getBillTariffResponseDTO())) {
                isClaimProcessed = true;
                log.info("Claim processing is done for claim id: {}", claimData.getId());
            /*
             Now as we know that claim modules are processed successfully. We need to get the latest result of all the
             modules to update the claim status, claim adjudication result and claim module stats.
             */
                PMLResponse pmlResponse = pmlResponseRepository.findPMLResponseByClaimDataIdLatest(claimData.getId());
                VneuronResponse vneuronResponse = vneuronResponseRepository.getVneuronResponseByClaimDataId(claimData.getId());
                if (claimAdjudicationResult == null) {
                    claimAdjudicationResult = new ClaimAdjudicationResult();
                }
                /*
                PML	    vNeuron	Final Decision
                Accept	Reject	Reject
                Reject	Accept	Reject
                Accept	Query	Query
                Reject	Query	Query
                Accept	Accept	Accept
                Reject	Reject	Reject
                */

                if (vneuronResultBinded) {
                    if (vneuronResponse != null && pmlResponse != null) {
                        if (AdjudicationStatus.QUERY.getAdjudicationStatus().equalsIgnoreCase(vneuronResponse.getVneuronDecision())) {
                            if ("APPROVED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                                claimData.setAdjudicationStatus(AdjudicationStatus.QUERY.getAdjudicationStatus());
                            } else if ("REJECTED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                                claimData.setAdjudicationStatus(AdjudicationStatus.REJECTED.getAdjudicationStatus());
                                setPmlRejectionReasons(claimData, pmlResponse);
                            }
                        } else if (AdjudicationStatus.ACCEPTED.getAdjudicationStatus().equalsIgnoreCase(vneuronResponse.getVneuronDecision())) {
                            if ("REJECTED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                                claimData.setAdjudicationStatus(AdjudicationStatus.REJECTED.getAdjudicationStatus());
                                setPmlRejectionReasons(claimData, pmlResponse);
                            } else if ("APPROVED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                                claimData.setAdjudicationStatus(AdjudicationStatus.APPROVED.getAdjudicationStatus());
                            }
                        } else if (AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(vneuronResponse.getVneuronDecision())) {
                            if ("REJECTED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                                claimData.setAdjudicationStatus(AdjudicationStatus.REJECTED.getAdjudicationStatus());
                            } else if ("APPROVED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                                claimData.setAdjudicationStatus(AdjudicationStatus.REJECTED.getAdjudicationStatus());
                            }
                            setPmlRejectionReasons(claimData, pmlResponse);
                        } else {
                            claimData.setAdjudicationStatus(AdjudicationStatus.MANUAL.getAdjudicationStatus());
                        }
                    } else {
                        claimData.setAdjudicationStatus(AdjudicationStatus.MANUAL.getAdjudicationStatus());
                    }
                } else {
                    if (!(ClaimStatus.isPreAuthStage(claimData.getClaimStatus())
                            || ClaimStatus.isInterimStage(claimData.getClaimStatus()))
                            && vneuronResponse != null && AdjudicationStatus.QUERY.getAdjudicationStatus()
                            .equalsIgnoreCase(vneuronResponse.getVneuronDecision())) {
                        claimData.setAdjudicationStatus(AdjudicationStatus.QUERY.getAdjudicationStatus());
                    } else if (billTariffResponse != null && pmlResponse != null
                            && ("TARIFF_APPLIED".equalsIgnoreCase(billTariffResponse.getBillTariffDecision())
                            || "DEFAULT_TARIFF_APPLIED".equalsIgnoreCase(billTariffResponse.getBillTariffDecision()))
                            && "APPROVED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                        claimData.setAdjudicationStatus(AdjudicationStatus.APPROVED.getAdjudicationStatus());
                    } else if (pmlResponse != null && "REJECTED".equalsIgnoreCase(pmlResponse.getPmlDecision())) {
                        claimData.setAdjudicationStatus(AdjudicationStatus.REJECTED.getAdjudicationStatus());
                        setPmlRejectionReasons(claimData, pmlResponse);
                    } else {
                        claimData.setAdjudicationStatus(AdjudicationStatus.MANUAL.getAdjudicationStatus());
                    }
                }

                claimData.setEnhancementProcessed(isClaimProcessed);
                claimData.setStatus(isClaimProcessed ? EnhancementStatus.ENHANCEMENT_COMPLETED
                        : EnhancementStatus.ENHANCEMENT_IN_PROGRESS);
                // Now the time for claim module status
                if (isClaimProcessed) {
                    claimModuleStats.setClaimEndTime(new Date());
                    claimModuleStats.setClaimTat(DateUtil.getTimeDifference(claimModuleStats.getClaimStartTime(),
                            claimModuleStats.getClaimEndTime()));
                }
                claimModuleStats.setDateUpdated(new Date());

                // Prepare claim adjudication result
                boolean isPreAuth = false;
                boolean isDischarge = false;
                BigDecimal requestedAmount = billTariffResponse != null ?
                        billTariffResponse.getClaimBillAmountRequested() : BigDecimal.ZERO;

                if (ClaimStatus.isPreAuthStage(claimData.getClaimStatus())) {
                    isPreAuth = true;
                    claimAdjudicationResult.setClaimStage(ClaimRequestTypeEnum.preauth_request.toString());
                    claimAdjudicationResult.setPreAuthDecision(claimData.getAdjudicationStatus());
                    claimData.setInitialTat(claimModuleStats.getClaimTat());
                } else if (ClaimStatus.isDischargeStage(claimData.getClaimStatus())) {
                    isDischarge = true;
                    claimAdjudicationResult.setClaimStage(ClaimRequestTypeEnum.final_enhancement_request.toString());
                    claimAdjudicationResult.setDischargeClaimDecision(claimData.getAdjudicationStatus());
                    claimData.setDischargeTat(claimModuleStats.getClaimTat());
                }

                if (AdjudicationStatus.REJECTED.toString().equals(claimData.getAdjudicationStatus())) {
                    if (isPreAuth) {
                        claimAdjudicationResult.setPreAuthAmountApproved(BigDecimal.ZERO);
                        claimAdjudicationResult.setPreAuthSavings(requestedAmount);
                    }

                    if (isDischarge) {
                        claimAdjudicationResult.setDischargeAmountApproved(BigDecimal.ZERO);
                        claimAdjudicationResult.setDischargeSavings(requestedAmount);
                    }
                } else {
                    // Current PML is the module which will give the final approved amount. In future need arises we will revisit this.
                    BigDecimal approvedAmount = BigDecimal.ZERO;
                    if (pmlResponse != null) {
                        approvedAmount = pmlResponse.getApprovedAmount() != null ? pmlResponse.getApprovedAmount() : BigDecimal.ZERO;
                    }

                    if (isPreAuth) {
                        claimAdjudicationResult.setPreAuthAmountApproved(approvedAmount);
                        NivaUcrClaims nivaUcrClaims = nivaUcrClaimsRepo.findLatestByClaimDataId(claimData.getId());
                        if (nivaUcrClaims != null) {
                            if (nivaUcrClaims.getFinalApprovedAmount() != null
                                    && nivaUcrClaims.getFinalApprovedAmount().compareTo(BigDecimal.ZERO) > 0) {
                                claimAdjudicationResult.setPreAuthAmountApproved(nivaUcrClaims.getFinalApprovedAmount());
                            }
                        }
                        claimAdjudicationResult.setPreAuthSavings(requestedAmount != null && claimAdjudicationResult.getPreAuthAmountApproved() != null
                                ? requestedAmount.subtract(claimAdjudicationResult.getPreAuthAmountApproved()) : BigDecimal.ZERO);
                    }

                    if (isDischarge) {
                        claimAdjudicationResult.setDischargeAmountApproved(requestedAmount.subtract(approvedAmount));
                    }
                }

                claimAdjudicationResult.setClaimDecision(claimData.getAdjudicationStatus());
                if (isPreAuth) {
                    claimAdjudicationResult.setPreAuthBillAmount(requestedAmount);
                } else if (isDischarge) {
                    claimAdjudicationResult.setDischargeBillAmount(requestedAmount);
                }

                claimData.setClaimRerun(true);
                claimAdjudicationService.saveClaimAdjudicationResult(claimAdjudicationResult);
                claimModuleStatsRepository.save(claimModuleStats);
                claimDataRepository.save(claimData);

                claimTransitionService.saveClaimTransitionData(claimData.getId(),
                        claimData.getAdjudicationStatus() + " - By Vitraya", claimData.getTxnId());

                log.info("Save MANUAL Data in error message logs in checkClaimModuleCompletion for claim data id: {}", claimData.getId());
                if (claimData.getAdjudicationStatus() != null
                        && claimData.getAdjudicationStatus().equalsIgnoreCase("MANUAL")) {
                    ErrorMsgType error = ErrorMsgType.MANUAL;
                        errorMessageLogsService.saveErrorMessages(claimData, error);
                        log.info("Saving Error Message Logs for MANUAL in checkClaimModuleCompletion method for {}", claimData.getIntimationNumber());
                }
            } else {
                log.info("Claim processing is not done for claim id: {}", claimData.getId());
            }
        }

        return claimData;
    }

    private void setPmlRejectionReasons(ClaimData claimData, PMLResponse pmlResponse) {
        PMLResponseDTO pmlResponseDTO = pmlResponse.getPmlResponseDTO();
        if (pmlResponseDTO != null) {
            List<String> coverageFailedReasons = pmlResponseDTO.getClaim_result() != null
                    ? pmlResponseDTO.getClaim_result().getCoverageFailedReasons()
                    : null;
            if (coverageFailedReasons != null && !coverageFailedReasons.isEmpty()) {
                StringBuilder coverageFailedReasonsString = new StringBuilder();
                for (String reason : coverageFailedReasons) {
                    coverageFailedReasonsString.append(reason).append("|");
                }
                coverageFailedReasonsString.deleteCharAt(coverageFailedReasonsString.length() - 1);
                saveClaimRejectionReason(claimData, coverageFailedReasonsString);
            }
        }
    }

    public boolean isClaimProcessingDone(ClaimModuleStats claimModuleStats, BillTariffResponseDTO billTariffResponseDTO) {
        return !isBillOpenForEditing(billTariffResponseDTO)
                && claimModuleStats.isClaimModuleProcessingDone();
    }

    public boolean isClaimUnderProcessing(ClaimModuleStats claimModuleStats, BillTariffResponseDTO billTariffResponseDTO) {
        return !isClaimProcessingDone(claimModuleStats, billTariffResponseDTO);
    }

    public void saveClaimRejectionReason(ClaimData claimData, StringBuilder coverageFailedReasonsString) {
        ClaimRejectionReason claimRejectionReason = claimRejectionReasonRepo.findByClaimIdClaimStageAndStatus(claimData.getId(),
                claimData.getClaimStatus(), claimData.getAdjudicationStatus());
        if (claimRejectionReason == null) {
            claimRejectionReason = new ClaimRejectionReason();
            claimRejectionReason.setClaimDataId(claimData.getId());
            claimRejectionReason.setClaimStage(claimData.getClaimStatus().toString());
            claimRejectionReason.setClaimStatus(claimData.getAdjudicationStatus());
            claimRejectionReason.setReason(coverageFailedReasonsString.toString());
            claimRejectionReason.setDateCreated(new Date());
        } else {
            String existingReason = claimRejectionReason.getReason();
            claimRejectionReason.setReason(existingReason);
        }
        claimRejectionReason.setDateUpdated(new Date());
        claimRejectionReasonRepo.save(claimRejectionReason);
    }

    public String getIntimationNumber(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        return vitrayaInsurerClaimData.getRequest().getClaim().getIntimationNumber()
                + "_" + vitrayaInsurerClaimData.getRequest().getClaim().getId();
    }

    public String getIntimationNumberPlain(String intimationNumber) {
        String[] parts = intimationNumber.split("_");
        return Arrays.stream(parts, 0, parts.length - 1)
                .collect(Collectors.joining("_"));
    }

    public int getClaimIdPlain(String intimationNumber) {
        String[] claimNumber = intimationNumber.split("_");
        String vhiClaimId = claimNumber[claimNumber.length - 1].trim();
        return Integer.parseInt(vhiClaimId);
    }

    public List<ClaimData> getClaimsOfPendingStateByTime(int claimCheckTime) {
        Date thresholdDate = DateUtil.getAdjustedDate(claimCheckTime);
        return claimDataRepository.getClaimsToBePushToInsurer(thresholdDate);
    }

    public void updateDuplicateClaimAdjudicationResult(ClaimAdjudicationResult claimAdjudicationResult) {
        claimAdjudicationResult.setPreAuthBillAmount(BigDecimal.ZERO);
        claimAdjudicationService.saveClaimAdjudicationResult(claimAdjudicationResult);
    }
}
