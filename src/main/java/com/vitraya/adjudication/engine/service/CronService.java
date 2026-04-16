package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.ClaimStatus;
import com.vitraya.adjudication.engine.mysql.entity.BillTariffResponse;
import com.vitraya.adjudication.engine.mysql.entity.ClaimAdjudicationResult;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class CronService {
    @Value("${bill.tariff.response.threshold}")
    private int billTariffResponseThreshold;

    @Value("${past.days.claim.check.threshold}")
    private int PAST_DAYS_CLAIM_CHECK_THRESHOLD;

    private final ClaimAdmissionService claimAdmissionService;
    private final ClaimService claimService;
    private final ClaimAdjudicationService claimAdjudicationService;
    private final BillTariffService billTariffService;
    private final ClaimCommonService claimCommonService;

    public CronService(ClaimService claimService, ClaimAdjudicationService claimAdjudicationService, BillTariffService billTariffService, ClaimCommonService claimCommonService, ClaimAdmissionService claimAdmissionService) {
        this.claimService = claimService;
        this.claimAdjudicationService = claimAdjudicationService;
        this.billTariffService = billTariffService;
        this.claimCommonService = claimCommonService;
        this.claimAdmissionService = claimAdmissionService;
    }

    public boolean submitPendingClaims() {
        // Pending preauth claims
        List<ClaimData> claimDataList = claimService.getPendingClaimDataList();
        log.info("Received claim data list of size: {}", (claimDataList != null ? claimDataList.size() : 0));
        if (claimDataList != null) {
            for (ClaimData claimData : claimDataList) {
                boolean isClaimAdjudicationResultUpdated = claimAdjudicationService.checkAdjudicationResult(claimData, true, false);
                if (isClaimAdjudicationResultUpdated && claimService.pushClaimDecision(claimData.getId())) {
                    log.info("Pre Auth claim {} submitted successfully from CRON", claimData.getId());
                } else {
                    log.info("Pre Auth claim {} submission failed from CRON", claimData.getId());
                }
            }
        }

        // Time for the interim claims and discharge claims
        List<ClaimData> extensionClaimDataList = claimService.getExtensionPendingClaimDataList();
        log.info("Received extension claim data list of size: {}", (extensionClaimDataList != null ? extensionClaimDataList.size() : 0));
        if (extensionClaimDataList != null) {
            for (ClaimData claimData : extensionClaimDataList) {
                boolean isClaimAdjudicationResultUpdated = claimAdjudicationService.checkAdjudicationResult(claimData,
                        false, claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RAISED));
                if (isClaimAdjudicationResultUpdated && claimService.pushClaimDecision(claimData.getId())) {
                    log.info("Extension claim {} submitted successfully from CRON", claimData.getId());
                } else {
                    log.info("Extension claim {} submission failed from CRON", claimData.getId());
                }
            }
        }
        return true;
    }

    public boolean checkBillTariffPendingClaims() throws InterruptedException {
        Date thresholdDate = DateUtil.getPastOrFutureDateMinuteBased(new Date(), -billTariffResponseThreshold);
        Date startDate = DateUtil.getPastOrFutureDate(new Date(), -PAST_DAYS_CLAIM_CHECK_THRESHOLD);
        List<ClaimAdjudicationResult> claimAdjudicationResultList = claimAdjudicationService.getPendingClaimsList(startDate, thresholdDate);
        log.info("Received pending claim data list of size: {} for startDate {} and thresholdDate {}",
                (claimAdjudicationResultList != null ? claimAdjudicationResultList.size() : 0), startDate, thresholdDate);

        if (claimAdjudicationResultList != null) {
            for (ClaimAdjudicationResult claimAdjudicationResult : claimAdjudicationResultList) {
                log.info("Processing claim adjudication result for claim {}", claimAdjudicationResult.getClaimDataId());
                ClaimData claimData = claimService.getClaimData(claimAdjudicationResult.getClaimDataId());
                if (claimData == null) {
                    log.error("Claim data not found for claim data id {}", claimAdjudicationResult.getClaimDataId());
                    continue;
                }
                if(claimData.isEmailFlow()){
                    log.info("Claim data is email flow, skipping claim data id {}", claimAdjudicationResult.getClaimDataId());
                    continue;
                }

                if (claimData.getClaimStatus().equals(ClaimStatus.DUPLICATE)) {
                    log.error("Claim data found duplicate for claim data id {}", claimAdjudicationResult.getClaimDataId());
                    claimCommonService.updateDuplicateClaimAdjudicationResult(claimAdjudicationResult);
                    continue;
                }

                BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimAdjudicationResult.getClaimDataId());

                boolean isBillTariffResponseCreated = false;
                if (billTariffResponse != null && claimCommonService.isClaimAdmissionDetailsUse(claimData, billTariffResponse)) {
                    isBillTariffResponseCreated = true;
                    billTariffResponse = claimAdmissionService.getBillTariffResponseFromAdmission(claimData, billTariffResponse);
                }

                if (billTariffResponse == null) {
                    log.error("Bill tariff response not found for claim data id {}", claimAdjudicationResult.getClaimDataId());
                    continue;
                }

                claimService.processPmlRequestFromAdmissionDetails(claimData, billTariffResponse, null, isBillTariffResponseCreated);
                log.info("going ahead to check auto submission condition for claim data id {}", claimAdjudicationResult.getClaimDataId());
//                claimService.checkClaimAutoSubmissionCondition(claimData.getId());
                log.info("Claim adjudication result pending for claim data id {} processed", claimAdjudicationResult.getClaimDataId());
                Thread.sleep(1000);
            }
        }

        return true;
    }
}
