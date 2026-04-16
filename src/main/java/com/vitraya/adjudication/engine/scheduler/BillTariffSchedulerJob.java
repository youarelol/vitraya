package com.vitraya.adjudication.engine.scheduler;

import com.vitraya.adjudication.engine.service.ClaimService;
import com.vitraya.adjudication.engine.service.CronService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillTariffSchedulerJob {


    @Value("${cron.enabled}")
    private boolean isCronEnabled;

    @Value("${cron.enabled.for.manual}")
    private boolean isCronEnabledForManual;

    private final CronService cronService;
    private final ClaimService claimService;

    public BillTariffSchedulerJob(CronService cronService, ClaimService claimService) {
        this.cronService = cronService;
        this.claimService = claimService;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void checkBillTariffPendingClaims() throws InterruptedException {
        log.info("Checking pending claims for bill tariff");
        if (isCronEnabled) {
            cronService.checkBillTariffPendingClaims();
        }
    }

    @Scheduled(cron = "0 0/2 * * * ?")
    public void pushTOMaximus() throws InterruptedException {
        log.info("running cron to submit pending claims");
        if (isCronEnabledForManual) {
            claimService.checkAndUpdateClaimDecisions();
        }

    }
}
