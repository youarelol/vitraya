package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.IllnessResultDTO;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.service.IllnessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/illness")
public class IllnessController {
    private final IllnessService illnessService;

    public IllnessController(IllnessService illnessService) {
        this.illnessService = illnessService;
    }

    @GetMapping("/by-procedure/{procedureId}")
    public RestAPIResponse getByProcedure(@PathVariable String procedureId) {
        return RestAPIResponse.buildSuccess(illnessService.getIllnessesByProcedureId(procedureId));
    }
}
