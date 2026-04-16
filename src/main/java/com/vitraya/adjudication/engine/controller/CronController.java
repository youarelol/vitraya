package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.service.ClaimService;
import com.vitraya.adjudication.engine.service.CronService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/cron")
public class CronController {

    private final ClaimService claimService;
    private final CronService cronService;

    public CronController(ClaimService claimService, CronService cronService) {
        this.claimService = claimService;
        this.cronService = cronService;
    }

    @PostMapping("/check-claim-data")
    public RestAPIResponse updateClaimData() throws IOException {
        return claimService.checkAndUpdateClaimDecisions() != null
                ? RestAPIResponse.buildSuccess()
                : RestAPIResponse.buildFail(200, null, null,
                "Not Updated Claim Data Successfully");
    }

    @PostMapping("/push-pending-claims")
    public RestAPIResponse submitPendingClaimData() throws IOException {
        return cronService.submitPendingClaims()
                ? RestAPIResponse.buildSuccess()
                : RestAPIResponse.buildFail(200, null, null,
                "Not Updated Claim Data Successfully");
    }

    @PostMapping("/check-bill-tariff-claims")
    public RestAPIResponse checkBillTariffPendingClaims() throws InterruptedException {
        return cronService.checkBillTariffPendingClaims()
                ? RestAPIResponse.buildSuccess()
                : RestAPIResponse.buildFail(200, null, null,
                "Not Updated Claim Data Successfully");
    }
}
