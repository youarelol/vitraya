package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.service.HospitalServiceTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/hospital")
public class HospitalController {

    private final HospitalServiceTypeService hospitalServiceTypeService;
    @Autowired
    private CacheManager cacheManager;

    public HospitalController(HospitalServiceTypeService hospitalServiceTypeService) {
        this.hospitalServiceTypeService = hospitalServiceTypeService;
    }

    @GetMapping("/list")
    @Cacheable(value = "allHospitals")
    public Map<String, Object> getAllHospitals() {
        return hospitalServiceTypeService.getAllHospitals();
    }

    @GetMapping("/rooms/{hospitalId}")
    @Cacheable(value = "hospitalRooms", key = "#hospitalCd")
    public Map<String, Object> getHospitalRooms(@PathVariable("hospitalId") String hospitalCd) {
        return hospitalServiceTypeService.getHospitalRoomTypes(hospitalCd);
    }

    @GetMapping("/drop-cache")
    public void evictAllCacheValues() {
        Objects.requireNonNull(cacheManager.getCache("hospitals")).clear();
        Objects.requireNonNull(cacheManager.getCache("allHospitals")).clear();
        Objects.requireNonNull(cacheManager.getCache("hospitalRooms")).clear();
    }
}
