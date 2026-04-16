package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.CashlessResponse;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExternalClaimService {
    private final ClaimService claimService;
    private final ClaimAdjudicationService claimAdjudicationService;
    private final ClaimTransitionService claimTransitionService;
    private final DocumentService documentMasterService;
    private final CorporateService corporateService;
    private final S3FileService s3FileUploadService;

    public ExternalClaimService(ClaimService claimService, ClaimAdjudicationService claimAdjudicationService,
                                ClaimTransitionService claimTransitionService, DocumentService documentMasterService,
                                CorporateService corporateService, S3FileService s3FileUploadService) {
        this.claimService = claimService;
        this.claimAdjudicationService = claimAdjudicationService;
        this.claimTransitionService = claimTransitionService;
        this.documentMasterService = documentMasterService;
        this.corporateService = corporateService;
        this.s3FileUploadService = s3FileUploadService;
    }

    public void processCashlessResponse(CashlessResponse cashlessResponse) throws IOException {
        log.info("cashlessResponse: {}", cashlessResponse);
        ClaimData claimData = validateCashlessResponse(cashlessResponse);
        Corporate corporate = corporateService.findCorporateById((int) claimData.getHospitalId());
        log.info("Claim data found for claim number: {}", cashlessResponse.getClaimNumber());
        boolean isHospitalResponse = cashlessResponse.getClaimStatusInString().equalsIgnoreCase("Query Replied") ||
                cashlessResponse.getClaimStatusInString().equalsIgnoreCase("Reconsideration");
        if (corporate != null && isHospitalResponse) {
            log.info("Corporate found for hospital id: {}", claimData.getHospitalId());
            String[] claimNumber = cashlessResponse.getClaimNumber().split("_");
            String vhiClaimId = claimNumber[claimNumber.length - 1].trim();

            fetchAndSaveDocsFromVHI(cashlessResponse, corporate, claimData, vhiClaimId);
        }
        claimAdjudicationService.processAndSaveCashlessResponse(claimData, cashlessResponse);
        String status = isHospitalResponse
                ? cashlessResponse.getClaimStatusInString() + " (Hospital) "
                : cashlessResponse.getClaimStatusInString() + " (Insurer) ";
        claimTransitionService.saveClaimTransitionData(claimData.getId(), status, claimData.getTxnId());
        if (!(cashlessResponse.getClaimStatusInString().equalsIgnoreCase("Acknowledgement") &&
                "Acknowledgement".equalsIgnoreCase(claimData.getClaimStatusText())))     {
            claimData.setClaimStatusText(claimData.getClaimStatusText());
        }

        if(cashlessResponse.getClaimStatusInString().equalsIgnoreCase("Acknowledgement")){
            claimData.setInsurerIdentifier(cashlessResponse.getPreAuthId());
        }

        if (!cashlessResponse.getClaimStatusInString().equalsIgnoreCase("Acknowledgement")) {
            claimData.setReceivedReverseFeed(true);
        }
        claimService.saveClaimData(claimData);

    }

    private void fetchAndSaveDocsFromVHI(CashlessResponse cashlessResponse, Corporate corporate, ClaimData claimData, String vhiClaimId) throws IOException {
        List<DocumentMaster> documentMasterList = new ArrayList<>();
        for (CashlessResponse.FilesObj filesObj : cashlessResponse.getFiles()) {
            DocumentMaster documentMasterObj = new DocumentMaster();
            if (filesObj == null || filesObj.getDocId() == null) {
                log.info("No document id found in the cashless response files object");
                continue;
            }
            ResponseEntity<ByteArrayResource> resourceResponseEntity = documentMasterService.getClaimDocumentByteArrayResource(Integer.parseInt(filesObj.getDocId()), corporate.getCorporateCode());
            ByteArrayResource byteArrayResource = resourceResponseEntity.getBody();
            if (byteArrayResource == null) {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY);
            }
            int claimIdReceived = Integer.parseInt(vhiClaimId);
            String fileName = s3FileUploadService.getS3FileName(claimIdReceived, filesObj.getName());
            String preSignedUrl = s3FileUploadService.uploadFileToS3(byteArrayResource, fileName);

            documentMasterList.add(documentMasterService.prepareDocumentMasterDataFromCashlessResponse(claimData, cashlessResponse.getClaimStatusInString(),
                    documentMasterObj, preSignedUrl, fileName, claimIdReceived, claimData.getTxnId()));
        }
        if (!documentMasterList.isEmpty()) {
            documentMasterService.saveDocumentMasterList(documentMasterList);
        } else {
            log.info("No documents found in the cashless response files object");
        }
    }

    private ClaimData validateCashlessResponse(CashlessResponse cashlessResponse) {
        // let's handle a special scenerio where the claim result is empty against the dummy reverse feed.
        if (cashlessResponse != null && cashlessResponse.getClaimStatusInString() == null) {
            log.info("Claim number and status not received. It might be dummy response.");
        }

        if (cashlessResponse.getClaimNumber() == null) {
            log.info("Claim number not received");
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM);
        }
        if (cashlessResponse.getClaimStatusInString() == null) {
            log.info("Claim status not received");
            throw new VitrayaException(VitrayaErrorCodes.INVALID_STATUS_RECEIVED);
        }
        ClaimData claimData = claimService.getClaimDataByIntimationNumber(cashlessResponse.getClaimNumber());
        if (claimData == null) {
            log.info("Claim data not found for claim number: {}", cashlessResponse.getClaimNumber());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM);
        } else {
            return claimData;
        }
    }
}
