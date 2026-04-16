package com.vitraya.adjudication.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import com.vitraya.adjudication.engine.dto.enums.NivaRequestType;
import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.DocumentMasterListItem;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.repository.*;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class S3FileService {
    private final DocumentMasterRepository documentMasterRepository;
    private final RestService restService;
    private final BillTariffResponseRepository billTariffResponseRepository;
    private final CorporateRepository corporateRepository;
    private final VneuronResponseRepository vneuronResponseRepository;
    private final ClaimDataRepository claimDataRepository;
    private final EncryptionUtils encryptionUtils;
    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.key}")
    private String awsS3Key;

    @Value("${file.download.url}")
    private String fileDownloadUrl;

    @Value("${tariff.doc.url}")
    private String tariffBaseURL;

    @Value("${tariff.doc.url.token}")
    private String tariffBaseURLToken;

    @Value("${bill.doc.url}")
    private String billDocURL;

    @Value("${bill.doc.url.token}")
    private String billDocURLToken;

    @Value("${vneuron.doc.url}")
    private String vneuronDocURL;

    @Value("${vcvm.base.url}")
    private String vcvmBaseUrl;

    @Value("${vcvm.user}")
    private String vneuronUser;

    @Value("${vcvm.pass}")
    private String vneuronPassword;

    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;

    @Value("${prefix.insurer.specific}")
    private String documentPrefix;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final SftpIntegrationService sftpIntegrationService;
    private final NivaRequestDataRepository nivaRequestDataRepository;

    public S3FileService(S3Client s3Client, S3Presigner s3Presigner, DocumentMasterRepository documentMasterRepository,
                         SftpIntegrationService sftpIntegrationService, NivaRequestDataRepository nivaRequestDataRepository, RestService restService,
                         BillTariffResponseRepository billTariffResponseRepository, CorporateRepository corporateRepository,
                         VneuronResponseRepository vneuronResponseRepository, ClaimDataRepository claimDataRepository, EncryptionUtils encryptionUtils) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.documentMasterRepository = documentMasterRepository;
        this.sftpIntegrationService = sftpIntegrationService;
        this.nivaRequestDataRepository = nivaRequestDataRepository;
        this.restService = restService;
        this.billTariffResponseRepository = billTariffResponseRepository;
        this.corporateRepository = corporateRepository;
        this.vneuronResponseRepository = vneuronResponseRepository;
        this.claimDataRepository = claimDataRepository;
        this.encryptionUtils = encryptionUtils;
    }

    public String uploadFileToS3(Resource resource, String fileName) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            // Upload file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(awsS3Key + File.separator + fileName)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, resource.contentLength())
            );

            // Generate pre-signed URL
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(awsS3Key + File.separator + fileName)
                    .responseContentDisposition("inline")
                    .responseContentType("application/pdf")
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofDays(7))
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

            return presignedGetObjectRequest.url().toExternalForm();
        }
    }

    /**
     * Method to download file from S3
     *
     * @param documentMaster
     * @param downloadPath
     * @param isPreSignedUrlGenerate
     * @return
     * @throws IOException
     */
    public File downloadFile(DocumentMaster documentMaster, Path downloadPath, boolean isPreSignedUrlGenerate) throws IOException {
        // Ensure correct file path
        File file = downloadPath.toFile();

        // Ensure parent directory exists
        if (!file.getParentFile().exists()) {
            if (!file.exists()) {
                boolean dirsCreated = file.getParentFile().mkdirs();
                if (!dirsCreated) {
                    log.error("Failed to create parent directories for path: {}", file.getParentFile().getAbsolutePath());
                    throw new IOException("Failed to create parent directories for " + file.getAbsolutePath());
                }
            } else {
                log.info("File already exists, using the existing directory: {}", file.getParentFile().getAbsolutePath());
            }
        }

        // Check if file already exists before downloading
        if (!file.exists()) {
            log.info("Received flag value as {}, so downloading file from S3: {}", file.exists(), file.getAbsolutePath());
            try (InputStream s3InputStream = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(awsS3Key + File.separator + documentMaster.getFileName())
                            .build()
            )) {
                Files.copy(s3InputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (FileAlreadyExistsException fileAlreadyExistsException) {
                log.info("File already exists, using the existing file: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("Error downloading file from S3: {}", e.getMessage());
                throw new IOException("Error downloading file from S3", e);
            }
        }

        // Generate pre-signed URL if required
        if (isPreSignedUrlGenerate) {
            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(awsS3Key + File.separator + documentMaster.getFileName())
                        .responseContentDisposition("inline")
                        .responseContentType("application/pdf")
                        .build();

                GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                        .getObjectRequest(getObjectRequest)
                        .signatureDuration(Duration.ofDays(7))
                        .build();

                PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
                documentMaster.setPreSignedUrl(presignedGetObjectRequest.url().toExternalForm());
                documentMasterRepository.save(documentMaster);
            } catch (Exception e) {
                log.error("Error generating pre-signed URL: {}", e.getMessage());
                throw new IOException("Error generating pre-signed URL", e);
            }
        }

        return file;
    }

    // Method to download file content as a String
    public String downloadFileContent(String bucketName, String key) {
        return s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        ).asUtf8String();
    }

    public ArrayList<File> getClaimFiles(List<DocumentMaster> documentMasterList) throws IOException {
        ArrayList<File> files = new ArrayList<>();
        for (DocumentMaster documentMaster : documentMasterList) {
            Path downloadPath = Path.of(fileDownloadUrl, documentMaster.getFileName());
            File file = downloadFile(documentMaster, downloadPath, false);
            files.add(file);
        }
        return files;
    }

    public boolean saveFilesToWdms(ClaimData claimData,
                                   ArrayList<File> files,
                                   boolean isPreAuth,
                                   boolean isUpdateFlow,
                                   boolean isDischarge,
                                   boolean isReconsider,
                                   boolean isQueryReply,
                                   boolean isSettlement,
                                   boolean isRepush,
                                   NivaPushEvent nivaPushEvent) {

        log.info("claim -{} here to save files in wdms", claimData.getId());
        log.info("here to save files in wdms for claimID : {}", claimData.getId());
        try {
            String wdmsUniqueNo = getWdmsUniqueNo(claimData.getHospitalId(), claimData.getInsuranceAgencyId());
            boolean filesSubmittedSuccessfully = sftpIntegrationService.connectWithSftp(wdmsUniqueNo,
                    claimData, files, nivaPushEvent);

            if (filesSubmittedSuccessfully) {
                log.info("claim -{} Files submitted successfully to wdms ", claimData.getId());

                NivaRequestData nivaRequestData = new NivaRequestData();
                nivaRequestData.setWdmsUniqueNo(wdmsUniqueNo);
                nivaRequestData.setClaimDataId(claimData.getId());

                if (isPreAuth) {
                    nivaRequestData.setRequestType(NivaRequestType.PRE_AUTH.toString());
                } else if (isUpdateFlow) {
                    nivaRequestData.setRequestType(NivaRequestType.INTERIM_ENHANCEMENT.toString());
                } else if (isReconsider) {
                    nivaRequestData.setRequestType(NivaRequestType.RECONSIDER.toString());
                } else if (isQueryReply) {
                    nivaRequestData.setRequestType(NivaRequestType.QUERY.toString());
                } else if (isDischarge) {
                    nivaRequestData.setRequestType(NivaRequestType.DISCHARGE.toString());
                } else if (isSettlement) {
                    nivaRequestData.setRequestType(NivaRequestType.SETTLEMENT.toString());
                }

                nivaRequestData.setDateCreated(new Date());
                nivaRequestDataRepository.save(nivaRequestData);
                return true;
            } else {
                log.info("claim -{} Files not submitted successfully to wdms", claimData.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception caught while saving files to wdms for claimID : {}", claimData.getId(), e);
            return false;
        }
    }

    private String getWdmsUniqueNo(long hospitalId, long insuranceAgencyId) {
        long timestamp = Instant.now().getEpochSecond();
        return documentPrefix + "_" + hospitalId + "_" + insuranceAgencyId + "_" + timestamp;
    }

    public String getS3FileName(int claimId, String fileName) {
        return claimId + File.separator + new Date().getTime() + "_" + fileName;
    }

    public String uploadDownloadedFileToS3(String fileName, File extracted) {
        // Upload to S3
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(awsS3Key + File.separator + fileName)
                        .contentType("application/pdf")
                        .build(),
                RequestBody.fromFile(extracted));

        // Generate pre-signed URL
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(awsS3Key + File.separator + fileName)
                .responseContentDisposition("inline")
                .responseContentType("application/pdf")
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofDays(7))
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toExternalForm();
    }

    @SkipResponseLogging
    public byte[] getFileFromS3(String s3Url) {
        // Now we need to use the S3 file url to download the file from S3
        return restService.fetchFileFromS3(s3Url);
    }

    public String getFileNameFromS3Url(String s3Url) {
        if (s3Url != null) {
            try {
                URL url = URI.create(s3Url).toURL(); // Convert the S3 URL to a URL object
                String path = url.getPath(); // Get the path component of the URL
                return path.substring(path.lastIndexOf('/') + 1); // Extract the file name
            } catch (Exception e) {
                log.error("Error extracting file name from S3 URL: {}", e.getMessage());
            }
        }

        return null;
    }

    public long getClaimDataId(String intimationNumber) {
        long claimDataid = 0;
        try {
            ClaimData claimData = claimDataRepository.findByIntimationNumber(intimationNumber).orElse(null);
            if (claimData != null) {
                claimDataid = claimData.getId();
            }
        } catch (Exception e) {
            log.error("Error in  get claim data id for for intimationNumber {}: {}", intimationNumber, e.getMessage());
        }
        return claimDataid;
    }


    public String getIntimationNumber(String encryptedKey) {
        String decryptedIntimationNumber = "";
        try {
            String safeKey = encryptedKey.replace(" ", "+");
            decryptedIntimationNumber = encryptionUtils.decrypt(safeKey, apiParamEncryptiondecryptionKey);
        } catch (Exception e) {
            log.error("Error in  get intimation number");
            return "";
        }
        return decryptedIntimationNumber;
    }

    public void checkFileExpiry(ClaimData claimData, BillTariffResponse billTariffResponse, VneuronResponse vneuronResponse) {
        try {
            String hospitalCode = corporateRepository.findHospitalCodeByIntimationNumber(claimData.getIntimationNumber());
            if (hospitalCode == null) {
                log.info("Hospital Code not found for intimationNumber: {}", claimData.getIntimationNumber());
            }
            // Bill S3 URL and Tariff Doc URL
            List<DocumentMaster> documentMasterList = documentMasterRepository.findByParentTableIntimation(claimData.getIntimationNumber());
            checkBillDoc(billTariffResponse, hospitalCode, claimData.getIntimationNumber());
            checkTariffDoc(billTariffResponse, hospitalCode, claimData.getIntimationNumber());
            // Vneuron DOC
            checkMADoc(claimData.getIntimationNumber(), hospitalCode, vneuronResponse);
            // Claim Documents
            checkClaimDocuments(claimData.getIntimationNumber(), documentMasterList);

        } catch (Exception e) {
            log.info("Error in checkFileExpiry for intimationNumber {}: {}", claimData.getIntimationNumber(), e.getMessage());
        }
    }

    private void checkBillDoc(BillTariffResponse billTariffResponse, String hospitalCode, String intimationNumber) {
        try {
            if (billTariffResponse == null
                    || billTariffResponse.getBillTariffResponseDTO() == null
                    || billTariffResponse.getBillTariffResponseDTO().getData() == null
                    || billTariffResponse.getBillTariffResponseDTO().getData().getMetadata() == null
                    || billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getBill_s3_url() == null) {
                log.info("Bill URL not found for intimationNumber: {} in billTariffResponse", intimationNumber);
                return;
            }
            boolean success = billTariffResponse.getBillTariffResponseDTO().isSuccess();
            String responseCode = billTariffResponse.getBillTariffResponseDTO().getResponse_code();
            String billS3Url = billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getBill_s3_url();
            billTariffResponse.setDocAvailable(true);
            if (!success && responseCode != null && responseCode.equals("DEFAULT_TARIFF_APPLIED")) {
                log.info("Skipping Bill S3 check for {} as success:{} and responseCode:{}", intimationNumber, success, responseCode);
                validateAndUpdateDocUrl(billS3Url, billTariffResponse, intimationNumber);
                log.info("Validate And Update Doc for intimationNumber: {}", intimationNumber);
            } else {
                if (isS3UrlExpired(billS3Url)) {
                    String newBillUrl = fetchNewFileUrl(hospitalCode, intimationNumber, false, true, false);
                    if (newBillUrl != null && !newBillUrl.startsWith("Record not found or s3_object_pdf missing")) {
                        billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().setBill_s3_url(newBillUrl);
                        log.info("Updated Bill URL: {} for intimationNumber: {}", newBillUrl, intimationNumber);
                    } else {
                        billTariffResponse.setDocAvailable(false);
                        log.info("No valid Bill file found for intimationNumber: {} ", intimationNumber);
                    }
                } else {
                    log.info("Bill URL valid and accessible for intimationNumber: {}", intimationNumber);
                }
            }
            String updatedJson = GsonUtils.toJson(billTariffResponse.getBillTariffResponseDTO());
            billTariffResponse.setBillTariffResponse(updatedJson);
            billTariffResponseRepository.save(billTariffResponse);
            log.info("updated Bill URL saved in billTariffResponse for intimationNumber: {}", intimationNumber);
        } catch (Exception e) {
            log.info("Error while processing Bill Document for intimationNumber: {}. Root cause: {}", intimationNumber, e.getMessage(), e);
        }
    }

    private void checkTariffDoc(BillTariffResponse billTariffResponse, String hospitalCode, String intimationNumber) {
        try {
            Map<String, String> documentsMap = new HashMap<>();
            String newTariffUrl = "";
            if (billTariffResponse == null
                    || billTariffResponse.getBillTariffResponseDTO() == null
                    || billTariffResponse.getBillTariffResponseDTO().getData() == null
                    || billTariffResponse.getBillTariffResponseDTO().getData().getMetadata() == null
                    || billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getHospital_claim_metadata() == null) {
                log.info("Tariff Document data missing for intimationNumber: {} in billTariffResponse", intimationNumber);
            } else {
                Metadata metadata = billTariffResponse.getBillTariffResponseDTO().getData().getMetadata();
                DocumentsNameWithLocation docs = metadata.getHospital_claim_metadata().getDocuments_name_with_location();
                if (docs != null) {
                    if (docs.getAnh() != null && !docs.getAnh().isEmpty()) {
                        documentsMap.put("anh", docs.getAnh());
                    }
                    if (docs.getSoc() != null && !docs.getSoc().isEmpty()) {
                        documentsMap.put("soc", docs.getSoc());
                    }
                } else {
                    log.info("Documents data not found for intimationNumber:{} in billTariffResponse", intimationNumber);
                }
                // SOC/ANH DOC
                boolean updated = false;
                if (!documentsMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : documentsMap.entrySet()) {
                        String docType = entry.getKey();
                        String url = entry.getValue();
                        if (isS3UrlExpired(url)) {
                            newTariffUrl = fetchNewFileUrl(hospitalCode, intimationNumber, true, false, false);
                            if (newTariffUrl != null) {
                                if ("anh".equalsIgnoreCase(docType)) {
                                    HospitalClaimMetadata metadataObj = GsonUtils.fromJson(newTariffUrl, HospitalClaimMetadata.class);
                                    metadata.setHospital_claim_metadata(metadataObj);
                                } else if ("soc".equalsIgnoreCase(docType)) {
                                    HospitalClaimMetadata metadataObj = GsonUtils.fromJson(newTariffUrl, HospitalClaimMetadata.class);
                                    metadata.setHospital_claim_metadata(metadataObj);
                                }
                                updated = true;
                            } else {
                                log.info("newTariffUrl is getting null for doctype:{} and intimationNumber: {}", docType, intimationNumber);
                            }
                        } else {
                            log.info("{} Tariff document URL valid and accessible for intimationNumber: {}", docType, intimationNumber);
                        }
                    }
                    if (updated) {
                        String updatedJson = GsonUtils.toJson(billTariffResponse.getBillTariffResponseDTO());
                        billTariffResponse.setBillTariffResponse(updatedJson);
//                        log.info("final response before update: {}", billTariffResponse);
                        billTariffResponseRepository.save(billTariffResponse);
                        log.info("Updated tariff documents url saved in Bill Tariff Response for intimationNumber: {}", intimationNumber);
                    }
                } else {
                    log.info("No SOC/ANH documents found for intimationNumber: {}", intimationNumber);
                }
            }
        } catch (Exception e) {
            log.info("Error in checkTariffDoc for intimationNumber {}. Root cause: {}", intimationNumber, e.getMessage());
        }
    }

    private void checkMADoc(String intimationNumber, String hospitalCode, VneuronResponse vneuronResponse) {
        try {
            VNeuronResponseDTO vneuronResponseDTO = vneuronResponse != null
                    ? GsonUtils.fromJson(vneuronResponse.getVnueronResponse(), VNeuronResponseDTO.class)
                    : null;
            if (vneuronResponseDTO == null
                    || vneuronResponseDTO.getDocuments() == null
                    || vneuronResponseDTO.getDocuments().isEmpty()) {
                log.info("Medical Admissibility documents not found for intimationNumber: {} in VneuronResponse", intimationNumber);
                return;
            }
            List<DocumentsItem> documents = vneuronResponseDTO.getDocuments();
            List<DocumentsItem> updatedDocuments = new ArrayList<>();
            for (DocumentsItem doc : documents) {
                String vneuronUrl = doc.getOriginal_document_url();
                if (vneuronUrl == null) {
                    doc.setDocAvailable(false);
                    log.info("Medical Admissibility document url not found for intimationNumber: {} in VneuronResponse", intimationNumber);
                    continue;
                }
                if (isS3UrlExpired(vneuronUrl)) {
                    String newVneuronUrl = fetchNewFileUrl(hospitalCode, intimationNumber, false, false, true);
                    if (newVneuronUrl != null && !newVneuronUrl.startsWith("No files found for claim_id=")) {
                        doc.setOriginal_document_url(newVneuronUrl);
                        doc.setDocAvailable(true);
                        log.info("Updated expired document URL for intimationNumber: {} and fileName: {}", intimationNumber, doc.getFileName());
                    } else {
                        doc.setDocAvailable(false);
                        log.info("No valid file found for expired document for intimationNumber: {} and fileName: {}", intimationNumber, doc.getFileName());
                    }
                } else {
                    doc.setDocAvailable(true);
                    log.info("Document URL valid for intimationNumber: {} and fileName: {}", intimationNumber, doc.getFileName());
                }
                updatedDocuments.add(doc);
            }
            vneuronResponseDTO.setDocuments(updatedDocuments);
            String updatedJson = GsonUtils.toJson(vneuronResponseDTO);
            vneuronResponse.setVnueronResponse(updatedJson);
            vneuronResponseRepository.save(vneuronResponse);
            log.info("checkMADoc completed for intimationNumber: {}", intimationNumber);
        } catch (Exception e) {
            log.info("Error in checkMADoc for intimationNumber: {}. Root cause: {}", intimationNumber, e.getMessage(), e);
        }
    }


    private void checkClaimDocuments(String intimationNumber, List<DocumentMaster> documentMasterList) {
        List<DocumentMaster> updatedDocuments = new ArrayList<>();
        try {
            if (documentMasterList == null || documentMasterList.isEmpty()) {
                log.warn("Claim documents not found for intimationNumber: {}", intimationNumber);
            } else {
                for (DocumentMaster documentMaster : documentMasterList) {
                    String fileName = documentMaster.getFileName();
                    String preSignUrl = documentMaster.getPreSignedUrl();
                    String s3Key = awsS3Key + "/" + fileName;
                    log.info("Checking document '{}' for intimationNumber: {}", fileName, intimationNumber);
                    if (!isFileExistsInS3(s3Key)) {
                        log.error("File '{}' not accessible in S3 for intimationNumber: {}", fileName, intimationNumber);
                        documentMaster.setDocAvailable(false);
                        updatedDocuments.add(documentMaster);
                        continue;
                    }
                    if (isS3UrlExpired(preSignUrl)) {
                        log.info("URL expired for file '{}'. Generating a new one...", fileName);
                        log.info("bucketName: {}, s3Key: {}, fileName: {}", bucketName, s3Key, fileName);
                        String newUrl = generatePresignedUrl(bucketName, fileName);
                        documentMaster.setPreSignedUrl(newUrl);
                        documentMaster.setDocPath(newUrl);
                        documentMaster.setDocAvailable(true);
                        updatedDocuments.add(documentMaster);
                        log.info("New presigned URL generated for file '{}'", fileName);
                    } else {
                        documentMaster.setDocAvailable(true);
                        updatedDocuments.add(documentMaster);
                        log.info("File '{}' has a valid pre-signed URL.", fileName);
                    }
                }
                if (!updatedDocuments.isEmpty()) {
                    documentMasterRepository.saveAll(updatedDocuments);
                    log.info("Updated claim documents url :{} saved for intimationNumber {}", updatedDocuments.size(), intimationNumber);
                }
            }
        } catch (Exception e) {
            log.info("Error in checkClaimDocuments for intimationNumber {}. Root cause: {}", intimationNumber, e.getMessage());
        }
    }


    private String fetchNewFileUrl(String hospitalCode, String uniqueIdentifier, boolean tariff, boolean bill, boolean vneuron) {
        String apiUrl = "";
        try {
            HttpHeaders headers = new HttpHeaders();
            ResponseEntity<Map> response = null;
            if (tariff) {
                headers.set("Content-Type", "application/json");
                apiUrl = tariffBaseURL + "/tariff/get-url?hospital_code=" + hospitalCode + "&unique_identifier=" + uniqueIdentifier;
                headers.set("Authorization", tariffBaseURLToken);
            } else if (bill) {
                headers.set("Content-Type", "application/json");
                apiUrl = billDocURL + "/ocr/bill/get-url?hospital_code=" + hospitalCode + "&unique_identifier=" + uniqueIdentifier;
                headers.set("Authorization", billDocURLToken);
            } else if (vneuron) {
                apiUrl = vneuronDocURL + "/get_file_url?referenceClaimId=" + uniqueIdentifier;
            } else {
                log.warn("No API type selected in fetchNewFileUrl() fir intimation number :{}", uniqueIdentifier);
                return null;
            }
            if (vneuron) {
                HashMap<String, String> headerMap = new HashMap<>();
                headerMap.put("Authorization", "Bearer " + getVNeuronToken());
                log.info("Header info: {}", headerMap);
                String responseObj = restService.get(apiUrl, String.class, headerMap, null);
                log.info("API Url : {} and Response of FetchNewFileURL for Vneuron : {}", apiUrl, responseObj);
                Map<String, Object> responseMap = GsonUtils.fromJson(responseObj, Map.class);
                if (responseMap.get("status") != null
                        && responseMap.get("status").toString().equalsIgnoreCase("success")
                        && responseMap.get("file_url") != null) {
                    return responseMap.get("file_url").toString();
                } else {
                    return responseMap.get("message").toString();
                }
            } else {
                HttpEntity<String> entity = new HttpEntity<>(headers);
                log.info("Login info: {}", headers);
                RestTemplate restTemplate = new RestTemplate();
                response = restTemplate.getForEntity(apiUrl, Map.class, entity);
                log.info("API Url and Response of FetchNewFileURL : {} , {}", apiUrl, response);
                Map<String, Object> responseBody = response.getBody();
                if (responseBody == null) {
                    log.error("responseBody is getting null in fetch new file URL from API: {}", apiUrl);
                    return null;
                }
                if (tariff) {
                    if (responseBody.get("success") != null
                            && responseBody.get("success").toString().equalsIgnoreCase("true")
                            && responseBody.get("hospital_claim_metadata") != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsString(responseBody.get("hospital_claim_metadata"));
                    }
                }
                if (bill) {
                    if (responseBody.get("success") != null
                            && responseBody.get("success").toString().equalsIgnoreCase("true")
                            && responseBody.get("bill_s3_url") != null) {
                        return responseBody.get("bill_s3_url").toString();
                    } else if (responseBody.get("success") != null
                            && responseBody.get("success").toString().equalsIgnoreCase("false")
                            && responseBody.get("error") != null) {
                        return responseBody.get("error").toString();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error : {} in fetching new file URL from API: {}", e, apiUrl);
        }
        return null;
    }

    private boolean isS3UrlExpired(String urlStr) throws Exception {
        Map<String, String> queryParams = getQueryParams(urlStr);
        String amzDate = queryParams.get("X-Amz-Date");
        long amzExpires = Long.parseLong(queryParams.get("X-Amz-Expires"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
        Instant urlCreated = ZonedDateTime.parse(amzDate, formatter).toInstant();
        Instant urlExpiry = urlCreated.plusSeconds(amzExpires);
        return Instant.now().isAfter(urlExpiry);
    }

    private Map<String, String> getQueryParams(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        String query = url.getQuery();
        Map<String, String> queryPairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
            queryPairs.put(key, value);
        }
        return queryPairs;
    }

    public String getVNeuronToken() {
        MultiValueMap<String, Object> valueMap = new LinkedMultiValueMap<>();
        String loginUrl;

        valueMap.put("username", Collections.singletonList(vneuronUser));
        valueMap.put("password", Collections.singletonList(vneuronPassword));
        loginUrl = "login";

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE);
        headerMap.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        VCVMTokenResponse vcvmTokenResponse = restService.post(vcvmBaseUrl + loginUrl, valueMap, VCVMTokenResponse.class, headerMap);

        return vcvmTokenResponse.getAccess_token();
    }

    private boolean isFileExistsInS3(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            } else {
                log.error("Error checking S3 object: {}", e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("Unexpected error checking S3 object: {}", e.getMessage(), e);
            return false;
        }
    }


    private String generatePresignedUrl(String bucketName, String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(awsS3Key + File.separator + fileName)
                    .responseContentDisposition("inline")
                    .responseContentType("application/pdf")
                    .build();
            log.info("Generating presigned URL for bucket: {}, key: {}", bucketName, awsS3Key + File.separator + fileName);
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofDays(7))
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            return presignedGetObjectRequest.url().toExternalForm();
        } catch (Exception e) {
            log.error("Error generating presigned URL: {}", e.getMessage(), e);
            return null;
        }
    }

    private void validateAndUpdateDocUrl(String url, BillTariffResponse billTariffResponse, String intimationNumber) {
        try {
            List<String> billUrlList = new ArrayList<>();
            String fileName = extractFileNameFromUrl(url);
            if (fileName == null || fileName.isEmpty()) {
                log.error("Unable to get filename from URL for {}", intimationNumber);
                billTariffResponse.setDocAvailable(false);
                return;
            }
            log.info("Validating url file '{}' for {}", fileName, intimationNumber);
            // Check S3 file availability
            String s3Key = awsS3Key + "/" + fileName;
            if (!isFileExistsInS3(s3Key)) {
                log.error("Doc '{}' not available in S3 for {}", fileName, intimationNumber);
                billTariffResponse.setDocAvailable(false);
                return;
            }
            // Check URL expiry and regenerate if required
            if (isS3UrlExpired(url)) {
                log.info("URL expired. Generating new presigned URL for {}", intimationNumber);
                String newUrl = generatePresignedUrl(bucketName, fileName);
                if (newUrl == null || newUrl.isBlank()) {
                    log.error("Presigned URL generation failed for file '{}' on {}", fileName, intimationNumber);
                    billTariffResponse.setDocAvailable(false);
                    return;
                }
                billUrlList.add(newUrl);
                billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().setBill_s3_url(newUrl);
                billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().setBillS3UrlList(billUrlList);
                billTariffResponse.setDocAvailable(true);
                log.info("Generated new Doc presigned URL for {}", intimationNumber);
            } else {
                billTariffResponse.setDocAvailable(true);
                log.info("Doc has valid URL for {}", intimationNumber);
            }
        } catch (Exception e) {
            log.error("Error in validateAndUpdateDocUrl for {} -> {}", intimationNumber, e.getMessage(), e);
        }
    }

    public String extractFileNameFromUrl(String signedUrl) throws Exception {
        try {
            URL url = new URL(signedUrl);
            String key = url.getPath();
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            if (key.startsWith("dashboard/")) {
                key = key.substring("dashboard/".length());
            }
            log.info("Extracted Key: {}" , key);
            return key;
        } catch (Exception e) {
            log.error("Error :{} in extractFileNameFromUrl for {}", e.getMessage(), signedUrl);
            return null;
        }
    }

    public static void main(String[] args) {
        String signedUrl = "https://claims-nbhi-prod.s3.ap-south-1.amazonaws.com/dashboard/1198367/1763714762107_VCC_1198367_11zon_merged-PDF%20-%202025-11-21T134603.212_11zon.pdf?response-content-disposition=inline&response-content-type=application%2Fpdf&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20251129T055621Z&X-Amz-SignedHeaders=host&X-Amz-Expires=604800&X-Amz-Credential=AKIAXCDDPMZLQONL3UKB%2F20251129%2Fap-south-1%2Fs3%2Faws4_request&X-Amz-Signature=4895e2ff561e8a823be68ea01792f3d0f946dce32d4ef7d29489c4cdc91137cd";
        try {
            URL url = new URL(signedUrl);
            String key = url.getPath();
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
//            key = URLDecoder.decode(key, StandardCharsets.UTF_8);
            if (key.startsWith("dashboard/")) {
                key = key.substring("dashboard/".length());
            }

            System.out.println("Extracted Key: " + key);
            System.out.println("Extracted Key: " + key);
        } catch (Exception e) {
            log.error("Error :{} in extractFileNameFromUrl for {}", e.getMessage(), signedUrl);
        }
    }

}


