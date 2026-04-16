package com.vitraya.adjudication.engine.controller;


import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.request.CashlessResponse;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.service.ExternalClaimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/external")
public class ExternalController {

    @Autowired
    private ExternalClaimService externalClaimService;


    @PostMapping("/cashless/response")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse cashlessResponse(@RequestBody CashlessResponse cashlessResponse) throws VitrayaException, IOException {
        log.info("Received cashless response from niva through hospital portal with response: {}", new Gson().toJson(cashlessResponse));
        if (cashlessResponse == null) {
            throw new VitrayaException("Invalid cashless response");
        } else {
            externalClaimService.processCashlessResponse(cashlessResponse);
        }
        return RestAPIResponse.buildSuccess();
    }
}
