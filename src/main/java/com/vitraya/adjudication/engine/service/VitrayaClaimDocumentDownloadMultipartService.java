package com.vitraya.adjudication.engine.service;


import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.DocumentMasterListItem;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class VitrayaClaimDocumentDownloadMultipartService implements ClaimDocumentDownloadService {

    private final S3FileService s3FileUploadService;
    private final DocumentService documentMasterService;

    public VitrayaClaimDocumentDownloadMultipartService(S3FileService s3FileUploadService, DocumentService documentMasterService) {
        this.s3FileUploadService = s3FileUploadService;
        this.documentMasterService = documentMasterService;
    }

    @Override
    public void saveDocuments(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, MultipartFile[] files) throws IOException {
        List<DocumentMaster> documentMasterList = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String fileName = s3FileUploadService.getS3FileName(vitrayaInsurerClaimData.getRequest().getClaim().getId(), file.getOriginalFilename());
                if (fileName == null || fileName.isEmpty()) {
                    throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY);
                }
                DocumentMasterListItem documentMasterListItem = new DocumentMasterListItem();
                documentMasterListItem.setFileName(fileName);
                documentMasterListItem.setFileType(file.getContentType());
                documentMasterListItem.setFileSupported(true);
                ByteArrayResource byteArrayResource = new ByteArrayResource(file.getBytes());
                String preSignedUrl = s3FileUploadService.uploadFileToS3(byteArrayResource, fileName);
                DocumentMaster documentMaster = new DocumentMaster();
                documentMasterList.add(documentMasterService.prepareDocumentMasterData(vitrayaInsurerClaimData,
                        documentMasterListItem, documentMaster, preSignedUrl, fileName));
            }
            documentMasterService.saveDocumentMasterList(documentMasterList);
        } else {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY);
        }
    }
}
