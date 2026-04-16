package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.InsurerDTO;
import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.CorporateRequest;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.repository.CorporateRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CorporateService {
    private final CorporateRepository corporateRepository;
    private final UserService userService;

    public CorporateService(CorporateRepository corporateRepository, UserService userService) {
        this.corporateRepository = corporateRepository;
        this.userService = userService;
    }

    public void validateCorporateRequest(@Valid CorporateRequest corporateRequest) {
        isCorporateUnique(corporateRequest);
    }

    private void isCorporateUnique(CorporateRequest corporateRequest) {
        Corporate corporate = corporateRepository.findCorporateByCorporateCode(corporateRequest.getCorporateCode());

        if (corporate != null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CORPORATE_CODE.getCode(),
                    new Object[]{corporateRequest.getCorporateCode()});
        }

        corporate = corporateRepository.findCorporateByName(corporateRequest.getName());

        if (corporate != null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CORPORATE_NAME.getCode(),
                    new Object[]{corporateRequest.getName()});
        }
    }

    public Corporate createCorporate(CorporateRequest corporateRequest) {
        Corporate corporate = Corporate.builder()
                .name(corporateRequest.getName())
                .corporateCode(corporateRequest.getCorporateCode())
                .status(corporateRequest.getStatus())
                .type(corporateRequest.getType())
                .enabledForReview(false)
                .build();

        return corporateRepository.save(corporate);
    }

    public Iterable<Corporate> getAllCorporates() {
        return corporateRepository.findAll();
    }

    public Optional<Corporate> getCorporateByID(int corporateId) {
        return corporateRepository.findById((long) corporateId);
    }

    public Corporate findCorporateById(int corporateId) {
        return corporateRepository.findCorporateById(corporateId);
    }

    public Corporate getUserCorporate(int corporateId) {
        Optional<Corporate> corporate = getCorporateByID(corporateId);
        if (corporate.isEmpty()) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CORPORATE_ID.getCode(),
                    new Object[]{});
        }

        return corporate.get();
    }

    public int getInsuranceAgencyId(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        if (vitrayaInsurerClaimData != null && vitrayaInsurerClaimData.getPayorCode() != null) {
            Corporate corporate = corporateRepository.findCorporateByCorporateCode(vitrayaInsurerClaimData.getPayorCode().toUpperCase());
            if (corporate != null) {
                return Math.toIntExact(corporate.getId());
            } else {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_CORPORATE_CODE);
            }
        } else {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }
    }

    // Todo: need to think of a solution where provider code is different for same hospital w.r.t to different insurers.
    public int getHospitalId(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        if (vitrayaInsurerClaimData != null && vitrayaInsurerClaimData.getProviderCode() != null) {
            Corporate corporate = corporateRepository.findCorporateByCorporateCode(vitrayaInsurerClaimData.getProviderCode());
            if (corporate != null) {
                return Math.toIntExact(corporate.getId());
            } else {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_CORPORATE_CODE);
            }
        } else {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }
    }

    //GET all Active Insurers
    public List<InsurerDTO> findAllInsurer(){
        return corporateRepository.findAllInsurer();
    }
}
