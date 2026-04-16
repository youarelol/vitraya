package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.AdjudicationStatus;
import com.vitraya.adjudication.engine.dto.enums.ErrorMsgType;
import com.vitraya.adjudication.engine.dto.request.CashlessResponse;
import com.vitraya.adjudication.engine.mysql.entity.BillTariffResponse;
import com.vitraya.adjudication.engine.mysql.entity.ClaimAdjudicationResult;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.ErrorMessageLogs;
import com.vitraya.adjudication.engine.mysql.repository.BillTariffResponseRepository;
import com.vitraya.adjudication.engine.mysql.repository.ClaimAdjudicationRepository;
import com.vitraya.adjudication.engine.mysql.repository.ErrorMessageLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ClaimAdjudicationService {

    private final ClaimAdjudicationRepository claimAdjudicationResultRepository;
    private final BillTariffResponseRepository billTariffResponseRepository;
    private final ErrorMessageLogsService errorMessageLogsService;
    private final ErrorMessageLogRepository errorMessageLogRepository;

    public ClaimAdjudicationService(ClaimAdjudicationRepository claimAdjudicationResultRepository, BillTariffResponseRepository billTariffResponseRepository,
                                    ErrorMessageLogsService errorMessageLogsService,ErrorMessageLogRepository errorMessageLogRepository) {
        this.claimAdjudicationResultRepository = claimAdjudicationResultRepository;
        this.billTariffResponseRepository = billTariffResponseRepository;
        this.errorMessageLogsService = errorMessageLogsService;
        this.errorMessageLogRepository = errorMessageLogRepository;
    }

    public void processAndSaveCashlessResponse(ClaimData claimData, CashlessResponse cashlessResponse) {
        ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationResultRepository.findByClaimDataId(claimData.getId());
        if (claimAdjudicationResult == null) {
            log.info("Claim adjudication result not found for claim data id: {}", claimData.getId());
            claimAdjudicationResult = new ClaimAdjudicationResult();
            claimAdjudicationResult.setClaimDataId(claimData.getId());
            claimAdjudicationResult.setDateCreated(new Date());
        } else {
            log.info("Claim adjudication result found for claim data id: {}", claimData.getId());
        }
        claimAdjudicationResult.setRemarks(cashlessResponse.getQuery());
        claimAdjudicationResult.setFinalInsurerDecisionReversefeed(cashlessResponse.getClaimStatusInString());
        claimAdjudicationResult.setFinalInsurerAmountReversefeed(cashlessResponse.getApprovedAmount());
        claimAdjudicationResult.setDateUpdated(new Date());
        claimAdjudicationResultRepository.save(claimAdjudicationResult);
    }

    public ClaimAdjudicationResult getClaimAdjudicationResult(long claimDataId) {
        return claimAdjudicationResultRepository.findByClaimDataId(claimDataId);
    }

    public void saveClaimAdjudicationResult(ClaimAdjudicationResult claimAdjudicationResult) {
        claimAdjudicationResultRepository.save(claimAdjudicationResult);
    }

    public boolean checkAdjudicationResult(ClaimData claimData, boolean isPreAuth, boolean isDischarge) {
        ClaimAdjudicationResult claimAdjudicationResult = getClaimAdjudicationResult(claimData.getId());
        BillTariffResponse billTariffResponse = billTariffResponseRepository
                .getBillTariffResponseByClaimDataIdOrderByIdDesc(claimData.getId());
        BigDecimal requestedAmount = billTariffResponse != null ?
                billTariffResponse.getClaimBillAmountRequested() : BigDecimal.ZERO;
        if (claimAdjudicationResult != null) {
            if (isPreAuth) {
                if (claimAdjudicationResult.getClaimDecision() == null
                        || claimAdjudicationResult.getPreAuthInsurerDecision() == null) {
                    log.info("Claim adjudication result not found for claim data id: {}", claimData.getId());
                    claimAdjudicationResult.setClaimDecision(AdjudicationStatus.MANUAL.toString());
                    claimAdjudicationResult.setPreAuthDecision(AdjudicationStatus.MANUAL.toString());
                    claimAdjudicationResult.setPreAuthBillAmount(requestedAmount);
                    claimAdjudicationResult.setPreAuthAmountApproved(BigDecimal.ZERO);
                    claimAdjudicationResult.setPreAuthSavings(BigDecimal.ZERO);
                }
            }
            if (isDischarge) {
                if (claimAdjudicationResult.getClaimDecision() == null
                        || claimAdjudicationResult.getPreAuthInsurerDecision() == null) {
                    log.info("Claim adjudication result not found for claim data id: {}", claimData.getId());
                    claimAdjudicationResult.setClaimDecision(AdjudicationStatus.MANUAL.toString());
                    claimAdjudicationResult.setDischargeClaimDecision(AdjudicationStatus.MANUAL.toString());
                    claimAdjudicationResult.setDischargeBillAmount(requestedAmount);
                    claimAdjudicationResult.setDischargeAmountApproved(BigDecimal.ZERO);
                    claimAdjudicationResult.setDischargeSavings(BigDecimal.ZERO);
                }
            }
//            log.info("Save MANUAL Data in error message logs in checkAdjudicationResult for claim data id: {}", claimData.getId());
//            if (claimData.getAdjudicationStatus() != null
//                    && claimData.getAdjudicationStatus().equalsIgnoreCase("MANUAL")) {
//                ErrorMsgType error = ErrorMsgType.MANUAL;
//                    errorMessageLogsService.saveErrorMessages(claimData, error);
//                    log.info("Saving Error Message Logs for MANUAL in checkAdjudicationResult method for {}", claimData.getIntimationNumber());
//            }

            claimAdjudicationResult.setRemarks("Claim hit timeout");

            log.info("Claim adjudication result {} updated for claim data id: {}", claimAdjudicationResult,
                    claimData.getId());
            saveClaimAdjudicationResult(claimAdjudicationResult);
        }

        return true;
    }

    public List<ClaimAdjudicationResult> getPendingClaimsList(Date startDate, Date thresholdDate) {
        List<ClaimAdjudicationResult> preAuthPendingClaims = claimAdjudicationResultRepository.getPendingClaimsListPreAuth(startDate, thresholdDate);
        List<ClaimAdjudicationResult> dischargePendingClaims = claimAdjudicationResultRepository.getPendingClaimsListDischarge(startDate, thresholdDate);

        // Now merge the two lists
        preAuthPendingClaims.addAll(dischargePendingClaims);
        return preAuthPendingClaims;
    }
}
