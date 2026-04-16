package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.DocumentMasterListItem;
import com.vitraya.adjudication.engine.dto.request.FileDownloadRequest;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import com.vitraya.adjudication.engine.mysql.repository.DocumentMasterRepository;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class DocumentService {
    private final ClaimCommonService claimCommonService;
    @Value("${insurer.claim.download.url}")
    private String insurerClaimDownloadUrl;

    @Value("${file.download.url}")
    private String fileDownloadUrl;

    @Value("${x-auth-token}")
    private String xAuthToken;

    @Value("${a2s.callback.token}")
    private String a2sCallbackToken;

    private final DocumentMasterRepository documentMasterRepository;
    private final EncryptionUtils encryptionUtils;
    private final RestService restService;
    private final S3FileService s3FileService;

    public DocumentService(EncryptionUtils encryptionUtils, RestService restService,
                           DocumentMasterRepository documentMasterRepository, S3FileService s3FileService, ClaimCommonService claimCommonService) {
        this.encryptionUtils = encryptionUtils;
        this.restService = restService;
        this.documentMasterRepository = documentMasterRepository;
        this.s3FileService = s3FileService;
        this.claimCommonService = claimCommonService;
    }

    public DocumentMaster prepareDocumentMasterData(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData,
                                                    DocumentMasterListItem documentMasterListItem, DocumentMaster documentMaster,
                                                    String preSignedUrl, String storageFileName) {
        // Prepare document master object to save in the database using documentMasterListItem object.
        documentMaster.setParentTableId(vitrayaInsurerClaimData.getRequest().getClaim().getId());
        documentMaster.setParentTableIntimation(claimCommonService.getIntimationNumber(vitrayaInsurerClaimData));
        documentMaster.setDocumentType(UploadFilePrefix.EXTERNAL_CLAIM_DOCUMENT);
        documentMaster.setFileName(storageFileName);
        documentMaster.setStorageFileName(storageFileName);
        documentMaster.setFileType(documentMasterListItem.getFileType());
        documentMaster.setFileSupported(documentMasterListItem.isFileSupported());
        documentMaster.setDocPath(preSignedUrl);
        documentMaster.setPreSignedUrl(preSignedUrl);
        documentMaster.setDocumentStatus(DocumentStatusEnum.DOCUMENT_ADDED);
        documentMaster.setOmniDocsImageIndex("NA");
        documentMaster.setStage(DocClaimStage.getStage(getClaimStatus(vitrayaInsurerClaimData.getRequestType())));
        documentMaster.setLatestBillDocuments(true);
        documentMaster.setTxnId(vitrayaInsurerClaimData.getTxnId());

        return documentMaster;
    }

    public DocumentMaster prepareDocumentMasterDataFromCashlessResponse(ClaimData claimData,
                                                                        String claimStatusInString,
                                                                        DocumentMaster documentMaster,
                                                                        String preSignedUrl, String storageFileName,
                                                                        int claimIdReceived, String txnId) {
        documentMaster.setParentTableId(claimIdReceived);
        documentMaster.setParentTableIntimation(claimData.getIntimationNumber());
        documentMaster.setDocumentType(UploadFilePrefix.EXTERNAL_CLAIM_DOCUMENT);
        documentMaster.setFileName(storageFileName);
        documentMaster.setStorageFileName(storageFileName);
        documentMaster.setFileType("PDF");
        documentMaster.setFileSupported(true);
        documentMaster.setDocPath(preSignedUrl);
        documentMaster.setPreSignedUrl(preSignedUrl);
        documentMaster.setDocumentStatus(DocumentStatusEnum.DOCUMENT_ADDED);
        documentMaster.setOmniDocsImageIndex("NA");
        DocClaimStage stage = claimStatusInString.equalsIgnoreCase("Query Replied") ? DocClaimStage.QUERY_REPLIED :
                DocClaimStage.RECONSIDERATION;
        documentMaster.setStage(stage);
        documentMaster.setLatestBillDocuments(true);
        documentMaster.setTxnId(txnId);
        return documentMaster;
    }

    private ClaimStatus getClaimStatus(ClaimRequestTypeEnum requestType) {
        if (requestType == ClaimRequestTypeEnum.preauth_request) {
            return ClaimStatus.PRE_AUTHORISATION_RAISED;
        } else if (requestType == ClaimRequestTypeEnum.interim_enhancement_request) {
            return ClaimStatus.INTERIM_RAISED;
        } else if (requestType == ClaimRequestTypeEnum.query_response) {
            return ClaimStatus.QUERY_REPLY_RAISED;
        } else if (requestType == ClaimRequestTypeEnum.reconsideration_request) {
            return ClaimStatus.DENIAL_RECONSIDERATION_RAISED;
        } else if (requestType == ClaimRequestTypeEnum.settlement_request) {
            return ClaimStatus.SETTLEMENT_RAISED;
        } else if (requestType == ClaimRequestTypeEnum.final_enhancement_request) {
            return ClaimStatus.DISCHARGE_RAISED;
        } else {
            return ClaimStatus.PRE_AUTHORISATION_RAISED;
        }
    }

    public ResponseEntity<ByteArrayResource> getClaimDocumentByteArrayResource(int docId, String hosCode) {
        FileDownloadRequest fileDownloadRequest = new FileDownloadRequest(docId, hosCode);
        String encryptedFileDownloadRequest = encryptionUtils.encrypt(new Gson().toJson(fileDownloadRequest));

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("X-AUTH-TOKEN", a2sCallbackToken);
        headerMap.put("data", encryptedFileDownloadRequest);

        String urlToSend = insurerClaimDownloadUrl + docId;

        return restService.downloadFile(urlToSend, null, headerMap);
    }

    public void saveDocumentMasterList(List<DocumentMaster> documentMasterList) {
        documentMasterRepository.saveAll(documentMasterList);
    }

    public List<DocumentMaster> getClaimDocumentMasterList(String intimationNumber) {
        return documentMasterRepository.findByParentTableIntimation(intimationNumber);
    }

    public List<DocumentMaster> getClaimDocumentMasterList(String intimationNumber, DocClaimStage docClaimStage) {
        return documentMasterRepository.findByParentTableIntimation(intimationNumber, docClaimStage);
    }

    public List<DocumentMaster> getClaimDocumentMasterList(String intimationNumber, String claimTxnId) {
        return documentMasterRepository.findByParentTableIdAndTxnId(intimationNumber, claimTxnId);
    }

    public void addFileStorageResourceListToBody(HashMap<String, Object> requestBody, List<DocumentMaster> documentMasterList,
                                                 String intimationNumber) throws IOException {
        if (documentMasterList == null || documentMasterList.isEmpty()) {
            log.info("Document not found.");
            throw new VitrayaException(VitrayaErrorCodes.CLAIM_DOCUMENT_NOT_FOUND);
        } else if (documentMasterList.size() == 1) {
            processSingleDocument(requestBody, documentMasterList.getFirst());
        } else {
            processMultipleDocuments(requestBody, documentMasterList);
        }
    }

    private void processSingleDocument(HashMap<String, Object> requestBody, DocumentMaster documentMaster) throws IOException {
        Path downloadPath = Path.of(fileDownloadUrl, documentMaster.getFileName());
        boolean isPreSignedUrlGenerate = documentMaster.getPreSignedUrl() == null;

        File file = s3FileService.downloadFile(documentMaster, downloadPath, isPreSignedUrlGenerate);

        if (file != null) {
            if (file.getName().toLowerCase().endsWith(".zip")) {
                requestBody.put("documents", unzipFiles(file, fileDownloadUrl));
            } else {
                requestBody.put("documents", convertToFileSystemResource(file));
            }
        } else {
            requestBody.put("documents", null);
        }
    }

    private void processMultipleDocuments(HashMap<String, Object> requestBody, List<DocumentMaster> documentMasterList) {
        List<Resource> fileSystemResourceList = new ArrayList<>();
        documentMasterList.forEach(documentMaster -> {
            Path downloadPath = Path.of(fileDownloadUrl, documentMaster.getFileName());
            boolean isPreSignedUrlGenerate = documentMaster.getPreSignedUrl() == null;
            File file = null;
            try {
                file = s3FileService.downloadFile(documentMaster, downloadPath, isPreSignedUrlGenerate);
            } catch (IOException e) {
                log.error("Error while downloading file as", e);
            }

            if (file != null) {
                if (file.getName().toLowerCase().endsWith(".zip")) {
                    fileSystemResourceList.addAll(unzipFiles(file, fileDownloadUrl));
                } else {
                    if (!fileSystemResourceList.contains(convertToFileSystemResource(file))) {
                        fileSystemResourceList.add(convertToFileSystemResource(file));
                    }
                }
            }
        });
        requestBody.put("documents", fileSystemResourceList);
    }

    private List<FileSystemResource> unzipFiles(File zipFile, String destDirectory) {
        List<FileSystemResource> unzippedResources = new ArrayList<>();
        try {
            List<File> files = unzip(zipFile, destDirectory);
            if (!files.isEmpty()) {
                for (File file : files) {
                    unzippedResources.add(new FileSystemResource(file));
                }
            }
        } catch (Exception e) {
            log.info("caught some exception while unzipping the file as {}", e);
        }
        return unzippedResources;
    }

    private List<File> unzip(File zipFile, String destDirectory) throws IOException {
        List<File> extractedFiles = new ArrayList<>();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(destDirectory + fileName);
            // create directories for sub directories in zip
//            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            extractedFiles.add(newFile);
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return extractedFiles;
    }

    /**
     * Converts a File object to a FileSystemResource.
     *
     * @param file the File to be converted
     * @return the FileSystemResource representation of the file
     */
    public static Resource convertToFileSystemResource(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        return new FileSystemResource(file);
    }

    public void updateDocumentMaster(DocumentMaster documentMaster) {
        documentMasterRepository.save(documentMaster);
    }

    public List<DocumentMaster> getLatestTxnClaimDocuments(String intimationNumber, String txnId) {
        List<DocumentMaster> documentMasterList = documentMasterRepository.findByParentTableIdAndTxnId(intimationNumber, txnId);
        if (documentMasterList == null || documentMasterList.isEmpty()) {
            log.info("No latest bill documents found for claim id: {}", intimationNumber);
            throw new VitrayaException(VitrayaErrorCodes.CLAIM_DOCUMENT_NOT_FOUND);
        }

        return documentMasterList;
    }
}
