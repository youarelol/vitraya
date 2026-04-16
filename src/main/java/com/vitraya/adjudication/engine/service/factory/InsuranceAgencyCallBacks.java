package com.vitraya.adjudication.engine.service.factory;

import com.vitraya.adjudication.engine.dto.response.NivaResponse;
import com.vitraya.adjudication.engine.dto.response.NivaSettlementResponse;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.NivaPushEvent;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PreAuthRequest;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.SettlementRequest;

public interface InsuranceAgencyCallBacks {
    boolean sendWdmsSoapRequest(String intimationNumber, String activityType,
                                String preAuthId, ClaimData claimData, NivaPushEvent nivaPushEvent);

    NivaResponse sendClaimRequestToNiva(Object preAuthRequest,
                                        ClaimData claimData,
                                        boolean isPreAuth,
                                        boolean isInterim,
                                        boolean isDischarge,
                                        boolean isQueryReply,
                                        boolean isReconsideration,
                                        boolean isSettlement,
                                        boolean isRepush,
                                        NivaPushEvent nivaPushEvent,
                                        long preAuthStartTime);

    NivaSettlementResponse sendSettlementRequestToNiva(SettlementRequest settlementRequest, ClaimData claimData
            , NivaPushEvent nivaPushEvent, long settlementStartTime);
}
