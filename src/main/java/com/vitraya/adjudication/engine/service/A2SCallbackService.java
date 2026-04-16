package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.A2SCallBackRequest;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.NivaPushEvent;
import com.vitraya.adjudication.engine.mysql.entity.NivaRequestData;
import com.vitraya.adjudication.engine.mysql.repository.ClaimDataRepository;
import com.vitraya.adjudication.engine.utils.AppConstants;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class A2SCallbackService {

    private final ClaimCommonService claimCommonService;

    private final KafkaProducerService kafkaProducerService;


    @Value("${a2s.callback.url}")
    private String a2sCallbackUrl;

    @Value("${a2s.callback.endpoint}")
    private String a2sCallbackEndpoint;

    @Value("${a2s.callback.token}")
    private String a2sCallbackToken;

    private final ClaimDataRepository claimDataRepository;

    private final RestService restService;
    private final NivaPushEventService nivaPushEventService;

    public A2SCallbackService(RestService restService, ClaimDataRepository claimDataRepository, ClaimCommonService claimCommonService, KafkaProducerService kafkaProducerService, NivaPushEventService nivaPushEventService) {
        this.restService = restService;
        this.claimDataRepository = claimDataRepository;
        this.claimCommonService = claimCommonService;
        this.kafkaProducerService = kafkaProducerService;
        this.nivaPushEventService = nivaPushEventService;
    }


    public void processA2SSuccessCallback(long claimId, ClaimStatus claimStatus, String intimationNumber,
                                          String preAuthId, boolean isDuplicateClaim, boolean isPreAuth, boolean isInterim,
                                          boolean isDischarge, boolean isQueryReply, boolean isReconsideration,
                                          boolean isSettlement, NivaRequestData nivaRequestData,
                                          Long startTime, NivaPushEvent nivaPushEvent, boolean savePushEvent) {
        log.info("Processing success A2S Callback for claim id: {}", claimId);
        if (savePushEvent) {
            try {
                long endTime = System.currentTimeMillis();
                nivaPushEventService.updateEvent(nivaPushEvent.getId(),  NivaPushStatus.PUSH_SUCCESS.toString(), "N/A", endTime - startTime);
            } catch (Exception ex) {
                log.error("claim -{} Exception found while fetching niva push event data. {}", claimId, ex.getLocalizedMessage());
            }
        }
        A2SCallBackRequest A2SRequestDTO = prepareA2SCallbackRequest(intimationNumber, preAuthId, true);
        if (isPreAuth) {
            A2SRequestDTO.setRequestData(nivaRequestData != null ? nivaRequestData.getRequestData() : null);
        }
        CallBackStageEnum callBackStage = getClaimStage(claimStatus);
        if (callBackStage != null) {
            A2SRequestDTO.setCallback_stage(callBackStage.toString());
        }
        if (isDuplicateClaim) {
            A2SRequestDTO.setCallback_stage(CallBackStageEnum.DUPLICATE_PREAUTH.toString());
        }
        try {
            makeA2SCallbackPostCall(A2SRequestDTO);
        } catch (Exception e) {
            log.error("Error occurred while making A2S Callback post call", e);
        }
    }

    public void processA2SFailureCallback(long claimId, ClaimStatus claimStatus, String intimationNumber, Long startTime,
                                          NivaPushEvent nivaPushEvent) {
        try {
            long endTime = System.currentTimeMillis();
            nivaPushEventService.updateEvent(nivaPushEvent.getId(), NivaPushStatus.PUSH_FAILED.toString(), "N/A", endTime - startTime);
        } catch (Exception ex) {
            log.error("claim -{} Exception found while fetching niva push event data. {}", claimId, ex.getLocalizedMessage());
        }
        log.info("Processing failure A2S Callback for claim id: {}", claimId);
        //send claim to email
        ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                .claimRunIdentifier(ClaimRunIdentifier.API_FAILURE_EMAIL_CLAIM_RUN)
                .claimId(claimId)
                .sendCallbackToHospitalPortal(false)
                .claimModuleStats(claimCommonService.getLatestClaimModuleStats(claimId)).build();

        addClaimToEmailQueue(claimRunDTO);

        A2SCallBackRequest A2SRequestDTO = prepareA2SCallbackRequest(intimationNumber, null, false);
        A2SRequestDTO.setRequestData(null);
        CallBackStageEnum callBackStage = getClaimStage(claimStatus);
        if (callBackStage != null) {
            A2SRequestDTO.setCallback_stage(callBackStage.toString());
        }
        try {
            makeA2SCallbackPostCall(A2SRequestDTO);
        } catch (Exception e) {
            log.error("Error occurred while making A2S Callback post call: ", e);
        }
    }

    public void addClaimToEmailQueue(ClaimRunDTO claimRunDTO) {
        kafkaProducerService.sendMessage(AppConstants.EMAIL_FLOW_CLAIM_TOPIC_NAME, claimRunDTO.getClaimId(), GsonUtils.toJson(claimRunDTO));
    }

    private A2SCallBackRequest prepareA2SCallbackRequest(String intimationNumber, String preAuthId, boolean isSuccess) {
        A2SCallBackRequest A2SRequestDTO = new A2SCallBackRequest();
        A2SRequestDTO.setSuccess(isSuccess);
        A2SRequestDTO.setPreauth_id(preAuthId);
        A2SRequestDTO.setIntimation_number(claimCommonService.getIntimationNumberPlain(intimationNumber));
        if (isSuccess) {
            A2SRequestDTO.setMessage("Claim processed successfully");
        } else {
            A2SRequestDTO.setMessage("Claim not processed successfully");
        }
        return A2SRequestDTO;
    }

    private CallBackStageEnum getClaimStage(ClaimStatus claimStatus) {
        return CallBackStageEnum.mapClaimStage(claimStatus.toString());
    }


    public String makeA2SCallbackPostCall(A2SCallBackRequest A2SRequestDTO) throws VitrayaException {
        String A2SCallBackUrl = a2sCallbackUrl + a2sCallbackEndpoint;
        Map<String, Object> apiResponse = null;
        try {
            log.info("Making post call, URL: {} \n Request sent to A2S: {}", A2SCallBackUrl, new Gson().toJson(A2SRequestDTO));
            apiResponse = restService.post(A2SCallBackUrl, A2SRequestDTO, Map.class, getA2SCalBackHeaders());
            // {success=true, data=, message=null}
            log.info("Response received from A2SCallBack Api: {}", new Gson().toJson(apiResponse));
        } catch (Exception e) {
            log.info("Some error occurred while making NIVA api call please check logs for details:", e);
            throw e;
        }

        return apiResponse.toString();
    }

    private Map<String, String> getA2SCalBackHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headerMap.put("X-Auth-Token", a2sCallbackToken);
        return headerMap;
    }

}
