package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.InsurerDTO;
import com.vitraya.adjudication.engine.dto.request.CorporateRequest;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.service.CorporateService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/corporates")
public class CorporateController {
    private final CorporateService corporateService;

    public CorporateController(CorporateService corporateService) {
        this.corporateService = corporateService;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse getAllCorporates() {
        Iterable<Corporate> corporateList = corporateService.getAllCorporates();
        return RestAPIResponse.buildSuccess(corporateList);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public RestAPIResponse createCorporate(@Valid @RequestBody CorporateRequest corporateRequest) {
        corporateService.validateCorporateRequest(corporateRequest);
        Corporate corporate = corporateService.createCorporate(corporateRequest);
        return RestAPIResponse.buildSuccess(corporate);
    }

    @GetMapping("/roles")
    @Cacheable(value = "userRoles")
    public List<InsurerDTO> getAllHospitals() {
        return this.corporateService.findAllInsurer();
    }
}
