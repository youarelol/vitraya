package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.response.VCVMTokenResponse;
import com.vitraya.adjudication.engine.dto.response.VNeuronResponseDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.repository.ClaimDataRepository;
import com.vitraya.adjudication.engine.mysql.repository.ErrorMessageLogRepository;
import com.vitraya.adjudication.engine.mysql.repository.VneuronResponseRepository;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class VNeuronService {

    private final ErrorMessageLogsService errorMessageLogsService;
    private final ProcedureService procedureService;
    private final CorporateService corporateService;
    private final DocumentService documentService;
    private final ClaimCommonService claimCommonService;
    private final VneuronResponseRepository vneuronResponseRepository;
    private final ClaimDataRepository claimDataRepository;
    private final CommunicationService communicationService;
    private final RestService restService;

    @Value("${vcvm.base.url}")
    private String vcvmBaseUrl;

    @Value("${vcvm.user}")
    private String vneuronUser;

    @Value("${vcvm.pass}")
    private String vneuronPassword;

    @Value("${max.rest.retry.count:3}")
    private int MAX_REST_RETRY_COUNT;

    @Value("${module.failed.email.to}")
    private String CLAIM_REGISTRATION_FAILED_EMAIL_TO;


    public VNeuronService(RestService restService, ProcedureService procedureService, CorporateService corporateService,
                          DocumentService documentService, ClaimCommonService claimCommonService,
                          VneuronResponseRepository vneuronResponseRepository, ClaimDataRepository claimDataRepository,
                          CommunicationService communicationService,
                          ErrorMessageLogsService errorMessageLogsService) {
        this.restService = restService;
        this.procedureService = procedureService;
        this.corporateService = corporateService;
        this.documentService = documentService;
        this.claimCommonService = claimCommonService;
        this.vneuronResponseRepository = vneuronResponseRepository;
        this.claimDataRepository = claimDataRepository;
        this.communicationService = communicationService;
        this.errorMessageLogsService = errorMessageLogsService;
    }

    public void processVNeuronRequest(ClaimRunDTO claimRunDTO) throws IOException, InterruptedException {
        ClaimData claimData = claimDataRepository.findById(claimRunDTO.getClaimId()).orElse(null);

        if (claimData == null) {
            log.error("Claim data not found for claim id: {}", claimRunDTO.getClaimId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }

        long startTime = System.currentTimeMillis();
        // Fetch the vNeuron token
        String token = getVNeuronToken();
        boolean isNewClaim = claimRunDTO.getClaimRunIdentifier().equals(ClaimRunIdentifier.FRESH_CLAIM_VNEURON_RUN);
        HashMap<String, Object> requestBody = prepareVneuronRequestBody(claimData, claimRunDTO.getClaimId(), isNewClaim);

        // ToDo: We need to start storing the
        String url = vcvmBaseUrl + "claims";

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer " + token);

        String responseObj = null;
        int retryCount = 0;

        boolean isVneuronExecuted = false;
//        while (retryCount < MAX_REST_RETRY_COUNT) {
            try {
                responseObj = getVneuronResponse(url, requestBody, headerMap);
                isVneuronExecuted = true;
//                break; // Exit loop if successful
            } catch (Exception e) {
//                e.printStackTrace();
//                retryCount++;
//                if (retryCount >= MAX_REST_RETRY_COUNT) {
//                    log.error("vNeuron error after max retry attempt. Attempt: {}", retryCount, e);
//                } else {
//                    log.info("Received error from vNeuron service. Retrying... Attempt: {}", retryCount);
//                }

                log.error("{} Caught an exception while executing the vNeuron as - {}", claimData.getIntimationNumber(), e.getMessage());
//            }
        }

        long endTime = System.currentTimeMillis();
        log.info("vNeuron response received for claim data id: {} is {}", claimData.getId(), responseObj);

        VNeuronResponseDTO vNeuronResponseDTO;
        if (responseObj == null || responseObj.equalsIgnoreCase("null") || responseObj.isEmpty()) {
            communicationService.sendEmail(claimData.getIntimationNumber() + " VNeuron Failed ",
                    claimData.getIntimationNumber() + "Not received the response from vNeuron please check",
                    CLAIM_REGISTRATION_FAILED_EMAIL_TO);
            String result = "Medical admissibility failed to run";
            String id = "Medical Admissibility Failed";
            vNeuronResponseDTO = getvNeuronResponseDTO(result, id);
            ErrorMsgType error = ErrorMsgType.MEDICAL_ADMISSIBILITY_FAILURE;
            errorMessageLogsService.saveErrorMessages(claimData, error);
            log.info("Saving Error Message Logs for Vneuron failure in processVNeuronRequest method for {}", claimData.getIntimationNumber());
        } else {
            vNeuronResponseDTO = GsonUtils.fromJson(responseObj, VNeuronResponseDTO.class);
        }

        // ToDo: This is the temp block
        processVNeuronResponse(claimData, claimRunDTO, vNeuronResponseDTO, startTime, endTime, isVneuronExecuted);
    }

    public VNeuronResponseDTO getvNeuronResponseDTO(String result, String id) {
        VNeuronResponseDTO vNeuronResponseDTO;
        vNeuronResponseDTO = new VNeuronResponseDTO();
        vNeuronResponseDTO.setAdjudicationResult(result);
        vNeuronResponseDTO.setId(id);
        return vNeuronResponseDTO;
    }

    public void processVNeuronResponse(ClaimData claimData, ClaimRunDTO claimRunDTO, VNeuronResponseDTO vNeuronResponseDTO,
                                       long startTime, long endTime, boolean vNeuronSuccess) {
        try {
            saveVneuronResponse(claimData, vNeuronResponseDTO, (endTime - startTime), vNeuronSuccess);
            ClaimModuleStats claimModuleStats = claimCommonService.recordClaimModuleStats(claimData, claimRunDTO.getClaimModuleStats(),
                    ClaimModulesEnum.MEDICAL_ADMISSIBILITY,
                    vNeuronResponseDTO.getId() != null ? vNeuronResponseDTO.getId() : "NA",
                    (int) (endTime - startTime));
        } catch (Exception e) {
            log.error("Error while saving vNeuron response for claim data id: {}", claimData.getId(), e);
        }
        claimCommonService.checkClaimModuleCompletion(claimData, vNeuronSuccess);
    }

    private String getVneuronResponse(String url, HashMap<String, Object> requestBody, HashMap<String, String> headerMap) throws InterruptedException {
        return restService.executePostWithFile(url, requestBody, String.class, headerMap);
    }

    private void saveVneuronResponse(ClaimData claimData, VNeuronResponseDTO vNeuronResponseDTO, long tat,
                                     boolean vNeuronSuccess) {
        try {
            VneuronResponse vneuronResponse = new VneuronResponse();
            vneuronResponse.setClaimDataId(claimData.getId());
            vneuronResponse.setVnueronResponse(GsonUtils.toJson(vNeuronResponseDTO));
            vneuronResponse.setMedicalIdentifier(vNeuronResponseDTO != null ? vNeuronResponseDTO.getId() : "NA");
            vneuronResponse.setDateCreated(new Date());
            vneuronResponse.setVneuronTat(tat + " ms");
            vneuronResponse.setVneuronDecision(vNeuronResponseDTO != null ? vNeuronResponseDTO.getAdjudicationResult() : "NA");
            vneuronResponseRepository.save(vneuronResponse);

            // Save vNeuron response to claim data
            // ToDo: have to implement the claim level saving and audit data.
        } catch (Exception e) {
            log.info("Caught exception while saving vneuron data", e);
        }

    }

    public VneuronResponse getVneuronResponseByClaimDataId(long claimId) {
        return vneuronResponseRepository.getVneuronResponseByClaimDataId(claimId);
    }

    private String getVNeuronToken() {
        MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
        String loginUrl;

        // ToDo: Move this to database
        valueMap.put("username", Collections.singletonList(vneuronUser));
        valueMap.put("password", Collections.singletonList(vneuronPassword));
        loginUrl = "login";

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
        headerMap.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        VCVMTokenResponse vcvmTokenResponse = restService.post(vcvmBaseUrl + loginUrl, valueMap, VCVMTokenResponse.class, headerMap);

        return vcvmTokenResponse.getAccess_token();
    }

    private HashMap<String, Object> prepareVneuronRequestBody(ClaimData claimData, long claimId, boolean isNewClaim) throws IOException {
        Procedures procedures = procedureService.findProcedureById(claimData.getProcedureId()).orElse(null);
        Corporate corporate = corporateService.getUserCorporate((int) claimData.getHospitalId());

        HashMap<String, Object> requestBody = new HashMap<>();
        if (isNewClaim) {
            requestBody.put("patientName", claimData.getPatientName());
            requestBody.put("referenceClaimId", claimData.getIntimationNumber());
            requestBody.put("caseType", getCaseType(claimData.getClaimStatus()));
            requestBody.put("adjudicate", Boolean.TRUE);
            requestBody.put("hospitalCode", "hos-default");
            requestBody.put("procedureCode", procedures != null ? procedures.getVneuronSctidCode() : null);

            List<DocumentMaster> documentMaster = documentService.getClaimDocumentMasterList(claimData.getIntimationNumber());
            documentService.addFileStorageResourceListToBody(requestBody, documentMaster, claimData.getIntimationNumber());
        } else {
            requestBody.put("status", "OCRED");
            requestBody.put("rerunFlag", true);
            requestBody.put("adjudicate", true);
        }

        return requestBody;
    }

    private String getCaseType(ClaimStatus claimStatus) {
        if (ClaimStatus.isPreAuthStage(claimStatus)) {
            return "PRE_AUTH";
        } else
            return "SETTLEMENT";
    }
}
