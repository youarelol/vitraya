package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.response.BillTariffResponseDTO;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.service.BillTariffService;
import com.vitraya.adjudication.engine.service.ClaimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/bill")
public class BillTariffController {

    private final BillTariffService billTariffService;
    private final ClaimService claimService;

    public BillTariffController(BillTariffService billTariffService, ClaimService claimService) {
        this.billTariffService = billTariffService;
        this.claimService = claimService;
    }

    /**
     * This method is used to create a new claim.
     *
     * @param billTariffResponseDTO
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse updateClaimBillTariffResponse(@RequestBody BillTariffResponseDTO billTariffResponseDTO)
            throws IOException {
        log.info("Received BillTariffResponse request: {}", billTariffResponseDTO);
        billTariffService.updateInsurerIrdaiPayable(billTariffResponseDTO);
        ClaimData claimData = billTariffService.processBillTariffResponse(billTariffResponseDTO);
        claimService.checkClaimAutoSubmissionCondition(claimData.getId());
        return RestAPIResponse.buildSuccess();
    }
}
