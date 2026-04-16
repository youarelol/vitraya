package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PreAuthRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ClaimDataRequestService {
    VitrayaInsurerClaimDataDTO validateAndParseClaimRequest(Object claimObject, MultipartFile[] files);

    boolean pushClaimDecision(ClaimData claimData);

    PreAuthRequest pushClaimDecisionPreview(ClaimData claimData);
}
