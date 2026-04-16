package com.vitraya.adjudication.engine.service.factory;

import com.vitraya.adjudication.engine.dto.enums.ClaimDataParseMapping;
import com.vitraya.adjudication.engine.service.ClaimDataRequestService;
import com.vitraya.adjudication.engine.service.VitrayaClaimDataRequestService;
import org.springframework.stereotype.Service;

@Service
public class ClaimsFactory {

    private final VitrayaClaimDataRequestService vitrayaClaimDataRequestService;

    public ClaimsFactory(VitrayaClaimDataRequestService vitrayaClaimDataRequestService) {
        this.vitrayaClaimDataRequestService = vitrayaClaimDataRequestService;
    }

    public ClaimDataRequestService getClaimsFactory(ClaimDataParseMapping identifier) {
        switch (identifier) {
            case ClaimDataParseMapping.DEFAULT_MAPPING:
                return vitrayaClaimDataRequestService;
            default:
                return null;
        }
    }
}
