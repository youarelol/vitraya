package com.vitraya.adjudication.engine.dto;

import com.vitraya.adjudication.engine.dto.enums.ClaimRunIdentifier;
import com.vitraya.adjudication.engine.mysql.entity.ClaimModuleStats;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimRunDTO {
    private long claimId;
    private ClaimRunIdentifier claimRunIdentifier;
    private ClaimModuleStats claimModuleStats;
    private boolean verifiedClaim=true;
    private boolean sendCallbackToHospitalPortal=true;
    private boolean doAdjudication=true;
}
