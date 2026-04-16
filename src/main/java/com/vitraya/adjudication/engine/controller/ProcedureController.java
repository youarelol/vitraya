package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.service.ProcedureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/procedure")
public class ProcedureController {

    private final ProcedureService procedureService;
    @Autowired
    private CacheManager cacheManager;

    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @GetMapping("/list")
    @Cacheable("allProcedures")
    public Map<String, Object> getAllProcedures() {
        return procedureService.getAllProcedures();
    }

    @GetMapping("/illnesses/{prcId}")
    @Cacheable("prcIllnesses")
    public Map<String, Object> getProceduresIllnesses(@PathVariable("prcId") long prcId) {
        return procedureService.getProceduresIllnesses(prcId);
    }
    @GetMapping("/drop-cache")
    public void evictAllCacheValues() {
        Objects.requireNonNull(cacheManager.getCache("allProcedures")).clear();
        Objects.requireNonNull(cacheManager.getCache("prcIllnesses")).clear();
    }
}
