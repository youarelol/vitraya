package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.TariffLineItemUpdateRequest;
import com.vitraya.adjudication.engine.dto.request.TariffRequest;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.repository.*;
import com.vitraya.adjudication.engine.utils.AppConstants;
import com.vitraya.adjudication.engine.mysql.repository.ErrorMessageLogRepository;
import com.vitraya.adjudication.engine.mysql.repository.InsurerFetchResponsesRepository;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.NivaPolicyDataDTO;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PolicyHolderDetailsDTO;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.vitraya.adjudication.engine.dto.response.BenefitDataDto.addValue;
import static com.vitraya.adjudication.engine.dto.response.BenefitDataDto.subtract;

@Service
@Slf4j
public class BillTariffService {

    private final S3FileService s3FileService;
    private final SseEmittersService sseEmittersService;
    private final ErrorMessageLogRepository errorMessageLogRepository;
    private final ErrorMessageLogsService errorMessageLogsService;
    private final InsurerFetchResponsesRepository insurerFetchResponsesRepository;

    @Value("${bill.tariff.url}")
    private String billTariffURL;

    @Value("${tariff.url}")
    private String tariffURL;

    @Value("${bill.tariff.auth.token}")
    private String billTariffAuthToken;

    @Value("${max.rest.retry.count:3}")
    private int MAX_REST_RETRY_COUNT;

    @Value("${module.failed.email.to}")
    private String MODULE_FAILED_EMAIL_TO;

    @Value("${claim.registration.failed.email.to}")
    private String CLAIM_REGISTRATION_FAILED_EMAIL_TO;

    private final BillTariffResponseRepository billTariffResponseRepository;
    private final ClaimDataRepository claimDataRepository;
    private final ClaimModuleStatsRepository claimModuleStatsRepository;
    private final PMLService pmlService;
    private final HospitalServiceTypeService hospitalServiceTypeService;
    private final CorporateService corporateService;
    private final ProcedureService procedureService;
    private final RestService restService;
    private final DocumentService documentService;
    private final ClaimCommonService claimCommonService;
    private final CommunicationService communicationService;
    private final S3FileService s3FileUploadService;
    private final ClaimModulesSavingService claimModulesSavingService;
    private final ClaimAdmissionService claimAdmissionService;

    public BillTariffService(PMLService pmlService, HospitalServiceTypeService hospitalServiceTypeService,
                             CorporateService corporateService, ProcedureService procedureService, RestService restService,
                             DocumentService documentService, ClaimCommonService claimCommonService,
                             BillTariffResponseRepository billTariffResponseRepository, ClaimDataRepository claimDataRepository,
                             ClaimModuleStatsRepository claimModuleStatsRepository, CommunicationService communicationService,
                             ClaimModulesSavingService claimModulesSavingService, S3FileService s3FileService,
                             S3FileService s3FileUploadService, SseEmittersService sseEmittersService, ClaimAdmissionService claimAdmissionService,
                             ErrorMessageLogRepository errorMessageLogRepository, ErrorMessageLogsService errorMessageLogsService,
                             InsurerFetchResponsesRepository insurerFetchResponsesRepository) {
        this.pmlService = pmlService;
        this.hospitalServiceTypeService = hospitalServiceTypeService;
        this.corporateService = corporateService;
        this.procedureService = procedureService;
        this.restService = restService;
        this.documentService = documentService;
        this.claimCommonService = claimCommonService;
        this.billTariffResponseRepository = billTariffResponseRepository;
        this.claimDataRepository = claimDataRepository;
        this.claimModuleStatsRepository = claimModuleStatsRepository;
        this.communicationService = communicationService;
        this.s3FileService = s3FileService;
        this.s3FileUploadService = s3FileUploadService;
        this.sseEmittersService = sseEmittersService;
        this.claimModulesSavingService = claimModulesSavingService;
        this.claimAdmissionService = claimAdmissionService;
        this.errorMessageLogRepository = errorMessageLogRepository;
        this.errorMessageLogsService = errorMessageLogsService;
        this.insurerFetchResponsesRepository = insurerFetchResponsesRepository;
    }

    public BillTariffResponse initialBillTariffResponseEntry(ClaimData claimData) {
        BillTariffResponse billTariffResponse = new BillTariffResponse();
        billTariffResponse.setClaimDataId(claimData.getId());
        billTariffResponse.setDateCreated(new Date());
        BillTariffResponseDTO initialBillTariffEntry = getBillTariffResponseDTO(claimData, "Initial Bill tariff Request Received..");
        billTariffResponse.setBillTariffResponse(GsonUtils.toJson(initialBillTariffEntry));
        return billTariffResponseRepository.save(billTariffResponse);
    }

    public BillTariffResponse processBillTariffRequest(ClaimRunDTO claimRunDTO) throws IOException {
        ClaimData claimData = claimDataRepository.findById(claimRunDTO.getClaimId()).orElse(null);

        if (claimData == null) {
            log.error("Invalid claim data ID: {}", claimRunDTO.getClaimId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        BillTariffResponse billTariffResponse = initialBillTariffResponseEntry(claimData);

        HashMap<String, Object> billTariffRequestData = prepareBillTariffRequestData(claimData);
        HashMap<String, String> headerMap = getBillTariffHeaderMap();


        log.info("billtariffRequestData: {}", billTariffRequestData.toString());
        try {
            long startTime = System.currentTimeMillis();
            BillTariffResponseDTO billTariffResponseDTO = null;
            int retryCount = 0;
            String response = null;

            while (retryCount < MAX_REST_RETRY_COUNT) {
                try {
                    response = restService.executePostWithFile(billTariffURL, billTariffRequestData,
                            String.class, headerMap);
                    break;
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount >= MAX_REST_RETRY_COUNT) {
                        log.error("Bill tariff error after max retry attempt. Attempt: {}", retryCount, e);
                    } else {
                        log.info("Received error from Bill tariff service. Retrying... Attempt: {}", retryCount);
                    }
                }
            }

            // ToDo: Instead of throwing error capture the bill module fail result.
            if (response == null) {
                log.error("For claim Id {}. We have received invalid response from bill tariff service", claimData.getId());
                String message = "[Manual]: Invalid response received from bill tariff service";
                billTariffResponseDTO = getBillTariffResponseDTO(claimData, message);
            } else {
                billTariffResponseDTO = GsonUtils.fromJson(response, BillTariffResponseDTO.class);
                updateInsurerIrdaiPayable(billTariffResponseDTO);
                if (billTariffResponseDTO.getMessage() != null
                        && "Bill already in progress".equalsIgnoreCase(billTariffResponseDTO.getMessage())) {
                    communicationService.sendEmail(claimData.getIntimationNumber() + "Bill Module Not Run",
                            response, CLAIM_REGISTRATION_FAILED_EMAIL_TO);
                }
            }

            return processBillTariffResponse(claimRunDTO, claimData, billTariffResponseDTO, startTime, billTariffResponse);
        } catch (Exception e) {
            log.error("Exception while processing bill tariff request for claim ID: {}", claimRunDTO.getClaimId(), e);
            communicationService.sendEmail(claimData.getIntimationNumber() + "Caught exception while processing bill tariff request",
                    e.getMessage(), CLAIM_REGISTRATION_FAILED_EMAIL_TO);
            throw new VitrayaException(VitrayaErrorCodes.ERROR_OCCURED_PROCESSING_BILL_TARIFF_REQUEST);
        }
    }

    public void updateInsurerIrdaiPayable(BillTariffResponseDTO billTariffResponseDTO) {
        if (billTariffResponseDTO != null
                && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getLine_items() != null
                && !billTariffResponseDTO.getData().getLine_items().isEmpty()) {
            for (LineItemsItem lineItemsItem : billTariffResponseDTO.getData().getLine_items()) {
                if (lineItemsItem.getData() != null && lineItemsItem.getData().getTariff() != null) {
                    lineItemsItem.getData().getTariff()
                            .setInsurer_irdai_payable(lineItemsItem.getData().getTariff().isIrdai_payable());
                }
            }
        }

        if (billTariffResponseDTO != null
                && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getCategory_summary() != null
                && !billTariffResponseDTO.getData().getCategory_summary().isEmpty()) {
            for (CategorySummaryItem categorySummaryItem : billTariffResponseDTO.getData().getCategory_summary()) {
                if (categorySummaryItem.getLine_items() != null
                        && !categorySummaryItem.getLine_items().isEmpty()) {
                    for (LineItemsItem lineItemsItem : categorySummaryItem.getLine_items()) {
                        if (lineItemsItem.getData() != null && lineItemsItem.getData().getTariff() != null) {
                            lineItemsItem.getData().getTariff()
                                    .setInsurer_irdai_payable(lineItemsItem.getData().getTariff().isIrdai_payable());
                        }
                    }
                }

            }
        }

    }

    public BillTariffResponseDTO getBillTariffResponseDTO(ClaimData claimData, String message) {
        BillTariffResponseDTO billTariffResponseDTO;
        billTariffResponseDTO = new BillTariffResponseDTO();
        billTariffResponseDTO.setSuccess(false);
        billTariffResponseDTO.setMessage(message);
        billTariffResponseDTO.setUnique_identifier(claimData.getIntimationNumber());
        billTariffResponseDTO.setBill_code(BillCodeEnum.BILL_RESPONSE_NOT_FOUND.name());
        return billTariffResponseDTO;
    }

    public void updateBillTariffResponse(BillTariffResponse billTariffResponse,
                                         BillTariffResponseDTO billTariffResponseDTO, ClaimData claimData,
                                         ClaimModuleStats claimModuleStats) throws IOException {
        Date startTime = billTariffResponse.getDateCreated();
        billTariffResponse.setBillTariffResponse(GsonUtils.toJson(billTariffResponseDTO));
        ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                .claimId(claimData.getId())
                .claimRunIdentifier(ClaimRunIdentifier.CLAIM_BILL_TARIFF_QC_RESPONSE)
                .claimModuleStats(claimModuleStats)
                .build();

        processBillTariffResponse(claimRunDTO, claimData, billTariffResponseDTO, startTime.getTime(), billTariffResponse);
    }

    public BillTariffResponse processTariffRequest(ClaimRunDTO claimRunDTO) throws IOException {
        ClaimData claimData = claimDataRepository.findById(claimRunDTO.getClaimId()).orElse(null);

        if (claimData == null) {
            log.error("Invalid claim data ID for tariff request: {}", claimRunDTO.getClaimId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        BillTariffResponse billTariffResponse = getBillTariffResponseByClaimDataId(claimData.getId());
        TariffRequest tariffRequestData = prepareTariffRequestData(billTariffResponse.getBillTariffResponseDTO(), claimData);
        log.info("Tariff request data: {}", GsonUtils.toJson(tariffRequestData));
        HashMap<String, String> headerMap = getBillTariffHeaderMap();
        try {
            long startTime = System.currentTimeMillis();
            BillTariffResponseDTO billTariffResponseDTO = null;
            int retryCount = 0;
            String response = null;

            while (retryCount < MAX_REST_RETRY_COUNT) {
                try {
                    response = restService.post(tariffURL, tariffRequestData,
                            String.class, headerMap);
                    break;
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount >= MAX_REST_RETRY_COUNT) {
                        log.error("Tariff error after max retry attempt. Attempt: {}", retryCount, e);
                    } else {
                        log.info("Received error from tariff service. Retrying... Attempt: {}", retryCount);
                    }
                }
            }
            if (response == null) {
                log.error("For claim Id {}. We have received invalid response from tariff service", claimData.getId());
                billTariffResponseDTO = new BillTariffResponseDTO();
                billTariffResponseDTO.setSuccess(false);
                billTariffResponseDTO.setMessage("[Manual]: Invalid response received from tariff service");
                billTariffResponseDTO.setUnique_identifier(claimData.getIntimationNumber());
                billTariffResponseDTO.setBill_code(BillCodeEnum.TARIFF_RESPONSE_NOT_FOUND.name());

                return billTariffResponse;
            } else {
                billTariffResponseDTO = GsonUtils.fromJson(response, BillTariffResponseDTO.class);
                updateInsurerIrdaiPayable(billTariffResponseDTO);
                if (billTariffResponseDTO != null) {
                    communicationService.sendEmail(claimData.getIntimationNumber() + " got Tariff Response",
                            response, CLAIM_REGISTRATION_FAILED_EMAIL_TO);
                }
            }

            return processBillTariffResponse(claimRunDTO, claimData, billTariffResponseDTO, startTime, null);
        } catch (Exception e) {
            log.error("Exception while processing tariff request for claim ID: {}", claimRunDTO.getClaimId(), e);
            communicationService.sendEmail(claimData.getIntimationNumber() + "Caught exception while processing tariff request",
                    e.getMessage(), CLAIM_REGISTRATION_FAILED_EMAIL_TO);
            throw new VitrayaException(VitrayaErrorCodes.ERROR_OCCURRED_PROCESSING_TARIFF_REQUEST);
        }
    }

    private TariffRequest prepareTariffRequestData(BillTariffResponseDTO billTariffResponseDTO, ClaimData claimData) {
        List<RiderDetails> riderDetails = getRiderList(claimData);
        Corporate payer = corporateService.getUserCorporate((int) claimData.getInsuranceAgencyId());
        TariffRequest tariffRequest = new TariffRequest();
        tariffRequest.setSuccess(billTariffResponseDTO.isSuccess());
        tariffRequest.setBill_code(billTariffResponseDTO.getBill_code());
        tariffRequest.setResponse_code(billTariffResponseDTO.getResponse_code());
        tariffRequest.setData(billTariffResponseDTO.getData());
        tariffRequest.setRerun(billTariffResponseDTO.getRerun());
        tariffRequest.setRider_details(riderDetails);
        tariffRequest.setInsurer_code(payer.getCorporateCode());
        log.info("Prepared tariff request data for claim ID: {}", claimData.getId());
        return tariffRequest;
    }

    public BillTariffResponse processBillTariffResponse(ClaimRunDTO claimRunDTO, ClaimData claimData,
                                                        BillTariffResponseDTO billTariffResponseDTO, long startTime,
                                                        BillTariffResponse billTariffResponse) throws IOException {
        log.info("Processing bill tariff response for claim ID: {}", claimData.getId());
        long endTime = System.currentTimeMillis();

        // Check did we received the pages in the bill tariff response. If yes then please segregate the pages.
        /*if (billTariffResponseDTO != null && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getMetadata() != null
                && billTariffResponseDTO.getData().getMetadata().getBill_pages() != null
                && !billTariffResponseDTO.getData().getMetadata().getBill_pages().isEmpty()) {
            String s3Url = extractPagesFromS3Link(billTariffResponseDTO.getData().getMetadata().getBill_s3_url(),
                    billTariffResponseDTO.getData().getMetadata().getBill_pages(), claimData.getIntimationNumber());
            if (s3Url != null) {
                billTariffResponseDTO.getData().getMetadata().setBill_s3_url(s3Url);
            }
        }*/

        billTariffResponse = saveBillTariffResponse(claimData, billTariffResponseDTO, (endTime - startTime), billTariffResponse, false);
        ClaimModuleStats claimModuleStats = claimRunDTO.getClaimModuleStats();
        claimCommonService.recordClaimModuleStats(claimData, claimModuleStats, ClaimModulesEnum.BILL_TARIFF,
                billTariffResponseDTO.getUnique_identifier(), (int) (endTime - startTime));

        // Now we need to send mail if the bill not executed successfully. Which means if we not received the parsed line items.
        if (!billTariffResponseDTO.isSuccess()) {
            String mailBody = "Bill Tariff Failed for claim ID: " + claimData.getId()
                    + "Please find the response below: \n\n" + GsonUtils.toJson(billTariffResponseDTO);

            communicationService.sendEmail(claimData.getIntimationNumber() + " | Bill Tariff Failed", mailBody,
                    MODULE_FAILED_EMAIL_TO);

            ErrorMsgType error = ErrorMsgType.BILL_TARIFF_ENGINE_FAILURE;
            errorMessageLogsService.saveErrorMessages(claimData, error);
            log.info("Saving Error Message Logs for Bill Tariff Failure in processBillTariffResponse method for {}", claimData.getIntimationNumber());
        }
        /*
        Below line is committed because whenever bill runs, pml will run for sure. So we will check the condition in PML
        if in case PML is not enabled. Still we are checking for the PML. So in case in future need arise we will add check
        then and there.
         */
        // claimCommonService.checkClaimModuleCompletion(claimData, claimModuleStats);

        return billTariffResponse;
    }

    private String extractPagesFromS3Link(String billS3Url, List<Integer> billPages, String intimationNumber) throws IOException {
        // Step 1: Download from URL
        File downloaded = downloadPdfFromUrl(billS3Url);

        // Step 2: Sort pages
        List<Integer> sortedPages = billPages.stream().distinct().sorted().toList();

        // Step 3: Extract pages
        File extracted = File.createTempFile("extracted-", ".pdf");
        try (PDDocument sourceDoc = PDDocument.load(downloaded);
             PDDocument newDoc = new PDDocument()) {

            for (int page : sortedPages) {
                int index = page - 1;
                if (index >= 0 && index < sourceDoc.getNumberOfPages()) {
                    newDoc.addPage(sourceDoc.getPage(index));
                }
            }
            newDoc.save(extracted);
        }

        String fileName = s3FileUploadService.getS3FileName(claimCommonService.getClaimIdPlain(intimationNumber),
                "segregated-" + downloaded.getName());
        return s3FileService.uploadDownloadedFileToS3(fileName, extracted);
    }

    private File downloadPdfFromUrl(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        File tempFile = File.createTempFile("downloaded-", ".pdf");
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return tempFile;
    }


    /**
     * Save the bill tariff response to the database.
     *
     * @param claimData
     * @param billTariffResponseDTO
     * @param tat
     */
    public BillTariffResponse saveBillTariffResponse(ClaimData claimData, BillTariffResponseDTO billTariffResponseDTO, long tat,
                                                     BillTariffResponse billTariffResponseReceived, boolean isEditFlow) {

        log.info("Saving bill tariff response : {}", billTariffResponseReceived);
        BillTariffResponse billTariffResponse = (billTariffResponseReceived != null) ? billTariffResponseReceived : new BillTariffResponse();

        if (billTariffResponseReceived == null) {
            billTariffResponse.setClaimDataId(claimData.getId());
            billTariffResponse.setDateCreated(new Date());
        } else {
            billTariffResponse.setDateUpdated(new Date());
        }

        billTariffResponse.setBillIdentifier(billTariffResponseDTO.getUnique_identifier());
        billTariffResponse.setBillTariffResponse(GsonUtils.toJson(billTariffResponseDTO));
        billTariffResponse.setBillTariffTat(tat + " ms");
        billTariffResponse.setBillTariffDecision(billTariffResponseDTO.getResponse_code());
        BillTariffResponse billResponse = billTariffResponseRepository.save(billTariffResponse);
        // claimModulesSavingService.updateBillTariffSaving(claimData,billTariffResponseReceived);

//        try {
//            if (billResponse.getBillTariffDecision() != null
//                    && (billResponse.getBillTariffDecision().equalsIgnoreCase("TARIFF_APPLIED")
//                    || billResponse.getBillTariffDecision().equalsIgnoreCase("DEFAULT_TARIFF_APPLIED"))) {
//                claimModulesSavingService.prepareModuleSavingsData(claimData, billResponse, isEditFlow);
//            }
//        } catch (Exception e) {
//            log.error("Exception while saving bill tariff response for claim ID: {}", claimData.getId(), e);
//        }

        return billResponse;
    }

    @SkipResponseLogging
    public BillTariffResponse getBillTariffResponseByClaimDataId(long claimDataId) {
        return billTariffResponseRepository.getBillTariffResponseByClaimDataIdOrderByIdDesc(claimDataId);
    }

    private HashMap<String, String> getBillTariffHeaderMap() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", billTariffAuthToken);

        return headerMap;
    }

    private HashMap<String, Object> prepareBillTariffRequestData(ClaimData claimData) throws IOException {
        HospitalServiceType hospitalServiceType = hospitalServiceTypeService.getHospitalPrivateRoom(
                claimData.getHospitalId());

        Corporate hospital = corporateService.getUserCorporate((int) claimData.getHospitalId());
        Corporate payer = corporateService.getUserCorporate((int) claimData.getInsuranceAgencyId());
        Procedures procedures = procedureService.findProcedureById(claimData.getProcedureId()).orElse(null);
        List<RiderDetails> riderDetails = getRiderList(claimData);

        DocClaimStage claimStage = DocClaimStage.getTariffStage(claimData.getClaimStatus());
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("additional_room_type", hospitalServiceType == null ? "Private Room" : hospitalServiceType.getHospitalRoomName());
        requestBody.put("unique_identifier", claimData.getIntimationNumber());
        requestBody.put("out_of_scope_hospital", false);
        requestBody.put("insurer_code", payer.getCorporateCode());
        requestBody.put("claim_type", claimData.getClaimType().toString());
        requestBody.put("procedure", procedures != null ? procedures.getName() : "null");
        requestBody.put("intimation_number", claimData.getIntimationNumber());
        requestBody.put("rerun", true); // ToDo: Replace this with logic
        requestBody.put("add_non_parsed", true);
        requestBody.put("segregation_details", null);
        requestBody.put("hospital_code", hospital.getCorporateCode());
        requestBody.put("patient_name", claimData.getPatientName());
        requestBody.put("procedure_code", procedures != null ? procedures.getVneuronSctidCode() : null);
        requestBody.put("patient_age", claimData.getPatientAge());
        requestBody.put("operation", AppConstants.TARIFF);
        requestBody.put("room_type", claimData.getRoomType());
        requestBody.put("curve_fit", true);
        requestBody.put("claim_stage", claimStage.toString());
        requestBody.put("plan_name", "");
        requestBody.put("sub_plan_name", "");
        requestBody.put("rider_details", riderDetails);


        List<DocumentMaster> documentMaster = documentService.getClaimDocumentMasterList(claimData.getIntimationNumber(), claimStage);
        documentService.addFileStorageResourceListToBody(requestBody, documentMaster, claimData.getIntimationNumber());
        return requestBody;
    }

    public List<TariffLineItemResponseDTO> getTariffData(ClaimData claimData, BillTariffResponse billTariffResponse) {
        List<TariffLineItemResponseDTO> tariffLineItemResponseDTOS = new ArrayList<>();
        if (billTariffResponse == null || claimData == null || billTariffResponse.getBillTariffResponseDTO() == null
                || billTariffResponse.getBillTariffResponseDTO().getData() == null
                || billTariffResponse.getBillTariffResponseDTO().getData().getLine_items() == null) {
            return null;
        } else {
            String roomType = billTariffResponse.getBillTariffResponseDTO().getData().getMetadata() != null
                    ? billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getRoom_type() : null;

            for (LineItemsItem lineItem : billTariffResponse.getBillTariffResponseDTO().getData().getLine_items()) {
                TariffLineItemResponseDTO lineItemData = TariffLineItemResponseDTO.populateLineItemInformation(lineItem, roomType);
                if (lineItemData != null && !lineItemData.isDeleted()) {
                    String lineItemColor = getColorForPayableNonPayables(lineItemData);
                    lineItemData.setColor(lineItemColor);
                    tariffLineItemResponseDTOS.add(lineItemData);
                }
            }
        }
        return tariffLineItemResponseDTOS;
    }

    private String getColorForPayableNonPayables(TariffLineItemResponseDTO lineItemData) {
        String lineItemColor = TariffSavingLineColorEnum.NO_SAVINGS_COLOR.getColorCode();
        if (lineItemData.getTotal_bill_amount() != null && lineItemData.getSavings() != null) {
            if (!lineItemData.isProcedure_construct_payable()
                    && lineItemData.getTotal_bill_amount().compareTo(lineItemData.getSavings()) == 0) {
                lineItemColor = TariffSavingLineColorEnum.PC_NON_PAYABLE_COLOR.getColorCode();
            } else if (lineItemData.getTotal_bill_amount().compareTo(lineItemData.getSavings()) == 0) {
                lineItemColor = TariffSavingLineColorEnum.FULL_NON_PAYABLE_COLOR.getColorCode();
            } else if (lineItemData.getSavings().compareTo(new BigDecimal(0)) > 0) {
                lineItemColor = TariffSavingLineColorEnum.SAVINGS_COLOR.getColorCode();
            }
        } else {
            log.info("getColorForPayableNonPayables: total bill amount is {} , savings is {}",
                    lineItemData.getTotal_bill_amount(), lineItemData.getSavings());
        }
        return lineItemColor;

    }

    public static String readJsonFileAsString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path); // Reads the file as a String
    }

    public BillTariffResponse getBillTariffResponseByBillIdentifier(String billIdentifier) {
        return billTariffResponseRepository.getBillTariffResponseByBillIdentifier(billIdentifier);
    }

    public ClaimModuleStats getClaimModuleStats(long claimDataId) {
        ClaimModuleStats claimModuleStats = claimModuleStatsRepository.findLatestByClaimDataId(claimDataId);
        if (claimModuleStats == null) {
            log.error("ClaimModuleStats not found for claim ID: {}", claimDataId);
            throw new VitrayaException(VitrayaErrorCodes.CLAIM_MODULE_NOT_FOUND);
        }

        return claimModuleStats;
    }

    public HashMap<String, CategorySummaryItem> getUpdatedCategorySummary(TariffLineItemResponseDTO lineItem,
                                                                          LineItemsItem item,
                                                                          String newMasterCategory,
                                                                          HashMap<String, CategorySummaryItem> categoryMap) {
        CategorySummaryItem categorySummary = categoryMap.get(newMasterCategory);
        BigDecimal admissibleAmountConsidered = lineItem.getInsurer_amount() != null ? lineItem.getInsurer_amount() : lineItem.getAdmissible_amount();
        // this is a new line item so the category could be existing.
        if (categorySummary != null && categorySummary.getCategory_name().equals(newMasterCategory)) {
            List<LineItemsItem> lineItemCategoryLevel = new ArrayList<>(categorySummary.getLine_items());
            lineItemCategoryLevel.add(item);
            categorySummary.setLine_items(lineItemCategoryLevel);
            categorySummary.setAdmissible_amount(addValue(categorySummary.getAdmissible_amount(), admissibleAmountConsidered));
            categorySummary.setAdmissible_amount_without_procedure_construct(addValue(categorySummary.getAdmissible_amount_without_procedure_construct(), item.getData().getTariff().getAdmissible_amount_without_procedure_construct()));
            categorySummary.setAmount_after_procedure_construct(addValue(categorySummary.getAmount_after_procedure_construct(), item.getData().getTariff().getAdmissible_amount()));
            categorySummary.setAmount_for_irdai_payable(addValue(categorySummary.getAmount_for_irdai_payable(), item.getData().getTariff().getAdmissible_amount()));
            categorySummary.setRequested_amount(addValue(categorySummary.getRequested_amount(), BigDecimal.valueOf(Double.parseDouble(item.getData().getAmount().getValue()))));
            categoryMap.put(newMasterCategory, categorySummary);
        } else {
            // case when it is a new category, add every thing.
            categorySummary = new CategorySummaryItem(newMasterCategory,
                    lineItem.getTotal_bill_amount(), admissibleAmountConsidered,
                    lineItem.getAdmissible_amount_without_procedure_construct(),
                    lineItem.getAdmissible_amount(), lineItem.getAdmissible_amount(),
                    BigDecimal.valueOf(1), Collections.singletonList(item));
            categoryMap.put(newMasterCategory, categorySummary);
        }
        return categoryMap;
    }

    public HashMap<String, CategorySummaryItem> getUpdatedCategorySummary(TariffLineItemResponseDTO lineItem,
                                                                          LineItemsItem item,
                                                                          int rowId, String originalMasterCategory,
                                                                          String newMasterCategory, BigDecimal amountDifference,
                                                                          HashMap<String, CategorySummaryItem> categoryMap) {
        CategorySummaryItem categorySummary = categoryMap.get(originalMasterCategory);
        // If both original master category and new master category is SIMILAR
        if (categorySummary != null && categorySummary.getCategory_name().equals(newMasterCategory)) {
            List<LineItemsItem> lineItemCategoryLevel = categorySummary.getLine_items();
            lineItemCategoryLevel.forEach(itemObj -> {
                if (rowId == itemObj.getData().getRow_id()) {
                    itemObj.getData().getVitraya_master_category().setValue(lineItem.getMaster_category());
                    itemObj.getData().getTariff().setInsurer_amount(lineItem.getInsurer_amount());
                    itemObj.getData().getTariff().setRemarks(lineItem.getRemarks());
                    itemObj.getData().getTariff().setInsurer_irdai_payable(lineItem.isIrdai_payable());
                    itemObj.getData().getTariff().setInsurer_procedure_construct_payable(lineItem.isInsurer_procedure_construct_payable());
                    itemObj.getData().getTariff().setInsurer_unit_actual(lineItem.getInsurer_unit_actual());
                    itemObj.getData().getTariff().setInsurer_unit_amount(lineItem.getInsurer_unit_amount());
                    itemObj.getData().getTariff().setInsurer_bill_amount(lineItem.getInsurer_bill_amount());
                    itemObj.getData().getTariff().setInsurer_tariff_rate(lineItem.getInsurer_tariff_rate());
                    itemObj.getData().getTariff().setInsurer_tariff_amount(lineItem.getInsurer_tariff_amount());
                    itemObj.getData().getTariff().setInsurer_savings(lineItem.getInsurer_savings());
                    itemObj.getData().getTariff().setInsurer_remarks(lineItem.getInsurer_remarks());
                    itemObj.getData().getTariff().setInsurer_amount_actual(lineItem.getInsurer_amount_actual());
                }
            });
            categorySummary.setLine_items(lineItemCategoryLevel);
            categorySummary.setAdmissible_amount(categorySummary.getAdmissible_amount().add(amountDifference));
            categoryMap.put(originalMasterCategory, categorySummary);
        } else if (categorySummary != null) {
            // FIRST REMOVE THE MODIFIED LINE ITEM FROM OLD CATEGORY
            // If both original master category and new master category is DIFFERENT
            List<LineItemsItem> lineItemCategoryLevel = categorySummary.getLine_items();
            // If the new master category has > 0 line items -> remove the modified line item
            if (!lineItemCategoryLevel.isEmpty() && lineItemCategoryLevel.size() > 1) {
                lineItemCategoryLevel = new ArrayList<>();
                for (LineItemsItem list : categorySummary.getLine_items()) {
                    if (rowId == list.getData().getRow_id()) {
                        categorySummary.setAdmissible_amount(subtract(categorySummary.getAdmissible_amount(), list.getData().getTariff().getAdmissible_amount()));
                        categorySummary.setAdmissible_amount_without_procedure_construct(subtract(categorySummary.getAdmissible_amount_without_procedure_construct(), list.getData().getTariff().getAdmissible_amount_without_procedure_construct()));
                        categorySummary.setAmount_after_procedure_construct(subtract(categorySummary.getAmount_after_procedure_construct(), list.getData().getTariff().getAdmissible_amount()));
                        categorySummary.setAmount_for_irdai_payable(subtract(categorySummary.getAmount_for_irdai_payable(), list.getData().getTariff().getAdmissible_amount()));
                        categorySummary.setRequested_amount(subtract(categorySummary.getRequested_amount(), BigDecimal.valueOf(Double.parseDouble(list.getData().getAmount().getValue()))));
                        continue;
                    }
                    lineItemCategoryLevel.add(list);
                }
                categorySummary.setLine_items(lineItemCategoryLevel);
                categorySummary.setAdmissible_amount(categorySummary.getAdmissible_amount().add(amountDifference));
                categoryMap.put(originalMasterCategory, categorySummary);
            } else {
                // If it was the only line item then -> remove the modified only line item
                categoryMap.remove(originalMasterCategory);
            }

            // SECOND ADD THE MODIFIED LINE ITEM IN NEW CATEGORY
            CategorySummaryItem categorySummaryNew = categoryMap.get(newMasterCategory);
            // add new line item with existing ones in the new category
            if (categorySummaryNew != null && !categorySummaryNew.getLine_items().isEmpty()) {
                log.info("Going ahead to add new line item with existing ones in the new category");
                lineItemCategoryLevel = categorySummaryNew.getLine_items();
                lineItemCategoryLevel.add(item);
                log.info("Amount difference: {}, requested amount: {}, admissible amount: {}", amountDifference,
                        categorySummaryNew.getRequested_amount(), categorySummaryNew.getAdmissible_amount());
                log.info("item: {}", item);
                categorySummaryNew.setAdmissible_amount_without_procedure_construct(addValue(categorySummaryNew.getAdmissible_amount_without_procedure_construct(), (item.getData().getTariff().getAdmissible_amount_without_procedure_construct())));
                categorySummaryNew.setAmount_after_procedure_construct(addValue(categorySummaryNew.getAmount_after_procedure_construct(), (item.getData().getTariff().getAdmissible_amount())));
                categorySummaryNew.setAmount_for_irdai_payable(addValue(categorySummaryNew.getAmount_for_irdai_payable(), (item.getData().getTariff().getAdmissible_amount())));
                categorySummaryNew.setRequested_amount(addValue(categorySummaryNew.getRequested_amount(), BigDecimal.valueOf(Double.parseDouble(item.getData().getAmount().getValue()))));
                if (amountDifference.compareTo(BigDecimal.ZERO) == 0) {
                    categorySummaryNew.setAdmissible_amount(addValue(categorySummaryNew.getAdmissible_amount(), item.getData().getTariff().getAdmissible_amount()));
                } else {
                    categorySummaryNew.setAdmissible_amount(categorySummaryNew.getAdmissible_amount().add(amountDifference));
                }
//                categorySummaryNew.setRequested_amount(categorySummaryNew.getRequested_amount().add(amountDifference.abs()));
                categorySummaryNew.setLine_items(lineItemCategoryLevel);
            } else {
                log.info("Going ahead to add a new category and a new line item altogether");
                // add a new category and a new line item altogether
                categorySummaryNew = new CategorySummaryItem(newMasterCategory,
                        lineItem.getTotal_bill_amount(), lineItem.getInsurer_amount(),
                        lineItem.getAdmissible_amount_without_procedure_construct(),
                        lineItem.getAdmissible_amount(), lineItem.getAdmissible_amount(),
                        BigDecimal.valueOf(1), Collections.singletonList(item));
            }
            categoryMap.put(newMasterCategory, categorySummaryNew);
        } else {
            log.info("Going ahead to Add a completely new category.");
            // case when it is a new category, add every thing.
            categorySummary = new CategorySummaryItem(newMasterCategory,
                    lineItem.getTotal_bill_amount(), lineItem.getInsurer_amount(),
                    lineItem.getAdmissible_amount_without_procedure_construct(),
                    lineItem.getAdmissible_amount(), lineItem.getAdmissible_amount(),
                    BigDecimal.valueOf(1), Collections.singletonList(item));
            categoryMap.put(newMasterCategory, categorySummary);
        }
        return categoryMap;
    }

    public HashMap<String, CategorySummaryItem> getUpdatedCategorySummary(int rowId, LineItemsItem item, String masterCategory,
                                                                          HashMap<String, CategorySummaryItem> categoryMap) {
        CategorySummaryItem categorySummary = categoryMap.get(masterCategory);
        // this is a new line item so the category could be existing.
        if (categorySummary != null && categorySummary.getCategory_name().equals(masterCategory)) {
            List<LineItemsItem> lineItemCategoryLevel = categorySummary.getLine_items();
            lineItemCategoryLevel.forEach(itemObj -> {
                if (rowId == itemObj.getData().getRow_id()) {
                    itemObj.getData().getTariff().setDeleted(true);
                }
            });
            //lineItemCategoryLevel.remove(item);
            if (lineItemCategoryLevel.size() == 1) {
                categoryMap.remove(masterCategory);
            } else {
                categorySummary.setLine_items(lineItemCategoryLevel);
                categorySummary.setAdmissible_amount(subtract(categorySummary.getAdmissible_amount(), item.getData().getTariff().getAdmissible_amount()));
                categorySummary.setAdmissible_amount_without_procedure_construct(subtract(categorySummary.getAdmissible_amount_without_procedure_construct(), item.getData().getTariff().getAdmissible_amount_without_procedure_construct()));
                categorySummary.setAmount_after_procedure_construct(subtract(categorySummary.getAmount_after_procedure_construct(), item.getData().getTariff().getAdmissible_amount()));
                categorySummary.setAmount_for_irdai_payable(subtract(categorySummary.getAmount_for_irdai_payable(), item.getData().getTariff().getAdmissible_amount()));
                categorySummary.setRequested_amount(subtract(categorySummary.getRequested_amount(), BigDecimal.valueOf(Double.parseDouble(item.getData().getAmount().getValue()))));
                categoryMap.put(masterCategory, categorySummary);
            }
        } else {
            // in case category is not found.
            log.info("Category not found for master category: {}", masterCategory);
        }
        return categoryMap;
    }

    public ClaimData processBillTariffResponse(BillTariffResponseDTO billTariffResponseDTO) throws IOException {
        if (billTariffResponseDTO == null) {
            log.error("Received BillTariffResponse request as null");
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }

        BillTariffResponse billTariffResponse = getBillTariffResponseByBillIdentifier(billTariffResponseDTO.getUnique_identifier());

        if (billTariffResponse == null) {
            log.error("BillTariffResponse for billIdentifier: {}", billTariffResponseDTO.getUnique_identifier());
            throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
        }

        ClaimData claimData = claimDataRepository.findById(billTariffResponse.getClaimDataId()).orElse(null);

        ClaimModuleStats claimModuleStats = getClaimModuleStats(claimData.getId());
        if (claimModuleStats == null) {
            log.error("ClaimModuleStats not found for claim ID: {}", claimData.getId());
            throw new VitrayaException(VitrayaErrorCodes.CLAIM_MODULE_NOT_FOUND);
        }

        if (!billTariffResponseDTO.isSuccess()
                && billTariffResponseDTO.getBill_code().equalsIgnoreCase("BILL_NOT_FOUND")
                && billTariffResponseDTO.getResponse_code().equalsIgnoreCase("BILL_FAILED")) {
            log.info("Bill not found, setting the default entry for claimId - {}", claimData.getId());
            billTariffResponse = claimAdmissionService.getBillTariffResponseFromAdmission(claimData, billTariffResponse);
            billTariffResponseDTO = billTariffResponse.getBillTariffResponseDTO();
        }
        updateBillTariffResponse(billTariffResponse, billTariffResponseDTO, claimData, claimModuleStats);
        if (billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getResponse_code() != null
                && !isBillTariffApplied(billTariffResponse.getBillTariffResponseDTO().getResponse_code())) {
            log.info("Tariff is Failed For this claim.");
            communicationService.sendEmail(claimData.getIntimationNumber() + " | Tariff Failed",
                    "Tariff Failed for claim ID: " + claimData.getId(), MODULE_FAILED_EMAIL_TO);
        } else {
            pmlService.prepareAndProcessPMLRequest(billTariffResponse, billTariffResponseDTO, claimModuleStats, claimData);
        }

        return claimData;
    }

    public boolean isBillTariffApplied(String billTariffStatus) {
        return billTariffStatus != null && (billTariffStatus.equalsIgnoreCase("TARIFF_APPLIED")
                || billTariffStatus.equalsIgnoreCase("DEFAULT_TARIFF_APPLIED"));
    }

    public List<BillTariffResponse> getPendingBillTariffResponse(Date startDate, Date thresholdDate) {
        return billTariffResponseRepository.findAllByPendingBillPendingClaims(startDate, thresholdDate);
    }

    public boolean validateTariffLineItemUpdateRequest(TariffLineItemUpdateRequest tariffLineItemUpdateRequest) {
        boolean isValid = true;
        for (TariffLineItemResponseDTO tariffLineItemResponseDTO : tariffLineItemUpdateRequest.getEditedLineItems()) {
            if (isNegativeLineItemExist(tariffLineItemResponseDTO)) {
                isValid = false;
                break;
            }
        }

        return isValid;
    }

    boolean isNegativeLineItemExist(TariffLineItemResponseDTO tariffLineItemResponseDTO) {
        return (tariffLineItemResponseDTO.getTariff_per_unit_amount() != null && tariffLineItemResponseDTO.getTariff_per_unit_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getUnit_amount() != null && tariffLineItemResponseDTO.getUnit_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getTotal_bill_amount() != null && tariffLineItemResponseDTO.getTotal_bill_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getDeduction() != null && tariffLineItemResponseDTO.getDeduction().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getTariff_rate() != null && tariffLineItemResponseDTO.getTariff_rate().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getSavings() != null && tariffLineItemResponseDTO.getSavings().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getSavingsAfterModification() != null && tariffLineItemResponseDTO.getSavingsAfterModification().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_amount() != null && tariffLineItemResponseDTO.getInsurer_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_tariff_amount() != null && tariffLineItemResponseDTO.getInsurer_tariff_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_unit() < 0)
                || (tariffLineItemResponseDTO.getUnit() < 0)
                || (tariffLineItemResponseDTO.getProcedure_construct_savings() < 0)
                || (tariffLineItemResponseDTO.getInsurer_bill_amount() != null && tariffLineItemResponseDTO.getInsurer_bill_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getAllowed_quantity_by_construct() != null && tariffLineItemResponseDTO.getAllowed_quantity_by_construct().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getAdmissible_amount() != null && tariffLineItemResponseDTO.getAdmissible_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_unit_amount() != null && tariffLineItemResponseDTO.getInsurer_unit_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getTariff_amount() != null && tariffLineItemResponseDTO.getTariff_amount().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getAdmissible_amount_without_procedure_construct() != null && tariffLineItemResponseDTO.getAdmissible_amount_without_procedure_construct().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_tariff_rate() != null && tariffLineItemResponseDTO.getInsurer_tariff_rate().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_savings() != null && tariffLineItemResponseDTO.getInsurer_savings().compareTo(BigDecimal.ZERO) < 0)
                || (tariffLineItemResponseDTO.getInsurer_amount_actual() != null && tariffLineItemResponseDTO.getInsurer_amount_actual().compareTo(BigDecimal.ZERO) < 0);
    }

    private List<RiderDetails> getRiderList(ClaimData claimData) {
        List<RiderDetails> riderList = new ArrayList<>();
        try {
            log.info("Fetching insurer response for claim data id: {}", claimData.getId());

            InsurerFetchResponses insurerFetchResponses =
                    insurerFetchResponsesRepository.getInsurerFetchResponsesByClaimIntimationNumber(
                            claimData.getIntimationNumber());

            if (insurerFetchResponses == null ||
                    insurerFetchResponses.getPolicyData() == null ||
                    insurerFetchResponses.getPolicyData().isEmpty()) {

                log.info("No insurer response or policy data found for claim data id: {}", claimData.getId());
                return riderList;
            }

            NivaPolicyDataDTO nivaPolicyDataDTO =
                    new Gson().fromJson(insurerFetchResponses.getPolicyData(), NivaPolicyDataDTO.class);

            if (nivaPolicyDataDTO == null || nivaPolicyDataDTO.getPolicy() == null ||
                    nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails() == null) {
                log.info("Policy holder details are missing for claim data id: {}", claimData.getId());
                return riderList;
            }

            PolicyHolderDetailsDTO policyHolderDetails = nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails();

            if (policyHolderDetails.getRiders() != null) {
                for (PolicyHolderDetailsDTO.Riders rider : policyHolderDetails.getRiders()) {
                    String benefitName = rider.getBenefitName();
                    if (benefitName == null || benefitName.isEmpty()) {
                        continue;
                    }
                    if (benefitName.equalsIgnoreCase(PolicyHolderDetailsDTO.RidersEnum.SAFEGUARD.getRiderName())) {
                        RiderDetails dto = new RiderDetails();
                        dto.setBenefitName(PolicyHolderDetailsDTO.RidersEnum.SAFEGUARD.getRiderName());
                        dto.setBenefitValue("Yes");
                        dto.setBenifitRemarks(rider.getBenifitRemarks() != null ? rider.getBenifitRemarks() : "");
                        dto.setBenefitAmount("");
                        riderList.add(dto);
                    } else if (benefitName.equalsIgnoreCase(PolicyHolderDetailsDTO.RidersEnum.SAFEGUARD_PLUS.getRiderName()) ||
                            benefitName.equalsIgnoreCase(PolicyHolderDetailsDTO.RidersEnum.SAFEGUARD_PS.getRiderName())) {
                        RiderDetails dto = new RiderDetails();
                        dto.setBenefitName("Safeguard Plus");
                        dto.setBenefitValue("Yes");
                        dto.setBenifitRemarks(rider.getBenifitRemarks() != null ? rider.getBenifitRemarks() : "");
                        dto.setBenefitAmount("");
                        riderList.add(dto);
                    }
                }
            }
            if (policyHolderDetails.getRefill_Flag_Policy() != null && !policyHolderDetails.getRefill_Flag_Policy().isEmpty()
                    && "Y".equalsIgnoreCase(policyHolderDetails.getRefill_Flag_Policy())) {
                RiderDetails riderDetails = new RiderDetails();
                riderDetails.setBenefitName(PolicyHolderDetailsDTO.RidersEnum.REFILL.getRiderName());
                riderDetails.setBenefitValue("Yes");
                riderDetails.setBenifitRemarks("");
                riderDetails.setBenefitAmount(policyHolderDetails.getRefill_Benefit_Amount());
                riderList.add(riderDetails);
            }

            if (policyHolderDetails.getReassure_Benefit_Amount() != null && !policyHolderDetails.getReassure_Benefit_Amount().isEmpty()
                    && "Y".equalsIgnoreCase(policyHolderDetails.getReassure_Benefit_Amount())) {
                RiderDetails dto = new RiderDetails();
                dto.setBenefitName(PolicyHolderDetailsDTO.RidersEnum.REASSURE.getRiderName());
                dto.setBenefitValue("Yes");
                dto.setBenifitRemarks("");
                dto.setBenefitAmount("");
                riderList.add(dto);
            }
            if (policyHolderDetails.getPolicy_Ported() != null && !policyHolderDetails.getPolicy_Ported().isEmpty()
                    && "Y".equalsIgnoreCase(policyHolderDetails.getPolicy_Ported())) {
                RiderDetails dto = new RiderDetails();
                dto.setBenefitName(PolicyHolderDetailsDTO.RidersEnum.PORTED_POLICY.getRiderName());
                dto.setBenefitValue("Yes");
                dto.setBenifitRemarks("");
                dto.setBenefitAmount("");
                riderList.add(dto);
            }

            log.info("Rider list details: {} for claim data id: {}", new Gson().toJson(riderList), claimData.getId());

            return riderList;

        } catch (Exception e) {
            log.error("Error while fetching rider details for {} ", claimData.getId(), e);
            return riderList;
        }
    }

}

