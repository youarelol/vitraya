package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.InsurerEncryptedClaimRequest;
import com.vitraya.adjudication.engine.dto.request.TariffLineItemUpdateRequest;
import com.vitraya.adjudication.engine.dto.request.UpdateClaimRequest;
import com.vitraya.adjudication.engine.dto.request.UpdateDecisionRequest;
import com.vitraya.adjudication.engine.dto.response.BillTariffPmlAmountData;
import com.vitraya.adjudication.engine.dto.response.ClaimDetailsResponseDTO;
import com.vitraya.adjudication.engine.dto.response.RestAPIResponse;
import com.vitraya.adjudication.engine.dto.response.TariffLineItemResponseDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PreAuthRequest;
import com.vitraya.adjudication.engine.service.ClaimService;
import com.vitraya.adjudication.engine.service.IframeService;
import com.vitraya.adjudication.engine.service.PMLService;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/iframe")
public class IframeController {
    private final ClaimService claimService;
    private final IframeService iframeService;
    private final EncryptionUtils encryptionUtils;
    private final PMLService pmlService;

    public IframeController(ClaimService claimService, IframeService iframeService, EncryptionUtils encryptionUtils, PMLService pmlService) {
        this.claimService = claimService;
        this.iframeService = iframeService;
        this.encryptionUtils = encryptionUtils;
        this.pmlService = pmlService;
    }

    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;

    @GetMapping("/data")
    public RestAPIResponse getClaimDetails(@RequestParam("encryptedData") String encryptedData) throws Exception {
        log.info("IFRAME: Request received to get claim details");
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new VitrayaException(VitrayaErrorCodes.IFRAME_INVALID_REQUEST);
        }

        InsurerEncryptedClaimRequest insurerEncryptedClaimRequest = iframeService.getDecryptedIframeData(encryptedData.replaceAll(" ", "+"));
        ClaimDetailsResponseDTO claimDetailsResponseDTO = claimService.prepareClaimDataByIntimationNumber(
                insurerEncryptedClaimRequest.getVitrayaClaimId());
        //here the id in claimDetailsResponseDTO i want to encrypt and return in the response
        String claimId = claimDetailsResponseDTO.getId();

        String encryptedClaimId = encryptionUtils.encrypt(String.valueOf(claimId), apiParamEncryptiondecryptionKey).replaceAll("/", "_");

        claimDetailsResponseDTO.setId(encryptedClaimId);

        if (claimDetailsResponseDTO != null) {
            return RestAPIResponse.buildSuccess(claimDetailsResponseDTO);
        } else {
            return RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "Claims not found for the given criteria",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST.getCode(), "Claims not found for the given criteria");
        }
    }

    @PostMapping("/tariff/update")
    public RestAPIResponse updateClaimTariffDataById(@Valid @RequestBody TariffLineItemUpdateRequest tariffLineItemUpdateRequest) throws VitrayaException, IOException {
        log.info("IFRAME CLAIM UPDATE: Request received to update tariff line items");
        tariffLineItemUpdateRequest.setClaimDataIdStr(encryptionUtils.validateEncrytedString(tariffLineItemUpdateRequest.getClaimDataIdStr()));
        String decryptedClaimId = encryptionUtils.decrypt(tariffLineItemUpdateRequest.getClaimDataIdStr(), apiParamEncryptiondecryptionKey);
        tariffLineItemUpdateRequest.setClaimDataId(Long.parseLong(decryptedClaimId));
        List<TariffLineItemResponseDTO> lineItemList = claimService.addUpdateTariffLineItems(tariffLineItemUpdateRequest);
        return lineItemList != null && !lineItemList.isEmpty()
                ? RestAPIResponse.buildSuccess(lineItemList)
                : RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "Claims not found for the given criteria",
                VitrayaErrorCodes.ERROR_UPDATING_LINE_ITEMS.getCode(), "Claims not found for the given criteria");
    }

    @PostMapping(value = {"/update/decision"})
    public RestAPIResponse updateDecision(@Valid @RequestBody UpdateDecisionRequest updateDecisionRequest) throws VitrayaException {
        log.info("IFRAME CLAIM UPDATE: Request received to update decision for claim with id: {}", updateDecisionRequest.getClaimDataId());
        updateDecisionRequest.setClaimDataIdStr(encryptionUtils.validateEncrytedString(updateDecisionRequest.getClaimDataIdStr()));
        String decryptedClaimId = encryptionUtils.decrypt(updateDecisionRequest.getClaimDataIdStr(), apiParamEncryptiondecryptionKey);
        updateDecisionRequest.setClaimDataId(Long.parseLong(decryptedClaimId));
        if (decryptedClaimId == null || decryptedClaimId.isEmpty()) {
            return RestAPIResponse.buildFail(400, "Invalid claim ID",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_ID.getCode(), "Claim ID cannot be null or empty");
        }

        boolean isDecisionUpdated = claimService.updateDecision(updateDecisionRequest);
        return isDecisionUpdated
                ? RestAPIResponse.buildSuccess("Decision Updated Successfully")
                : RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "Claim decision not updated",
                VitrayaErrorCodes.CLAIM_DECISION_NOT_UPDATED.getCode(), "Claim decision not updated");
    }

    @PostMapping(value = {"/push/decision/{claimDataId}"})
    public RestAPIResponse pushDecisionToInsurer(@PathVariable("claimDataId") String encryptedClaimId) throws VitrayaException {
        encryptedClaimId = encryptionUtils.validateEncrytedString(encryptedClaimId);
        String decryptedClaimIdString = encryptionUtils.decrypt(encryptedClaimId, apiParamEncryptiondecryptionKey);
        if (decryptedClaimIdString == null || decryptedClaimIdString.isEmpty()) {
            return RestAPIResponse.buildFail(400, "Invalid claim ID",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_ID.getCode(), "Claim ID cannot be null or empty");
        }
        long decryptedClaimId = Long.parseLong(decryptedClaimIdString);
        log.info("Request received to push the claim decision to insurer for claimDataId: {}", decryptedClaimId);
        boolean isDecisionPushed = claimService.pushClaimDecision(decryptedClaimId);
        return isDecisionPushed
                ? RestAPIResponse.buildSuccess("Claim decision pushed successfully")
                : RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "Claim decision not pushed",
                VitrayaErrorCodes.CLAIM_DECISION_NOT_PUSHED.getCode(), "Claim decision not pushed");
    }

    @PostMapping(value = {"/push/decision/preview/{claimDataId}"})
    public RestAPIResponse pushDecisionPreviewToInsurer(@PathVariable("claimDataId") String encryptedClaimId) throws VitrayaException {
        encryptedClaimId = encryptionUtils.validateEncrytedString(encryptedClaimId);
        String decryptedClaimIdString = encryptionUtils.decrypt(encryptedClaimId, apiParamEncryptiondecryptionKey);
        if (decryptedClaimIdString == null || decryptedClaimIdString.isEmpty()) {
            return RestAPIResponse.buildFail(400, "Invalid claim ID",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_ID.getCode(), "Claim ID cannot be null or empty");
        }
        long decryptedClaimId = Long.parseLong(decryptedClaimIdString);
        log.info("Request received to push the claim decision to insurer for claimDataId: {}", decryptedClaimId);
        PreAuthRequest preAuthRequest = claimService.pushClaimDecisionPreview(decryptedClaimId);

        return RestAPIResponse.buildSuccess(preAuthRequest);
    }

    @PostMapping("/update/claim")
    public RestAPIResponse getClaimDetailsForUpdate(@RequestBody UpdateClaimRequest updateClaimRequest) throws VitrayaException, ParseException {
        log.info("IFRAME CLAIM UPDATE: Request received to get claim details for update with request: {}", GsonUtils.toJson(updateClaimRequest));
        updateClaimRequest.setClaimId(encryptionUtils.validateEncrytedString(updateClaimRequest.getClaimId()));
        String decryptedClaimId = encryptionUtils.decrypt(updateClaimRequest.getClaimId(), apiParamEncryptiondecryptionKey);
        if (decryptedClaimId == null || decryptedClaimId.isEmpty()) {
            return RestAPIResponse.buildFail(400, "Invalid claim ID",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_ID.getCode(), "Claim ID cannot be null or empty");
        }
        updateClaimRequest.setClaimId(decryptedClaimId);
        ClaimData claimData = claimService.updateClaimData(updateClaimRequest);
        if (claimData != null) {
            return RestAPIResponse.buildSuccess("Claim details updated successfully");
        } else {
            return RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "Claims not found for the given criteria",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST.getCode(), "Claims not found for the given criteria");
        }
    }

    @PostMapping("/pml/data/update/{claimDataId}")
    public RestAPIResponse updatePMLData(@RequestBody List<BillTariffPmlAmountData> billTariffPmlAmountDataList,
                                         @PathVariable("claimDataId") String claimDataId) throws VitrayaException {
        log.info("[IFRAME] PML Data Update request received as : {}", GsonUtils.toJson(billTariffPmlAmountDataList));
        claimDataId = encryptionUtils.validateEncrytedString(claimDataId);
        String decryptedClaimDataId = encryptionUtils.decrypt(claimDataId, apiParamEncryptiondecryptionKey);
        boolean isUpdated = pmlService.updatePmlRuleData(Long.parseLong(decryptedClaimDataId),
                billTariffPmlAmountDataList);
        if (isUpdated) {
            return RestAPIResponse.buildSuccess("PML details updated successfully");
        } else {
            return RestAPIResponse.buildFail(HttpStatus.BAD_REQUEST.value(), "PML data not updated",
                    VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST.getCode(), "PML data not updated");
        }
    }
}