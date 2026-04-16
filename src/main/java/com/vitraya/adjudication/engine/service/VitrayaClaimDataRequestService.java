package com.vitraya.adjudication.engine.service;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.PrepareEpisodeDetailsDataDTO;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.ClaimIllnessTreatmentDetailsDTO;
import com.vitraya.adjudication.engine.dto.request.CostEstimateDTO;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.entity.PMLResponse;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrClaims;
import com.vitraya.adjudication.engine.mysql.repository.*;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.EpisodeDetails;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.NivaPolicyDataDTO;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.PreAuthRequest;
import com.vitraya.adjudication.engine.rulesengine.niva.dto.SettlementRequest;
import com.vitraya.adjudication.engine.service.factory.InsuranceAgencyCallBacks;
import com.vitraya.adjudication.engine.utils.AppConstants;
import com.vitraya.adjudication.engine.utils.DateUtil;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VitrayaClaimDataRequestService implements ClaimDataRequestService {
    @Value("${normal.maternity.procedure}")
    private List<Long> normalMaternityProcedure;

    @Value("${c.section.maternity.procedure}")
    private List<Long> cSectionMaternityProcedure;

    @Value("${test.environment}")
    private boolean TEST_ENVIRONMENT;

    @Value("${claim.received.email.to}")
    private String CLAIM_RECEIVED_EMAIL_TO;

    @Value("${day.care.procedure}")
    private String dayCareProcedureStr;

    @Value("${dialysis.procedure}")
    private String dialysisProcedureStr;

    @Value("${radio.procedure}")
    private String radiotherapyProcedureStr;

    @Value("${chemo.procedure}")
    private String chemoProcedureStr;

    @Value("${maternity.procedure}")
    private String maternityProcedureIdsStr;

    @Value("${benefit.head}")
    private String benefitHeadStr;


    private Set<Long> dayCareProcedureIds;
    private Set<Long> dialysisProcedureIds;
    private Set<Long> radiotherapyProcedureIds;
    private Set<Long> chemoProcedureIds;
    private Set<Long> maternityProcedureIds;


    private final HospitalServiceTypeService hospitalServiceTypeService;
    private final ClaimTransitionService claimTransitionService;
    private final ClaimAdmissionService claimAdmissionService;
    private final NivaUcrClaimsRepo nivaUcrClaimsRepo;
    private final ErrorMessageLogsService errorMessageLogsService;
    private final ClaimDataRepository claimDataRepository;
    private final A2SCallbackService a2SCallbackService;
    private final BillTariffResponseRepository billTariffResponseRepository;
    public final ProcedureService procedureService;
    public final IllnessService illnessService;
    private final CorporateService corporateService;
    private final PolicyProductService policyProductService;
    private final S3FileService s3FileService;
    private final DocumentService documentService;
    private final NivaRequestDataRepository nivaRequestDataRepository;
    private final InsuranceAgencyCallBacks insuranceAgencyCallBacks;
    private final InsurerFetchResponsesRepository insurerFetchResponsesRepository;
    private final ClaimAdjudicationRepository claimAdjudicationRepository;
    private final DayCareProceduresMappingRepository dayCareProceduresMappingRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PMLResponseRepository pmlResponseRepository;
    private final PMLService pmlService;
    private final CommunicationService communicationService;
    private final ClaimCommonService claimCommonService;
    private final UserService userService;
    private final NivaPushEventService nivaPushEventService;


    @PostConstruct
    public void init() {
        dayCareProcedureIds = parseToSet(dayCareProcedureStr);
        dialysisProcedureIds = parseToSet(dialysisProcedureStr);
        radiotherapyProcedureIds = parseToSet(radiotherapyProcedureStr);
        chemoProcedureIds = parseToSet(chemoProcedureStr);
        maternityProcedureIds = parseToSet(maternityProcedureIdsStr);
    }

    private Set<Long> parseToSet(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }


    public VitrayaClaimDataRequestService(ProcedureService procedureService,
                                          IllnessService illnessService, CorporateService corporateService,
                                          PolicyProductService policyProductService, S3FileService s3FileService,
                                          DocumentService documentService, NivaRequestDataRepository nivaRequestDataRepository,
                                          InsuranceAgencyCallBacks insuranceAgencyCallBacks, InsurerFetchResponsesRepository insurerFetchResponsesRepository,
                                          ClaimAdjudicationRepository claimAdjudicationRepository, DayCareProceduresMappingRepository dayCareProceduresMappingRepository,
                                          ServiceTypeRepository serviceTypeRepository, ClaimDataRepository claimDataRepository,
                                          A2SCallbackService a2SCallbackService, BillTariffResponseRepository billTariffResponseRepository,
                                          PMLResponseRepository pmlResponseRepository, PMLService pmlService, CommunicationService communicationService,
                                          ClaimCommonService claimCommonService, HospitalServiceTypeService hospitalServiceTypeService,
                                          ClaimTransitionService claimTransitionService, UserService userService, ClaimAdmissionService claimAdmissionService,
                                          NivaUcrClaimsRepo nivaUcrClaimsRepo, ErrorMessageLogsService errorMessageLogsService, NivaPushEventService nivaPushEventService) {
        this.procedureService = procedureService;
        this.illnessService = illnessService;
        this.corporateService = corporateService;
        this.policyProductService = policyProductService;
        this.s3FileService = s3FileService;
        this.documentService = documentService;
        this.nivaRequestDataRepository = nivaRequestDataRepository;
        this.insuranceAgencyCallBacks = insuranceAgencyCallBacks;
        this.insurerFetchResponsesRepository = insurerFetchResponsesRepository;
        this.claimAdjudicationRepository = claimAdjudicationRepository;
        this.dayCareProceduresMappingRepository = dayCareProceduresMappingRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.claimDataRepository = claimDataRepository;
        this.a2SCallbackService = a2SCallbackService;
        this.billTariffResponseRepository = billTariffResponseRepository;
        this.pmlResponseRepository = pmlResponseRepository;
        this.pmlService = pmlService;
        this.communicationService = communicationService;
        this.claimCommonService = claimCommonService;
        this.hospitalServiceTypeService = hospitalServiceTypeService;
        this.userService = userService;
        this.claimTransitionService = claimTransitionService;
        this.claimAdmissionService = claimAdmissionService;
        this.nivaUcrClaimsRepo = nivaUcrClaimsRepo;
        this.errorMessageLogsService = errorMessageLogsService;
        this.nivaPushEventService = nivaPushEventService;
    }

    @Override
    public VitrayaInsurerClaimDataDTO validateAndParseClaimRequest(Object claimObject, MultipartFile[] files) {
        VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData;
        try {
            vitrayaInsurerClaimData = GsonUtils.fromJson(GsonUtils.toJson(claimObject), VitrayaInsurerClaimDataDTO.class);
            log.info("parsed claimObject from vhi to vitrayaInsurerClaimData- {}", new Gson().toJson(vitrayaInsurerClaimData));
        } catch (Exception e) {
            log.error("Exception occurred while parsing the claim data", e);
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }

        if (vitrayaInsurerClaimData == null) {
            log.error("Received Claim data is null");
            throw new VitrayaException(VitrayaErrorCodes.EMPTY_CREATE_CLAIM_REQUEST_RECEIVED);
        }

        validateClaimData(vitrayaInsurerClaimData, files);
        checkAndMapData(vitrayaInsurerClaimData, files);
        return vitrayaInsurerClaimData;
    }

    @Override
    public boolean pushClaimDecision(ClaimData claimData) {
        if (claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RAISED) && claimData.getInsurerIdentifier() == null) {
            return processPreAuthRequest(claimData, true, false, false, false, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RESPONSE_RE_PUSHED)) {
            return processPreAuthRequest(claimData, true, false, false, false, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.INTERIM_RAISED)) {
            return processPreAuthRequest(claimData, false, true, false, false, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.INTERIM_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.INTERIM_RESPONSE_RE_PUSH)) {
            return processPreAuthRequest(claimData, false, true, false, false, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RAISED)) {
            return processPreAuthRequest(claimData, false, false, true, false, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RESPONSE_RE_PUSH)) {
            return processPreAuthRequest(claimData, false, false, true, false, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.QUERY_REPLY_RAISED)) {
            return processPreAuthRequest(claimData, false, false, false, true, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.QUERY_REPLY_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.QUERY_REPLY_RE_PUSHED)) {
            return processPreAuthRequest(claimData, false, false, false, true, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.SETTLEMENT_RAISED)) {
            return processPreAuthRequest(claimData, false, false, false, false, false, true, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.SETTLEMENT_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.SETTLEMENT_REPUSHED)) {
            return processPreAuthRequest(claimData, false, false, false, false, false, true, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DENIAL_RECONSIDERATION_RAISED)) {
            return processPreAuthRequest(claimData, false, false, false, false, true, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.DENIAL_RECONSIDERATION_REPUSHED)) {
            return processPreAuthRequest(claimData, false, false, false, false, true, false, true);
        }

        return false;
    }

    @Override
    public PreAuthRequest pushClaimDecisionPreview(ClaimData claimData) {
        if (claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RAISED) && claimData.getInsurerIdentifier() == null) {
            return processPreAuthRequestPreview(claimData, true, false, false, false, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.PRE_AUTHORISATION_RESPONSE_RE_PUSHED)) {
            return processPreAuthRequestPreview(claimData, true, false, false, false, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.INTERIM_RAISED)) {
            return processPreAuthRequestPreview(claimData, false, true, false, false, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.INTERIM_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.INTERIM_RESPONSE_RE_PUSH)) {
            return processPreAuthRequestPreview(claimData, false, true, false, false, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RAISED)) {
            return processPreAuthRequestPreview(claimData, false, false, true, false, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.DISCHARGE_RESPONSE_RE_PUSH)) {
            return processPreAuthRequestPreview(claimData, false, false, true, false, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.QUERY_REPLY_RAISED)) {
            return processPreAuthRequestPreview(claimData, false, false, false, true, false, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.QUERY_REPLY_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.QUERY_REPLY_RE_PUSHED)) {
            return processPreAuthRequestPreview(claimData, false, false, false, true, false, false, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.SETTLEMENT_RAISED)) {
            return processPreAuthRequestPreview(claimData, false, false, false, false, false, true, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.SETTLEMENT_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.SETTLEMENT_REPUSHED)) {
            return processPreAuthRequestPreview(claimData, false, false, false, false, false, true, true);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DENIAL_RECONSIDERATION_RAISED)) {
            return processPreAuthRequestPreview(claimData, false, false, false, false, true, false, false);
        } else if (claimData.getClaimStatus().equals(ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT) || claimData.getClaimStatus().equals(ClaimStatus.DENIAL_RECONSIDERATION_REPUSHED)) {
            return processPreAuthRequestPreview(claimData, false, false, false, false, true, false, true);
        }
        return null;
    }

    private boolean processPreAuthRequest(ClaimData claimData, boolean isPreAuth, boolean isInterim, boolean isDischarge, boolean isQueryReply, boolean isReconsideration, boolean isSettlement, boolean isRepush) {
        NivaPushEvent nivaPushEvent = nivaPushEventService.recordEvent(claimData.getIntimationNumber(), NivaPushStatus.PUSH_STARTED.toString(),
                "N/A", 0L);
        long startTime = System.currentTimeMillis();
        try {
            List<DocumentMaster> documentMasterList = documentService.getLatestTxnClaimDocuments(claimData.getIntimationNumber(), claimData.getTxnId());
            ArrayList<File> files = s3FileService.getClaimFiles(documentMasterList);
            BillTariffResponse billTariffResponse = billTariffResponseRepository.getBillTariffResponseByClaimDataIdOrderByIdDesc(claimData.getId());
            PMLResponse pmlResponse = pmlResponseRepository.findPMLResponseByClaimDataIdLatest(claimData.getId());

            //get latest nivarequest data
            NivaRequestData previousStageNivaRequestData = getLatestNivaRequestOfAClaim(claimData);
            boolean isWdmsApiSuccess;
            if (!TEST_ENVIRONMENT) {
                isWdmsApiSuccess = s3FileService.saveFilesToWdms(claimData, files, isPreAuth, isInterim, isDischarge,
                        isReconsideration, isQueryReply, isSettlement, isRepush, nivaPushEvent);
            } else {
                isWdmsApiSuccess = true;
            }

            log.info("claim -{} started preauth integration process", claimData.getId());
            log.info("claim -{}", claimData.getId());

            NivaRequestData nivaRequestData = new NivaRequestData();

            String preAuthId = "";
            String activityType = "";
            String nivaRequestType = "";
            if (isPreAuth) {
                activityType = "New Pre-Auth";
                nivaRequestType = NivaRequestType.PRE_AUTH.toString();
            } else if (isInterim) {
                activityType = "Extension Pre-Auth";
                preAuthId = claimData.getInsurerIdentifier();
                nivaRequestType = NivaRequestType.INTERIM_ENHANCEMENT.toString();
            } else if (isQueryReply) {
                activityType = "Pending Pre-Auth";
                preAuthId = claimData.getInsurerIdentifier();
                nivaRequestType = NivaRequestType.QUERY.toString();
            } else if (isReconsideration) {
                activityType = "Extension Pre-Auth";
                preAuthId = claimData.getInsurerIdentifier();
                nivaRequestType = NivaRequestType.RECONSIDER.toString();
            } else if (isSettlement) {
                activityType = "Provider Documents";
                preAuthId = claimData.getInsurerIdentifier();
                nivaRequestType = NivaRequestType.SETTLEMENT.toString();
            } else if (isDischarge) {
                activityType = "Discharge Pre-Auth";
                preAuthId = claimData.getInsurerIdentifier();
                nivaRequestType = NivaRequestType.DISCHARGE.toString();
            }

            nivaRequestData = nivaRequestDataRepository.findByClaimIdAndRequestType(claimData.getId(), nivaRequestType);
            if (nivaRequestData == null) {
                log.info("nivaRequestData is null for {}", claimData.getId());
            }
            if (!TEST_ENVIRONMENT) {
                if (nivaRequestData != null) {
                    isWdmsApiSuccess = insuranceAgencyCallBacks.sendWdmsSoapRequest(String.valueOf(nivaRequestData.getWdmsUniqueNo()),
                            activityType, preAuthId, claimData, nivaPushEvent);
                }
            } else {
                isWdmsApiSuccess = true;
                if (nivaRequestData == null) {
                    nivaRequestData = new NivaRequestData();
                    nivaRequestData.setWdmsUniqueNo("ABCD-111");
                }
            }

            InsurerFetchResponses insurerFetchResponses = insurerFetchResponsesRepository.getInsurerFetchResponsesByClaimDataId(claimData.getId());

            if (isWdmsApiSuccess && nivaRequestData != null
                    && insurerFetchResponses != null
                    && insurerFetchResponses.getPolicyData() != null &&
                    !insurerFetchResponses.getPolicyData().isEmpty()) {
                log.info("claim -{} wdms api success", claimData.getId());
                NivaPolicyDataDTO nivaPolicyDataDTO = new Gson().fromJson(insurerFetchResponses.getPolicyData(), NivaPolicyDataDTO.class);
                log.info("claim -{} policy number {}", claimData.getId(), nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getPolicy_Number());

                ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationRepository.findByClaimDataId(claimData.getId());
                boolean isRejected = isClaimRejected(isPreAuth, isDischarge, claimAdjudicationResult);

                HospitalServiceType hospitalServiceType = hospitalServiceTypeService.getHospitalRoomType(claimData);
                ServiceType serviceType = null;

                String benefitType = "A";
                String masterCategoryName = "surgical package";
                String roomType = "";

                if (isNormalMaternityProcedure(claimData.getProcedureId())) {
                    benefitType = "F";
                } else if (isCSenctionMaternityProcedure(claimData.getProcedureId())) {
                    benefitType = "N";
                } else if (isDayCare(claimData.getProcedureId())) {
                    benefitType = "C";
                    roomType = "";
                }

                roomType = hospitalServiceType != null && hospitalServiceType.getInsurerRoomCategoryMapping() != null
                        ? hospitalServiceType.getInsurerRoomCategoryMapping() : "A";

                List<EpisodeDetails> episodeDetailsList;
                log.info("claim -{} its a preauth flow creating full episode details", claimData.getId());
                //prashant
//                log.info("Received bill response is {} and pml response is {}", billTariffResponse, pmlResponse);
                BillTariffPmlDto billTariffPmlDto = pmlService.getPMLDataMergedV1(pmlResponse, billTariffResponse);
                log.info("PML Data merged BillTariffPmlDto {} for claimId {}", new Gson().toJson(billTariffPmlDto), claimData.getId());


                HashMap<String, BigDecimal> deductibleMap = pmlService.getDeductibleMap(pmlResponse);
                BigDecimal hospitalPayableDeductible = deductibleMap.get("hospitalPayableDeductible");
                BigDecimal hospitalAmount = hospitalPayableDeductible != null ? hospitalPayableDeductible : BigDecimal.ZERO;
                BigDecimal tariffDeduction = billTariffPmlDto != null ? billTariffPmlDto.getNmeTariffDeductionAmount() : BigDecimal.ZERO;
                tariffDeduction = tariffDeduction != null ? tariffDeduction : BigDecimal.ZERO;
                BigDecimal tariffDifferential = hospitalAmount.add(tariffDeduction);
                episodeDetailsList = getMasterCategoryMapping(false, billTariffPmlDto, claimData.getHospitalId(),
                        nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getBed_Type(),
                        benefitType, claimAdjudicationResult, claimData.getDateOfAdmission(),
                        claimData.getDateOfDischarge(), claimData, isRejected, isPreAuth, isDischarge);

                claimData.setPolicyNumber(nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getPolicy_Number());
                Corporate hospital = corporateService.getUserCorporate((int) claimData.getHospitalId());
                Illnesses illnesses = illnessService.getFirstIllnessByICDCode(claimData.getIcdCode());
                PreAuthRequest preAuthRequest = new PreAuthRequest();
                SettlementRequest settlementRequest = new SettlementRequest();
                NivaResponse nivaResponse = null;
                NivaSettlementResponse nivaSettlementResponse = null;
                boolean isLastStageDischarge = false;
                if (isSettlement) {
                    settlementRequest = SettlementRequest.buildSettlementRequest(claimData, hospital, nivaRequestData.getWdmsUniqueNo(), claimAdjudicationResult);
                    nivaRequestData.setRequestData(new Gson().toJson(settlementRequest));
                    log.info("Niva Settlement Request Format: " + new Gson().toJson(settlementRequest));
                    long settlementStartTime = System.currentTimeMillis();
                    nivaSettlementResponse = insuranceAgencyCallBacks.sendSettlementRequestToNiva(settlementRequest, claimData, nivaPushEvent, settlementStartTime);
                } else {
                    if (billTariffPmlDto != null && billTariffPmlDto.getTotal_bill_amount() == null) {
                        ClaimAdmissionDetails claimAdmissionDetails = claimAdmissionService.getClaimAdmissionDetails(claimData.getId());
                        if (claimAdmissionDetails != null && claimAdmissionDetails.getCostEstimation() != null) {
                            CostEstimateDTO costEstimateDTO = GsonUtils.fromJson(claimAdmissionDetails.getCostEstimation(), CostEstimateDTO.class);
                            /*
                             Add the value so that the no change is required in the internal logic of the data preparation.
                             As the field belongs to the amount requested. As asked in case of the requested is null or 0.
                             We need to pass the pre auth requested amount as the total bill amount.
                             */

                            billTariffPmlDto.setTotal_bill_amount(costEstimateDTO.getTotalCost());
                        }
                    }
                    preAuthRequest = PreAuthRequest.buildPreAuthRequest(nivaRequestData.getWdmsUniqueNo(), claimData,
                            billTariffPmlDto, claimAdjudicationResult, episodeDetailsList, serviceType, roomType,
                            isPreAuth, nivaPolicyDataDTO, hospital, illnesses, previousStageNivaRequestData, isPreAuth,
                            isInterim, isDischarge, isQueryReply, isReconsideration, isSettlement, isRepush);

                    // isQueryReply || isReconsideration

                    if (isPreAuth || isInterim) {
                        /*
                         In case of preauth we need to group the episode details into a single row and set the approved amount
                         if insurer updated the same.
                         */
                        processEpisodeDetailsForPreAuth(claimAdjudicationResult, preAuthRequest, claimData.getProcedureId());
                    } else if (isQueryReply || isReconsideration) {
                        // ToDo: We need to implement the logic for grouping episode details in case of query reply or reconsideration

                        List<ClaimTransition> claimTransitionList = claimTransitionService.findAllClaimTransitionByClaimDataIdLatest(claimData.getId());
                        if (claimTransitionList != null && !claimTransitionList.isEmpty()) {
                            for (ClaimTransition claimTransition : claimTransitionList) {
                                if (claimTransition.getStatus().equalsIgnoreCase("Discharge Request Received")) {
                                    isLastStageDischarge = true;
                                    break;
                                } else if (claimTransition.getStatus().equalsIgnoreCase("Interim Request Received")) {
                                    break;
                                }
                            }
                        }

                        if (isLastStageDischarge) {
                            processEpisodeDetailForDischarge(claimAdjudicationResult, preAuthRequest, claimData.getProcedureId());
                        } else {
                            processEpisodeDetailsForPreAuth(claimAdjudicationResult, preAuthRequest, claimData.getProcedureId());
                        }
                    } else if (isDischarge) {
                        processEpisodeDetailForDischarge(claimAdjudicationResult, preAuthRequest, claimData.getProcedureId());
                    } else {
                        throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
                    }

                    preAuthRequest.checkAndCorrectData();
                    preAuthRequest.getClaimDetails().setTariffDifferential(String.valueOf(tariffDifferential.intValue()));
                    nivaRequestData.setRequestData(new Gson().toJson(preAuthRequest));

                    log.info("Niva Cashless Request Format: " + new Gson().toJson(preAuthRequest));
                    long startTimePreAuth = System.currentTimeMillis();
                    nivaResponse = insuranceAgencyCallBacks.sendClaimRequestToNiva(preAuthRequest, claimData, isPreAuth,
                            isInterim, isDischarge, isQueryReply, isReconsideration, isSettlement, isRepush, nivaPushEvent, startTimePreAuth);
                }

                try {
                    if (isSettlement) {
                        String response = "Please find the Niva Settlement request details below: <br><br> Response: " + new Gson().toJson(settlementRequest) + "<br><br> Niva Response: " + new Gson().toJson(nivaSettlementResponse);
                        communicationService.sendEmail(claimData.getIntimationNumber() + " | Niva Settlement Request Status ", response, CLAIM_RECEIVED_EMAIL_TO);
                    } else {
                        String response = "Please find the Niva cashless request details below: <br><br> Response: " + new Gson().toJson(preAuthRequest) + "<br><br> Niva Response: " + new Gson().toJson(nivaResponse);
                        communicationService.sendEmail(claimData.getIntimationNumber() + " | Niva Pre Auth Request Status ", response, CLAIM_RECEIVED_EMAIL_TO);
                    }
                } catch (Exception e) {
                    log.info("claim -{} Exception occurred in sending email for preauth niva request sts", claimData.getId(), e);
                }

                if (nivaResponse != null) {
                    if (isPreAuth) {
                        if (nivaResponse.getStatus().equalsIgnoreCase("True") && !isRepush) {
                            if (nivaResponse.getPreauthId() != null && !nivaResponse.getPreauthId().equalsIgnoreCase("0")) {
                                claimData.setInsurerIdentifier(nivaResponse.getPreauthId());
                                claimData.setClaimStatus(ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT);
                                claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                                nivaRequestData.setSuccess(true);
                                claimData.setPushedToInsurer(true);
                                claimAdjudicationResult.setPreAuthIdentifier(nivaResponse.getPreauthId());
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        nivaResponse.getPreauthId(), false, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                                claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                            } else {
                                nivaRequestData.setErrorMsg(new Gson().toJson(nivaResponse.getErrorList()));
                                log.info("claim -{} error message from niva at preauth creation stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                                nivaRequestData.setSuccess(false);
                                if (checkIfDuplicateClaim(nivaResponse.getErrorList())) {
                                    claimData.setClaimStatus(ClaimStatus.DUPLICATE);
                                    claimData.setInsurerIdentifier(ClaimStatus.DUPLICATE.toString());
                                    claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                                    a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                            null, true, isPreAuth, isInterim, isDischarge, isQueryReply,
                                            isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                                    claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                                    claimData.setPushedToInsurer(true);
                                    claimAdjudicationResult.setPreAuthIdentifier("DUPLICATE");
                                } else {
                                    a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT,
                                            claimData.getIntimationNumber(), startTime, nivaPushEvent);
                                }

                            }
                        } else if (isRepush && nivaResponse.getStatus().equalsIgnoreCase("Success")) {
                            claimData.setClaimStatus(ClaimStatus.PRE_AUTHORISATION_RESPONSE_RE_PUSHED);
                            claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                            nivaRequestData.setSuccess(true);
                            claimData.setPushedToInsurer(true);
                            claimAdjudicationResult.setPreAuthIdentifier(claimData.getInsurerIdentifier());
                            markApiPushedSuccess(claimData, nivaPushEvent, startTime);
                            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                        } else {
                            nivaRequestData.setErrorMsg(new Gson().toJson(nivaResponse.getErrorList()));
                            log.info("claim -{} error message from niva at preauth creation stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                            nivaRequestData.setSuccess(false);
                            if (checkIfDuplicateClaim(nivaResponse.getErrorList())) {
                                claimData.setClaimStatus(ClaimStatus.DUPLICATE);
                                claimData.setInsurerIdentifier(ClaimStatus.DUPLICATE.toString());
                                claimData.setPushedToInsurer(true);
                                claimAdjudicationResult.setPreAuthIdentifier("DUPLICATE");
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        null, true, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                                claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                            } else {
                                a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT,
                                        claimData.getIntimationNumber(), startTime, nivaPushEvent);
                            }
                        }
                    } else if (isInterim) {
                        if (nivaResponse.getStatus().equalsIgnoreCase("Success") || nivaResponse.getStatus().equalsIgnoreCase("True")) {
                            claimData.setClaimStatus(isRepush ? ClaimStatus.INTERIM_RESPONSE_RE_PUSH : ClaimStatus.INTERIM_RESPONSE_SENT);
                            claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                            nivaRequestData.setSuccess(true);
                            claimData.setPushedToInsurer(true);
                            if (!isRepush) {
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        nivaResponse.getPreauthId(), false, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                            } else {
                                markApiPushedSuccess(claimData, nivaPushEvent, startTime);
                            }
                            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                        } else {
                            nivaRequestData.setErrorMsg(new Gson().toJson(nivaResponse.getErrorList()));
                            log.info("claim -{} error message from niva at interim creation stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                            nivaRequestData.setSuccess(false);
                            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.INTERIM_RESPONSE_SENT,
                                    claimData.getIntimationNumber(), startTime, nivaPushEvent);
                        }
                    } else if (isDischarge) {
                        if (nivaResponse.getStatus().equalsIgnoreCase("Success")) {
                            claimData.setClaimStatus(isRepush ? ClaimStatus.DISCHARGE_RESPONSE_RE_PUSH : ClaimStatus.DISCHARGE_RESPONSE_SENT);
                            claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                            nivaRequestData.setSuccess(true);
                            claimData.setPushedToInsurer(true);
                            if (!isRepush) {
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        nivaResponse.getPreauthId(), false, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                            } else {
                                markApiPushedSuccess(claimData, nivaPushEvent, startTime);
                            }
                            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                        } else {
                            nivaRequestData.setErrorMsg(new Gson().toJson(nivaResponse.getErrorList()));
                            log.info("claim -{} error message from niva at discharge creation stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                            nivaRequestData.setSuccess(false);
                            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.DISCHARGE_RESPONSE_SENT,
                                    claimData.getIntimationNumber(), startTime, nivaPushEvent);
                        }
                    } else if (isQueryReply) {
                        if (nivaResponse.getStatus().equalsIgnoreCase("Success")) {
                            claimData.setClaimStatus(isRepush ? ClaimStatus.QUERY_REPLY_RE_PUSHED : ClaimStatus.QUERY_REPLY_RESPONSE_SENT);
                            claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                            nivaRequestData.setSuccess(true);
                            claimData.setPushedToInsurer(true);
                            if (!isRepush) {
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        nivaResponse.getPreauthId(), false, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                            } else {
                                markApiPushedSuccess(claimData, nivaPushEvent, startTime);
                            }
                            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                        } else {
                            nivaRequestData.setErrorMsg(new Gson().toJson(nivaResponse.getErrorList()));
                            log.info("claim -{} error message from niva at query reply stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                            nivaRequestData.setSuccess(false);
                            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.QUERY_REPLY_RESPONSE_SENT,
                                    claimData.getIntimationNumber(), startTime, nivaPushEvent);
                        }
                    } else if (isReconsideration) {
                        if (nivaResponse.getStatus().equalsIgnoreCase("Success")) {
                            claimData.setClaimStatus(isRepush ? ClaimStatus.DENIAL_RECONSIDERATION_REPUSHED : ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT);
                            claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                            nivaRequestData.setSuccess(true);
                            claimData.setPushedToInsurer(true);
                            if (!isRepush) {
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        nivaResponse.getPreauthId(), false, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, isSettlement, nivaRequestData, startTime, nivaPushEvent, true);
                            } else {
                                markApiPushedSuccess(claimData, nivaPushEvent, startTime);
                            }
                            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                        } else {
                            nivaRequestData.setErrorMsg(new Gson().toJson(nivaResponse.getErrorList()));
                            log.info("claim -{} error message from niva at reconsideration stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                            nivaRequestData.setSuccess(false);
                            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT,
                                    claimData.getIntimationNumber(), startTime, nivaPushEvent);
                        }
                    }
                } else if (nivaSettlementResponse != null) {
                    if (isSettlement) {
                        if (nivaSettlementResponse.getStatus().equalsIgnoreCase("True")
                                || nivaSettlementResponse.getStatus().equalsIgnoreCase("Success")) {
                            claimData.setClaimStatus(isRepush ? ClaimStatus.SETTLEMENT_REPUSHED : ClaimStatus.SETTLEMENT_RESPONSE_SENT);
                            claimData.setClaimNumber(nivaSettlementResponse.getClaimNumber());
//                            claimData.setAdjudicationStatus(preAuthRequest.getAdjudicationResult().getAdjudicationDecision());
                            nivaRequestData.setSuccess(true);
                            claimData.setPushedToInsurer(true);
                            if (!isRepush) {
                                a2SCallbackService.processA2SSuccessCallback(claimData.getId(), claimData.getClaimStatus(), claimData.getIntimationNumber(),
                                        nivaSettlementResponse.getClaimNumber(), false, isPreAuth, isInterim, isDischarge, isQueryReply,
                                        isReconsideration, true, nivaRequestData, startTime, nivaPushEvent, true);
                            } else {
                                markApiPushedSuccess(claimData, nivaPushEvent, startTime);
                            }
                            claimTransitionService.saveClaimTransitionData(claimData.getId(), "Sent to " + AppConstants.NIVA + " Successfully", claimData.getTxnId());
                        } else {
                            nivaRequestData.setErrorMsg(new Gson().toJson(nivaSettlementResponse.getErrorList()));
                            log.info("claim -{} error message from niva at settlement stage {}", claimData.getId(), nivaRequestData.getErrorMsg());
                            nivaRequestData.setSuccess(false);
                            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.SETTLEMENT_RESPONSE_SENT,
                                    claimData.getIntimationNumber(), startTime, nivaPushEvent);
                        }
                    }
                } else {
                    log.info("nivaRequestData.getErrorMsg- {}", nivaRequestData.getErrorMsg());
                    nivaRequestData.setSuccess(false);
                    makeFailureCallbackAPICall(claimData, isPreAuth, isInterim, isDischarge, isQueryReply, isReconsideration,
                            isSettlement, startTime, nivaPushEvent);
                }
                nivaRequestData.setDateCreated(new Date());
                nivaRequestDataRepository.save(nivaRequestData);
                claimDataRepository.save(claimData);
                claimAdjudicationRepository.save(claimAdjudicationResult);
                /// save claim level data
//                log.info("Save MANUAL Data in error message logs in processPreAuthRequest for claim data id: {}", claimData.getId());
//                if (claimData.getAdjudicationStatus().equalsIgnoreCase("MANUAL")) {
//                    ErrorMsgType error = ErrorMsgType.MANUAL;
//                    errorMessageLogsService.saveErrorMessages(claimData, error);
//                    log.info("Saving Error Message Logs for MANUAL in processPreAuthRequest method for {}", claimData.getIntimationNumber());
//                }
            } else {
                makeFailureCallbackAPICall(claimData, isPreAuth, isInterim, isDischarge, isQueryReply, isReconsideration,
                        isSettlement, startTime, nivaPushEvent);
                return false;
            }
        } catch (Exception e) {
            log.info("claim -{} Exception occurred in sending preauth/interim/discharge data", claimData.getId(), e);
            makeFailureCallbackAPICall(claimData, isPreAuth, isInterim, isDischarge, isQueryReply, isReconsideration,
                    isSettlement, startTime, nivaPushEvent);
            return false;
        }

        return true;
    }

    private void markApiPushedSuccess(ClaimData claimData, NivaPushEvent nivaPushEvent, long startTime) {
        try {
            long endTime = System.currentTimeMillis();
            nivaPushEventService.updateEvent(nivaPushEvent.getId(), NivaPushStatus.PUSH_SUCCESS.toString(), "N/A",endTime - startTime);
        } catch (Exception ex) {
            log.error("claim -{} Exception found while fetching niva push event data. {}", claimData.getId(), ex.getLocalizedMessage());
        }
    }

    private void processEpisodeDetailForDischarge(ClaimAdjudicationResult claimAdjudicationResult, PreAuthRequest preAuthRequest,
                                                         long procedureId) {
        BigDecimal dischargeAmountApproved = BigDecimal.ZERO;
        if (claimAdjudicationResult.getInsurerDischargeAmountApproved() != null) {
            dischargeAmountApproved = claimAdjudicationResult.getInsurerDischargeAmountApproved();
        } else if (claimAdjudicationResult.getDischargeAmountApproved() != null) {
            dischargeAmountApproved = claimAdjudicationResult.getDischargeAmountApproved();
        }

        preAuthRequest.groupEpisodeDetailsByServiceType(dischargeAmountApproved, claimAdjudicationResult);
        setServiceTypeBasedOnSelectedProceduresForDischarge(procedureId, preAuthRequest);
    }

    private void processEpisodeDetailsForPreAuth(ClaimAdjudicationResult claimAdjudicationResult, PreAuthRequest preAuthRequest, long procedureId) {
        BigDecimal preAuthAmountApproved = BigDecimal.ZERO;
        if (claimAdjudicationResult.getPreAuthInsurerAmountApproved() != null) {
            preAuthAmountApproved = claimAdjudicationResult.getPreAuthInsurerAmountApproved();
        }
        NivaUcrClaims nivaUcrClaims = nivaUcrClaimsRepo.findLatestByClaimDataId(claimAdjudicationResult.getClaimDataId());
        preAuthRequest.groupEpisodeDetailsIntoSingleRow(preAuthAmountApproved, claimAdjudicationResult, nivaUcrClaims);
        EpisodeDetails groupedEpisodes = preAuthRequest.getEpisodeDetails().getFirst();
        setServiceTypeBasedOnSelectedProcedures(procedureId, groupedEpisodes);
    }

    private EpisodeDetails setServiceTypeBasedOnSelectedProcedures(long procedureId, EpisodeDetails groupedEpisodes) {
        if (isDayCareProcedure(procedureId) || isDialysisProcedure(procedureId)
                || isRadiotherapyProcedure(procedureId) || isChemotherapyProcedure(procedureId)) {
            log.info("ProcedureId: {} is a Day Care procedure", procedureId);
            // For Day Care, we need to set the below-mentioned values
            groupedEpisodes.setServiceType("11");
            groupedEpisodes.setBenType("C");
            groupedEpisodes.setBenHead("C8");
        } else if (isMaternityProcedure(procedureId)) {
            log.info("ProcedureId: {} is a Maternity procedure", procedureId);
            groupedEpisodes.setServiceType("27");
            groupedEpisodes.setBenType("F,N");
            groupedEpisodes.setBenHead("F8");
        }
        return groupedEpisodes;
    }

    private boolean isChemotherapyProcedure(long procedureId) {
        return chemoProcedureIds != null && chemoProcedureIds.contains(procedureId);
    }

    private boolean isRadiotherapyProcedure(long procedureId) {
        return radiotherapyProcedureIds != null && radiotherapyProcedureIds.contains(procedureId);
    }

    private boolean isDialysisProcedure(long procedureId) {
        return dialysisProcedureIds != null && dialysisProcedureIds.contains(procedureId);
    }

    private boolean isDayCareProcedure(long procedureId) {
        return dayCareProcedureIds != null && dayCareProcedureIds.contains(procedureId);
    }

    private boolean isMaternityProcedure(long procedureId) {
        return maternityProcedureIds != null && maternityProcedureIds.contains(procedureId);
    }

    private void makeFailureCallbackAPICall(ClaimData claimData, boolean isPreAuth, boolean isInterim, boolean isDischarge, boolean isQueryReply,
                                            boolean isReconsideration, boolean isSettlement, Long startTime, NivaPushEvent nivaPushEvent) {
        if (isPreAuth) {
            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.PRE_AUTHORISATION_RESPONSE_SENT, claimData.getIntimationNumber(),
                    startTime, nivaPushEvent);
        } else if (isInterim) {
            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.INTERIM_RESPONSE_SENT, claimData.getIntimationNumber(),
                    startTime, nivaPushEvent);
        } else if (isDischarge) {
            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.DISCHARGE_RESPONSE_SENT, claimData.getIntimationNumber(),
                    startTime, nivaPushEvent);
        } else if (isQueryReply) {
            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.QUERY_REPLY_RESPONSE_SENT, claimData.getIntimationNumber(),
                    startTime, nivaPushEvent);
        } else if (isReconsideration) {
            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.DENIAL_RECONSIDERATION_RESPONSE_SENT, claimData.getIntimationNumber(),
                    startTime, nivaPushEvent);
        } else if (isSettlement) {
            a2SCallbackService.processA2SFailureCallback(claimData.getId(), ClaimStatus.SETTLEMENT_RESPONSE_SENT, claimData.getIntimationNumber(),
                    startTime, nivaPushEvent);
        }
    }

    private NivaRequestData getLatestNivaRequestOfAClaim(ClaimData claimData) {

        NivaRequestData nivaRequestData = nivaRequestDataRepository.findTopByClaimDataIdOrderByIdDesc(claimData.getId());

        return nivaRequestData;

    }

    private List<EpisodeDetails> getMasterCategoryMapping(boolean isPackage, BillTariffPmlDto billTariffPmlDto,
                                                          long hospitalId, String bedType, String benefitType,
                                                          ClaimAdjudicationResult claimAdjudicationResult,
                                                          Date admissionDate, Date dischargeDate, ClaimData claimData,
                                                          boolean isRejected, boolean isPreAuth, boolean isDischarge) {
        try {
            log.info("claim -{} preparing master category mapping ", claimData.getId());
            List<EpisodeDetails> episodeDetailsList = new ArrayList<>();

            ServiceType serviceType;
            BigDecimal copayAmt = BigDecimal.ZERO;
            BigDecimal upperLimit = BigDecimal.ZERO;
            boolean isUpperLimitAplicable = false;

            log.debug("isUpperLimitAplicable() - {}", isUpperLimitAplicable);
            log.debug("getUpperLimit() - {}", upperLimit);

            if (billTariffPmlDto != null && billTariffPmlDto.getBillTariffPmlAmountList() != null) {
                for (BillTariffPmlAmountData billTariffPmlAmountData : billTariffPmlDto.getBillTariffPmlAmountList()) {
                    if (billTariffPmlAmountData.getBillItemList() != null) {
                        /*
                        We have stored the patient deduction and hospital deduction in the billItemResultDto at category
                        level, so we need to set the deduction on the first item of the billItemList.
                         */
                        boolean isDeductionSet = false;
                        for (BillItemResultDto billItemResultDto : billTariffPmlAmountData.getBillItemList()) {
                            EpisodeDetails episodeDetails = new EpisodeDetails();
                            serviceType = getServiceType(billItemResultDto.getMaster_category(), benefitType, true);
//                            if (claimData.getLineOfTreatmentDetails().equalsIgnoreCase(ClaimIllnessTreatmentDetailsDTO.ManagementType.MEDICAL.getManagementType())) {
//                                serviceType = getServiceType(getMedicalManagementOpenBillingTypeMapping(billItemResultDto.getMaster_category()),
//                                        benefitType, true);
//                            } else if (claimData.getLineOfTreatmentDetails().equalsIgnoreCase(ClaimIllnessTreatmentDetailsDTO.ManagementType.SURGICAL.getManagementType())) {
//                                serviceType = getServiceType(getSurgicalManagementOpenBillingTypeMapping(billItemResultDto.getMaster_category()),
//                                        benefitType, true);
//                            }
                            if (serviceType != null) {
                                episodeDetails.setServiceType(serviceType.getServiceType());
                                episodeDetails.setBenType(serviceType.getBenefitType());
                                if (serviceType.getBenefitHead() != null && serviceType.getBenefitHead().contains(",")) {
                                    episodeDetails.setBenHead(serviceType.getBenefitHead().split(",")[0]);
                                } else {
                                    episodeDetails.setBenHead(serviceType.getBenefitHead());
                                }
                            }
                            episodeDetails.setBedType(bedType != null ? bedType : "1");

                            copayAmt = billItemResultDto.getCopay() != null ? (billItemResultDto.getActual_amount().multiply(billItemResultDto.getCopay())).divide(new BigDecimal("100"), RoundingMode.DOWN) : BigDecimal.ZERO;

                            BigDecimal lineItemDeduction = billItemResultDto.getRequested_amount() != null && billItemResultDto.getFinal_amount_after_drop() != null ? billItemResultDto.getRequested_amount().subtract(billItemResultDto.getFinal_amount_after_drop()) : BigDecimal.ZERO; // ToDo: Need to add the required information in DTO
                            BigDecimal patientDeduction = !isDeductionSet && billTariffPmlAmountData.getPatientPayableDeduction() != null ? billTariffPmlAmountData.getPatientPayableDeduction() : BigDecimal.ZERO;
                            BigDecimal hospitalDeduction = !isDeductionSet && billTariffPmlAmountData.getHospitalPayableDeduction() != null ? billTariffPmlAmountData.getHospitalPayableDeduction() : BigDecimal.ZERO;

                            BigDecimal rejectedAmount = lineItemDeduction.add(patientDeduction).add(hospitalDeduction);
                            isRejected = BigDecimal.ZERO.compareTo(rejectedAmount) < 0 || claimAdjudicationResult.isClaimRejected(isPreAuth, isDischarge);

                            log.info("[Claim Data ID: {}] Rejected Amount {} is with rejected flag as {}", claimData.getId(), rejectedAmount, isRejected);

                            episodeDetails.setServiceCode("");
                            episodeDetails.setDateFrom(DateUtil.parseDateInNivaFormat(admissionDate));
                            episodeDetails.setDateTo(DateUtil.parseDateInNivaFormat(dischargeDate));


                            episodeDetails.setQTY(billItemResultDto.getRequested_units() > 0 ? String.valueOf(billItemResultDto.getRequested_units()) : BigDecimal.ONE.toString()); // ToDO: Need to add the required information in DTO

                            // Please adjust the patient and hospital deduction based on the category deduction
                            episodeDetails.setCopayAmt(String.valueOf(copayAmt));
                            episodeDetails.setDeductibleAmt("0"); //sending 0 because deductible is out of scope
                            episodeDetails.setDiscountAmt(billItemResultDto.getMou_discount() != null && billItemResultDto.getMou_discount().compareTo(BigDecimal.ZERO) > 0 ? String.valueOf(billItemResultDto.getMou_discount()) : "0");
                            episodeDetails.setMPolicy("0");
                            episodeDetails.setMedical(isRejected ? "1" : "0");
                            episodeDetails.setIneligible("0");
                            episodeDetails.setStatus("A");
                            episodeDetails.setRejectQty(isRejected ? (billItemResultDto.getRequested_units() > 0 ? String.valueOf(billItemResultDto.getRequested_units()) : BigDecimal.ONE.toString()) : "");

                            /*
                             Now please add a checkpoint to check and set the requested amount, approved amount,
                             reject amount and notes.

                             Also as discussed if the insurer updated the category level information then we need to use
                             the remark they have confirmed or edited.
                             */

                            if (patientDeduction.compareTo(BigDecimal.ZERO) > 0 || hospitalDeduction.compareTo(BigDecimal.ZERO) > 0) {
                                episodeDetails.setNotes(billTariffPmlAmountData.getRemarks());
                            } else if (claimAdjudicationResult.isClaimRejected(isPreAuth, isDischarge)) {
                                episodeDetails.setNotes("claim rejected");
                            } else {
                                episodeDetails.setNotes(billItemResultDto.getRemarks() != null ? billItemResultDto.getRemarks() : "actuals");
                            }
                            episodeDetails.setRejectionReason(isRejected ? "116" : "");

                            episodeDetails.setAmountComponentsWithNote(claimData.getId(), billItemResultDto, patientDeduction, hospitalDeduction, isRejected, rejectedAmount);
                            episodeDetails.setProcedure("");
                            episodeDetailsList.add(episodeDetails);
                            isDeductionSet = true;
                        }
                    }
                }
                log.debug("claim -{} episodeDetails - {}", claimData.getId(), new Gson().toJson(episodeDetailsList));
                return episodeDetailsList;
            }
        } catch (Exception e) {
            log.info("claim -{} Caught some exception while mapping to niva masters", claimData.getId(), e);
        }
        return new ArrayList<>();
    }

    private ServiceType getServiceType(String masterCategoryName, String benefitType, boolean isEnabled) {
        List<ServiceType> serviceTypeList = serviceTypeRepository.findByVitrayaMasterAndBenefitTypeAndIsEnabled(masterCategoryName, benefitType, isEnabled);
        ServiceType serviceType = new ServiceType();
        if (serviceTypeList != null && !serviceTypeList.isEmpty() && serviceTypeList.getFirst() != null) {
            serviceType = serviceTypeList.getFirst();
        } else {
            serviceTypeList = serviceTypeRepository.findByVitrayaMasterAndBenefitTypeAndIsEnabled("Others", benefitType, isEnabled);
            serviceType = serviceTypeList.getFirst();
        }
        return serviceType;
    }

    private boolean isDayCare(long procedureId) {
        DayCareProceduresMapping dayCareProceduresMapping = dayCareProceduresMappingRepository.findByProcedureId(procedureId);
        log.debug("dayCareProceduresMapping {}", dayCareProceduresMapping);
        return dayCareProceduresMapping != null;
    }

    private boolean isNormalMaternityProcedure(long procedureId) {
        return normalMaternityProcedure.contains(procedureId);
    }

    private boolean isCSenctionMaternityProcedure(long procedureId) {
        return cSectionMaternityProcedure.contains(procedureId);
    }

    private void checkAndMapData(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, MultipartFile[] files) {

        if (vitrayaInsurerClaimData.getPayorCode().isEmpty()) {
            Users user = userService.getCurrentUser();
            Corporate corporate = corporateService.getUserCorporate(user.getCorporateId());
            vitrayaInsurerClaimData.setPayorCode(corporate.getCorporateCode());
        }

        PolicyProduct policyProduct = policyProductService.getPolicyProduct(vitrayaInsurerClaimData.getRequest().getClaim().getPolicyName());
        if (policyProduct == null) {
            vitrayaInsurerClaimData.getRequest().getClaim().setPolicyName(AdjudicationStatus.OUT_OF_SCOPE_POLICY.getAdjudicationStatus());
            vitrayaInsurerClaimData.getRequest().getClaim().setInScopePolicy(false);
        } else {
            vitrayaInsurerClaimData.getRequest().getClaim().setPolicyName(policyProduct.getProductCode());
            vitrayaInsurerClaimData.getRequest().getClaim().setInScopePolicy(true);
        }

        vitrayaInsurerClaimData.getRequest().setProcedure(procedureService.getProcedure(vitrayaInsurerClaimData));
        vitrayaInsurerClaimData.getRequest().setIllness(illnessService.getIllness(vitrayaInsurerClaimData));
        vitrayaInsurerClaimData.getRequest().getClaim().setInsuranceAgencyId(corporateService.getInsuranceAgencyId(vitrayaInsurerClaimData));
        try {
            vitrayaInsurerClaimData.getRequest().getClaim().setHospitalId(corporateService.getHospitalId(vitrayaInsurerClaimData));
            vitrayaInsurerClaimData.getRequest().getClaim().setInScopeHospital(true);
        } catch (VitrayaException ve) {
            if (VitrayaErrorCodes.INVALID_CORPORATE_CODE.getCode().equals(ve.getCode())) {
                vitrayaInsurerClaimData.getRequest().getClaim().setHospitalId(0);
                vitrayaInsurerClaimData.getRequest().getClaim().setInScopeHospital(false);
            } else {
                throw ve;
            }
        }

        // ToDo: Update the text based on the status
        if (ClaimRequestTypeEnum.preauth_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            vitrayaInsurerClaimData.getRequest().getClaim().setClaimStatusText("Initial Request Received");
        } else if (ClaimRequestTypeEnum.interim_enhancement_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            vitrayaInsurerClaimData.getRequest().getClaim().setClaimStatusText("Interim Request Received");
        } else if (ClaimRequestTypeEnum.final_enhancement_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            vitrayaInsurerClaimData.getRequest().getClaim().setClaimStatusText("Discharge Request Received");
        } else {
            vitrayaInsurerClaimData.getRequest().getClaim().setClaimStatusText("Request Received");
        }

        if (vitrayaInsurerClaimData.getTxnId() == null || vitrayaInsurerClaimData.getTxnId().isEmpty()) {
            vitrayaInsurerClaimData.setTxnId("VTXN_" + new Date().getTime());
        }
    }

    private void validateClaimData(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, MultipartFile[] files) {
        // Validation of the claim data will be performed here.
        if (vitrayaInsurerClaimData == null || vitrayaInsurerClaimData.getRequest() == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST);
        }

        if ((vitrayaInsurerClaimData.getRequest().getDocumentMasterList() == null || vitrayaInsurerClaimData.getRequest().getDocumentMasterList().isEmpty())) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_DOCUMENT_EMPTY);
        }

        if (vitrayaInsurerClaimData.getRequest().getClaim() == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_REQUEST_CLAIM_EMPTY);
        }

        String intimationNumber = claimCommonService.getIntimationNumber(vitrayaInsurerClaimData);
        ClaimData claimData = claimDataRepository.findByIntimationNumber(intimationNumber).orElse(null);

        // Validation if claim data does not exist. This is for interim/discharge requests.
        if (claimData == null && !ClaimRequestTypeEnum.preauth_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM);
        }

        if (claimData != null && ClaimRequestTypeEnum.preauth_request.equals(vitrayaInsurerClaimData.getRequestType())) {
            throw new VitrayaException(VitrayaErrorCodes.DUPLICATE_CLAIM_REQUEST);
        }

        if (vitrayaInsurerClaimData.getProviderCode() == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CORPORATE_CODE);
        }
    }


    private boolean checkIfDuplicateClaim(List<NivaResponse.ErrorList> errorList) {
        for (NivaResponse.ErrorList error : errorList) {
            if (error.getErrorCode().equalsIgnoreCase("1002")) {
                return true;
            }
        }
        return false;
    }

    private PreAuthRequest processPreAuthRequestPreview(ClaimData claimData, boolean isPreAuth,
                                                        boolean isInterim, boolean isDischarge,
                                                        boolean isQueryReply, boolean isReconsideration,
                                                        boolean isSettlement, boolean isRepush) {
        PreAuthRequest preAuthRequest = new PreAuthRequest();
        try {
            NivaRequestData nivaRequestData = new NivaRequestData();
            nivaRequestData.setWdmsUniqueNo("ABCD-111");
            InsurerFetchResponses insurerFetchResponses = insurerFetchResponsesRepository.getInsurerFetchResponsesByClaimDataId(claimData.getId());
            if (insurerFetchResponses != null && insurerFetchResponses.getPolicyData() != null && !insurerFetchResponses.getPolicyData().isEmpty()) {
                NivaPolicyDataDTO nivaPolicyDataDTO = new Gson().fromJson(insurerFetchResponses.getPolicyData(), NivaPolicyDataDTO.class);
                ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationRepository.findByClaimDataId(claimData.getId());
                claimData.setPolicyNumber(nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getPolicy_Number());
                Corporate hospital = corporateService.getUserCorporate((int) claimData.getHospitalId());
                SettlementRequest settlementRequest = new SettlementRequest();
                if (isSettlement) {
                    settlementRequest = SettlementRequest.buildSettlementRequest(claimData, hospital, nivaRequestData.getWdmsUniqueNo(), claimAdjudicationResult);
                    nivaRequestData.setRequestData(new Gson().toJson(settlementRequest));
                    log.info("Niva Settlement Request Format: " + new Gson().toJson(settlementRequest));
                } else {
                    BillTariffResponse billTariffResponse = billTariffResponseRepository.getBillTariffResponseByClaimDataIdOrderByIdDesc(claimData.getId());
                    PMLResponse pmlResponse = pmlResponseRepository.findPMLResponseByClaimDataIdLatest(claimData.getId());
                    BillTariffPmlDto billTariffPmlDto = pmlService.getPMLDataMergedV1(pmlResponse, billTariffResponse);
                    log.info("PML Data merged BillTariffPmlDto {} for claimId {}", new Gson().toJson(billTariffPmlDto), claimData.getId());
                    boolean defaultTariffApplied = billTariffResponse.getBillTariffResponseDTO().getResponse_code()
                            .equalsIgnoreCase("DEFAULT_TARIFF_APPLIED");
                    String managementType = claimData.getLineOfTreatmentDetails();
                    NivaRequestData previousStageNivaRequestData = getLatestNivaRequestOfAClaim(claimData);
                    boolean isRejected = isClaimRejected(isPreAuth, isDischarge, claimAdjudicationResult);
                    Illnesses illnesses = illnessService.getFirstIllnessByICDCode(claimData.getIcdCode());
                    HospitalServiceType hospitalServiceType = hospitalServiceTypeService.getHospitalRoomType(claimData);
                    String benefitType = "A";
                    String roomType = "";

                    if (isNormalMaternityProcedure(claimData.getProcedureId())) {
                        benefitType = "F";
                    } else if (isCSenctionMaternityProcedure(claimData.getProcedureId())) {
                        benefitType = "N";
                    } else if (isDayCare(claimData.getProcedureId())) {
                        benefitType = "C";
                    }
                    roomType = hospitalServiceType != null && hospitalServiceType.getInsurerRoomCategoryMapping() != null ? hospitalServiceType.getInsurerRoomCategoryMapping() : "A";

                    List<EpisodeDetails> episodeDetailsList = new ArrayList<>();

                    PrepareEpisodeDetailsDataDTO prepareEpisodeDetailsDataDTO = new PrepareEpisodeDetailsDataDTO();
                    prepareEpisodeDetailsDataDTO.setPackageCase(false);
                    prepareEpisodeDetailsDataDTO.setBillTariffPmlDto(billTariffPmlDto);
                    prepareEpisodeDetailsDataDTO.setHospitalId(hospital.getId());
                    prepareEpisodeDetailsDataDTO.setBenefitType(benefitType);
                    prepareEpisodeDetailsDataDTO.setBedType(roomType);
                    prepareEpisodeDetailsDataDTO.setClaimAdjudicationResult(claimAdjudicationResult);
                    prepareEpisodeDetailsDataDTO.setAdmissionDate(claimData.getDateOfAdmission());
                    prepareEpisodeDetailsDataDTO.setDischargeDate(claimData.getDateOfDischarge());
                    prepareEpisodeDetailsDataDTO.setClaimData(claimData);
                    prepareEpisodeDetailsDataDTO.setRejected(isRejected);
                    prepareEpisodeDetailsDataDTO.setPreAuth(isPreAuth);
                    prepareEpisodeDetailsDataDTO.setDischarge(isDischarge);
                    prepareEpisodeDetailsDataDTO.setInterim(isInterim);
                    prepareEpisodeDetailsDataDTO.setManagementType(managementType);
                    prepareEpisodeDetailsDataDTO.setQuery(isQueryReply);
                    prepareEpisodeDetailsDataDTO.setReconsideration(isReconsideration);
                    prepareEpisodeDetailsDataDTO.setDefaultTariffApplied(defaultTariffApplied);

                    log.info("Prepared PrepareEpisodeDetailsDataDTO for claimId {} : {}", claimData.getId(), new Gson().toJson(prepareEpisodeDetailsDataDTO));
                    preAuthRequest = PreAuthRequest.buildPreAuthRequest(nivaRequestData.getWdmsUniqueNo(),
                            claimData, billTariffPmlDto, claimAdjudicationResult, episodeDetailsList,
                            null, roomType, isPreAuth, nivaPolicyDataDTO, hospital, illnesses,
                            previousStageNivaRequestData, isPreAuth, isInterim, isDischarge, isQueryReply,
                            isReconsideration, false, isRepush);
                    episodeDetailsList = prepareEpisodeDetails(prepareEpisodeDetailsDataDTO, preAuthRequest);
                    log.info("Prepared Episode Details for claimId {} : {}", claimData.getId(), new Gson().toJson(episodeDetailsList));
                    preAuthRequest.setEpisodeDetails(episodeDetailsList);
                    preAuthRequest.checkAndCorrectData();

                    HashMap<String, BigDecimal> deductibleMap = pmlService.getDeductibleMap(pmlResponse);
                    BigDecimal hospitalPayableDeductible = deductibleMap.get("hospitalPayableDeductible");
                    BigDecimal hospitalAmount = hospitalPayableDeductible != null ? hospitalPayableDeductible : BigDecimal.ZERO;
                    BigDecimal tariffDeduction = billTariffPmlDto != null ? billTariffPmlDto.getNmeTariffDeductionAmount() : BigDecimal.ZERO;
                    tariffDeduction = tariffDeduction != null ? tariffDeduction : BigDecimal.ZERO;
                    BigDecimal tariffDifferential = hospitalAmount.add(tariffDeduction);

                    preAuthRequest.getClaimDetails().setTariffDifferential(String.valueOf(tariffDifferential.intValue()));
                    log.info("New approach Niva Cashless Request Format: - {} ", new Gson().toJson(preAuthRequest));
                }
            }
        } catch (Exception e) {
            log.info("claim -{} Caught some exception while preparing PreAuthRequest", claimData.getId(), e);
        }
        return preAuthRequest;
    }

    private static boolean isClaimRejected(boolean isPreAuth, boolean isDischarge, ClaimAdjudicationResult claimAdjudicationResult) {
        boolean isRejected = false;
        if (isPreAuth) {
            if (claimAdjudicationResult.getPreAuthInsurerDecision() != null) {
                isRejected = AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getPreAuthInsurerDecision());
            } else if (claimAdjudicationResult.getClaimDecision() != null) {
                isRejected = AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getClaimDecision());
            }
        } else if (isDischarge) {
            if (claimAdjudicationResult.getDischargeInsurerDecision() != null) {
                isRejected = AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getDischargeInsurerDecision());
            } else if (claimAdjudicationResult.getClaimDecision() != null) {
                isRejected = AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getClaimDecision());
            }
        }
        return isRejected;
    }


    private List<EpisodeDetails> prepareEpisodeDetails(PrepareEpisodeDetailsDataDTO prepareEpisodeDetailsDataDTO, PreAuthRequest preAuthRequest) {
        List<EpisodeDetails> episodeDetailsList = new ArrayList<>();
        try {
            log.info("new method---prepareEpisodeDetails");
            log.info("claim -{} preparing master category mapping based on new logic", prepareEpisodeDetailsDataDTO.getClaimData().getId());
            boolean isLastStageDischarge = false;
            if (prepareEpisodeDetailsDataDTO.getBillTariffPmlDto() != null
                    && prepareEpisodeDetailsDataDTO.getBillTariffPmlDto().getBillTariffPmlAmountList() != null) {
                episodeDetailsList = prepareInitialEpisodeList(prepareEpisodeDetailsDataDTO);
                if (prepareEpisodeDetailsDataDTO.isPreAuth() || prepareEpisodeDetailsDataDTO.isInterim()) {
                    episodeDetailsList = prepareCombineEpisodeDetailsForPreAuth(prepareEpisodeDetailsDataDTO.getClaimAdjudicationResult(),
                            prepareEpisodeDetailsDataDTO.getClaimData().getProcedureId(), episodeDetailsList);
                } else if (prepareEpisodeDetailsDataDTO.isQuery() || prepareEpisodeDetailsDataDTO.isReconsideration()) {
                    List<ClaimTransition> claimTransitionList = claimTransitionService.findAllClaimTransitionByClaimDataIdLatest(prepareEpisodeDetailsDataDTO.getClaimData().getId());
                    if (claimTransitionList != null && !claimTransitionList.isEmpty()) {
                        for (ClaimTransition claimTransition : claimTransitionList) {
                            if (claimTransition.getStatus().equalsIgnoreCase("Discharge Request Received")) {
                                isLastStageDischarge = true;
                                break;
                            } else if (claimTransition.getStatus().equalsIgnoreCase("Interim Request Received")) {
                                break;
                            }
                        }
                    }
                    if (isLastStageDischarge) {
                        episodeDetailsList = prepareAndGroupEpisodeDetailForDischarge(prepareEpisodeDetailsDataDTO.getClaimAdjudicationResult(),
                                episodeDetailsList, preAuthRequest, prepareEpisodeDetailsDataDTO.getClaimData().getProcedureId());
                    } else {
                        episodeDetailsList = prepareCombineEpisodeDetailsForPreAuth(prepareEpisodeDetailsDataDTO.getClaimAdjudicationResult(),
                                prepareEpisodeDetailsDataDTO.getClaimData().getProcedureId(), episodeDetailsList);
                    }
                } else if (prepareEpisodeDetailsDataDTO.isDischarge()) {
                    episodeDetailsList = prepareAndGroupEpisodeDetailForDischarge(prepareEpisodeDetailsDataDTO.getClaimAdjudicationResult(),
                            episodeDetailsList, preAuthRequest, prepareEpisodeDetailsDataDTO.getClaimData().getProcedureId());
                }
                log.debug("claim -{} episodeDetails - {}", prepareEpisodeDetailsDataDTO.getClaimData().getId(), new Gson().toJson(episodeDetailsList));
                return episodeDetailsList;
            }
        } catch (Exception e) {
            log.info("claim -{} Caught some exception while mapping to niva masters", prepareEpisodeDetailsDataDTO.getClaimData().getId(), e);
        }
        return episodeDetailsList;
    }

    private List<EpisodeDetails> prepareInitialEpisodeList(PrepareEpisodeDetailsDataDTO prepareEpisodeDetailsDataDTO) {
        ServiceType serviceType;
        BigDecimal copayAmt = BigDecimal.ZERO;
        List<EpisodeDetails> episodeDetailsList = new ArrayList<>();
        log.info("new method---prepareInitialEpisodeList");
        for (BillTariffPmlAmountData billTariffPmlAmountData : prepareEpisodeDetailsDataDTO.getBillTariffPmlDto().getBillTariffPmlAmountList()) {
            if (billTariffPmlAmountData.getBillItemList() != null) {
                boolean isDeductionSet = false;
                for (BillItemResultDto billItemResultDto : billTariffPmlAmountData.getBillItemList()) {
                    EpisodeDetails episodeDetails = new EpisodeDetails();
                    serviceType = getServiceType(billItemResultDto.getMaster_category(), prepareEpisodeDetailsDataDTO.getBenefitType(), true);
                    if (prepareEpisodeDetailsDataDTO.getManagementType().equalsIgnoreCase(ClaimIllnessTreatmentDetailsDTO.ManagementType.MEDICAL.getManagementType())) {
                        serviceType = getServiceType(getMedicalManagementOpenBillingTypeMapping(billItemResultDto.getMaster_category()),
                                prepareEpisodeDetailsDataDTO.getBenefitType(), true);
                    } else if (prepareEpisodeDetailsDataDTO.getManagementType().equalsIgnoreCase(ClaimIllnessTreatmentDetailsDTO.ManagementType.SURGICAL.getManagementType())) {
                        serviceType = getServiceType(getSurgicalManagementOpenBillingTypeMapping(billItemResultDto.getMaster_category()),
                                prepareEpisodeDetailsDataDTO.getBenefitType(), true);
                    }
                    if (serviceType != null) {
                        episodeDetails.setServiceType(serviceType.getServiceType());
//                        episodeDetails.setServiceTypeDescription(serviceType.getServiceTypeDescription());
                        episodeDetails.setBenType(serviceType.getBenefitType());
                        if (serviceType.getBenefitHead() != null && serviceType.getBenefitHead().contains(",")) {
                            episodeDetails.setBenHead(serviceType.getBenefitHead().split(",")[0]);
                        } else {
                            episodeDetails.setBenHead(serviceType.getBenefitHead());
                        }
                    } //
                    episodeDetails.setBedType(prepareEpisodeDetailsDataDTO.getBedType() != null
                            ? prepareEpisodeDetailsDataDTO.getBedType()
                            : "1");

                    copayAmt = billItemResultDto.getCopay() != null
                            ? (billItemResultDto.getActual_amount().multiply(billItemResultDto.getCopay())).divide(new BigDecimal("100"), RoundingMode.DOWN)
                            : BigDecimal.ZERO;

                    BigDecimal lineItemDeduction = billItemResultDto.getRequested_amount() != null && billItemResultDto.getFinal_amount_after_drop() != null
                            ? billItemResultDto.getRequested_amount().subtract(billItemResultDto.getFinal_amount_after_drop())
                            : BigDecimal.ZERO; // ToDo: Need to add the required information in DTO
                    BigDecimal patientDeduction = !isDeductionSet && billTariffPmlAmountData.getPatientPayableDeduction() != null
                            ? billTariffPmlAmountData.getPatientPayableDeduction()
                            : BigDecimal.ZERO;
                    BigDecimal hospitalDeduction = !isDeductionSet && billTariffPmlAmountData.getHospitalPayableDeduction() != null
                            ? billTariffPmlAmountData.getHospitalPayableDeduction()
                            : BigDecimal.ZERO;

                    BigDecimal rejectedAmount = lineItemDeduction.add(patientDeduction).add(hospitalDeduction);
                    boolean isRejected = BigDecimal.ZERO.compareTo(rejectedAmount) < 0
                            || prepareEpisodeDetailsDataDTO.getClaimAdjudicationResult().isClaimRejected(prepareEpisodeDetailsDataDTO.isPreAuth(), prepareEpisodeDetailsDataDTO.isDischarge());

                    log.info("[Claim Data ID: {}] Rejected Amount {} is with rejected flag as {}", prepareEpisodeDetailsDataDTO.getClaimData().getId(), rejectedAmount, isRejected);

                    episodeDetails.setServiceCode("");
                    episodeDetails.setDateFrom(DateUtil.parseDateInNivaFormat(prepareEpisodeDetailsDataDTO.getAdmissionDate()));
                    episodeDetails.setDateTo(DateUtil.parseDateInNivaFormat(prepareEpisodeDetailsDataDTO.getDischargeDate()));
                    episodeDetails.setQTY(BigDecimal.ONE.toString()); // ToDO: Need to add the required information in DTO

                    // Please adjust the patient and hospital deduction based on the category deduction
                    episodeDetails.setCopayAmt(String.valueOf(copayAmt));
                    episodeDetails.setDeductibleAmt("0"); //sending 0 because deductible is out of scope
                    episodeDetails.setDiscountAmt(billItemResultDto.getMou_discount() != null && billItemResultDto.getMou_discount().compareTo(BigDecimal.ZERO) > 0
                            ? String.valueOf(billItemResultDto.getMou_discount())
                            : "0");
                    episodeDetails.setMPolicy("0");
                    episodeDetails.setMedical(isRejected ? "1" : "0");
                    episodeDetails.setIneligible("0");
                    episodeDetails.setStatus("A");
                    episodeDetails.setRejectQty(isRejected ? BigDecimal.ONE.toString() : "");

                    if (patientDeduction.compareTo(BigDecimal.ZERO) > 0 || hospitalDeduction.compareTo(BigDecimal.ZERO) > 0) {
                        episodeDetails.setNotes(billTariffPmlAmountData.getRemarks());
                    } else if (prepareEpisodeDetailsDataDTO.getClaimAdjudicationResult().isClaimRejected(prepareEpisodeDetailsDataDTO.isPreAuth(), prepareEpisodeDetailsDataDTO.isDischarge())) {
                        episodeDetails.setNotes("claim rejected");
                    } else {
                        episodeDetails.setNotes(billItemResultDto.getRemarks() != null ? billItemResultDto.getRemarks() : "actuals");
                    }
                    episodeDetails.setRejectionReason(isRejected ? "116" : "");

                    episodeDetails.setAmountComponentsWithNote(prepareEpisodeDetailsDataDTO.getClaimData().getId(),
                            billItemResultDto, patientDeduction, hospitalDeduction, isRejected, rejectedAmount);
                    episodeDetails.setProcedure("");
                    episodeDetailsList.add(episodeDetails);
                    isDeductionSet = true;
                }
            }
        }
        return episodeDetailsList;
    }

    private List<EpisodeDetails> prepareCombineEpisodeDetailsForPreAuth(ClaimAdjudicationResult claimAdjudicationResult,
                                                                        long procedureId, List<EpisodeDetails> episodeDetailsList) {
        log.info("new method---prepareCombineEpisodeDetailsForPreAuth");
        BigDecimal preAuthAmountApproved = BigDecimal.ZERO;
        if (claimAdjudicationResult.getPreAuthInsurerAmountApproved() != null) {
            preAuthAmountApproved = claimAdjudicationResult.getPreAuthInsurerAmountApproved();
        }

        NivaUcrClaims nivaUcrClaims = nivaUcrClaimsRepo.findLatestByClaimDataId(claimAdjudicationResult.getClaimDataId());
        PreAuthRequest preAuthRequest = new PreAuthRequest();
        preAuthRequest.setEpisodeDetails(episodeDetailsList);
        preAuthRequest.groupEpisodeDetailsIntoSingleRow(preAuthAmountApproved, claimAdjudicationResult, nivaUcrClaims);
        EpisodeDetails groupedEpisodes = preAuthRequest.getEpisodeDetails() != null && !preAuthRequest.getEpisodeDetails().isEmpty()
                ? preAuthRequest.getEpisodeDetails().getFirst() : new EpisodeDetails();
        return Collections.singletonList(setServiceTypeBasedOnSelectedProcedures(procedureId, groupedEpisodes));
    }

    private List<EpisodeDetails> prepareAndGroupEpisodeDetailForDischarge(ClaimAdjudicationResult claimAdjudicationResult,
                                                                          List<EpisodeDetails> episodeDetailsList, PreAuthRequest preAuthRequest,
                                                                          long procedureId) {
        log.info("new method---prepareAndGroupEpisodeDetailForDischarge");
        BigDecimal dischargeAmountApproved = BigDecimal.ZERO;
        if (claimAdjudicationResult.getInsurerDischargeAmountApproved() != null) {
            dischargeAmountApproved = claimAdjudicationResult.getInsurerDischargeAmountApproved();
        } else if (claimAdjudicationResult.getDischargeAmountApproved() != null) {
            dischargeAmountApproved = claimAdjudicationResult.getDischargeAmountApproved();
        }
        preAuthRequest.setEpisodeDetails(episodeDetailsList);
        preAuthRequest.groupEpisodeDetailsByServiceType(dischargeAmountApproved, claimAdjudicationResult);
        setServiceTypeBasedOnSelectedProceduresForDischarge(procedureId, preAuthRequest);
        return preAuthRequest.getEpisodeDetails();
    }

    private String getMedicalManagementOpenBillingTypeMapping(String vitrayaMasterCategory) {
        Map<String, List<String>> managementTypeMapping = new HashMap<>();
        managementTypeMapping.put("Room Rent", Arrays.asList("ICU Charges", "Nursing Charges", "ICU Charges"));
        managementTypeMapping.put("Doctor Fees", Arrays.asList("Professional Fees", "Anaesthesia Charges", "surgical package",
                "Dental Charges, Cosmetic Charges"));
        managementTypeMapping.put("Investigation Charges", Arrays.asList("High Risk Charges", "Blood & Blood Products",
                 "Hospital SOC Package Charges"));
        managementTypeMapping.put("Medicines & Consumables", Arrays.asList("Food & Beverages"));
        managementTypeMapping.put("Procedure Charges", Arrays.asList("Package Charges", "Additional Package Procedure"));

        for (Map.Entry<String, List<String>> entry : managementTypeMapping.entrySet()) {
            if (entry.getValue().contains(vitrayaMasterCategory)) {
                return entry.getKey();
            }
        }
        return vitrayaMasterCategory;
    }

    private String getSurgicalManagementOpenBillingTypeMapping(String vitrayaMasterCategory) {
        Map<String, List<String>> surgicalTypeMapping = new HashMap<>();
        surgicalTypeMapping.put("Room Rent", Arrays.asList("ICU Charges", "Nursing Charges", "ICU Charges"));
        surgicalTypeMapping.put("Doctor Fees", Arrays.asList("Professional Fees", "Anaesthesia Charges", "surgical package",
                "Dental Charges, Cosmetic Charges"));
        surgicalTypeMapping.put("Investigation Charges", Arrays.asList("High Risk Charges", "Blood & Blood Products",
                "Hospital SOC Package Charges"));
        surgicalTypeMapping.put("Medicines & Consumables", Arrays.asList("Food & Beverages"));
        surgicalTypeMapping.put("Procedure Charges", Arrays.asList("Package Charges", "Additional Package Procedure"));

        for (Map.Entry<String, List<String>> entry : surgicalTypeMapping.entrySet()) {
            if (entry.getValue().contains(vitrayaMasterCategory)) {
                return entry.getKey();
            }
        }
        return vitrayaMasterCategory;
    }

    private void setServiceTypeBasedOnSelectedProceduresForDischarge(long procedureId, PreAuthRequest preAuthRequest) {
        List<EpisodeDetails> episodeDetailList = preAuthRequest.getEpisodeDetails();
        if (episodeDetailList != null && !episodeDetailList.isEmpty()) {
            for (EpisodeDetails episodeDetail : episodeDetailList) {
                if (benefitHeadStr.contains(episodeDetail.getBenHead())) {
                    if (isDayCareProcedure(procedureId)) {
                        log.info("ProcedureId: {} is a Day Care procedure", procedureId);
                        episodeDetail.setServiceType("11");
                        episodeDetail.setBenType("C");
                        episodeDetail.setBenHead("C8");
                    } else if (isChemotherapyProcedure(procedureId)) {
                        log.info("ProcedureId: {} is a Chemotherapy procedure", procedureId);
                        episodeDetail.setServiceType("29");
                        episodeDetail.setBenType("A");
                        episodeDetail.setBenHead("A9");
                    } else if (isRadiotherapyProcedure(procedureId)) {
                        log.info("ProcedureId: {} is a Radiotherapy procedure", procedureId);
                        episodeDetail.setServiceType("28");
                        episodeDetail.setBenType("A");
                        episodeDetail.setBenHead("A9");
                    } else if (isDialysisProcedure(procedureId)) {
                        log.info("ProcedureId: {} is a Dialysis procedure", procedureId);
                        episodeDetail.setServiceType("30");
                        episodeDetail.setBenType("A");
                        episodeDetail.setBenHead("A9");
                    } else if (isMaternityProcedure(procedureId)) {
                        log.info("ProcedureId: {} is a Maternity procedure", procedureId);
                        episodeDetail.setServiceType("27");
                        episodeDetail.setBenType("F");
                        episodeDetail.setBenHead("F8");
                    }
                    break;
                }
            }
        }
        log.info("episodeDetailList after setting service type based on selected procedure: {}", new Gson().toJson(episodeDetailList));
        preAuthRequest.setEpisodeDetails(episodeDetailList);
    }

    /* public static void main(String[] args) {
        ClaimAdjudicationResult claimAdjudicationResult = getClaimAdjudicationResult();
        // Reads the JSON file and creates the PreAuthRequest object
        PreAuthRequest preAuthRequest = GsonUtils.fromJsonFile("/home/himalaya/.config/JetBrains/IntelliJIdea2025.1/scratches/scratch_201.json", PreAuthRequest.class);
        System.out.println("Checkpoint 1: " + preAuthRequest);
        VitrayaClaimDataRequestService service = new VitrayaClaimDataRequestService(
                 null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        service.processEpisodeDetailsForPreAuth(claimAdjudicationResult, preAuthRequest, 15);
        System.out.println("Checkpoint 2: " + preAuthRequest);
        preAuthRequest.checkAndCorrectData();
        System.out.println("Checkpoint 3: " + GsonUtils.toJson(preAuthRequest));
    }
    private static ClaimAdjudicationResult getClaimAdjudicationResult() {
        ClaimAdjudicationResult claimAdjudicationResult = new ClaimAdjudicationResult();
        claimAdjudicationResult.setId(1524);
        claimAdjudicationResult.setClaimDataId(1385);
        claimAdjudicationResult.setClaimDecision("QUERY");
        claimAdjudicationResult.setClaimStage("preauth_request");
        claimAdjudicationResult.setPreAuthBillAmount(new BigDecimal("5500"));
        claimAdjudicationResult.setPreAuthAmountApproved(new BigDecimal("5000"));
        claimAdjudicationResult.setPreAuthDecision("QUERY");
        claimAdjudicationResult.setPreAuthSavings(new BigDecimal("500"));
        claimAdjudicationResult.setTxnId("VTXN_1755784366930");
        return claimAdjudicationResult;
    }*/
}
