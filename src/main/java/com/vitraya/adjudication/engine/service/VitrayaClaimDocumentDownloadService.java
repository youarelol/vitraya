package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.DocumentMasterListItem;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class VitrayaClaimDocumentDownloadService implements ClaimDocumentDownloadService {

    private final S3FileService s3FileUploadService;
    private final DocumentService documentMasterService;

    public VitrayaClaimDocumentDownloadService(S3FileService s3FileUploadService, DocumentService documentMasterService) {
        this.s3FileUploadService = s3FileUploadService;
        this.documentMasterService = documentMasterService;
    }

    @Override
    public void saveDocuments(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, MultipartFile[] files) throws IOException {
        if (vitrayaInsurerClaimData == null || vitrayaInsurerClaimData.getRequest() == null
                || vitrayaInsurerClaimData.getRequest().getDocumentMasterList() == null
                || vitrayaInsurerClaimData.getRequest().getDocumentMasterList().isEmpty()) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY);
        }

        String hosCode = vitrayaInsurerClaimData.getProviderCode();
        List<DocumentMaster> documentMasterList = new ArrayList<>();
        for (DocumentMasterListItem documentMasterListItem : vitrayaInsurerClaimData.getRequest().getDocumentMasterList()) {
            DocumentMaster documentMaster = new DocumentMaster();
            int docId = Integer.parseInt(documentMasterListItem.getNote());

            ResponseEntity<ByteArrayResource> resourceResponseEntity = documentMasterService.getClaimDocumentByteArrayResource(docId, hosCode);
            ByteArrayResource byteArrayResource = resourceResponseEntity.getBody();

            if (byteArrayResource == null) {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY);
            }

            // Now we need to save the resource object as a file.
            // String fileName = documentMasterListItem.getNote() + File.separator + documentMasterListItem.getFileName();
            String fileName = s3FileUploadService.getS3FileName(vitrayaInsurerClaimData.getRequest().getClaim().getId(), documentMasterListItem.getFileName());
            String preSignedUrl = s3FileUploadService.uploadFileToS3(byteArrayResource, fileName);

            documentMasterList.add(documentMasterService.prepareDocumentMasterData(vitrayaInsurerClaimData,
                    documentMasterListItem, documentMaster, preSignedUrl, fileName));
        }

        documentMasterService.saveDocumentMasterList(documentMasterList);
    }
}
