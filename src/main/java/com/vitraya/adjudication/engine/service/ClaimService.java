package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.annotation.SkipResponseLogging;
import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.*;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.email.service.EmailService;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.entity.PMLResponse;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrClaims;
import com.vitraya.adjudication.engine.mysql.impl.ClaimDataRepositoryImpl;
import com.vitraya.adjudication.engine.mysql.repository.*;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PreAuthRequest;
import com.vitraya.adjudication.engine.service.factory.ClaimDocumentFactory;
import com.vitraya.adjudication.engine.service.factory.ClaimsFactory;
import com.vitraya.adjudication.engine.utils.AppConstants;
import com.vitraya.adjudication.engine.utils.DateUtil;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.vitraya.adjudication.engine.dto.response.BenefitDataDto.addValue;

@Service
@Slf4j
public class ClaimService {

    private final ClaimRejectionReasonRepo claimRejectionReasonRepo;
    private final SseEmittersService sseEmittersService;
    private final EncryptionUtils encryptionUtils;
    private final S3FileService s3FileService;
    private final HospitalServiceTypeRepository hospitalServiceTypeRepository;
    private final A2SCallbackService a2SCallbackService;
    private final ErrorMessageLogsService errorMessageLogsService;
    private final ClaimRidersRepository claimRidersRepository;
    private final ErrorMessageLogRepository errorMessageLogRepository;
    @Value("${claim.module.response.wait.threshold}")
    private int CLAIM_MODULE_RESPONSE_WAIT_THRESHOLD;

    @Value("${claim.registration.failed.email.to}")
    private String CLAIM_REGISTRATION_FAILED_EMAIL_TO;

    @Value("${claim.check.time}")
    private int CLAIM_CHECK_TIME;

    @Value("${module.failed.email.to}")
    private String MODULE_FAILED_EMAIL_TO;

    @Value("${claim.push.to.maximus.threshold}")
    private int pushToMaximusThreshold;

    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;


    private final ClaimDocumentFactory claimDocumentFactory;
    private final ClaimDataRepository claimDataRepository;
    private final KafkaProducerService kafkaProducerService;
    private final EnhancementConfigRepository enhancementConfigRepository;
    private final CorporateService corporateService;
    private final BillTariffService billTariffService;
    private final VNeuronService vNeuronService;
    private final PMLService pmlService;
    private final ProcedureService procedureService;
    private final IllnessService illnessService;
    private final ClaimCommonService claimCommonService;
    private final DocumentService documentService;
    private final ClaimAdmissionService claimAdmissionService;
    private final ClaimAdjudicationRepository claimAdjudicationResultRepository;
    private final ClaimTransitionService claimTransitionService;
    private final ClaimsFactory claimsFactory;
    private final InsurerFetchResponsesRepository insurerFetchResponsesRepository;
    private final ClaimDataRepositoryImpl claimDataRepositoryImpl;
    private final CommunicationService communicationService;
    private final ClaimModulesSavingService billModuleSavingService;
    private final EmailService emailService;
    private final PMLResponseRepository pmlResponseRepository;
    private final NivaUcrClaimsRepo nivaUcrClaimsRepo;

    public ClaimService(ClaimDocumentFactory claimDocumentFactory, ClaimDataRepository claimDataRepository,
                        KafkaProducerService kafkaProducerService, EnhancementConfigRepository enhancementConfigRepository,
                        CorporateService corporateService, BillTariffService billTariffService, VNeuronService vNeuronService,
                        PMLService pmlService, ProcedureService procedureService, IllnessService illnessService,
                        ClaimCommonService claimCommonService, DocumentService documentService,
                        ClaimAdmissionService claimAdmissionService, ClaimAdjudicationRepository claimAdjudicationResultRepository,
                        ClaimTransitionService claimTransitionService, ClaimsFactory claimsFactory,
                        InsurerFetchResponsesRepository insurerFetchResponsesRepository, ClaimDataRepositoryImpl claimDataRepositoryImpl,
                        CommunicationService communicationService, ClaimRejectionReasonRepo claimRejectionReasonRepo, ClaimModulesSavingService billModuleSavingService,
                        SseEmittersService sseEmittersService, EncryptionUtils encryptionUtils, S3FileService s3FileService, EmailService emailService,
                        PMLResponseRepository pmlResponseRepository, HospitalServiceTypeRepository hospitalServiceTypeRepository, A2SCallbackService a2SCallbackService,
                        NivaUcrClaimsRepo nivaUcrClaimsRepo, ErrorMessageLogsService errorMessageLogsService, ClaimRidersRepository claimRidersRepository, ErrorMessageLogRepository errorMessageLogRepository) {
        this.claimDocumentFactory = claimDocumentFactory;
        this.claimDataRepository = claimDataRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.enhancementConfigRepository = enhancementConfigRepository;
        this.corporateService = corporateService;
        this.billTariffService = billTariffService;
        this.vNeuronService = vNeuronService;
        this.pmlService = pmlService;
        this.procedureService = procedureService;
        this.illnessService = illnessService;
        this.claimCommonService = claimCommonService;
        this.documentService = documentService;
        this.claimAdmissionService = claimAdmissionService;
        this.claimAdjudicationResultRepository = claimAdjudicationResultRepository;
        this.claimTransitionService = claimTransitionService;
        this.claimsFactory = claimsFactory;
        this.insurerFetchResponsesRepository = insurerFetchResponsesRepository;
        this.claimDataRepositoryImpl = claimDataRepositoryImpl;
        this.communicationService = communicationService;
        this.claimRejectionReasonRepo = claimRejectionReasonRepo;
        this.sseEmittersService = sseEmittersService;
        this.billModuleSavingService = billModuleSavingService;
        this.emailService = emailService;
        this.pmlResponseRepository = pmlResponseRepository;
        this.encryptionUtils = encryptionUtils;
        this.s3FileService = s3FileService;
        this.hospitalServiceTypeRepository = hospitalServiceTypeRepository;
        this.a2SCallbackService = a2SCallbackService;
        this.nivaUcrClaimsRepo = nivaUcrClaimsRepo;
        this.errorMessageLogsService = errorMessageLogsService;
        this.claimRidersRepository = claimRidersRepository;
        this.errorMessageLogRepository = errorMessageLogRepository;
    }

    /**
     * Fetches pending claim details and saves documents from the insurer.
     *
     * @param vitrayaInsurerClaimData The insurer claim data.
     * @param corporate               The corporate entity.
     * @throws IOException If an I/O error occurs.
     */
    public void fetchPendingClaimDetails(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, Corporate corporate, MultipartFile[] files) throws IOException {
        claimDocumentFactory
                .getDocumentDownloadFactory(vitrayaInsurerClaimData.getPayorCode(), files)
                .saveDocuments(vitrayaInsurerClaimData, files);
    }

    /**
     * Saves claim details.
     *
     * @param vitrayaInsurerClaimData The insurer claim data.
     * @return The saved claim data.
     * @throws ParseException If a parsing error occurs.
     */
    public ClaimData saveClaimDetails(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) throws ParseException {
        String intimationNumber = claimCommonService.getIntimationNumber(vitrayaInsurerClaimData);
        ClaimIllnessTreatmentDetailsDTO.DoctorDto doctorDto = null;
        if (vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails().getDoctorsDetails() != null
                && !vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails().getDoctorsDetails().isEmpty()
                && ClaimRequestTypeEnum.settlement_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            doctorDto = new Gson().fromJson(vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails().getDoctorsDetails(),
                    ClaimIllnessTreatmentDetailsDTO.DoctorDto.class);
        }
        ClaimData claimData = claimDataRepository
                .findByIntimationNumber(intimationNumber)
                .orElse(null);

        if (claimData == null) {
            claimData = ClaimData.builder()
                    .intimationNumber(intimationNumber)
                    .insuranceAgencyId(vitrayaInsurerClaimData.getRequest().getClaim().getInsuranceAgencyId())
                    .tpaId(getTpaId(vitrayaInsurerClaimData))
                    .hospitalId(vitrayaInsurerClaimData.getRequest().getClaim().getHospitalId())
                    .claimType(ClaimType.OPEN_BILL)
                    .patientName(vitrayaInsurerClaimData.getRequest().getClaim().getPatientName())
                    .dateOfBirth(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaim().getDob() != null
                            ? vitrayaInsurerClaimData.getRequest().getClaim().getDob() : null))
                    .patientAge(DateUtil.calculateAge(vitrayaInsurerClaimData.getRequest().getClaim().getDob() != null
                            ? vitrayaInsurerClaimData.getRequest().getClaim().getDob() : null))
                    .designation(null) //
                    .currentPolicyEndDate(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaim().getPolicyEndDate()))
                    .currentPolicyStartDate(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaim().getPolicyStartDate()))
                    .reasonForHospitalization(vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails().getHospitalizationType())
                    .coverCode(null) // Need to update abhi
                    .hospitalZone(null) // Need to update abhi
                    .baseSumInsured(vitrayaInsurerClaimData.getRequest().getClaim().getSumInsured())
                    .remainingSumInsured(vitrayaInsurerClaimData.getRequest().getClaim().getAvailableSumInsured())
                    .currentPolicyInceptionDate(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaim().getPolicyInceptionDate()))
                    .isEnhancementProcessed(false)
                    .active(true)
                    .deleted(false)
                    .dateOfAdmission(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getAdmissionDate()))
                    .dateOfDischarge(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getDischargeDate()))
                    .icdCode(vitrayaInsurerClaimData.getRequest().getIllness() != null ?
                            vitrayaInsurerClaimData.getRequest().getIllness().getDefaultICDCode() : null)
                    .policyRenewalHistory(null) // Need to update
                    .procedureId(vitrayaInsurerClaimData.getRequest().getProcedure() != null ? vitrayaInsurerClaimData.getRequest().getProcedure().getId() : 0)
                    .productCode(vitrayaInsurerClaimData.getRequest().getClaim().getPolicyName())
                    .roomType(vitrayaInsurerClaimData.getRequest().getHospitalServiceType().getRoomType())
                    .copayZone(null) // Need to update
                    .policyVariant(null) // Need to update
                    .treatmentType(null) // Need to update
                    .isInsurerVisible(false)
                    .pushedToInsurer(false)
                    .claimHistory(null) // Need to update
                    .policyNumber(vitrayaInsurerClaimData.getRequest().getClaim().getPolicyNumber())
                    .diagnosis(vitrayaInsurerClaimData.getRequest().getIllness() != null ?
                            vitrayaInsurerClaimData.getRequest().getIllness().getIllnessName() : null)
                    .pedList(null) // Need to update
                    .dateOfFirstDiagnosis(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails().getDateOfDiagnosis()))
                    .medicalCardNumber(vitrayaInsurerClaimData.getRequest().getClaim().getMedicalCardId())
                    .status(EnhancementStatus.ENHANCEMENT_REQ_RECEIVED) // Enhancement status
                    .claimStatus(ClaimStatus.PRE_AUTHORISATION_RAISED) // Status tells the current stage of the claims which means preauth received, approved, interim etc
                    .adjudicationStatus(AdjudicationStatus.PENDING.getAdjudicationStatus()) // Final adjudication status combining all the modules + portal buisness logic
                    .claimStatusText(ClaimStatusText.AUTHORISATION_REVIEW.toString())
                    .dateCreated(new Date())
                    .dateUpdated(new Date())
                    .inScopeHospital(vitrayaInsurerClaimData.getRequest().getClaim().isInScopeHospital())
                    .inScopePolicy(vitrayaInsurerClaimData.getRequest().getClaim().isInScopePolicy())
                    .inScopeProcedure(vitrayaInsurerClaimData.getRequest().getClaim().isInScopeProcedure())
                    .memberNo(vitrayaInsurerClaimData.getRequest().getClaim().getMemberNo())
                    .preauthBillPresentFlag(vitrayaInsurerClaimData.getRequest().isBillPresentInPreAuth())
                    .build();
        } else {
            boolean isDischarge = ClaimRequestTypeEnum.final_enhancement_request.equals(vitrayaInsurerClaimData.getRequestType());
            boolean isInterim = ClaimRequestTypeEnum.interim_enhancement_request.equals(vitrayaInsurerClaimData.getRequestType());
            boolean isSettlement = ClaimRequestTypeEnum.settlement_request.equals(vitrayaInsurerClaimData.getRequestType());
            boolean isQueryReply = ClaimRequestTypeEnum.query_response.equals(vitrayaInsurerClaimData.getRequestType());
            boolean isReconsider = ClaimRequestTypeEnum.reconsideration_request.equals(vitrayaInsurerClaimData.getRequestType());

            if (isDischarge) {
                claimData.setStatus(EnhancementStatus.ENHANCEMENT_REQ_RECEIVED);
                claimData.setEnhancementProcessed(false);
                claimData.setDateUpdated(new Date());
                claimData.setClaimStatus(ClaimStatus.DISCHARGE_RAISED);
                claimData.setDischargeReceivedTime(new Date());
                claimData.setClaimStatusText(ClaimStatusText.FINAL_AUTHORIZATION_REVIEW.toString());
                claimData.setPushedToInsurer(false);
            } else if (isInterim) {
                claimData.setStatus(EnhancementStatus.ENHANCEMENT_REQ_RECEIVED);
                claimData.setEnhancementProcessed(false);
                claimData.setDateUpdated(new Date());
                claimData.setClaimStatus(ClaimStatus.INTERIM_RAISED);
                claimData.setDischargeReceivedTime(new Date());
                claimData.setClaimStatusText(ClaimStatusText.REAUTHORIZATION_REVIEW.toString());
                claimData.setPushedToInsurer(false);
            } else if (isSettlement) {
                claimData.setStatus(EnhancementStatus.OUT_OF_SCOPE_CLAIM_STAGE);
                claimData.setEnhancementProcessed(true);
                claimData.setDateUpdated(new Date());
                claimData.setClaimStatus(ClaimStatus.SETTLEMENT_RAISED);
                claimData.setDischargeReceivedTime(null);
                claimData.setClaimStatusText(ClaimStatusText.SETTLEMENT_INITIATED.toString());
                claimData.setDoctorDetailForSettlement(doctorDto != null ? new Gson().toJson(doctorDto) : null);
                claimData.setPushedToInsurer(false);
            } else if (isQueryReply) {
                claimData.setStatus(EnhancementStatus.OUT_OF_SCOPE_CLAIM_STAGE);
                claimData.setEnhancementProcessed(true);
                claimData.setDateUpdated(new Date());
                claimData.setClaimStatus(ClaimStatus.QUERY_REPLY_RAISED);
                claimData.setDischargeReceivedTime(null);
                claimData.setPushedToInsurer(false);
            } else if (isReconsider) {
                claimData.setStatus(EnhancementStatus.OUT_OF_SCOPE_CLAIM_STAGE);
                claimData.setEnhancementProcessed(true);
                claimData.setDateUpdated(new Date());
                claimData.setClaimStatus(ClaimStatus.DENIAL_RECONSIDERATION_RAISED);
                claimData.setDischargeReceivedTime(null);
                claimData.setPushedToInsurer(false);
            } else {
                log.info("Claim data already exists for intimation number: {}", intimationNumber);
            }
            claimData.setProcedureId(vitrayaInsurerClaimData.getRequest().getProcedure() != null
                    ? vitrayaInsurerClaimData.getRequest().getProcedure().getId()
                    : 0);
            claimData.setInScopeProcedure(vitrayaInsurerClaimData.getRequest().getClaim().isInScopeProcedure());
            claimData.setBaseSumInsured(vitrayaInsurerClaimData.getRequest().getClaim().getSumInsured());
            claimData.setRemainingSumInsured(vitrayaInsurerClaimData.getRequest().getClaim().getAvailableSumInsured());
            claimData.setDateOfAdmission(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getAdmissionDate()));
            claimData.setDateOfDischarge(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getDischargeDate()));
            claimData.setIcdCode(vitrayaInsurerClaimData.getRequest().getIllness() != null ?
                    vitrayaInsurerClaimData.getRequest().getIllness().getDefaultICDCode() : null);
            claimData.setRoomType(vitrayaInsurerClaimData.getRequest().getHospitalServiceType().getRoomType());
            claimData.setDiagnosis(vitrayaInsurerClaimData.getRequest().getIllness() != null ?
                    vitrayaInsurerClaimData.getRequest().getIllness().getIllnessName() : null);
        }

        if (vitrayaInsurerClaimData.getRequest().getClaim().getPreAuthId() != null) {
            claimData.setInsurerIdentifier(vitrayaInsurerClaimData.getRequest().getClaim().getPreAuthId());
        }
        claimData.setReceivedReverseFeed(false);
        claimData.setTxnId(vitrayaInsurerClaimData.getTxnId());
        claimData.setLineOfTreatmentDetails(vitrayaInsurerClaimData.getRequest() != null
                ? (vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails() != null
                ? vitrayaInsurerClaimData.getRequest().getClaimIllnessTreatmentDetails().getLineOfTreatmentDetails()
                : "Medical Management")
                : "Medical Management");
        return claimDataRepository.save(claimData);
    }

    /**
     * Retrieves the TPA ID from the insurer claim data.
     *
     * @param vitrayaInsurerClaimData The insurer claim data.
     * @return The TPA ID.
     */
    private long getTpaId(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        int insuranceAgencyId = vitrayaInsurerClaimData.getRequest().getClaim().getInsuranceAgencyId();
        Corporate corporate = corporateService.findCorporateById(insuranceAgencyId);
        if (corporate != null && corporate.getType().equals(CorporateTypeEnum.TPA)) {
            return corporate.getId();
        }
        return -1;
    }

    /**
     * Adds a claim to the Kafka queue.
     *
     * @param claimRunDTO The claim run DTO.
     */
    public void addClaimToQueue(ClaimRunDTO claimRunDTO) {
        kafkaProducerService.sendMessage(AppConstants.CLAIM_TOPIC_NAME, claimRunDTO.getClaimId(), GsonUtils.toJson(claimRunDTO));
    }

    public void addClaimToEmailQueue(ClaimRunDTO claimRunDTO) {
        kafkaProducerService.sendMessage(AppConstants.EMAIL_FLOW_CLAIM_TOPIC_NAME, claimRunDTO.getClaimId(), GsonUtils.toJson(claimRunDTO));
    }

    public void addClaimToNonAdjudicationQueue(ClaimRunDTO claimRunDTO) {
        kafkaProducerService.sendMessage(AppConstants.NON_ADJUDICATION_CLAIM_TOPIC_NAME, claimRunDTO.getClaimId(), GsonUtils.toJson(claimRunDTO));
    }

    /**
     * Processes a claim.
     *
     * @param message The claim message.
     * @throws IOException If an I/O error occurs.
     */
    public void processClaim(String message) throws IOException {
        log.info("Processing Claim: {}", message);

        ClaimRunDTO claimRunDTO = GsonUtils.fromJson(message, ClaimRunDTO.class);
        ClaimData claimData = getClaimData(claimRunDTO.getClaimId());

        if (claimData == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }
        if (!claimRunDTO.isDoAdjudication()) {
            claimRunDTO.setClaimRunIdentifier(ClaimRunIdentifier.NON_ADJUDICATION_CLAIM_RUN);
            addClaimToNonAdjudicationQueue(claimRunDTO);
        } else if (claimData.getRoomType().isEmpty()
                || claimData.getRoomType().equalsIgnoreCase("Not Selected")) {
            log.info("Room type not present for this request: {}", claimRunDTO.getClaimId());
            claimRunDTO.setClaimRunIdentifier(ClaimRunIdentifier.NON_ADJUDICATION_CLAIM_RUN);
            addClaimToEmailQueue(claimRunDTO);
        } else if (!claimRunDTO.isVerifiedClaim()) {
            //TODO: this claim should go into manual route and email flow.
            claimRunDTO.setClaimRunIdentifier(ClaimRunIdentifier.NON_VERIFIED_EMAIL_CLAIM_RUN);
            addClaimToEmailQueue(claimRunDTO);
        } else {
            EnhancementConfig enhancementConfig = enhancementConfigRepository.findByHospitalIdAndInsuranceAgencyId(
                    claimData.getHospitalId(), claimData.getInsuranceAgencyId());

            log.info("EnhancementConfig: {}", enhancementConfig);
            if (enhancementConfig != null) {
                processEnhancementConfig(enhancementConfig, claimRunDTO, claimData);
            } else {
                log.info("Enhancement config not found for hospital id {}", claimData.getHospitalId());
                throw new VitrayaException(VitrayaErrorCodes.ENHANCEMENT_CONFIG_NOT_FOUND);
            }
        }

    }

    public void sendClaimFromEmailFlow(String message) throws Exception {

        ClaimRunDTO claimRunDTO = GsonUtils.fromJson(message, ClaimRunDTO.class);
        ClaimData claimData = getClaimData(claimRunDTO.getClaimId());

        DocClaimStage docClaimStage = DocClaimStage.getStage(claimData.getClaimStatus());

        PMLResponse pmlResponse = pmlResponseRepository.findPMLResponseByClaimDataIdLatest(claimData.getId());
        saveClaimData(claimData);
        PMLResponseDTO pmlResponseDTO = null;
        if (pmlResponse != null) {
            pmlResponseDTO = pmlResponse.getPmlResponseDTO();
        }

        assert docClaimStage != null;
        ClaimStatus claimStatus = null;
        boolean isMailSent = false;
        if (docClaimStage.equals(DocClaimStage.PRE_AUTH)) {
            isMailSent = emailService.sendEmailToInsurer(claimData, pmlResponseDTO, ClaimFlowType.PRE_AUTH);
            claimStatus = ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT;
        } else if (docClaimStage.equals(DocClaimStage.INTERIM)) {
            isMailSent = emailService.sendEmailToInsurer(claimData, pmlResponseDTO, ClaimFlowType.INTERIM_ENHANCEMENT);
            claimStatus = ClaimStatus.INTERIM_RESPONSE_SENT;
        } else if (docClaimStage.equals(DocClaimStage.DISCHARGE)) {
            isMailSent = emailService.sendEmailToInsurer(claimData, pmlResponseDTO, ClaimFlowType.DISCHARGE);
            claimStatus = ClaimStatus.DISCHARGE_RESPONSE_SENT;
        } else if (docClaimStage.equals(DocClaimStage.SETTLEMENT)) {
            isMailSent = emailService.sendEmailToInsurer(claimData, pmlResponseDTO, ClaimFlowType.SETTLEMENT);
            claimStatus = ClaimStatus.SETTLEMENT_RESPONSE_SENT;
        } else if (docClaimStage.equals(DocClaimStage.RECONSIDERATION)) {
            isMailSent = emailService.sendEmailToInsurer(claimData, pmlResponseDTO, ClaimFlowType.RECONSIDER);
            claimStatus = ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT;
        } else if (docClaimStage.equals(DocClaimStage.QUERY)) {
            isMailSent = emailService.sendEmailToInsurer(claimData, pmlResponseDTO, ClaimFlowType.QUERY);
            claimStatus = ClaimStatus.QUERY_REPLY_RESPONSE_SENT;
        }
        if (isMailSent) {
            claimData.setPushedToInsurer(true);
            claimData.setEmailFlow(true);
            claimData.setClaimStatus(claimStatus);
            saveClaimData(claimData);
            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent via email channel", claimData.getTxnId());
            log.info("Email sent for claim {} with status {}: {}", claimData.getId(), docClaimStage, isMailSent);
            ErrorMsgType error = ErrorMsgType.SEND_VIA_EMAIL_CHANNEL;
            errorMessageLogsService.saveErrorMessages(claimData, error);
            log.info("Saving Error Message Logs for sent mail channel in sendClaimFromEmailFlow method for {}", claimData.getIntimationNumber());

            //TODO:send success call back if not already done
            if (claimRunDTO.isSendCallbackToHospitalPortal()) {
                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                        claimData.getInsurerIdentifier(), false, false, false,
                        false, false, false, false, null,
                        null, null, false);
            }
        } else {
            log.error("Email not sent for claim {} with status {}: {}", claimData.getId(), docClaimStage, isMailSent);
        }
    }

    public void processNonAdjudicationClaim(String message) {
        ClaimRunDTO claimRunDTO = GsonUtils.fromJson(message, ClaimRunDTO.class);
        ClaimData claimData = getClaimData(claimRunDTO.getClaimId());

        DocClaimStage docClaimStage = DocClaimStage.getStage(claimData.getClaimStatus());

        if (claimData.isEmailFlow()) {
            log.info("Claim {} is email flow, adding to email queue", claimData.getId());
            claimRunDTO.setClaimRunIdentifier(ClaimRunIdentifier.NON_ADJUDICATION_EMAIL_CLAIM_RUN);
            addClaimToEmailQueue(claimRunDTO);
        } else {
            boolean pushed = claimsFactory.getClaimsFactory(ClaimDataParseMapping.DEFAULT_MAPPING).pushClaimDecision(claimData);
            log.info("Claim {} pushed to insurer: {}, docClaimStage: {}", claimData.getId(), pushed, docClaimStage);

        }

    }


    /**
     * Processes the enhancement configuration.
     *
     * @param enhancementConfig The enhancement configuration.
     * @param claimRunDTO       The claim run DTO.
     * @param claimData         The claim data.
     * @throws IOException If an I/O error occurs.
     */
    private void processEnhancementConfig(EnhancementConfig enhancementConfig, ClaimRunDTO claimRunDTO,
                                          ClaimData claimData) throws IOException {
        int rowUpdated = addClaimInProcessStatus(claimData);
        ClaimModuleStats claimModuleStats = claimCommonService.getLatestClaimModuleStats(claimData.getId());

        if (enhancementConfig.isVneuronEnabled()
                && isVneuronRunRequested(claimRunDTO) && claimData.isInScopeProcedure()) {
            addClaimVNeuronQueue(getClaimRunDTOVNeuron(claimRunDTO));
        } else {
            log.info("For claim {}, vNeuron is not enabled for insurance {} and hospital id {}",
                    claimData.getId(), claimData.getInsuranceAgencyId(), claimData.getHospitalId());

            /*
             We need capture the stats for the vNeuron module only in case when the previous is failed and rerun not requested.
             */
            if (claimModuleStats != null && claimModuleStats.getMedicalIdentifier() == null) {
                claimCommonService.recordClaimModuleStats(claimData, claimRunDTO.getClaimModuleStats(),
                        ClaimModulesEnum.MEDICAL_ADMISSIBILITY, "NA", 0);
            }
        }

        log.info("vNeuron added to queue for claim {}", claimData.getId());
        boolean isPmlExecuted = false;
        if (enhancementConfig.isBillTariffEnabled() && isBillRunRequested(claimRunDTO) && claimData.isInScopeHospital()) {
            log.info("{}: Bill Tariff is enabled for insurance {} and hospital id {}",
                    claimData.getIntimationNumber(), claimData.getInsuranceAgencyId(), claimData.getHospitalId());
            log.info("{}: billIdentifier for pre-auth {}", claimData.getIntimationNumber(), claimData.isPreauthBillPresentFlag());
            BillTariffResponse billTariffResponse = null;
            if (ClaimStatus.isPreAuthStage(claimData.getClaimStatus())
                    && !claimData.isPreauthBillPresentFlag()) {
                billTariffResponse = billTariffService.initialBillTariffResponseEntry(claimData);
                claimCommonService.recordClaimModuleStats(claimData, claimModuleStats, ClaimModulesEnum.BILL_TARIFF,
                        billTariffResponse.getBillIdentifier(), 0);
            } else {
                billTariffResponse = addClaimBillTariffQueue(getClaimRunDTOBill(claimRunDTO));
            }
            log.info("{} Received bill response {} now going to check for the PML", claimData.getIntimationNumber(),
                    billTariffResponse);
            checkAndProcessPML(enhancementConfig, getClaimRunDTOPML(claimRunDTO), claimData, billTariffResponse);
            isPmlExecuted = true;
        } else {
            log.info("For claim {}, Bill Tariff is not enabled for insurance {} and hospital id {}",
                    claimData.getId(), claimData.getInsuranceAgencyId(), claimData.getHospitalId());

            if (claimModuleStats != null && claimModuleStats.getBillIdentifier() == null) {
                claimCommonService.recordClaimModuleStats(claimData, claimRunDTO.getClaimModuleStats(),
                        ClaimModulesEnum.BILL_TARIFF, "NA", 0);
            }

            String emailBody = claimData.getIntimationNumber() + "Bill Module Not Run. Please find the details below: <br>"
                    + "Bill Tariff Enabled: " + (enhancementConfig.isBillTariffEnabled() ? "Enabled" : "Disabled") + "<br>"
                    + "Hospital ID: " + (isBillRunRequested(claimRunDTO) ? "Bill Run Requested" : "Bill Rerun Not Requested") + "<br>"
                    + "Hospital Scope: " + (claimData.isInScopeHospital() ? "In Scope" : "Out of Scope") + "<br>";

            communicationService.sendEmail(claimData.getIntimationNumber() + "Bill Module Not Run",
                    emailBody, CLAIM_REGISTRATION_FAILED_EMAIL_TO);
        }
        log.info("{}: Is PML executed flag {} claimRunDTO {}", claimData.getIntimationNumber(), isPmlExecuted, claimRunDTO);
        if (!isPmlExecuted && isPMLRunRequested(claimRunDTO)) {
            BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimData.getId());
            checkAndProcessPML(enhancementConfig, claimRunDTO, claimData, billTariffResponse);
        }
    }

    private boolean isPMLRunRequested(ClaimRunDTO claimRunDTO) {
        return isBillRunRequested(claimRunDTO)
                || ClaimRunIdentifier.RERUN_PML.equals(claimRunDTO.getClaimRunIdentifier())
                || ClaimRunIdentifier.FRESH_CLAIM_PML_RUN.equals(claimRunDTO.getClaimRunIdentifier());
    }

    private boolean isBillRunRequested(ClaimRunDTO claimRunDTO) {
        return ClaimRunIdentifier.FRESH_CLAIM_RUN.equals(claimRunDTO.getClaimRunIdentifier())
                || ClaimRunIdentifier.FRESH_CLAIM_BILL_TARIFF_RUN.equals(claimRunDTO.getClaimRunIdentifier())
                || ClaimRunIdentifier.CLAIM_RERUN.equals(claimRunDTO.getClaimRunIdentifier())
                || ClaimRunIdentifier.RERUN_BILL_TARIFF_PML.equals(claimRunDTO.getClaimRunIdentifier());
    }

    private static boolean isVneuronRunRequested(ClaimRunDTO claimRunDTO) {
        return ClaimRunIdentifier.FRESH_CLAIM_RUN.equals(claimRunDTO.getClaimRunIdentifier())
                || ClaimRunIdentifier.CLAIM_RERUN.equals(claimRunDTO.getClaimRunIdentifier())
                || ClaimRunIdentifier.RERUN_VNEURON.equals(claimRunDTO.getClaimRunIdentifier());

    }

    private ClaimRunDTO getClaimRunDTOPML(ClaimRunDTO claimRunDTO) {
        return ClaimRunDTO.builder()
                .claimRunIdentifier(ClaimRunIdentifier.FRESH_CLAIM_PML_RUN)
                .claimId(claimRunDTO.getClaimId())
                .claimModuleStats(claimRunDTO.getClaimModuleStats())
                .build();
    }

    private ClaimRunDTO getClaimRunDTOBill(ClaimRunDTO claimRunDTO) {
        return ClaimRunDTO.builder()
                .claimRunIdentifier(ClaimRunIdentifier.FRESH_CLAIM_RUN.equals(claimRunDTO.getClaimRunIdentifier())
                        ? ClaimRunIdentifier.FRESH_CLAIM_BILL_TARIFF_RUN : ClaimRunIdentifier.RERUN_BILL_TARIFF)
                .claimId(claimRunDTO.getClaimId())
                .claimModuleStats(claimRunDTO.getClaimModuleStats())
                .build();
    }

    private static ClaimRunDTO getClaimRunDTOVNeuron(ClaimRunDTO claimRunDTO) {
        return ClaimRunDTO.builder()
                .claimRunIdentifier(ClaimRunIdentifier.FRESH_CLAIM_VNEURON_RUN)
                .claimId(claimRunDTO.getClaimId())
                .claimModuleStats(claimRunDTO.getClaimModuleStats())
                .build();
    }


    /**
     * Adds a claim to the in-process status.
     *
     * @param claimData The claim data.
     */
    private int addClaimInProcessStatus(ClaimData claimData) {
        return claimDataRepository.updateClaimStatus(EnhancementStatus.ENHANCEMENT_IN_PROGRESS.name(), claimData.getId());
    }

    /**
     * Adds a claim to the bill tariff queue.
     *
     * @param claimRunDTO The claim run DTO.
     * @return The bill tariff response DTO.
     * @throws IOException If an I/O error occurs.
     */
    public BillTariffResponse addClaimBillTariffQueue(ClaimRunDTO claimRunDTO) throws IOException {
        return billTariffService.processBillTariffRequest(claimRunDTO);
    }

    /**
     * Adds a claim to the vNeuron queue.
     *
     * @param claimRunDTO The claim run DTO.
     * @throws IOException If an I/O error occurs.
     */
    private void addClaimVNeuronQueue(ClaimRunDTO claimRunDTO) throws IOException {
        kafkaProducerService.sendMessage(AppConstants.VNEURON_TOPIC_NAME, claimRunDTO.getClaimId(), GsonUtils.toJson(claimRunDTO));
    }

    /**
     * Retrieves claim data by claim ID.
     *
     * @param claimId The claim ID.
     * @return The claim data.
     */
    public ClaimData getClaimData(long claimId) {
        return claimDataRepository.findByClaimDataIdAndNonDelete(claimId);
    }

    /**
     * Retrieves claim data by intimation number.
     *
     * @param intimationNumber The intimation number.
     * @return The claim data.
     */
    public ClaimData getClaimDataByIntimationNumber(String intimationNumber) {
        return claimDataRepository.findByIntimationNumber(intimationNumber).orElse(null);
    }

    /**
     * Fetches all claim details.
     *
     * @param corporate           The corporate entity.
     * @param claimListRequestDTO The claim list request DTO.
     * @return The claim data list DTO.
     */
    public ClaimDataListDTO fetchAllClaimDetails(Corporate corporate, ClaimListRequestDTO claimListRequestDTO)
            throws UnsupportedEncodingException {
        return claimDataRepositoryImpl.findClaimData(corporate, claimListRequestDTO);
    }

    /**
     * Prepares the claim list request DTO.
     *
     * @param pageNo         The page number.
     * @param pageSize       The page size.
     * @param insurerId      The insurer ID.
     * @param startDate      The start date.
     * @param endDate        The end date.
     * @param attributeName  The attribute name.
     * @param attributeValue The attribute value.
     * @param sortBy         The sort by attribute.
     * @return The claim list request DTO.
     * @throws ParseException If a parsing error occurs.
     */
    public ClaimListRequestDTO getClaimListRequestDTO(int pageNo, int pageSize, String insurerId, String startDate,
                                                      String endDate, String attributeName, String attributeValue,
                                                      String sortBy) throws ParseException {
        return ClaimListRequestDTO.builder()
                .pageNo(pageNo - 1)
                .pageSize(pageSize)
                .insurerId(insurerId)
                .startDate(startDate)
                .endDate(DateUtil.dateToString(DateUtil.getPastOrFutureDate(DateUtil.stringToDate(endDate), 1)))
                .attributeName(attributeName)
                .attributeValue(attributeValue)
                .dataSortBy(DataSortEnum.DATE_CREATED)
                .build();
    }

    /**
     * Prepares claim data for the given claim ID.
     *
     * @param claimId The claim ID.
     * @return The claim details response DTO.
     */
    public ClaimDetailsResponseDTO prepareClaimData(long claimId) {
        ClaimData claimData = getClaimData(claimId);
        BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimId);
        PMLResponse pmlResponse = pmlService.getPMLResponseByClaimDataId(claimId);
        VneuronResponse vNeuronResponse = vNeuronService.getVneuronResponseByClaimDataId(claimId);
        s3FileService.checkFileExpiry(claimData,billTariffResponse,vNeuronResponse);
        List<ClaimTransition> claimTransitions = claimTransitionService.findAllClaimTransitionByClaimDataIdLatest(claimId);
        return setClaimDetailsResponseDTO(claimData, billTariffResponse, pmlResponse, vNeuronResponse, claimTransitions);
    }

    /**
     * Prepares claim data for the given claim ID.
     *
     * @param intimationNumber Claim intimation number.
     * @return The claim details response DTO.
     */
    public ClaimDetailsResponseDTO prepareClaimDataByIntimationNumber(String intimationNumber) {
        ClaimData claimData = getClaimDataByIntimationNumber(intimationNumber);
        if (claimData == null) {
            return null;
        }
        BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimData.getId());
        PMLResponse pmlResponse = pmlService.getPMLResponseByClaimDataId(claimData.getId());
        VneuronResponse vNeuronResponse = vNeuronService.getVneuronResponseByClaimDataId(claimData.getId());
        s3FileService.checkFileExpiry(claimData,billTariffResponse,vNeuronResponse);
        List<ClaimTransition> claimTransitions = claimTransitionService.findAllClaimTransitionByClaimDataIdLatest(claimData.getId());
        return setClaimDetailsResponseDTO(claimData, billTariffResponse, pmlResponse, vNeuronResponse, claimTransitions);
    }

    /**
     * Sets the claim details response DTO.
     *
     * @param claimData          The claim data.
     * @param billTariffResponse The bill tariff response.
     * @param pmlResponse        The PML response.
     * @param vNeuronResponse    The vNeuron response.
     * @return The claim details response DTO.
     */
    @SkipResponseLogging
    private ClaimDetailsResponseDTO setClaimDetailsResponseDTO(ClaimData claimData, BillTariffResponse billTariffResponse,
                                                               PMLResponse pmlResponse, VneuronResponse vNeuronResponse,
                                                               List<ClaimTransition> claimTransitions) {
        if (claimData == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }
        Corporate corporate = corporateService.getUserCorporate((int) claimData.getHospitalId());
        Procedures procedures = procedureService.findProcedureById(claimData.getProcedureId()).orElse(null);
        Illnesses illnesses = illnessService.getFirstIllnessByICDCode(claimData.getIcdCode());
        VNeuronResponseDTO vNeuronResponseDTO = vNeuronResponse != null ? vNeuronResponse.getVNeuronResponseDTO() : null;

        BigDecimal amountRequested = billTariffResponse != null && billTariffResponse.getClaimBillAmountRequested() != null
                ? billTariffResponse.getClaimBillAmountRequested() : BigDecimal.ZERO;
        BigDecimal amountAfterTariff = billTariffResponse != null ? billTariffResponse.getAmountAfterTariffApplication() : BigDecimal.ZERO;
        BigDecimal amountAfterPML = pmlResponse != null ? (pmlResponse.getApprovedAmount() != null ? pmlResponse.getApprovedAmount() : BigDecimal.ZERO) : BigDecimal.ZERO;

        HashMap<String, BigDecimal> deductibleMap = pmlService.getDeductibleMap(pmlResponse);
        BigDecimal patientPayableDeductible = deductibleMap.get("patientPayableDeductible");
        BigDecimal hospitalPayableDeductible = deductibleMap.get("hospitalPayableDeductible");
//        amountAfterPML = amountAfterPML != null ? amountAfterPML.subtract(patientPayableDeductible).subtract(hospitalPayableDeductible) : BigDecimal.ZERO;

//        BigDecimal tariff = amountAfterPML != null ? amountAfterPML : BigDecimal.ZERO;
        BigDecimal patientDeductible = patientPayableDeductible != null ? patientPayableDeductible : BigDecimal.ZERO;
        BigDecimal hospitalDeductible = hospitalPayableDeductible != null ? hospitalPayableDeductible : BigDecimal.ZERO;

        if (amountAfterPML != null && amountAfterPML.compareTo(BigDecimal.ZERO) > 0) {
            amountAfterPML = amountAfterPML.subtract(patientDeductible).subtract(hospitalDeductible);
        }

        boolean isBillMetaDataExist = isTariffMetaDataExist(billTariffResponse);
        List<DocumentMaster> documentMaster = documentService.getClaimDocumentMasterList(claimData.getIntimationNumber());

        ClaimModuleStats claimModuleStatsPreAuthLatest = claimCommonService.getLatestClaimModuleStatsByClaimIdAndClaimStage(claimData.getId(),
                ClaimRequestTypeEnum.preauth_request.toString());
        ClaimModuleStats claimModuleStatsDischargeLatest = claimCommonService.getLatestClaimModuleStatsByClaimIdAndClaimStage(claimData.getId(),
                ClaimRequestTypeEnum.final_enhancement_request.toString());
        ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationResultRepository.findByClaimDataId(claimData.getId());

        BigDecimal amountSaved = BigDecimal.ZERO;
        if (amountRequested != null && amountAfterPML != null) {
            amountSaved = amountRequested.subtract(amountAfterPML);
        } else if (amountRequested != null && amountAfterTariff != null) {
            amountSaved = amountRequested.subtract(amountAfterTariff);
        }

        BigDecimal amountSavedInPercentage = BigDecimal.ZERO;
        if (amountRequested != null && amountRequested.compareTo(BigDecimal.ZERO) > 0) {
            amountSavedInPercentage = amountSaved.divide(amountRequested, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        ClaimRejectionReason claimRejectionReason = claimRejectionReasonRepo.findByClaimIdClaimStageAndStatus(claimData.getId(),
                claimData.getClaimStatus(), claimData.getAdjudicationStatus());
        String billS3Url = getBillS3Url(billTariffResponse);
//        List<RiderDetails> riderDetailsList = getRiderDetails(billTariffResponse);
        List<Procedures> procedureList = procedureService.findAllProcedures();
        List<HospitalServiceType> hospitalServiceTypes = hospitalServiceTypeRepository.findByHospitalId(claimData.getHospitalId());
        NivaUcrClaims nivaUcrClaims = null;
        DocClaimStage docClaimStage = DocClaimStage.getStage(claimData.getClaimStatus());
        if (docClaimStage != null && docClaimStage.equals(DocClaimStage.PRE_AUTH)) {
            nivaUcrClaims = nivaUcrClaimsRepo.findLatestByClaimDataId(claimData.getId());
        }
        ClaimRiderDetails claimRiderDetails = claimRidersRepository.findByClaimDataId(claimData.getId());
        List<ErrorMessageLogs> errorMessageLogsList = errorMessageLogRepository.findByClaimDataId(claimData.getId());
        return ClaimDetailsResponseDTO.builder()
                .id(String.valueOf(claimData.getId()))
                .intimationNumber(claimData.getIntimationNumber())
                .hospitalId(claimData.getHospitalId())
                .insuranceAgencyId(claimData.getInsuranceAgencyId())
                .insurerClaimIdentifier(claimData.getIntimationNumber())
                .dateOfFirstDiagnosis(claimData.getDateOfFirstDiagnosis())
                .roomType(claimData.getRoomType())
                .claimType(claimData.getClaimType())
                .patientName(claimData.getPatientName())
                .patientAge(claimData.getPatientAge())
                .dateOfBirth(claimData.getDateOfBirth())
                .isEnhancementProcessed(claimData.isEnhancementProcessed())
                .isInsurerVisible(claimData.isInsurerVisible())
                .active(claimData.isActive())
                .deleted(claimData.isDeleted())
                .dateCreated(claimData.getDateCreated())
                .dateUpdated(claimData.getDateUpdated())
                .dateOfAdmission(claimData.getDateOfAdmission())
                .dateOfDischarge(claimData.getDateOfDischarge())
                .hospitalName(corporate != null ? corporate.getName() : "N/A")
                .hospitalCode(corporate != null ? corporate.getCorporateCode() : "N/A")
                .procedureName(procedures != null ? procedures.getName() : "Out Of Scope")
                .procedureId(claimData.getProcedureId())
                .procedureCode(procedures != null ? procedures.getVneuronSctidCode() : "Out Of Scope")
                .productName(claimData.getProductCode() != null ? claimData.getProductCode() : "-")
                .productCode(claimData.getProductCode())
                .claimBillAmountRequested(amountRequested)
                .amountAfterTariffApplication(amountAfterTariff)
                .amountDeductedInPercent(amountSavedInPercentage)
                .savings(amountSaved)
                .billIdentifier(billTariffResponse != null ? billTariffResponse.getBillIdentifier() : null)
                .tariffIdentifier(billTariffResponse != null ? billTariffResponse.getBillIdentifier() : null)
                .tariffData(billTariffService.getTariffData(claimData, billTariffResponse))
                .pmlIdentifier(claimData.getIntimationNumber())
                .medicalIdentifier(vNeuronResponse != null ? vNeuronResponse.getMedicalIdentifier() : null)
                .documentsNameWithLocation(isBillMetaDataExist
                        && billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getHospital_claim_metadata() != null
                        && billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getHospital_claim_metadata().getDocuments_name_with_location() != null
                        ? getEncryptedBillSOCUrl(billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getHospital_claim_metadata().getDocuments_name_with_location()) : null)
                .tariffPmlDataMerged(pmlService.getPMLDataMergedV1(pmlResponse, billTariffResponse))
                .amountAfterPolicyrulesApplication(amountAfterPML)
                .pmlClaimResult(pmlResponse != null && pmlResponse.getPmlResponseDTO() != null ? pmlResponse.getPmlResponseDTO().getClaim_result() : null)
                .vNeuronResponseDTO(getEncryptedVneuronDocUrl(vNeuronResponseDTO))
                .vneuronDecision(vNeuronResponseDTO != null ? ClaimVNeuronResponseStatus.getDecision(vNeuronResponseDTO.getAdjudicationResult()) : null)
                .vneuronRemarks(vNeuronResponseDTO != null ? vNeuronResponseDTO.getAdjudicationReason() : null)
                .amountAfterMedicalAdmissibility(getAmountAfterMedicalAdmissibility(nivaUcrClaims, amountAfterTariff, amountAfterPML, vNeuronResponse))
                .adjudicationStatus(claimData.getAdjudicationStatus())
                .tariffMatchPercentage(billTariffResponse != null ? billTariffResponse.getTariffMatchPercentage() : null)
                .treatmentType(claimData.getTreatmentType())
                .claimTransactionType(ClaimTransactionType.CASHLESS)
                .policyInceptionDate(claimData.getCurrentPolicyInceptionDate())
                .policyEndDate(claimData.getCurrentPolicyEndDate())
                .policyStartDate(claimData.getCurrentPolicyStartDate())
                .outOfScopeHospital(false)
                .policyNumber(claimData.getPolicyNumber())
                .claimStatus(String.valueOf(claimData.getClaimStatus()))
                .isBillFound(billTariffResponse != null && billTariffResponse.isBillFound())
                .billDocAvailable(billTariffResponse != null && billTariffResponse.isDocAvailable())
                .reasonForHospitalization(claimData.getReasonForHospitalization())
                .illnessName(illnesses != null ? illnesses.getIllnessName() : "-")
                .icdCode(claimData.getIcdCode())
                .pedList(null)
                .pushedToInsurer(claimData.isPushedToInsurer())
                .baseSumInsured(claimData.getBaseSumInsured())
                .remainingSumInsured(claimData.getRemainingSumInsured())
                .copayZone(claimData.getCopayZone())
                .hospitalZone(claimData.getHospitalZone())
                .coverCode(claimData.getCoverCode())
                .assigned(claimData.isAssigned())
                .assignedBy(claimData.getAssigned_by())
                .billName(s3FileService.getFileNameFromS3Url(billS3Url))
                .billS3Url(getEncryptedBillS3Url(billS3Url))
                .claimDocumentMaster(getEncryptedDocumentsUrl(documentMaster))
                .initialTat(claimModuleStatsPreAuthLatest != null ? claimModuleStatsPreAuthLatest.getClaimTat() : 0)
                .dischargeTat(claimModuleStatsDischargeLatest != null ? claimModuleStatsDischargeLatest.getClaimTat() : 0)
                .inScopeHospital(claimData.isInScopeHospital())
                .inScopePolicy(claimData.isInScopePolicy())
                .inScopeProcedure(claimData.isInScopeProcedure())
                .claimTransitions(claimTransitions)
                .status(claimData.getStatus())
                .claimStage(claimAdjudicationResult != null ? claimAdjudicationResult.getClaimStage() : null)
                .preAuthInsurerDecision(claimAdjudicationResult != null ? claimAdjudicationResult.getPreAuthInsurerDecision() : null)
                .claimRemarks(claimAdjudicationResult != null ? claimAdjudicationResult.getRemarks() : null)
                .queryText(claimAdjudicationResult != null ? claimAdjudicationResult.getQueryText() : null)
                .dischargeInsurerDecision(claimAdjudicationResult != null ? claimAdjudicationResult.getDischargeInsurerDecision() : null)
                .claimRerun(claimData.isClaimRerun())
                .preAuthId(claimData.getInsurerIdentifier() != null ? claimData.getInsurerIdentifier() : "N/A")
                .claimRejectionReason(claimRejectionReason != null ? claimRejectionReason.getReason() : null)
                .billRequestedAmount(amountRequested)
                .billApprovedAmount(amountAfterTariff)
                .billSavedAmount(getBillAmountSaved(amountRequested, amountAfterTariff))
                .procedureList(procedureList)
                .hospitalServiceTypeList(hospitalServiceTypes)
                .patientPayableDeductable(patientPayableDeductible)
                .hospitalPayableDeductable(hospitalPayableDeductible)
                .receivedReverseFeed(claimData.isReceivedReverseFeed())
                .emailClaim(claimData.isEmailFlow())
                .ucrApplied(nivaUcrClaims != null)
                .ucrApprovedAmount(nivaUcrClaims != null ? nivaUcrClaims.getFinalApprovedAmount() : null)
                .claimRiderDetails(claimRiderDetails)
                .errorMessageLogsList(errorMessageLogsList)
//                .ridersDetails(riderDetailsList)
                .build();
    }

    private VNeuronResponseDTO getEncryptedVneuronDocUrl(VNeuronResponseDTO vNeuronResponseDTO) {
        encryptedDocumentsUrl(vNeuronResponseDTO);
        encryptedProcedureUrl(vNeuronResponseDTO);
        encryptedDiagnosisUrl(vNeuronResponseDTO);
        encryptedEvidenceUrl(vNeuronResponseDTO);

        return vNeuronResponseDTO;
    }

    private void encryptedEvidenceUrl(VNeuronResponseDTO vNeuronResponseDTO) {
        if (vNeuronResponseDTO != null && vNeuronResponseDTO.getAdjudicationDetails() != null
                && vNeuronResponseDTO.getProcedureCodes() != null && vNeuronResponseDTO.getProcedureCodes().size() > 0
                && vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)) != null
                && vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getEvidence() != null
                && !vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getEvidence().isEmpty()) {
            List<ConditionObj> conditionObjList = vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getEvidence();
            conditionObjList.forEach(conditionObj -> {
                if (conditionObj.getDocumentUrl() != null) {
                    try {
                        conditionObj.setFileName(s3FileService.getFileNameFromS3Url(conditionObj.getDocumentUrl()));
                        conditionObj.setDocumentUrl(encryptionUtils.encryptInternalData(conditionObj.getDocumentUrl()));
                    } catch (Exception e) {
                        log.error("Error while encrypting the vNeuron diagnosis URL: {}", e.getMessage());
                    }
                }
            });
        }
    }

    private void encryptedDiagnosisUrl(VNeuronResponseDTO vNeuronResponseDTO) {
        if (vNeuronResponseDTO != null && vNeuronResponseDTO.getAdjudicationDetails() != null
                && vNeuronResponseDTO.getProcedureCodes() != null && vNeuronResponseDTO.getProcedureCodes().size() > 0
                && vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)) != null
                && vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getDiagnoses() != null
                && !vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getDiagnoses().isEmpty()) {
            List<ConditionObj> conditionObjList = vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getDiagnoses();
            conditionObjList.forEach(conditionObj -> {
                if (conditionObj.getDocumentUrl() != null) {
                    try {
                        conditionObj.setFileName(s3FileService.getFileNameFromS3Url(conditionObj.getDocumentUrl()));
                        conditionObj.setDocumentUrl(encryptionUtils.encryptInternalData(conditionObj.getDocumentUrl()));
                    } catch (Exception e) {
                        log.error("Error while encrypting the vNeuron diagnosis URL: {}", e.getMessage());
                    }
                }
            });
        }
    }

    private void encryptedProcedureUrl(VNeuronResponseDTO vNeuronResponseDTO) {
        if (vNeuronResponseDTO != null && vNeuronResponseDTO.getAdjudicationDetails() != null
                && vNeuronResponseDTO.getProcedureCodes() != null && vNeuronResponseDTO.getProcedureCodes().size() > 0
                && vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)) != null
                && vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getProcedures() != null
                && !vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getProcedures().isEmpty()) {
            List<ConditionObj> conditionObjList = vNeuronResponseDTO.getAdjudicationDetails().get(vNeuronResponseDTO.getProcedureCodes().get(0)).getProcedures();
            conditionObjList.forEach(conditionObj -> {
                if (conditionObj.getDocumentUrl() != null) {
                    try {
                        conditionObj.setFileName(s3FileService.getFileNameFromS3Url(conditionObj.getDocumentUrl()));
                        conditionObj.setDocumentUrl(encryptionUtils.encryptInternalData(conditionObj.getDocumentUrl()));
                    } catch (Exception e) {
                        log.error("Error while encrypting the vNeuron procedure URL: {}", e.getMessage());
                    }
                }
            });
        }
    }

    private void encryptedDocumentsUrl(VNeuronResponseDTO vNeuronResponseDTO) {
        if (vNeuronResponseDTO != null && vNeuronResponseDTO.getDocuments() != null
                && vNeuronResponseDTO.getDocuments().size() > 0) {
            vNeuronResponseDTO.getDocuments().forEach(document -> {
                if (document.getOriginal_document_url() != null) {
                    try {
                        document.setFileName(s3FileService.getFileNameFromS3Url(document.getOriginal_document_url()));
                        document.setOriginal_document_url(encryptionUtils.encryptInternalData(document.getOriginal_document_url()));
                    } catch (Exception e) {
                        log.error("Error while encrypting the vNeuron document URL: {}", e.getMessage());
                    }
                }

                if (document.getOcr_output_url() != null) {
                    try {
                        document.setOcr_output_url(encryptionUtils.encryptInternalData(document.getOcr_output_url()));
                    } catch (Exception e) {
                        log.error("Error while encrypting the vNeuron OCR output URL: {}", e.getMessage());
                    }
                }
            });
        }
    }

    private List<DocumentMaster> getEncryptedDocumentsUrl(List<DocumentMaster> documentMasterList) {
        if (documentMasterList != null && documentMasterList.size() > 0) {
            documentMasterList.forEach(documentMaster -> {
                if (documentMaster.getPreSignedUrl() != null) {
                    try {
                        documentMaster.setFileName(s3FileService.getFileNameFromS3Url(documentMaster.getPreSignedUrl()));
                        documentMaster.setPreSignedUrl(encryptionUtils.encryptInternalData(documentMaster.getPreSignedUrl()));
                    } catch (Exception e) {
                        log.error("Error while encrypting the document pre-signed URL: {}", e.getMessage());
                    }
                }
            });
        }

        return documentMasterList;
    }

    private DocumentsNameWithLocation getEncryptedBillSOCUrl(DocumentsNameWithLocation documentsNameWithLocation) {
        if (documentsNameWithLocation != null && documentsNameWithLocation.getSoc() != null) {
            try {
                documentsNameWithLocation.setSocFileName(s3FileService.getFileNameFromS3Url(documentsNameWithLocation.getSoc()));
                documentsNameWithLocation.setSoc(encryptionUtils.encryptInternalData(documentsNameWithLocation.getSoc()));
            } catch (Exception e) {
                log.error("Error while encrypting the bill SOC S3 URL: {}", e.getMessage());
            }
        }

        if (documentsNameWithLocation != null && documentsNameWithLocation.getAnh() != null) {
            try {
                documentsNameWithLocation.setAnhFileName(s3FileService.getFileNameFromS3Url(documentsNameWithLocation.getAnh()));
                documentsNameWithLocation.setAnh(encryptionUtils.encryptInternalData(documentsNameWithLocation.getAnh()));
            } catch (Exception e) {
                log.error("Error while encrypting the bill ANH S3 URL: {}", e.getMessage());
            }
        }

        return documentsNameWithLocation;
    }

    private String getEncryptedBillS3Url(String billS3Url) {
        // Now we need to encrypt the billS3Url if it exists
        if (billS3Url != null) {
            try {
                billS3Url = encryptionUtils.encryptInternalData(billS3Url);
            } catch (Exception e) {
                log.error("Error while encrypting the bill S3 URL: {}", e.getMessage());
            }
        }

        return billS3Url;
    }

    private String getBillS3Url(BillTariffResponse billTariffResponse) {
        return billTariffResponse != null
                && billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getData() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getMetadata() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getBill_s3_url() != null
                ? billTariffResponse.getBillTariffResponseDTO().getData().getMetadata().getBill_s3_url() : null;
    }

    private BigDecimal getBillAmountSaved(BigDecimal amountRequested, BigDecimal amountAfterTariff) {
        BigDecimal billSavedAmount = BigDecimal.ZERO;
        if (amountRequested != null && amountAfterTariff != null) {
            billSavedAmount = amountRequested.subtract(amountAfterTariff);
        }

        return billSavedAmount;
    }

    public PMLResponse processPmlRequestFromAdmissionDetails(ClaimData claimData, BillTariffResponse billTariffResponse,
                                                             PMLResponse pmlResponse, boolean isBillTariffResponseCreated) {
        log.info("Prepared bill tariff response from claim admission details {}", billTariffResponse);
        if (isBillTariffResponseCreated) {
            billTariffService.saveBillTariffResponse(claimData, billTariffResponse.getBillTariffResponseDTO(), 0,
                    null, false);
        }

        EnhancementConfig enhancementConfig = enhancementConfigRepository.findByHospitalIdAndInsuranceAgencyId(
                claimData.getHospitalId(), claimData.getInsuranceAgencyId());
        ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                .claimId(claimData.getId())
                .claimRunIdentifier(ClaimRunIdentifier.RERUN_PML)
                .claimModuleStats(billTariffService.getClaimModuleStats(claimData.getId()))
                .build();

        if (billTariffResponse.getBillTariffResponseDTO() != null) {
            if (enhancementConfig != null && enhancementConfig.isPmlEnabled()) {
                checkAndProcessPML(enhancementConfig, getClaimRunDTOPML(claimRunDTO), claimData, billTariffResponse);
                pmlResponse = pmlService.getPMLResponseByClaimDataId(claimData.getId());
            }
        }
        return pmlResponse;
    }

    private boolean isTariffMetaDataExist(BillTariffResponse billTariffResponse) {
        return billTariffResponse != null
                && billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getData() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getMetadata() != null;
    }

    private BigDecimal getAmountAfterMedicalAdmissibility(NivaUcrClaims nivaUcrClaims, BigDecimal amountAfterTariff, BigDecimal amountAfterPML,
                                                          VneuronResponse vNeuronResponse) {
        if (vNeuronResponse != null && vNeuronResponse.getVNeuronResponseDTO() != null
                && VNeuronStatusEnum.REJECTED.name().equals(vNeuronResponse.getVNeuronResponseDTO().getStatus())) {
            return BigDecimal.ZERO;
        }
        return nivaUcrClaims != null
                ? (nivaUcrClaims.getFinalApprovedAmount() != null
                ? nivaUcrClaims.getFinalApprovedAmount()
                : (amountAfterPML != null
                ? amountAfterPML
                : amountAfterTariff))
                : amountAfterPML != null ? amountAfterPML : amountAfterTariff;
    }

    public void rerunClaim(String decryptedClaimId, ClaimRerunRequestDTO claimRerunRequestDTO) throws IOException {
        ClaimData claimData = claimDataRepository.findById(Long.valueOf(decryptedClaimId))
                .orElseThrow(() -> new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID));
        if (claimData == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }

        if (claimData.isClaimRerun()) {
            claimData.setClaimRerun(false);
            claimData.setPushedToInsurer(false);
            claimDataRepository.save(claimData);
        }

        // Now we need to check for the claim_module_stats for the pending request.
        ClaimModuleStats claimModuleStatsExisting = claimCommonService.getLatestClaimModuleStats(claimData.getId());
        if (claimModuleStatsExisting == null) {
            claimModuleStatsExisting = claimCommonService.createClaimModuleStats(claimData.getId(),
                    ClaimRequestTypeEnum.preauth_request, claimData.getTxnId());
        }

        BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimData.getId());
        if (claimModuleStatsExisting != null
                && claimCommonService.isClaimUnderProcessing(claimModuleStatsExisting, billTariffResponse.getBillTariffResponseDTO())) {
            // Check if threshold time exceeded. If yes, then we can proceed with the rerun.
            if (claimModuleStatsExisting.getDateUpdated() != null
                    && isThresholdTimeExceeded(claimModuleStatsExisting.getDateUpdated())) {
                claimCommonService.updateClaimModuleStatsProcessed(claimModuleStatsExisting);
            } else {
                throw new VitrayaException(VitrayaErrorCodes.CLAIM_UNDER_PROCESSING);
            }
        }

        ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                .claimId(claimData.getId())
                .claimRunIdentifier(claimRerunRequestDTO.getClaimRunIdentifier())
                .claimModuleStats(claimModuleStatsExisting)
                .verifiedClaim(true)
                .doAdjudication(true)
                .build();

        log.info("Claim rerun requested with claimRunDTO: {}", claimRunDTO);
        addClaimToQueue(claimRunDTO);
    }

    private boolean isThresholdTimeExceeded(Date dateUpdated) {
        return DateUtil.getTimeDifference(dateUpdated, new Date()) > CLAIM_MODULE_RESPONSE_WAIT_THRESHOLD;
    }

    private void markClaimNotPushedToInsurer(long id) {
        claimDataRepository.updatePushToInsurerFlag(id, false);
    }

    public void checkAndProcessPML(EnhancementConfig enhancementConfig, ClaimRunDTO claimRunDTO, ClaimData claimData,
                                   BillTariffResponse billTariffResponse) {
        if (isPmlReRunOnlyRequested(claimRunDTO) || isTariffAndPmlRerunRequested(claimRunDTO)) {
            // ToDo: Capture the failure of the module.
            if (billTariffResponse == null) {
                log.error("Bill Tariff response not found for claim id: {}", claimData.getId());
                throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
            }

            if (claimCommonService.isClaimAdmissionDetailsUse(claimData, billTariffResponse)) {
                billTariffResponse = claimAdmissionService.getBillTariffResponseFromAdmission(claimData, billTariffResponse);
                billTariffService.saveBillTariffResponse(claimData, billTariffResponse.getBillTariffResponseDTO(), 0,
                        null, false);
                ClaimModuleStats claimModuleStats = claimRunDTO.getClaimModuleStats();
                claimCommonService.recordClaimModuleStats(claimData, claimModuleStats, ClaimModulesEnum.BILL_TARIFF,
                        billTariffResponse.getBillTariffResponseDTO().getUnique_identifier(), 0);
                log.info("[Claim Data Id: {}]: Saving the bill tariff response from claim admission details {}",
                        claimData.getId(), billTariffResponse);
            }

            if (billTariffResponse.getBillTariffResponseDTO() != null
                    && billTariffResponse.getBillTariffResponseDTO().getResponse_code() != null
                    && (billTariffResponse.getBillTariffResponseDTO().getResponse_code().equalsIgnoreCase("TARIFF_APPLIED")
                    || billTariffResponse.getBillTariffResponseDTO().getResponse_code().equalsIgnoreCase("DEFAULT_TARIFF_APPLIED"))) {
                checkForPML(enhancementConfig, billTariffResponse.getBillTariffResponseDTO(), claimData, claimRunDTO);
                checkClaimAutoSubmissionCondition(claimRunDTO.getClaimId());

            } else {
                log.info("Tariff is Failed For this claim.");
                communicationService.sendEmail(claimData.getIntimationNumber() + " | Tariff Failed",
                        "Tariff Failed for claim ID: " + claimData.getId(), MODULE_FAILED_EMAIL_TO);
            }
        }
    }

    private boolean isTariffAndPmlRerunRequested(ClaimRunDTO claimRunDTO) {
        return claimRunDTO.getClaimRunIdentifier() != null && claimRunDTO.getClaimRunIdentifier()
                .equals(ClaimRunIdentifier.RERUN_TARIFF_PML);
    }

    private boolean isPmlReRunOnlyRequested(ClaimRunDTO claimRunDTO) {
        return isPMLRunRequested(claimRunDTO);
    }

    /**
     * Checks for PML processing.
     *
     * @param enhancementConfig     The enhancement configuration.
     * @param billTariffResponseDTO The bill tariff response DTO.
     * @param claimData             The claim data.
     * @param claimRunDTO           The claim run DTO.
     */
    public void checkForPML(EnhancementConfig enhancementConfig, BillTariffResponseDTO billTariffResponseDTO,
                            ClaimData claimData, ClaimRunDTO claimRunDTO) {
        if (enhancementConfig.isPmlEnabled() && claimData.isInScopePolicy()) {
            if (billTariffResponseDTO != null
                    && !claimCommonService.isBillOpenForEditing(billTariffResponseDTO)) {
                addClaimPMLQueue(claimData, billTariffResponseDTO, claimRunDTO);
            } else {
                log.error("Please check the bill tariff response {} for claim id: {}.", billTariffResponseDTO, claimData.getId());
                claimCommonService.recordClaimModuleStats(claimData, claimRunDTO.getClaimModuleStats(),
                        ClaimModulesEnum.PML, "BILL_TARIFF_NOT_READY", 0);
            }
        } else {
            log.info("For claim {}, PML is not enabled for insurance {} and hospital id {}",
                    claimData.getId(), claimData.getInsuranceAgencyId(), claimData.getHospitalId());
            claimCommonService.recordClaimModuleStats(claimData, claimRunDTO.getClaimModuleStats(),
                    ClaimModulesEnum.PML, "PML_NOT_ENABLED", 0);
        }
    }

    /**
     * Adds a claim to the PML queue.
     *
     * @param claimData             The claim data.
     * @param billTariffResponseDTO The bill tariff response DTO.
     * @param claimRunDTO           The claim run DTO.
     */
    private void addClaimPMLQueue(ClaimData claimData, BillTariffResponseDTO billTariffResponseDTO, ClaimRunDTO claimRunDTO) {
        pmlService.processPMLRequest(claimData, claimRunDTO, billTariffResponseDTO);
    }

    public void processExternalClaim(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, Corporate corporate, MultipartFile[] files)
            throws IOException, ParseException {
        fetchPendingClaimDetails(vitrayaInsurerClaimData, corporate, files);
        ClaimData claimData = saveClaimDetails(vitrayaInsurerClaimData);
        claimAdmissionService.saveClaimAdmissionDetails(vitrayaInsurerClaimData, claimData);
        ClaimModuleStats claimModuleStats = claimCommonService.createClaimModuleStats(claimData.getId(),
                vitrayaInsurerClaimData.getRequestType(), vitrayaInsurerClaimData.getTxnId());
        claimCommonService.recordClaimAdjudicationResult(claimData, vitrayaInsurerClaimData);
        claimTransitionService.saveClaimTransition(claimData, vitrayaInsurerClaimData);
        if (ClaimRequestTypeEnum.preauth_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            saveInsurerFetchResponses(vitrayaInsurerClaimData, claimData);
        }
        errorMessageLogsService.saveRidersFlag(claimData.getIntimationNumber(), claimData);
        if (claimData != null) {
            if (!vitrayaInsurerClaimData.getRequest().getClaim().isInScopeProcedure()) {
                ErrorMsgType error = ErrorMsgType.OUT_OF_SCOPE_PROCEDURE;
                errorMessageLogsService.saveErrorMessages(claimData, error);
                log.info("Saving Error Message Logs for out of scope procedure in processExternalClaim method for {}", claimData.getIntimationNumber());
            }
            if (!vitrayaInsurerClaimData.getRequest().getClaim().isInScopePolicy()) {
                ErrorMsgType error = ErrorMsgType.OUT_OF_SCOPE_POLICY;
                errorMessageLogsService.saveErrorMessages(claimData, error);
                log.info("Saving Error Message Logs for out of scope policy in processExternalClaim method for {}", claimData.getIntimationNumber());
            }
          }
        ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                .claimId(claimData.getId())
                .claimRunIdentifier(ClaimRunIdentifier.FRESH_CLAIM_RUN)
                .claimModuleStats(claimModuleStats)
                .verifiedClaim(vitrayaInsurerClaimData.getRequest().isVerifiedClaim())
                .doAdjudication(isAdjudicationShouldBeDoneStageWise(claimData))
                .build();

        addClaimToQueue(claimRunDTO);
    }

    private boolean isAdjudicationShouldBeDoneStageWise(ClaimData claimData) {
        DocClaimStage docClaimStage = DocClaimStage.getStage(claimData.getClaimStatus());

        if ((docClaimStage == DocClaimStage.SETTLEMENT
                || docClaimStage == DocClaimStage.QUERY
                || docClaimStage == DocClaimStage.RECONSIDERATION) || claimData.isEmailFlow()) {
            return false;
        }
        return true;
    }

    private void saveInsurerFetchResponses(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, ClaimData claimData) {
        InsurerFetchResponses insurerFetchResponses = insurerFetchResponsesRepository
                .getInsurerFetchResponsesByClaimIntimationNumber(claimData.getIntimationNumber());

        if (insurerFetchResponses == null) {
            insurerFetchResponses = new InsurerFetchResponses();
        }

        insurerFetchResponses.setClaimDataId(claimData.getId());
        insurerFetchResponses.setClaimIntimationNumber(claimData.getIntimationNumber());
        if (vitrayaInsurerClaimData.getRequest().getInsurerFetchResponses() != null) {
            insurerFetchResponses.setPolicyData(vitrayaInsurerClaimData.getRequest().getInsurerFetchResponses().getPolicyData());
            insurerFetchResponses.setClaimHistory(vitrayaInsurerClaimData.getRequest().getInsurerFetchResponses().getClaimHistory());
        }
        insurerFetchResponses.setDateCreated(new Date());

        insurerFetchResponsesRepository.save(insurerFetchResponses);
    }

    public boolean updateDecision(UpdateDecisionRequest updateDecisionRequest) {
        try {
            ClaimData claimData = null;
            if (updateDecisionRequest.getClaimNumber() != null) {
                claimData = claimDataRepository.findByClaimDataId(updateDecisionRequest.getClaimDataId());
            }
            if (claimData != null) {
                AdjudicationStatus insurerAdjudicationStatus;
                if (updateDecisionRequest.getStatus().equalsIgnoreCase("APPROVE")) {
                    insurerAdjudicationStatus = AdjudicationStatus.APPROVED;
                    claimData.setAdjudicationStatus(AdjudicationStatus.APPROVED.getAdjudicationStatus());
                } else if (updateDecisionRequest.getStatus().equalsIgnoreCase("QUERY")) {
                    insurerAdjudicationStatus = AdjudicationStatus.QUERY;
                    claimData.setAdjudicationStatus(AdjudicationStatus.QUERY.getAdjudicationStatus());
                } else if (updateDecisionRequest.getStatus().equalsIgnoreCase("DENY")) {
                    insurerAdjudicationStatus = AdjudicationStatus.REJECTED;
                    claimData.setAdjudicationStatus(AdjudicationStatus.REJECTED.getAdjudicationStatus());
                } else if (updateDecisionRequest.getStatus().equalsIgnoreCase("FAIL")) {
                    insurerAdjudicationStatus = AdjudicationStatus.FAILED;
                    claimData.setAdjudicationStatus(AdjudicationStatus.FAILED.getAdjudicationStatus());
                } else {
                    log.info("Matching status not found: insurer decision: {}", updateDecisionRequest.getStatus());
                    return false;
                }
                ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationResultRepository.findByClaimDataId(claimData.getId());
                if (claimAdjudicationResult == null) {
                    claimAdjudicationResult = new ClaimAdjudicationResult();
                }

                if (claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RAISED)) {
                    claimAdjudicationResult.setPreAuthDecision(insurerAdjudicationStatus.getAdjudicationStatus());
                } else if (claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RAISED)) {
                    claimAdjudicationResult.setDischargeInsurerDecision(insurerAdjudicationStatus.getAdjudicationStatus());
                }

                if (claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT)
                        || claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RESPONSE_RE_PUSHED)) {
                    claimAdjudicationResult.setPreAuthInsurerDecision(insurerAdjudicationStatus.getAdjudicationStatus());
                    claimAdjudicationResult.setPreAuthInsurerAmountApproved(updateDecisionRequest.getAmountApproved());
                } else if (claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RESPONSE_SENT)
                        || claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RESPONSE_RE_PUSH)) {
                    claimAdjudicationResult.setDischargeInsurerDecision(insurerAdjudicationStatus.getAdjudicationStatus());
                    claimAdjudicationResult.setInsurerDischargeAmountApproved(updateDecisionRequest.getAmountApproved());
                }

                String remarks = updateDecisionRequest.getRemarks();
                String queryText = updateDecisionRequest.getQueryText();
                claimAdjudicationResult.setRemarks(remarks);
                claimAdjudicationResult.setQueryText(queryText);
                claimAdjudicationResult.setDateUpdated(new Date());

                claimData.setPushedToInsurer(false);
                claimTransitionService.saveClaimTransitionData(claimData.getId(),
                        insurerAdjudicationStatus.getAdjudicationStatus() + " - By " + AppConstants.NIVA, claimData.getTxnId());
                claimAdjudicationResultRepository.save(claimAdjudicationResult);
                claimDataRepository.save(claimData);
                if (updateDecisionRequest.getStatus().equalsIgnoreCase("DENY")) {
                    StringBuilder coverageFailedReasonsString = new StringBuilder(updateDecisionRequest.getRemarks());
                    claimCommonService.saveClaimRejectionReason(claimData, coverageFailedReasonsString);
                }
                log.info("Updated claim data decision to : {}, data: {}", updateDecisionRequest.getStatus(), claimData);
                return true;
            } else {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM);
            }
        } catch (Exception e) {
            log.info("Caught some exception while updating the decision", e);
            return false;
        }
    }

    public boolean pushClaimDecision(long claimId) {
        ClaimData claimData = claimDataRepository.findById(claimId).orElse(null);

        if (claimData == null) {
            log.error("Claim data not found for claim id: {}", claimId);
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        return claimsFactory.getClaimsFactory(ClaimDataParseMapping.DEFAULT_MAPPING).pushClaimDecision(claimData);
    }

    public PreAuthRequest pushClaimDecisionPreview(long claimId) {
        ClaimData claimData = claimDataRepository.findById(claimId).orElse(null);

        if (claimData == null) {
            log.error("Claim data not found for claim id: {}", claimId);
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        return claimsFactory.getClaimsFactory(ClaimDataParseMapping.DEFAULT_MAPPING).pushClaimDecisionPreview(claimData);
    }

    public List<ClaimData> checkAndUpdateClaimDecisions() {
        List<ClaimData> claimDataListUpdated = new ArrayList<>();
        try {
            List<ClaimData> claimDataList = claimCommonService.getClaimsOfPendingStateByTime(CLAIM_CHECK_TIME);
            if (claimDataList != null && !claimDataList.isEmpty()) {
                for (ClaimData claimData : claimDataList) {
                    if (ClaimStatus.isPreAuthStage(claimData.getClaimStatus())) {
                        Date thresholdDate = DateUtil.getPastOrFutureDateMinuteBased(claimData.getDateCreated(), pushToMaximusThreshold);
                        if (!new Date().after(thresholdDate)) {
                            continue;
                        }
                    }
                    ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationResultRepository.findByClaimDataId(claimData.getId());
                    ClaimModuleStats claimModuleStats = claimCommonService.getLatestClaimModuleStats(claimData.getId());
                    BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimData.getId());
                    VneuronResponse vNeuronResponse = vNeuronService.getVneuronResponseByClaimDataId(claimData.getId());
                    PMLResponse pmlResponse = pmlService.getPMLResponseByClaimDataId(claimData.getId());

                    claimAdjudicationResult.setClaimDecision(AdjudicationStatus.MANUAL.toString());
                    claimAdjudicationResult.setPreAuthDecision(AdjudicationStatus.MANUAL.toString());
                    claimAdjudicationResult.setPreAuthBillAmount(billTariffResponse != null ?
                            billTariffResponse.getClaimBillAmountRequested() : BigDecimal.ZERO);
                    claimAdjudicationResult.setPreAuthAmountApproved(BigDecimal.ZERO);
                    claimAdjudicationResult.setPreAuthSavings(BigDecimal.ZERO);
                    claimAdjudicationResult.setRemarks("Claim hit timeout");
                    claimAdjudicationResultRepository.save(claimAdjudicationResult);

                    claimData.setAdjudicationStatus(AdjudicationStatus.MANUAL.getAdjudicationStatus());
                    claimData.setStatus(EnhancementStatus.ENHANCEMENT_COMPLETED);
                    claimDataRepository.save(claimData);
                    claimTransitionService.saveClaimTransitionData(claimData.getId(),
                            claimData.getAdjudicationStatus() + " - By Vitraya", claimData.getTxnId());

//                    log.info("Save MANUAL Data in error message logs in checkAndUpdateClaimDecisions for claim data id: {}", claimData.getId());
//                    if (claimData.getAdjudicationStatus() != null
//                            && claimData.getAdjudicationStatus().equalsIgnoreCase("MANUAL")) {
//                        ErrorMsgType error = ErrorMsgType.MANUAL;
//                        errorMessageLogsService.saveErrorMessages(claimData, error);
//                        log.info("Saving Error Message Logs for MANUAL in checkAndUpdateClaimDecisions method for {}", claimData.getIntimationNumber());
//
//                    }
                    ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                            .claimId(claimData.getId())
                            .claimRunIdentifier(ClaimRunIdentifier.CRON_JOB)
                            .claimModuleStats(claimModuleStats)
                            .build();
                    if (billTariffResponse == null) {
                        String message = "[CRON]: Invalid response received from bill tariff service";
                        BillTariffResponseDTO billTariffResponseDTO = billTariffService.getBillTariffResponseDTO(claimData, message);
                        billTariffService.processBillTariffResponse(claimRunDTO, claimData, billTariffResponseDTO, System.currentTimeMillis(), null);
                    }
                    if (pmlResponse == null) {
                        PMLResponseDTO pmlResponseDTO = new PMLResponseDTO();
                        pmlResponseDTO.setMessage("[CRON]: Invalid response received from PML service");
                        pmlService.processPmlResponse(claimData, claimRunDTO, pmlResponseDTO, System.currentTimeMillis(), System.currentTimeMillis());
                    }
                    if (vNeuronResponse == null) {
                        String result = "[CRON]:Medical admissibility failed to run";
                        String id = "Medical Admissibility Failed";
                        VNeuronResponseDTO vNeuronResponseDTO = vNeuronService.getvNeuronResponseDTO(result, id);
                        vNeuronService.processVNeuronResponse(claimData, claimRunDTO, vNeuronResponseDTO, System.currentTimeMillis(), System.currentTimeMillis(), false);
                    }

                    ClaimData claimDataUpdated = claimCommonService.checkClaimModuleCompletion(claimData, true);
                    claimDataListUpdated.add(claimDataUpdated);
                    checkClaimAutoSubmissionCondition(claimData.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error while updating claim data", e);
        }
        return claimDataListUpdated;
    }

    public void checkClaimAutoSubmissionCondition(long claimDataId) {
        log.info("--------------inside checkClaimAutoSubmissionCondition for claimDataId------------------- {}", claimDataId);
        ClaimData claimData = getClaimData(claimDataId);
        if (claimData != null
                && ClaimStatus.isPreAuthStage(claimData.getClaimStatus())
                && claimData.getInsurerIdentifier() == null
                && claimData.getStatus() == EnhancementStatus.ENHANCEMENT_COMPLETED) {
            pushClaimDecision(claimData.getId());
        }
    }

    public List<ClaimData> getPendingClaimDataList() {
        return claimDataRepository.findAllPendingPreAuthClaimData();
    }

    public List<ClaimData> getExtensionPendingClaimDataList() {
        return claimDataRepository.findAllPendingExtensionClaimData();
    }

    public void saveClaimData(ClaimData claimData) {
        claimDataRepository.save(claimData);
    }

    public List<TariffLineItemResponseDTO> addUpdateTariffLineItems(TariffLineItemUpdateRequest tariffLineItemUpdateRequest) throws IOException {
        boolean runTariff = false;
        if (tariffLineItemUpdateRequest == null) {
            log.error("Tariff line item update request is null");
            throw new VitrayaException(VitrayaErrorCodes.INVALID_TARIFF_LINE_ITEM_ADD_UPDATE_REQUEST);
        }

        // Due to business requirement change rthe below condition is commented out
//        if (!billTariffService.validateTariffLineItemUpdateRequest(tariffLineItemUpdateRequest)) {
//            log.error("Tariff line item update request is null");
//            throw new VitrayaException(VitrayaErrorCodes.INVALID_TARIFF_LINE_ITEM_ADD_UPDATE_REQUEST);
//        }

        ClaimData claimData = claimDataRepository.findById(tariffLineItemUpdateRequest.getClaimDataId())
                .orElse(null);
        if (claimData == null) {
            log.error("Claim data not found for claim data id: {}", tariffLineItemUpdateRequest.getClaimDataId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        BillTariffResponse billTariffResponse = billTariffService.getBillTariffResponseByClaimDataId(claimData.getId());
        if (billTariffResponse == null) {
            log.error("No bill tariff response found for claim data id: {}", claimData.getId());
            throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
        }

        BillTariffResponseDTO billTariffResponseDTO = billTariffResponse.getBillTariffResponseDTO();
        if (billTariffResponseDTO == null) {
            log.error("No bill tariff response DTO found for claim data id: {}", claimData.getId());
            throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
        }

        BillTariffResponseData tariffData = billTariffResponseDTO.getData();
        if (tariffData == null) {
            log.error("No bill tariff response data found for claim data id: {}", claimData.getId());
            throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
        }

        if (tariffLineItemUpdateRequest.getNewLineItems() != null && !tariffLineItemUpdateRequest.getNewLineItems().isEmpty()) {
            log.info("Adding new line items for claim data id: {}", claimData.getId());
            addLineItemInTariffData(tariffLineItemUpdateRequest.getNewLineItems(), tariffData);
            runTariff = true;
        }

        if (tariffLineItemUpdateRequest.getEditedLineItems() != null && !tariffLineItemUpdateRequest.getEditedLineItems().isEmpty()) {
            log.info("Updating edited line items for claim data id: {}", claimData.getId());
            boolean amountUpdated = updateLineItemInTariffData(tariffLineItemUpdateRequest.getEditedLineItems(), tariffData);
            if (!amountUpdated) {
                log.info("amount change in edited line items for claim data id: {}", claimData.getId());
                runTariff = true;
            }
        }

        if (tariffLineItemUpdateRequest.getDeletedLineItems() != null && !tariffLineItemUpdateRequest.getDeletedLineItems().isEmpty()) {
            log.info("Deleting line items for claim data id: {}", claimData.getId());
            deleteLineItemInTariffData(tariffLineItemUpdateRequest.getDeletedLineItems(), tariffData);
        }

        updateBillTariffAmountComponents(tariffData);
        billTariffResponseDTO.setData(tariffData);
        billTariffResponse.setBillTariffResponseDTO(billTariffResponseDTO);
        long tat = Long.parseLong(billTariffResponse.getBillTariffTat().split(" ")[0].trim());
        BillTariffResponse billTariffResponseUpdated = billTariffService.saveBillTariffResponse(claimData, billTariffResponseDTO,
                tat, billTariffResponse, true);

        EnhancementConfig enhancementConfig = enhancementConfigRepository.findByHospitalIdAndInsuranceAgencyId(
                claimData.getHospitalId(), claimData.getInsuranceAgencyId());
        // only run pml if amount is changed
        if (!ClaimStatus.isPreAuthStage(claimData.getClaimStatus())
                && billTariffResponseUpdated.getBillTariffResponseDTO().getResponse_code().equalsIgnoreCase("TARIFF_APPLIED")
                && runTariff) {
            log.info("Rerunning tariff and PML for claim data id: {}", claimData.getId());
            ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                    .claimId(claimData.getId())
                    .claimRunIdentifier(ClaimRunIdentifier.RERUN_TARIFF_PML)
                    .claimModuleStats(billTariffService.getClaimModuleStats(claimData.getId()))
                    .build();
            addClaimTariffAndPMLQueue(claimRunDTO, enhancementConfig, claimData);
        } else {
            log.info("Rerunning PML for claim data id: {}", claimData.getId());
            ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                    .claimId(claimData.getId())
                    .claimRunIdentifier(ClaimRunIdentifier.RERUN_PML)
                    .claimModuleStats(billTariffService.getClaimModuleStats(claimData.getId()))
                    .build();
            checkAndProcessPML(enhancementConfig, claimRunDTO, claimData, billTariffResponseUpdated);
        }

        markClaimNotPushedToInsurer(claimData.getId());
        return billTariffService.getTariffData(claimData, billTariffResponseUpdated);
    }

    private void addClaimTariffAndPMLQueue(ClaimRunDTO claimRunDTO, EnhancementConfig enhancementConfig, ClaimData claimData) throws IOException {
        kafkaProducerService.sendMessage(AppConstants.TARIFF_PML_TOPIC_NAME, claimRunDTO.getClaimId(), GsonUtils.toJson(claimRunDTO));
    }


    private void addLineItemInTariffData(List<TariffLineItemResponseDTO> tariffLineItemResponseDTOS, BillTariffResponseData tariffData) {
        for (TariffLineItemResponseDTO lineItem : tariffLineItemResponseDTOS) {
            log.info("Adding new line item {} for row id: {}", lineItem.getBill_line_item(), lineItem.getRow_id());
            LineItemData data = new LineItemData();
            Tariff tariff = getTariffObj(null, lineItem, true, false, false);
            getUpdatedDataObjForNewItem(lineItem, data, tariff);

            LineItemsItem item = new LineItemsItem(data);
            List<LineItemsItem> line_items = tariffData.getLine_items() != null ? tariffData.getLine_items()
                    : new ArrayList<>();
            //line_items.add(item);
            addOrReplaceLineItem(line_items, item);
            tariffData.setLine_items(line_items);
        }
    }

    public void addOrReplaceLineItem(List<LineItemsItem> line_items, LineItemsItem item) {
        for (int i = 0; i < line_items.size(); i++) {
            if (line_items.get(i).getData().getTariff().getRow_id() == item.getData().getTariff().getRow_id()) {
                line_items.set(i, item);  // Replace if empId matches
                return;
            }
        }
        line_items.add(item);  // Add if no match found
    }

    private static void getUpdatedDataObjForNewItem(TariffLineItemResponseDTO lineItem, LineItemData data, Tariff tariff) {
        data.setRate(new ValueConfidenceDTO(String.valueOf(lineItem.getInsurer_unit_amount() != null
                ? lineItem.getInsurer_unit_amount() : lineItem.getUnit_amount()), 100));
        data.setAmount(new ValueConfidenceDTO(String.valueOf(lineItem.getInsurer_bill_amount() != null
                ? lineItem.getInsurer_bill_amount() : lineItem.getTotal_bill_amount()), 100));
        data.setRow_id(lineItem.getRow_id());
        data.setTariff(tariff);
        data.setQuantity(new ValueConfidenceDTO(String.valueOf(lineItem.getInsurer_unit() > 0
                ? lineItem.getInsurer_unit() : lineItem.getUnit()), 100));
        data.setDescription(new ValueConfidenceDTO(lineItem.getBill_line_item(), 100));
        data.setVitraya_master_category(new VitrayaMasterCategory(lineItem.getMaster_category(), 100, lineItem.getMaster_category()));
    }

    private static void getUpdatedDataObj(TariffLineItemResponseDTO lineItem, LineItemData data, Tariff tariff) {
        tariff.setInsurer_bill_amount(lineItem.getInsurer_bill_amount());
        tariff.setInsurer_unit_amount(lineItem.getInsurer_unit_amount());
        tariff.setInsurer_remarks(lineItem.getInsurer_remarks());
        tariff.setInsurer_tariff_rate(lineItem.getInsurer_tariff_rate());
        tariff.setInsurer_tariff_amount(lineItem.getInsurer_tariff_amount());
        tariff.setInsurer_unit(lineItem.getInsurer_unit());
        tariff.setInsurer_irdai_payable(lineItem.isInsurer_irdai_payable());
        data.setTariff(tariff);
        data.setVitraya_master_category(new VitrayaMasterCategory(lineItem.getMaster_category(), 100, lineItem.getMaster_category()));
        getUpdatedDataObjForNewItem(lineItem, data, tariff);
    }

    private static Tariff getTariffObj(Tariff tariff, TariffLineItemResponseDTO lineItem, boolean isAdded,
                                       boolean isEdited, boolean isDeleted) {
        // ToDo: later we need to do the calculations here based on the request data we have received.
        if (lineItem.getInsurer_tariff_amount() != null && lineItem.getInsurer_bill_amount() != null) {
            if (lineItem.getInsurer_tariff_amount().compareTo(lineItem.getInsurer_bill_amount()) > 0
                    && lineItem.getInsurer_tariff_amount().compareTo(BigDecimal.ZERO) > 0) {
                throw new VitrayaException(VitrayaErrorCodes.INVALID_TARIFF_LINE_ITEM_ADD_UPDATE_REQUEST);
            }
        }
        Tariff tariffInfo = tariff != null ? tariff : new Tariff();
        tariffInfo.setRow_id(lineItem.getRow_id());
        tariffInfo.setAllowed_units_by_proc_construct(BigDecimal.valueOf(lineItem.getAllowed_units_by_proc_construct()));
        tariffInfo.setTariff_per_unit_amount(lineItem.getTariff_rate());
        tariffInfo.setCost_depends_on_room_type(lineItem.isCost_depends_on_room_type());
        tariffInfo.setTariff_amount(lineItem.getTariff_amount_actual());
        tariffInfo.setTariff_line_item(lineItem.getTariff_application_reference_text());
        tariffInfo.setIrdai_payable(lineItem.isIrdai_payable());
        tariffInfo.setProcedure_construct_payable(lineItem.getProcedure_construct_payable_text());
        tariffInfo.setProcedure_construct_payable_boolean(lineItem.isProcedure_construct_payable());
        tariffInfo.setAdmissible_amount_without_procedure_construct(lineItem.getAdmissible_amount_without_procedure_construct());
        tariffInfo.setRemarks(lineItem.getRemarks());
        tariffInfo.setAdmissible_amount(lineItem.getAdmissible_amount());
        tariffInfo.setInsurer_amount(lineItem.getInsurer_amount());
        tariffInfo.setInsurer_unit(lineItem.getInsurer_unit());
        tariffInfo.setAdded(isAdded);
        tariffInfo.setEdited(isEdited);
        tariffInfo.setDeleted(isDeleted);
        tariffInfo.setInsurer_irdai_payable(lineItem.isIrdai_payable());
        tariffInfo.setInsurer_procedure_construct_payable(lineItem.isInsurer_procedure_construct_payable());
        tariffInfo.setInsurer_unit_actual(lineItem.getInsurer_unit_actual());
        tariffInfo.setInsurer_unit_amount(lineItem.getInsurer_unit_amount());
        tariffInfo.setInsurer_bill_amount(lineItem.getInsurer_bill_amount());
        tariffInfo.setInsurer_tariff_rate(lineItem.getInsurer_tariff_rate());
        tariffInfo.setInsurer_tariff_amount(lineItem.getInsurer_tariff_amount());
        tariffInfo.setInsurer_savings(lineItem.getInsurer_savings());
        tariffInfo.setInsurer_remarks(lineItem.getInsurer_remarks());
        tariffInfo.setInsurer_amount_actual(lineItem.getInsurer_amount_actual());
        return tariffInfo;
    }

    private void addLineItemCategorySummary(BillTariffResponseData tariffData, LineItemsItem item) {
        /*List<CategorySummaryItem> category_summary = tariffData.getCategory_summary();
        category_summary.stream()
                .filter(categorySummaryItem -> categorySummaryItem.getCategory_name()
                        .equalsIgnoreCase(item.getData().getVitraya_master_category().getValue()))
                .findFirst()
                .ifPresent(categorySummaryItem -> {
                    if (categorySummaryItem.getLine_items() == null) {
                        categorySummaryItem.setLine_items(new ArrayList<>());
                    }
                    categorySummaryItem.getLine_items().add(item);
                });*/
        List<CategorySummaryItem> category_summary = tariffData.getCategory_summary();
        Optional<CategorySummaryItem> matchingCategory = category_summary.stream()
                .filter(categorySummaryItem -> categorySummaryItem.getCategory_name()
                        .equalsIgnoreCase(item.getData().getVitraya_master_category().getValue()))
                .findFirst();

        if (matchingCategory.isPresent()) {
            CategorySummaryItem categorySummaryItem = matchingCategory.get();
            if (categorySummaryItem.getLine_items() == null) {
                categorySummaryItem.setLine_items(new ArrayList<>());
            }
            categorySummaryItem.getLine_items().add(item);
        } else {
            CategorySummaryItem newCategorySummaryItem = new CategorySummaryItem();
            newCategorySummaryItem.setCategory_name(item.getData().getVitraya_master_category().getValue());
            newCategorySummaryItem.setLine_items(new ArrayList<>());
            newCategorySummaryItem.getLine_items().add(item);
            category_summary.add(newCategorySummaryItem);
        }
    }

    private boolean updateLineItemInTariffData(List<TariffLineItemResponseDTO> editedLineItems, BillTariffResponseData tariffData) {
        // We need to loop over the edited line items and update the tariff data. Where we found the element with the same rowId.
        int amountCounter = 0;
        for (TariffLineItemResponseDTO requestLineItem : editedLineItems) {
            int requestRowId = requestLineItem.getRow_id();
            List<LineItemsItem> lineItemsListExisting = tariffData.getLine_items();
            for (LineItemsItem lineItemExisting : lineItemsListExisting) {
                if (lineItemExisting.getData().getTariff().getRow_id() == requestRowId) {
                    if (requestLineItem != null && requestLineItem.getInsurer_bill_amount() != null
                            && (!requestLineItem.getInsurer_bill_amount().equals(lineItemExisting.getData().getTariff().getInsurer_bill_amount())
                            || !requestLineItem.getTariff_amount().equals(lineItemExisting.getData().getTariff().getTariff_amount()))) {
                        log.info("amount updated for line item row id: {}", requestRowId);
                        amountCounter++;
                    }
                    log.info("Updating line item {} for row id: {}",
                            lineItemExisting.getData().getDescription().getValue(), requestRowId);
                    LineItemData lineItemData = lineItemExisting.getData();
                    Tariff tariff = getTariffObj(lineItemExisting.getData().getTariff(), requestLineItem,
                            false, true, false);
                    getUpdatedDataObj(requestLineItem, lineItemData, tariff);
                }
            }
        }
        return amountCounter > 0;
    }

    private void updateCategorySummary(BillTariffResponseData tariffData, LineItemsItem lineItemUpdated,
                                       String originalMasterCategory, String newMasterCategory) {
        List<CategorySummaryItem> categorySummaryItemList = tariffData.getCategory_summary();
        for (CategorySummaryItem categorySummaryItem : categorySummaryItemList) {
            if (originalMasterCategory.equalsIgnoreCase(newMasterCategory)) {
                // If the original master category and the new master category are same then update the line item in the category summary line items.
                if (categorySummaryItem.getCategory_name().equalsIgnoreCase(originalMasterCategory)) {
                    List<LineItemsItem> lineItemsItemExistingList = categorySummaryItem.getLine_items();
                    lineItemsItemExistingList.replaceAll(
                            item -> item.getData().getTariff().getRow_id() == lineItemUpdated.getData().getTariff().getRow_id()
                                    ? lineItemUpdated : item);
                }
            } else {
                // If we found the master category then add the line item in the category summary line items.
                if (categorySummaryItem.getCategory_name().equalsIgnoreCase(newMasterCategory)) {
                    // categorySummaryItem.setCategory_name(newMasterCategory);
                    List<LineItemsItem> lineItemsItemExistingList = categorySummaryItem.getLine_items();
                    lineItemsItemExistingList.add(lineItemUpdated);
                    categorySummaryItem.setLine_items(lineItemsItemExistingList);
                }

                // Check for the original master category and remove the line item from the category summary line items.
                if (categorySummaryItem.getCategory_name().equalsIgnoreCase(originalMasterCategory)) {
                    List<LineItemsItem> lineItemsItemExistingList = categorySummaryItem.getLine_items();
                    lineItemsItemExistingList.remove(lineItemUpdated);
                    categorySummaryItem.setLine_items(lineItemsItemExistingList);
                }
            }
        }
    }

    private void deleteLineItemInTariffData(List<TariffLineItemResponseDTO> deletedLineItems, BillTariffResponseData tariffData) {
        for (TariffLineItemResponseDTO lineItemDeleted : deletedLineItems) {
            int rowId = lineItemDeleted.getRow_id();
            for (LineItemsItem lineItemsItemExisting : tariffData.getLine_items()) {
                if (lineItemsItemExisting.getData() != null && lineItemsItemExisting.getData().getTariff() != null
                        && rowId == lineItemsItemExisting.getData().getTariff().getRow_id()
                        && !lineItemsItemExisting.getData().getTariff().isDeleted()) {
                    lineItemsItemExisting.getData().getTariff().setDeleted(true);
                    break;
                }
            }
        }
    }

    private void updateBillTariffAmountComponents(BillTariffResponseData tariffData) {
        BigDecimal totalBillAmount = BigDecimal.ZERO;
        BigDecimal totalSumOfLineItems = BigDecimal.ZERO;
        BigDecimal totalAdmissibleAmount = BigDecimal.ZERO;

        // Now we have to regenerate the category summary object.
        HashMap<String, CategorySummaryItem> categorySummaryItem = new HashMap<>();
        tariffData.setCategory_summary(null);
        for (LineItemsItem lineItemsItem : tariffData.getLine_items()) {
            LineItemData lineItemData = lineItemsItem.getData();
            if (lineItemData != null && lineItemData.getTariff() != null) {
                Tariff tariff = lineItemData.getTariff();
                if (!tariff.isDeleted()) {

                    totalBillAmount = addValue(totalBillAmount, new BigDecimal(lineItemData.getAmount().getValue()));
                    BigDecimal admissibleAmountCalulated = getLineItemAdmissibleAmt(lineItemData, tariff);
                    totalAdmissibleAmount = addValue(totalAdmissibleAmount, admissibleAmountCalulated);
                    totalSumOfLineItems = totalBillAmount;

                    /*
                     Okay now check if the master category exist if yes then fetch the line_items value and add it
                     else create new and add the line item. Along this we will update the other amount related fields as well.
                     */
                    if (categorySummaryItem.containsKey(lineItemData.getVitraya_master_category().getValue())) {
                        CategorySummaryItem categorySummaryItemObj = categorySummaryItem.get(lineItemData.getVitraya_master_category().getValue());
                        List<LineItemsItem> lineItemsItemList = categorySummaryItemObj.getLine_items();
                        lineItemsItemList.add(lineItemsItem);
                        BigDecimal deduction = categorySummaryItemObj.getDeductions().add(getLineItemDeduction(lineItemData));
                        BigDecimal requestedAmount = categorySummaryItemObj.getRequested_amount().add(getLineItemRequestedAmt(lineItemData));
                        BigDecimal admissibleAmount = categorySummaryItemObj.getAdmissible_amount().add(admissibleAmountCalulated);
                        categorySummaryItemObj.setRequested_amount(requestedAmount);
                        categorySummaryItemObj.setAdmissible_amount(admissibleAmount);
                        categorySummaryItemObj.setAmount_for_irdai_payable(admissibleAmount);
                        categorySummaryItemObj.setAmount_after_procedure_construct(admissibleAmount);
                        categorySummaryItemObj.setAdmissible_amount_without_procedure_construct(admissibleAmount);
                        categorySummaryItemObj.setLine_items(lineItemsItemList);
                        categorySummaryItemObj.setDeductions(deduction);
                        // Update the category summary amount items as well.
                    } else {
                        CategorySummaryItem categorySummaryItemObj = new CategorySummaryItem();
                        categorySummaryItemObj.setCategory_name(lineItemData.getVitraya_master_category().getValue());
                        List<LineItemsItem> lineItemsItemList = new ArrayList<>();
                        lineItemsItemList.add(lineItemsItem);
                        categorySummaryItemObj.setLine_items(lineItemsItemList);
                        categorySummaryItemObj.setDeductions(getLineItemDeduction(lineItemData));
                        categorySummaryItemObj.setCategory_name(lineItemData.getVitraya_master_category().getValue());
                        categorySummaryItemObj.setRequested_amount(getLineItemRequestedAmt(lineItemData));
                        categorySummaryItemObj.setAdmissible_amount(getLineItemAdmissibleAmt(lineItemData, tariff));
                        categorySummaryItemObj.setAmount_for_irdai_payable(categorySummaryItemObj.getAdmissible_amount());
                        categorySummaryItemObj.setAmount_after_procedure_construct(categorySummaryItemObj.getAdmissible_amount());
                        categorySummaryItemObj.setAdmissible_amount_without_procedure_construct(categorySummaryItemObj.getAdmissible_amount());
                        categorySummaryItem.put(lineItemData.getVitraya_master_category().getValue(), categorySummaryItemObj);
                    }
                }
            }
        }

        // Now update the values in the tariff data.
        tariffData.getBill_amounts().setTotal_bill_amount(totalBillAmount);
        tariffData.getBill_amounts().setTotal_sum_of_line_items(totalSumOfLineItems);
        tariffData.getBill_amounts().setDifference_in_amount(totalBillAmount.subtract(totalSumOfLineItems));
        tariffData.getTariff_amounts().setTotal_admissible_amount(totalAdmissibleAmount);

        tariffData.setCategory_summary(new ArrayList<>(categorySummaryItem.values()));
    }

    private static BigDecimal getLineItemAdmissibleAmt(LineItemData lineItemData, Tariff tariff) {
        return lineItemData.getTariff().getInsurer_tariff_amount() != null
                ? lineItemData.getTariff().getInsurer_tariff_amount() : tariff.getAdmissible_amount();
    }

    private static BigDecimal getLineItemRequestedAmt(LineItemData lineItemData) {
        return lineItemData.getTariff().getInsurer_bill_amount() != null
                ? lineItemData.getTariff().getInsurer_bill_amount() : new BigDecimal(lineItemData.getAmount().getValue());
    }

    private BigDecimal getLineItemDeduction(LineItemData lineItemData) {
        return lineItemData.getTariff().getInsurer_savings() != null
                ? lineItemData.getTariff().getInsurer_savings() : getBillSavings(lineItemData);
    }

    private BigDecimal getBillSavings(LineItemData lineItemData) {
        return new BigDecimal(lineItemData.getAmount().getValue()).subtract(lineItemData.getTariff().getAdmissible_amount());
    }

    public void processClaimTariffPML(String message) throws IOException {
        ClaimRunDTO claimRunDTO = GsonUtils.fromJson(message, ClaimRunDTO.class);
        ClaimData claimData = claimDataRepository.findById(claimRunDTO.getClaimId()).orElse(null);
        EnhancementConfig enhancementConfig = enhancementConfigRepository
                .findByHospitalIdAndInsuranceAgencyId(claimData.getHospitalId(), claimData.getInsuranceAgencyId());

        BillTariffResponse billTariffResponseUpdated = billTariffService.processTariffRequest(claimRunDTO);
        checkAndProcessPML(enhancementConfig, claimRunDTO, claimData, billTariffResponseUpdated);
    }

    public ClaimData updateClaimData(UpdateClaimRequest updateClaimRequest) throws ParseException {

        ClaimData claimData = claimDataRepository.findById(Long.valueOf(updateClaimRequest.getClaimId())).orElse(null);
        if (claimData == null) {
            log.error("Claim data not found for claim data id: {}", updateClaimRequest.getClaimId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            if (updateClaimRequest.getIcdCode() != null
                    && !updateClaimRequest.getIcdCode().equalsIgnoreCase("")) {
                claimData.setIcdCode(updateClaimRequest.getIcdCode());
            }
            if (updateClaimRequest.getRoomType() != null
                    && !updateClaimRequest.getRoomType().equalsIgnoreCase("")) {
                claimData.setRoomType(updateClaimRequest.getRoomType());
            }
            if (updateClaimRequest.getProcedure() != null
                    && !updateClaimRequest.getProcedure().equalsIgnoreCase("")) {
                claimData.setProcedureId(Long.parseLong(updateClaimRequest.getProcedure()));
            }
            if (updateClaimRequest.getDateOfAdmission() != null
                    && !updateClaimRequest.getDateOfAdmission().equalsIgnoreCase("")) {
                Date dateOfAdmission = formatter.parse(updateClaimRequest.getDateOfAdmission());
                claimData.setDateOfAdmission(dateOfAdmission);
            }
            if (updateClaimRequest.getDateOfDischarge() != null
                    && !updateClaimRequest.getDateOfDischarge().equalsIgnoreCase("")) {
                Date dateOfDischarge = formatter.parse(updateClaimRequest.getDateOfDischarge());
                claimData.setDateOfDischarge(dateOfDischarge);
            }
            claimData.setPushedToInsurer(false);
            claimDataRepository.save(claimData);
            return claimData;
        } catch (ParseException e) {
            log.error("Error while parsing date in updateClaimData", e);
        }
        return null;
    }

    private List<RiderDetails> getRiderDetails(BillTariffResponse billTariffResponse) {
        return billTariffResponse != null
                && billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getRiders_details() != null
                ? billTariffResponse.getBillTariffResponseDTO().getRiders_details() : null;
    }
}
