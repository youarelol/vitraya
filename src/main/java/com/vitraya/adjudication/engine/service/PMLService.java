package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.dto.enums.*;
import com.vitraya.adjudication.engine.dto.request.BenefitDTO;
import com.vitraya.adjudication.engine.dto.request.BillItemDataDTO;
import com.vitraya.adjudication.engine.dto.request.PMLRequestDTO;
import com.vitraya.adjudication.engine.dto.request.PolicyRenewalHistoryDTO;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.entity.PMLResponse;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrClaims;
import com.vitraya.adjudication.engine.mysql.repository.*;
import com.vitraya.adjudication.engine.service.factory.InsurerSpecificLimitsFactory;
import com.vitraya.adjudication.engine.utils.DateUtil;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class PMLService {
    @Value("${pml.service.url}")
    private String pmlServiceUrl;

    @Value("${max.rest.retry.count:3}")
    private int MAX_REST_RETRY_COUNT;

    @Value("${claim.registration.failed.email.to}")
    private String CLAIM_REGISTRATION_FAILED_EMAIL_TO;

    @Value("${pml.auth.token}")
    private String pmlAuthToken;

    private final SseEmittersService sseEmittersService;
    private final HelperService helperService;
    private final ClaimDataRepository claimDataRepository;
    private final ErrorMessageLogsService errorMessageLogsService;
    private final ClaimAdmissionService claimAdmissionService;
    private final ProcedureService procedureService;
    private final CorporateService corporateService;
    private final HospitalServiceTypeRepository hospitalServiceTypeRepository;
    private final ClaimCommonService claimCommonService;
    private final RestService restService;
    private final BenefitCodeMappingRepository benefitCodeMappingRepository;
    private final PMLResponseRepository pmlResponseRepository;
    private final CommunicationService communicationService;
    private final ClaimModulesSavingService billModuleSavingService;
    private final InsurerSpecificLimitsFactory insurerSpecificLimitsFactory;
    private final ClaimAdmissionDetailsRepository claimAdmissionDetailsRepository;
    private final NivaUcrClaimsRepo nivaUcrClaimsRepo;

    public PMLService(ProcedureService procedureService, CorporateService corporateService,
                      HospitalServiceTypeRepository hospitalServiceTypeRepository, ClaimCommonService claimCommonService,
                      RestService restService, BenefitCodeMappingRepository benefitCodeMappingRepository,
                      PMLResponseRepository pmlResponseRepository, ClaimAdmissionService claimAdmissionService,
                      CommunicationService communicationService, ClaimModulesSavingService billModuleSavingService,
                      SseEmittersService sseEmittersService, HelperService helperService, InsurerSpecificLimitsFactory insurerSpecificLimitsFactory,
                      ClaimAdmissionDetailsRepository claimAdmissionDetailsRepository, ClaimDataRepository claimDataRepository,
                      NivaUcrClaimsRepo nivaUcrClaimsRepo, ErrorMessageLogsService errorMessageLogsService) {
        this.procedureService = procedureService;
        this.corporateService = corporateService;
        this.hospitalServiceTypeRepository = hospitalServiceTypeRepository;
        this.claimCommonService = claimCommonService;
        this.restService = restService;
        this.benefitCodeMappingRepository = benefitCodeMappingRepository;
        this.pmlResponseRepository = pmlResponseRepository;
        this.claimAdmissionService = claimAdmissionService;
        this.communicationService = communicationService;
        this.sseEmittersService = sseEmittersService;
        this.billModuleSavingService = billModuleSavingService;
        this.helperService = helperService;
        this.insurerSpecificLimitsFactory = insurerSpecificLimitsFactory;
        this.claimAdmissionDetailsRepository = claimAdmissionDetailsRepository;
        this.claimDataRepository = claimDataRepository;
        this.nivaUcrClaimsRepo = nivaUcrClaimsRepo;
        this.errorMessageLogsService = errorMessageLogsService;
    }

    public void processPMLRequest(ClaimData claimData, ClaimRunDTO claimRunDTO, BillTariffResponseDTO billTariffResponseDTO) {
        log.info("Going to process the PML request {}", claimData);
        if (claimData == null) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        makePMLRequest(billTariffResponseDTO, claimData, claimRunDTO);
    }

    private void savePMLResponse(ClaimData claimData, PMLResponseDTO pmlResponseDTO, long tat, ClaimRunDTO claimRunDTO) {
        String pmlDecision = AdjudicationStatus.MANUAL.getAdjudicationStatus();
        if (pmlResponseDTO != null && pmlResponseDTO.getClaim_result() != null) {
            pmlDecision = pmlResponseDTO.getClaim_result().isClaim_approved()
                    ? AdjudicationStatus.APPROVED.getAdjudicationStatus()
                    : AdjudicationStatus.REJECTED.getAdjudicationStatus();
        }

        PMLResponse pmlResponse = new PMLResponse();
        pmlResponse.setClaimDataId(claimData.getId());
        pmlResponse.setPmlIdentifier(claimData.getIntimationNumber());
        pmlResponse.setPmlResponse(GsonUtils.toJson(pmlResponseDTO));
        pmlResponse.setPmlTat(tat + " ms");
        pmlResponse.setDateCreated(new Date());
        pmlResponse.setDateUpdated(new Date());
        pmlResponse.setPmlDecision(pmlDecision);
        pmlResponseRepository.save(pmlResponse);

//        billModuleSavingService.updatePMLSaving(claimData, pmlResponse, claimRunDTO);
    }

    private PMLRequestDTO preparePMLData(ClaimData claimData, BillTariffResponseDTO billTariffResponseDTO) {
        if (billTariffResponseDTO == null || billTariffResponseDTO.getData() == null) {
            log.info("Bill Tariff Response Data is empty for claim data id {}", claimData.getId());
            throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
        }

        Procedures procedures = procedureService.findProcedureById(claimData.getProcedureId()).orElse(null);
        Corporate hospital = corporateService.findCorporateById((int) claimData.getHospitalId());
        HospitalServiceType singlePrivateACServiceType = hospitalServiceTypeRepository
                .findByHospitalIdAndPrivateAc(claimData.getHospitalId(), true);
        HospitalServiceType roomOpted = hospitalServiceTypeRepository.getByServiceTypeAndHospitalId(claimData.getRoomType(), claimData.getHospitalId());

        return PMLRequestDTO.builder()
                .claimNumber(claimData.getIntimationNumber())
                .product_code(claimData.getProductCode())
                .product_name(claimData.getProductCode())
                .uin_number("")
                .policy_version("")
                .policy_subplan("")
                .policy_plan("")
                .total_claim_amount(billTariffResponseDTO.getData().getBill_amounts().getTotal_bill_amount())
                .procedure_name(procedures != null ? procedures.getName() : "")
                .snomed_code(procedures != null ? procedures.getVneuronSctidCode() : "")
                .policy_base_sum_insured(claimData.getBaseSumInsured() != null ? claimData.getBaseSumInsured() : BigDecimal.ZERO)
                .policy_available_sum_insured(claimData.getRemainingSumInsured())
                .policy_inception_date(DateUtil.dateToString(claimData.getCurrentPolicyInceptionDate()))
                .date_of_admission(DateUtil.dateToString(claimData.getDateOfAdmission()))
                .date_of_discharge(DateUtil.dateToString(claimData.getDateOfDischarge()))
                .date_of_first_diagnosis(DateUtil.dateToString(DateUtil.getPastOrFutureDate(new Date(), -1)))
                .policy_start_date(DateUtil.dateToString(claimData.getCurrentPolicyStartDate()))
                .policy_end_date(DateUtil.dateToString(claimData.getCurrentPolicyEndDate()))
                .reason_for_hospitalization(claimData.getReasonForHospitalization())
                .tariff_room_rent(BigDecimal.ZERO)
                .date_of_birth(DateUtil.dateToString(claimData.getDateOfBirth()))
                .actual_age(claimData.getPatientAge())
                .age_at_inception(claimData.getPatientAge())
                .copay_zone(claimData.getCopayZone())
                .hospital_zone(claimData.getHospitalZone())
                .policy_renewal(getPolicyRenewalHistory(claimData.getPolicyRenewalHistory()))
                .benefit_groups(getBenefitGroupList(billTariffResponseDTO))
                .claim_room_type(claimData.getRoomType())
                .hospital_code(hospital.getCorporateCode())
                .claim_stage(claimData.getClaimStatus().toString())
                .treatment_type(claimData.getTreatmentType() != null
                        ? claimData.getTreatmentType().toString() : TreatmentType.MEDICAL.getValue())
                .pedList(new ArrayList<>())
                .claimDataId(claimData.getId())
                .singlePrivateACRoomName(singlePrivateACServiceType != null
                        ? singlePrivateACServiceType.getHospitalRoomName() : null)
                .singlePrivateACTariffRate(singlePrivateACServiceType != null
                        ? singlePrivateACServiceType.getRoomTariffDay() : BigDecimal.ZERO)
                .optedRoomTariffRate(roomOpted != null ? roomOpted.getRoomTariffDay() : BigDecimal.ZERO)
                .relationship("")
                .category("")
                .build();
    }

    /**
     * This method is purely for the pml to get the benefit group list.
     *
     * @param billTariffResponseDTO
     * @return
     */
    private List<BenefitDTO> getBenefitGroupList(BillTariffResponseDTO billTariffResponseDTO) {
        List<BenefitDTO> benefitGroupList = new ArrayList<>();

        if (billTariffResponseDTO != null && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getCategory_summary() != null
                && !billTariffResponseDTO.getData().getCategory_summary().isEmpty()) {
            for (CategorySummaryItem categorySummary : billTariffResponseDTO.getData().getCategory_summary()) {
                String categoryName = categorySummary.getCategory_name();
                String benefitCode;
                if (categoryName.equalsIgnoreCase("others")) {
                    benefitCode = "others";
                } else {
                    BenefitCodeMapping benefitCodeMapping = benefitCodeMappingRepository.findByCategory(categoryName);
                    benefitCode = (benefitCodeMapping != null) ? benefitCodeMapping.getBenefitCode() : null;
                }

                List<BillItemDataDTO> billItemDataDTOS = new ArrayList<>();
                for (LineItemsItem line_item : categorySummary.getLine_items()) {
                    if (line_item != null && line_item.getData() != null && line_item.getData().getTariff() != null
                            && line_item.getData().getTariff().isDeleted()) {
                        continue;
                    }

                    BillItemDataDTO billItemDataDTO = new BillItemDataDTO();
                    if (line_item != null && line_item.getData() != null) {
                        billItemDataDTO.setRow_id(line_item.getData().getRow_id());

                        if (line_item.getData().getDescription() != null && line_item.getData().getAmount() != null) {
                            billItemDataDTO.setBill_item(line_item.getData().getDescription().getValue());
                            billItemDataDTO.setBill_rate(new BigDecimal(line_item.getData().getAmount().getValue() != null
                                    && !line_item.getData().getAmount().getValue().isEmpty() ? line_item.getData().getAmount().getValue() : "0"));
                            billItemDataDTO.setRequested_amount(new BigDecimal(line_item.getData().getAmount().getValue() != null
                                    ? line_item.getData().getAmount().getValue() : "0"));
                        }
                        if (line_item.getData().getTariff() != null) {
                            billItemDataDTO.setRow_id(line_item.getData().getTariff().getRow_id());
                            if (line_item.getData().getTariff().isDeleted())
                                continue;

                            if (line_item.getData().getTariff().getInsurer_tariff_amount() != null) {
                                billItemDataDTO.setTariff_amount_actual_room(line_item.getData().getTariff().getInsurer_tariff_amount());
                            } else {
                                billItemDataDTO.setTariff_amount_actual_room(line_item.getData().getTariff().getAdmissible_amount());
                            }

                            if (line_item.getData().getTariff().getInsurer_tariff_rate() != null) {
                                billItemDataDTO.setTariff_rate_actual_room(line_item.getData().getTariff().getInsurer_tariff_rate());
                            } else {
                                billItemDataDTO.setTariff_rate_actual_room(new BigDecimal(line_item.getData().getRate().getValue()));
                            }

                            billItemDataDTO.setCost_depends_on_room_type(line_item.getData().getTariff().isCost_depends_on_room_type() ? "Y" : "N");
                            billItemDataDTO.setAllowed_units_by_proc_construct(line_item.getData().getTariff().getInsurer_unit() > 0
                                    ? line_item.getData().getTariff().getInsurer_unit() : (line_item.getData().getQuantity().getValue() != null
                                    ? Double.parseDouble(line_item.getData().getQuantity().getValue()) : 1));
                            billItemDataDTO.setMapped_hospital_room_type(line_item.getData().getTariff().getMapped_hospital_room_type());
                        }
                    }

                    billItemDataDTOS.add(billItemDataDTO);
                }

                BenefitDTO benefitDTO = BenefitDTO.builder()
                        .benefit_group(categoryName)
                        .benefit_code((benefitCode == null) ? "" : benefitCode)
                        .benefit_group_claimed_amount(getCategoryAdmissibleAmount(categorySummary))
                        .bill_item_data(billItemDataDTOS)
                        .build();
                benefitGroupList.add(benefitDTO);
            }
        }

        return benefitGroupList;
    }

    private BigDecimal getCategoryAdmissibleAmount(CategorySummaryItem categorySummary) {
        final BigDecimal[] admissibleAmount = {BigDecimal.ZERO};
        if (categorySummary != null || categorySummary.getLine_items() != null && categorySummary.getLine_items().size() > 0) {
            categorySummary.getLine_items().stream().filter(line_item -> line_item != null && line_item.getData() != null
                    && line_item.getData().getTariff() != null && !line_item.getData().getTariff().isDeleted()).forEach(line_item -> {
                if (line_item.getData().getTariff().isEdited() && line_item.getData().getTariff().getInsurer_amount() != null) {
                    admissibleAmount[0] = helperService.safeAdd(admissibleAmount[0], line_item.getData().getTariff().getInsurer_tariff_amount());
                } else {
                    admissibleAmount[0] = helperService.safeAdd(admissibleAmount[0], line_item.getData().getTariff().getAdmissible_amount());
                }
            });
        }

        log.info("Admissible amount for category: {} is {}", categorySummary.getCategory_name(), admissibleAmount[0]);
        return admissibleAmount[0];
    }

    private List<PolicyRenewalHistoryDTO> getPolicyRenewalHistory(String policyRenewalHistory) {
        return new ArrayList<>();
    }

    public PMLResponse getPMLResponseByClaimDataId(long claimId) {
        return pmlResponseRepository.findPMLResponseByClaimDataIdLatest(claimId);
    }

    public BillTariffPmlDto getPMLDataMerged(PMLResponse pmlResponse, BillTariffResponse billTariffResponse) {
        if (pmlResponse == null) {
            return null;
        } else {
            BillTariffPmlDto billTariffPmlDto = new BillTariffPmlDto();
            HashMap<String, BenefitDataDto> billDataDtoMapping = null;
            PMLResponseDTO pmlData = pmlResponse.getPmlResponseDTO();
            if (pmlData != null) {
                List<BenefitResult> benefitResultList = pmlData.getBenefit_results();
                if (benefitResultList != null && !benefitResultList.isEmpty()) {
                    billDataDtoMapping = new HashMap<>();
                    for (BenefitResult benefitResult : benefitResultList) {
                        billDataDtoMapping.put(benefitResult.getBenefit_group(), new BenefitDataDto(benefitResult));
                    }
                }
                List<BillItemResultDto> benefitItemList = pmlData.getBill_item_result();
                if (benefitItemList != null && !benefitItemList.isEmpty()) {
                    if (billDataDtoMapping == null)
                        billDataDtoMapping = new HashMap<>();
                    for (BillItemResultDto billItemResultDto : benefitItemList) {
                        String masterCategory = billItemResultDto.getMaster_category();
                        BenefitDataDto benefitDataDto = billDataDtoMapping.get(masterCategory);
                        benefitDataDto.addBillItemResultData(billItemResultDto);
                        benefitDataDto.addBillTariffPmlAmountData(billItemResultDto, benefitDataDto.getBenefitResult());
                        billDataDtoMapping.put(masterCategory, benefitDataDto);
                    }
                }
            }

            if (billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData() != null) {
                List<CategorySummaryItem> categorySummaryList = billTariffResponse.getBillTariffResponseDTO().getData().getCategory_summary();
                if (categorySummaryList != null && !categorySummaryList.isEmpty()) {
                    for (CategorySummaryItem categorySummary : categorySummaryList) {
                        String vitrayaMasterCategory = categorySummary.getCategory_name();
                        BenefitDataDto benefitDataDto = (billDataDtoMapping != null
                                && !billDataDtoMapping.isEmpty()) ? billDataDtoMapping.get(vitrayaMasterCategory) : null;
                        billTariffPmlDto.addBillTariffPmlAmountList(categorySummary, benefitDataDto);
                    }
                }
            }
            return billTariffPmlDto;
        }
    }

    public BillTariffPmlDto getPMLDataMergedV1(PMLResponse pmlResponse, BillTariffResponse billTariffResponse) {
        getUpdatedBillTariffResponseBasedOnInsurerEdit(billTariffResponse);
        BillTariffPmlDto billTariffPmlDto = new BillTariffPmlDto();
        if (pmlResponse != null && pmlResponse.getPmlResponseDTO() != null && pmlResponse.getPmlResponseDTO().getClaim_result() != null) {
            billTariffPmlDto.setPml_status(pmlResponse.getPmlResponseDTO() != null
                    && pmlResponse.getPmlResponseDTO().getClaim_result() != null ? PMLStatusEnum.SUCCESSFUL.name() : PMLStatusEnum.FAILURE.name());
            billTariffPmlDto.setTotal_tariff_admissible_amount(billTariffResponse != null
                    && billTariffResponse.getBillTariffResponseDTO() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData().getTariff_amounts() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData().getTariff_amounts().getTotal_admissible_amount() != null
                    ? billTariffResponse.getBillTariffResponseDTO().getData().getTariff_amounts().getTotal_admissible_amount() : BigDecimal.ZERO);
            billTariffPmlDto.setTotal_pml_admissible_amount(pmlResponse.getPmlResponseDTO() != null
                    && pmlResponse.getPmlResponseDTO().getClaim_result() != null
                    && pmlResponse.getPmlResponseDTO().getClaim_result().getClaim_approved_amount() != null
                    ? pmlResponse.getPmlResponseDTO().getClaim_result().getClaim_approved_amount() : BigDecimal.ZERO);
            BigDecimal requestedAmount = billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData().getBill_amounts() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData().getBill_amounts().getTotal_bill_amount() != null
                    ? billTariffResponse.getBillTariffResponseDTO().getData().getBill_amounts().getTotal_bill_amount() : BigDecimal.ZERO;

            BigDecimal finalPmlApprovedAmount = pmlResponse.getPmlResponseDTO() != null
                    && pmlResponse.getPmlResponseDTO().getClaim_result() != null
                    && pmlResponse.getPmlResponseDTO().getClaim_result().getClaim_approved_amount() != null
                    ? pmlResponse.getPmlResponseDTO().getClaim_result().getClaim_approved_amount() : BigDecimal.ZERO;

            billTariffPmlDto.setTotal_deduction(requestedAmount.subtract(finalPmlApprovedAmount));
            billTariffPmlDto.setTotal_bill_amount(billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData().getBill_amounts() != null
                    && billTariffResponse.getBillTariffResponseDTO().getData().getBill_amounts().getTotal_bill_amount() != null
                    ? billTariffResponse.getBillTariffResponseDTO().getData().getBill_amounts().getTotal_bill_amount() : BigDecimal.ZERO);

            List<BillTariffPmlAmountData> billTariffPmlAmountList = getBillTariffPmlAmountList(pmlResponse, billTariffResponse);
            billTariffPmlDto.setBillTariffPmlAmountList(billTariffPmlAmountList);
            billTariffPmlDto.setClaim_approved_amount_after_ucr_application(pmlResponse.getPmlResponseDTO().getClaim_result().getClaim_approved_amount_after_ucr_application() != null
                    ? pmlResponse.getPmlResponseDTO().getClaim_result().getClaim_approved_amount_after_ucr_application() : BigDecimal.ZERO);
            HashMap<String, BigDecimal> tariffNonPayableMap = getTariffNonPayableMap(billTariffResponse);
            billTariffPmlDto.setIrdaiNonPayableDeductionAmount(tariffNonPayableMap.get("irdai"));
            billTariffPmlDto.setNmeTariffDeductionAmount(tariffNonPayableMap.get("tariffNME"));
        }

        return billTariffPmlDto;
    }

    private HashMap<String, BigDecimal> getTariffNonPayableMap(BillTariffResponse billTariffResponse) {
        HashMap<String, BigDecimal> tariffNonPayableMap = new HashMap<>();
        BigDecimal tariffDeduction = BigDecimal.ZERO;
        BigDecimal irdaiDeduction = BigDecimal.ZERO;

        // IRDAI - Patient
        // Pharmacy - Hospital
        // Tariff - Hospital
        if (billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getData() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getLine_items() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getLine_items().size() > 0) {
            for (LineItemsItem lineItemsItem : billTariffResponse.getBillTariffResponseDTO().getData().getLine_items()) {
                if (lineItemsItem != null && lineItemsItem.getData() != null && lineItemsItem.getData().getTariff() != null) {
                    LineItemData data = lineItemsItem.getData();
                    if (data != null && data.getTariff() != null) {
                        BigDecimal savings = data.getTariff().getInsurer_savings() != null
                                ? data.getTariff().getInsurer_savings() : getTariffSavings(data);

                        if (data.getTariff().isIrdai_payable()) {
                            tariffDeduction = tariffDeduction.add(savings);
                        } else {
                            irdaiDeduction = irdaiDeduction.add(savings);
                        }
                    }
                }
            }
        }

        tariffNonPayableMap.put("irdai", irdaiDeduction);
        tariffNonPayableMap.put("tariffNME", tariffDeduction);
        return tariffNonPayableMap;
    }

    private BigDecimal getTariffSavings(LineItemData data) {
        return data != null && data.getAmount() != null && data.getTariff() != null
                ? helperService.safeSubtract(helperService.getSafeValue(data.getAmount().getValue()), data.getTariff().getAdmissible_amount())
                : BigDecimal.ZERO;
    }

    private void getUpdatedBillTariffResponseBasedOnInsurerEdit(BillTariffResponse billTariffResponse) {
        if (billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getData() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getCategory_summary() != null) {
            for (CategorySummaryItem categorySummaryItem : billTariffResponse.getBillTariffResponseDTO().getData().getCategory_summary()) {
                if (categorySummaryItem.getLine_items() != null) {
                    BigDecimal requestedAmount = BigDecimal.ZERO;
                    BigDecimal admissibleAmount = BigDecimal.ZERO;
                    BigDecimal deduction = BigDecimal.ZERO;

                    for (LineItemsItem lineItemsItem : categorySummaryItem.getLine_items()) {
                        if (lineItemsItem.getData() != null && lineItemsItem.getData().getTariff() != null) {
                            LineItemData lineItemData = lineItemsItem.getData();

                            requestedAmount = helperService.safeAddDefault(requestedAmount,
                                    lineItemData.getTariff().getInsurer_bill_amount(),
                                    helperService.getSafeValue(lineItemData.getAmount().getValue()));

                            admissibleAmount = helperService.safeAddDefault(admissibleAmount,
                                    lineItemData.getTariff().getInsurer_tariff_amount(),
                                    lineItemData.getTariff().getAdmissible_amount());

                            deduction = helperService.safeAddDefault(deduction,
                                    lineItemData.getTariff().getInsurer_savings(),
                                    helperService.safeSubtract(
                                            helperService.getSafeValue(lineItemData.getAmount().getValue()),
                                            lineItemData.getTariff().getAdmissible_amount()));
                        }
                    }

                    categorySummaryItem.setAdmissible_amount(admissibleAmount);
                    categorySummaryItem.setRequested_amount(requestedAmount);
                    categorySummaryItem.setDeductions(deduction);
                }
            }
        }
    }

    private List<BillTariffPmlAmountData> getBillTariffPmlAmountList(PMLResponse pmlResponse, BillTariffResponse billTariffResponse) {
        List<BillTariffPmlAmountData> billTariffPmlAmountDataList = null;

        if (billTariffResponse != null && billTariffResponse.getBillTariffResponseDTO() != null
                && billTariffResponse.getBillTariffResponseDTO().getData() != null
                && billTariffResponse.getBillTariffResponseDTO().getData().getCategory_summary() != null) {
            billTariffPmlAmountDataList = new ArrayList<>();

            for (CategorySummaryItem categorySummaryItem : billTariffResponse.getBillTariffResponseDTO().getData().getCategory_summary()) {
                BenefitResult benefitResult = getBenefitResult(pmlResponse, categorySummaryItem.getCategory_name());
                BillTariffPmlAmountData billTariffPmlAmountData = new BillTariffPmlAmountData();
                billTariffPmlAmountData.setCategory_name(categorySummaryItem.getCategory_name());
                billTariffPmlAmountData.setBill_requested_amount(categorySummaryItem.getRequested_amount().setScale(2, RoundingMode.HALF_UP));
                billTariffPmlAmountData.setTariff_admissible_amount(categorySummaryItem.getAdmissible_amount() != null ? categorySummaryItem.getAdmissible_amount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                billTariffPmlAmountData.setTariff_admissible_amount_without_procedure_construct(
                        categorySummaryItem.getAdmissible_amount_without_procedure_construct().setScale(2, RoundingMode.HALF_UP));
                billTariffPmlAmountData.setBenefit_group_admissible_amount(benefitResult != null && benefitResult.getBenefit_group_admissible_amount() != null
                        ? benefitResult.getBenefit_group_admissible_amount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                billTariffPmlAmountData.setBenefit_group_claimed_amount(benefitResult != null && benefitResult.getBenefit_group_claimed_amount() != null
                        ? benefitResult.getBenefit_group_claimed_amount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                billTariffPmlAmountData.setBenefit_covered(benefitResult != null && benefitResult.isBenefit_covered());

                BigDecimal billSavings = categorySummaryItem.getRequested_amount().subtract(categorySummaryItem.getAdmissible_amount());
                BigDecimal pmlSavings = benefitResult != null ? benefitResult.getBenefit_group_claimed_amount()
                        .subtract(benefitResult.getAmount_after_drop()) : BigDecimal.ZERO;
                billTariffPmlAmountData.setSavings(billSavings.add(pmlSavings).setScale(2, RoundingMode.HALF_UP));
                billTariffPmlAmountData.setBillItemList(getBillItemResultDtoList(getPmlLineItems(pmlResponse), categorySummaryItem));
                billTariffPmlAmountData.setTariffAmount(categorySummaryItem.getAdmissible_amount() != null
                        ? categorySummaryItem.getAdmissible_amount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                billTariffPmlAmountData.setMouApplicable(BigDecimal.ZERO); // ToDo fix in case we are using it.
                billTariffPmlAmountData.setAmountAfterMou(getAggregatedAmountAfterMou(billTariffPmlAmountData.getBillItemList())); // ToDo fix in case we are using it.
                billTariffPmlAmountData.setRoomProportionalDiscount(BigDecimal.ZERO); // ToDo fix in case we are using it.
                billTariffPmlAmountData.setAmountAfterRoomProportionalDiscount(categorySummaryItem.getAmount_after_procedure_construct() != null
                        ? categorySummaryItem.getAmount_after_procedure_construct().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                billTariffPmlAmountData.setLimit(benefitResult != null ? benefitResult.getLimitValue() : BigDecimal.ZERO);
                billTariffPmlAmountData.setCopay(benefitResult != null ? benefitResult.getCopay() : BigDecimal.ZERO);
                billTariffPmlAmountData.setAmountBeforeCopay(benefitResult != null ? benefitResult.getAmount_before_copay() : BigDecimal.ZERO);
                billTariffPmlAmountData.setFinalAmount(benefitResult != null ? benefitResult.getAmount_after_drop().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                billTariffPmlAmountData.setAmountAfterDrop(benefitResult != null ? benefitResult.getAmount_after_drop() : BigDecimal.ZERO);
                billTariffPmlAmountData.setRemarks(benefitResult != null && benefitResult.getRemarks() != null ? benefitResult.getRemarks() : getCategoryRemark(billTariffPmlAmountData.getBillItemList()));
                // Create a seprate method to set the mou, copay and proportional discount on category level
                billTariffPmlAmountData.setBillSavingsComponents();
                billTariffPmlAmountData.setPatientPayableDeduction(benefitResult != null ? benefitResult.getPatientPayableDeduction() : null);
                billTariffPmlAmountData.setHospitalPayableDeduction(benefitResult != null ? benefitResult.getHospitalPayableDeduction() : null);
                billTariffPmlAmountData.setUpdated(benefitResult != null && benefitResult.isUpdated());
                billTariffPmlAmountDataList.add(billTariffPmlAmountData);
            }
        }

        return billTariffPmlAmountDataList;
    }

    private String getCategoryRemark(List<BillItemResultDto> billItemList) {
        if (billItemList == null || billItemList.isEmpty()) {
            return "";
        }
        StringBuilder nmeCategoryRemarks = null;
        StringBuilder nonNMECategoryRemarks = null;
        for (BillItemResultDto billItem : billItemList) {
            if (billItem != null && billItem.getRemarks() != null && !billItem.getRemarks().isEmpty()) {
                if (billItem.isIrdaiPayable()
                        && billItem.getRequested_amount().compareTo(billItem.getAdmissible_amount()) > 0) {
                    if (nonNMECategoryRemarks == null) {
                        nonNMECategoryRemarks = new StringBuilder();
                    } else {
                        nonNMECategoryRemarks.append(", ");
                    }

                    nonNMECategoryRemarks
                            .append(billItem.getBill_item_name().trim())
                            .append("(charged PU Rs.")
                            .append(getRequestedPerUnitAmount(billItem)).append(") tariff rate PU (Rs.")
                            .append(getTariffPerUnitAmount(billItem)).append("),");
                } else if (!billItem.isIrdaiPayable()) {
                    if (nmeCategoryRemarks == null) {
                        nmeCategoryRemarks = new StringBuilder();
                    } else {
                        nmeCategoryRemarks.append(", ");
                    }
                    nmeCategoryRemarks
                            .append(billItem.getBill_item_name().trim())
                            .append(" (Rs.")
                            .append(billItem.getRequested_amount())
                            .append(") * ")
                            .append(billItem.getRequested_units())
                            .append(" items");
                } else {
                    if (nonNMECategoryRemarks == null) {
                        nonNMECategoryRemarks = new StringBuilder();
                    }
                    nonNMECategoryRemarks.append(billItem.getRemarks() != null ? billItem.getRemarks() : "actuals");
                }
            }
        }

        StringBuilder categoryRemarks = new StringBuilder("");
        if (nmeCategoryRemarks != null && !nmeCategoryRemarks.isEmpty()) {
            categoryRemarks.append("NME Deduction: ").append(nmeCategoryRemarks);
        }

        if (nonNMECategoryRemarks != null && !nonNMECategoryRemarks.isEmpty()) {
            if (!categoryRemarks.isEmpty()) {
                categoryRemarks.append(" and ");
            }

            categoryRemarks.append(nonNMECategoryRemarks);
        }

        log.info(categoryRemarks.toString());
        return categoryRemarks.toString();
    }

    private BigDecimal getTariffPerUnitAmount(BillItemResultDto billItem) {
        BigDecimal requestedPUAmount = billItem.getActual_amount();
        try {
            return billItem.getActual_amount()
                    .divide(BigDecimal.valueOf(billItem.getRequested_units()), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error while getting requested per unit tariff rate for bill item: {}", billItem, e);
        }

        return requestedPUAmount;
    }

    private BigDecimal getRequestedPerUnitAmount(BillItemResultDto billItem) {
        BigDecimal requestedPUAmount = billItem.getRequested_amount();
        try {
            return billItem.getRequested_amount()
                    .divide(BigDecimal.valueOf(billItem.getRequested_units()), 2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error while getting requested per unit amount for bill item: {}", billItem, e);
        }

        return requestedPUAmount;
    }

    private BigDecimal getAggregatedAmountAfterMou(List<BillItemResultDto> billItemList) {
        BigDecimal amountAfterMou = BigDecimal.ZERO;
        if (billItemList != null) {
            return billItemList.stream()
                    .map(BillItemResultDto::getAmount_after_mou_discount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return amountAfterMou;
    }

    private List<BillItemResultDto> getPmlLineItems(PMLResponse pmlResponse) {
        return pmlResponse != null && pmlResponse.getPmlResponseDTO() != null && pmlResponse.getPmlResponseDTO().getBill_item_result() != null
                ? pmlResponse.getPmlResponseDTO().getBill_item_result() : null;
    }

    private List<BillItemResultDto> getBillItemResultDtoList(List<BillItemResultDto> pmlBillItemResultDtos
            , CategorySummaryItem categorySummaryItem) {
        List<BillItemResultDto> billItemResultDtoList = null;
        if (pmlBillItemResultDtos != null) {
            for (BillItemResultDto pmlBillItemResultDto : pmlBillItemResultDtos) {
                if (pmlBillItemResultDto.getMaster_category().equalsIgnoreCase(categorySummaryItem.getCategory_name())) {
                    for (LineItemsItem lineItem : categorySummaryItem.getLine_items()) {
                        if (lineItem != null && lineItem.getData() != null
                                && pmlBillItemResultDto.getRow_id() == lineItem.getData().getRow_id()) {
                            String remarks = lineItem.getData().getTariff() != null ? lineItem.getData().getTariff().getRemarks() : "N/A";
                            if (remarks == null || remarks.isEmpty()) {
                                BigDecimal requestedAmount = new BigDecimal(lineItem.getData().getAmount().getValue());
                                BigDecimal admissibleAmount = lineItem.getData().getTariff().getAdmissible_amount();
                                BigDecimal perUnitRequestedAmount = lineItem.getPerUnitAmount();
                                BigDecimal perUnitTariffRate = lineItem.getPerUnitTariffRate();

                                String itemName = lineItem.getData().getDescription().getValue();

                                int unit = lineItem.getData().getQuantity().getValue() != null
                                        ? (int) Math.round(Double.parseDouble(lineItem.getData().getQuantity().getValue())) : 1;
                                if (!lineItem.getData().getTariff().isIrdai_payable()) {
                                    remarks = String.format("%s (Rs.%s) * %d items", itemName, requestedAmount, unit);
                                } else if (perUnitTariffRate.compareTo(perUnitRequestedAmount) < 0) {
                                    remarks = String.format("%s (charged PU Rs.%s) tariff rate PU (Rs.%s)",
                                            itemName, perUnitRequestedAmount, perUnitTariffRate);
                                }
                            }
                            lineItem.getData().getTariff().setRemarks(remarks);
                            pmlBillItemResultDto.setRemarks(lineItem.getData().getTariff().getRemarks());
                            pmlBillItemResultDto.setIrdaiPayable(lineItem.getData().getTariff().isIrdai_payable());
                            pmlBillItemResultDto.setRequested_units(helperService.getSafeIntValue(lineItem.getData().getQuantity().getValue()));
                            break;
                        }
                    }

                    if (billItemResultDtoList == null) billItemResultDtoList = new ArrayList<>();
                    billItemResultDtoList.add(pmlBillItemResultDto);
                }
            }
        }

        return billItemResultDtoList;
    }

    private BenefitResult getBenefitResult(PMLResponse pmlResponse, String categoryName) {
        if (pmlResponse != null && pmlResponse.getPmlResponseDTO() != null
                && pmlResponse.getPmlResponseDTO().getBenefit_results() != null) {
            for (BenefitResult benefitResults : pmlResponse.getPmlResponseDTO().getBenefit_results()) {
                if (benefitResults.getBenefit_group().equalsIgnoreCase(categoryName)) {
                    return benefitResults;
                }
            }
        }
        return null;
    }

    public void prepareAndProcessPMLRequest(BillTariffResponse billTariffResponse,
                                            BillTariffResponseDTO billTariffResponseDTO, ClaimModuleStats claimModuleStats,
                                            ClaimData claimData) {
        if (billTariffResponse == null) {
            log.info("Bill Tariff Response Data is empty");
            throw new VitrayaException(VitrayaErrorCodes.BILL_TARIFF_RESPONSE_NOT_FOUND);
        }

        if (claimCommonService.isClaimAdmissionDetailsUse(claimData, billTariffResponse)) {
            billTariffResponse = claimAdmissionService.getBillTariffResponseFromAdmission(claimData, billTariffResponse);
            billTariffResponseDTO = GsonUtils.fromJson(billTariffResponse.getBillTariffResponse(), BillTariffResponseDTO.class);
        }

        if (claimData == null) {
            log.info("Claim Data is empty for claim data id {}", billTariffResponse.getClaimDataId());
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        ClaimRunDTO claimRunDTO = ClaimRunDTO.builder()
                .claimId(claimData.getId())
                .claimRunIdentifier(ClaimRunIdentifier.FRESH_CLAIM_PML_RUN)
                .claimModuleStats(claimModuleStats)
                .build();
        makePMLRequest(billTariffResponseDTO, claimData, claimRunDTO);
    }

    private void makePMLRequest(BillTariffResponseDTO billTariffResponseDTO, ClaimData claimData, ClaimRunDTO claimRunDTO) {
        PMLRequestDTO pmlRequestDTO = preparePMLData(claimData, billTariffResponseDTO);
        Map<String, String> headers = Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer " + pmlAuthToken);

        try {
            if (!"Out Of Scope Policy".equalsIgnoreCase(pmlRequestDTO.getProduct_code())) {
                long startTime = System.currentTimeMillis();
                PMLResponseDTO pmlResponseDTO = null;
                int retryCount = 0;
                boolean isPmlExecuted = false;
                while (retryCount < MAX_REST_RETRY_COUNT) {
                    try {
                        log.info("Making PML request for claim data id: {} - request {}", claimData.getId(), GsonUtils.toJson(pmlRequestDTO));
                        pmlResponseDTO = restService.post(pmlServiceUrl, pmlRequestDTO,
                                PMLResponseDTO.class, headers);

                        //check for any upper limits on top of policy should apply
                        DocClaimStage docClaimStage = DocClaimStage.getStage(claimData.getClaimStatus());
                        if (docClaimStage != null && docClaimStage.equals(DocClaimStage.PRE_AUTH)) {
                            pmlResponseDTO = applyUpperLimitOnTopOfPMLResponse(claimData, pmlResponseDTO);
                        }
                        pmlResponseDTO = applyClientSpecificOverrides(claimData, pmlResponseDTO);

                        isPmlExecuted = true;
                        break;
                    } catch (HttpClientErrorException ehc) {
                        String responseBody = ehc.getResponseBodyAsString();
                        pmlResponseDTO = GsonUtils.fromJson(responseBody, PMLResponseDTO.class);
                        communicationService.sendEmail(claimData.getIntimationNumber() + " PML Validation Failure Response Received",
                                responseBody, CLAIM_REGISTRATION_FAILED_EMAIL_TO);
                        ErrorMsgType error = ErrorMsgType.PML_ENGINE_FAILURE;
                        errorMessageLogsService.saveErrorMessages(claimData, error);
                        log.info("Saving Error Message Logs for PML Failure in makePMLRequest method for {}", claimData.getIntimationNumber());
                        break;
                    } catch (Exception e) {
                        retryCount++;
                        if (retryCount >= MAX_REST_RETRY_COUNT) {
                            log.error("PML error after max retry attempt. Attempt: {}", retryCount, e);
                        } else {
                            log.info("Received error from PML service. Retrying... Attempt: {}", retryCount);
                        }

                        log.error("Caught an exception while making pml rest call as", e);
                    }
                }

                if (pmlResponseDTO != null) {
                    if (pmlResponseDTO.getClaim_result() == null) {
                        String emailBody = claimData.getIntimationNumber() + " PML Failed Response Received. Please find the details below.\n"
                                + GsonUtils.toJson(pmlResponseDTO);
                        communicationService.sendEmail(claimData.getIntimationNumber() + " PML Failed Response Received",
                                emailBody, CLAIM_REGISTRATION_FAILED_EMAIL_TO);
                        ErrorMsgType error = ErrorMsgType.PML_ENGINE_FAILURE;
                        errorMessageLogsService.saveErrorMessages(claimData, error);
                        log.info("Saving Error Message Logs for PML Failure in makePMLRequest method for {}", claimData.getIntimationNumber());
                    }

                    Long endTime = System.currentTimeMillis();
                    processPmlResponse(claimData, claimRunDTO, pmlResponseDTO, endTime, startTime);
                    claimCommonService.checkClaimModuleCompletion(claimData, isPmlExecuted);
                } else {
                    String emailBody = claimData.getIntimationNumber() + "PML Response Not Received";
                    communicationService.sendEmail(claimData.getIntimationNumber() + "PML Response Not Received",
                            emailBody, CLAIM_REGISTRATION_FAILED_EMAIL_TO);
                }

                // ToDo: Need to figure out a way to get the userId
                sseEmittersService.createAndSendNotification(null, "PML Execution Completed",
                        NotificationTypeEnum.PML_EXECUTION_NOTIFICATION);
            } else {
                // Now policy is out of scope we need to update the claim module stats
                claimCommonService.checkClaimModuleCompletion(claimData, true);
            }
        } catch (Exception e) {
            log.info("Caught an exception while processing pml request as", e);
        }
    }

    public void processPmlResponse(ClaimData claimData, ClaimRunDTO claimRunDTO, PMLResponseDTO pmlResponseDTO, Long endTime, Long startTime) {
        savePMLResponse(claimData, pmlResponseDTO, (endTime - startTime), claimRunDTO);
        ClaimModuleStats claimModuleStats = claimCommonService.recordClaimModuleStats(claimData, claimRunDTO.getClaimModuleStats(), ClaimModulesEnum.PML, claimData.getIntimationNumber(), (int) (endTime - startTime));
    }

    public boolean updatePmlRuleData(long claimDataId, List<BillTariffPmlAmountData> billTariffPmlAmountDataList) {
        ClaimData claimData = claimDataRepository.findByClaimDataId(claimDataId);
        if (claimData == null) {
            log.error("Claim data not found for claimDataId: {}", claimDataId);
            throw new VitrayaException(VitrayaErrorCodes.INVALID_CLAIM_DATA_ID);
        }

        PMLResponse pmlResponse = pmlResponseRepository.findPMLResponseByClaimDataIdLatest(claimDataId);
        if (pmlResponse == null) {
            log.error("PML response not found for claimDataId: {}", claimDataId);
            throw new VitrayaException(VitrayaErrorCodes.PML_DATA_NOT_FOUND);
        }

        BigDecimal totalInsurerDeduction = BigDecimal.ZERO;
        BigDecimal totalRequestedAmount = BigDecimal.ZERO;
        if (pmlResponse.getPmlResponseDTO() != null && pmlResponse.getPmlResponseDTO().getBenefit_results() != null) {
            for (BillTariffPmlAmountData billTariffPmlAmountData : billTariffPmlAmountDataList) {
                for (BenefitResult benefitResult : pmlResponse.getPmlResponseDTO().getBenefit_results()) {
                    if (benefitResult.getBenefit_group().equalsIgnoreCase(billTariffPmlAmountData.getCategory_name())) {
                        benefitResult.setPatientPayableDeduction(billTariffPmlAmountData.getPatientPayableDeduction());
                        benefitResult.setHospitalPayableDeduction(billTariffPmlAmountData.getHospitalPayableDeduction());
                        benefitResult.setUpdated(billTariffPmlAmountData.isUpdated());
                        benefitResult.setRemarks(billTariffPmlAmountData.getRemarks());
                        benefitResult.setBenefit_group_admissible_amount(billTariffPmlAmountData.getFinalAmount());
                        benefitResult.setAmount_after_drop(billTariffPmlAmountData.getFinalAmount());

                        if (benefitResult.getHospitalPayableDeduction() != null) {
                            totalInsurerDeduction = totalInsurerDeduction.add(benefitResult.getHospitalPayableDeduction());
                        }

                        if (benefitResult.getPatientPayableDeduction() != null) {
                            totalInsurerDeduction = totalInsurerDeduction.add(benefitResult.getPatientPayableDeduction());
                        }

                        totalRequestedAmount = totalRequestedAmount.add(billTariffPmlAmountData.getBill_requested_amount());
                    }
                }
            }

            pmlResponse.setPmlResponse(GsonUtils.toJson(pmlResponse.getPmlResponseDTO()));
            pmlResponseRepository.save(pmlResponse);
            claimData.setPushedToInsurer(false);
            claimDataRepository.save(claimData);
            return true;
        } else {
            log.error("PML benefit result data is empty for claimDataId: {}", claimDataId);
            throw new VitrayaException(VitrayaErrorCodes.PML_DATA_NOT_FOUND);
        }
    }


    public PMLResponseDTO applyUpperLimitOnTopOfPMLResponse(ClaimData claimData, PMLResponseDTO pmlResponseDTO) {

        InsurerSpecificLimitsService insurerSpecificLimitsService = this.insurerSpecificLimitsFactory.getInsurerSpecificLimits(claimData.getInsuranceAgencyId());

        if (insurerSpecificLimitsService != null) {

            Procedures procedures = procedureService.findProcedureById(claimData.getProcedureId()).orElse(null);
            Corporate hospital = corporateService.findCorporateById((int) claimData.getHospitalId());
            ClaimAdmissionDetails claimAdmissionDetails = claimAdmissionDetailsRepository.findByClaimId(claimData.getId());

            if (claimAdmissionDetails == null) {
                log.info("Claim {} does not have admission details, no upper limit will be applied", claimData.getId());
                return pmlResponseDTO;
            }

            if (claimAdmissionDetails.isPackage()) {
                log.info("Claim {} is a package claim, no upper limit will be applied", claimData.getId());
                return pmlResponseDTO;
            }

            if (procedures != null && hospital != null) {

                //step 1 : claim should be approved in order to limit get applied
                if (pmlResponseDTO.getClaim_result().isClaim_approved()) {
                    log.info("Claim {} is approved, checking for upper limit", claimData.getId());
                    BigDecimal upperLimit = insurerSpecificLimitsService.getUpperLimit(procedures.getProcedureCode(), hospital.getCorporateCode());
                    if (upperLimit != null && upperLimit.compareTo(BigDecimal.ZERO) > 0) {
                        log.info("Upper limit for claim {} is {}", claimData.getId(), upperLimit);
                        BigDecimal approvedAmount = pmlResponseDTO.getClaim_result().getClaim_approved_amount();
                        BigDecimal admissibleAmount = pmlResponseDTO.getClaim_result().getClaim_admissible_amount();
                        BigDecimal finalApprovedAmount = BigDecimal.ZERO;
                        NivaUcrClaims nivaUcrClaims = nivaUcrClaimsRepo.findLatestByClaimDataId(claimData.getId());
                        if (admissibleAmount.compareTo(upperLimit) < 0) {
                            log.info("Admissible amount for claim {} is within the upper limit of {}", claimData.getId(), upperLimit);
                            //admissible amount is less than the ucr limit so no changes on approved amount
                            String coverageReasonUpdate = "Admissible amount is within UCR limit of " + upperLimit.toString();
                            pmlResponseDTO.getClaim_result().getCoverageReasons().add(coverageReasonUpdate);
                            pmlResponseDTO.getClaim_result().setClaim_approved_amount_after_ucr_application(approvedAmount);
                            finalApprovedAmount = approvedAmount;
//                            nivaUcrClaims.setFinalApprovedAmount(approvedAmount);
                        } else if (admissibleAmount.compareTo(upperLimit) > 0) {
                            log.info("Admissible amount for claim {} is beyond the upper limit of {}", claimData.getId(), upperLimit);
                            //admissible amount is beyond the upper limit so approved amount will be capped till upper limit
                            BigDecimal updatedApprovedAmount = approvedAmount.min(upperLimit);
                            String coverageReasonUpdate = "Admissible amount is higher than UCR limit of Rs." + upperLimit.toString() + " hence approved amount capped till UCR limit.";
                            String claimResultUpdatedRemark = pmlResponseDTO.getClaim_result().getClaim_result_remarks() + ", UCR limit applied of Rs." + upperLimit.toString();
                            pmlResponseDTO.getClaim_result().getCoverageReasons().add(coverageReasonUpdate);
                            pmlResponseDTO.getClaim_result().setClaim_result_remarks(claimResultUpdatedRemark);
//                            pmlResponseDTO.getClaim_result().setClaim_approved_amount(updatedApprovedAmount);
                            pmlResponseDTO.getClaim_result().setClaim_approved_amount_after_ucr_application(updatedApprovedAmount);
                            finalApprovedAmount = updatedApprovedAmount;
//                            nivaUcrClaims.setFinalApprovedAmount(updatedApprovedAmount);
                        } else {
                            String coverageReasonUpdate = "Admissible amount is same as UCR limit of " + upperLimit.toString();
                            pmlResponseDTO.getClaim_result().getCoverageReasons().add(coverageReasonUpdate);
                            pmlResponseDTO.getClaim_result().setClaim_approved_amount_after_ucr_application(upperLimit);
                            finalApprovedAmount = upperLimit;
//                            nivaUcrClaims.setFinalApprovedAmount(upperLimit);
                        }
                        if (nivaUcrClaims == null) {
                            nivaUcrClaims = new NivaUcrClaims();
                            nivaUcrClaims.setClaimDataId(claimData.getId());
                            nivaUcrClaims.setUcrAmount(upperLimit);
                            nivaUcrClaims.setDateCreated(new Date());
                            nivaUcrClaims.setPmlApprovedAmount(approvedAmount);
                            nivaUcrClaims.setFinalApprovedAmount(finalApprovedAmount);
                            nivaUcrClaimsRepo.save(nivaUcrClaims);
                        } else {
                            nivaUcrClaims.setPmlApprovedAmount(approvedAmount);
                            nivaUcrClaims.setFinalApprovedAmount(finalApprovedAmount);
//                            nivaUcrClaims.setDateUpdated(new Date());
                            nivaUcrClaimsRepo.updateUcrEntry(nivaUcrClaims.getPmlApprovedAmount(), nivaUcrClaims.getFinalApprovedAmount(), nivaUcrClaims.getId());
                        }
                    } else {
                        log.info("No upper limit found for claim {} with procedure code {} and hospital code {}", claimData.getId(), procedures.getProcedureCode(), hospital.getCorporateCode());
                    }
                } else {
                    log.info("Claim {} is not approved, no upper limit will be applied", claimData.getId());
                }

            } else {
                log.info("Claim {} does not have procedure or hospital details, no upper limit will be applied", claimData.getId());
            }

        } else {
            log.info("Insurer specific limits service is not available for insurer {}", claimData.getInsuranceAgencyId());
        }

        return pmlResponseDTO;

    }

    public PMLResponseDTO applyClientSpecificOverrides(ClaimData claimData, PMLResponseDTO pmlResponseDTO) {


        if (pmlResponseDTO != null) {
            //step 1 : claim should be approved in order to limit get applied
            if (!pmlResponseDTO.getClaim_result().isClaim_approved()) {
                log.info("Claim {} is rejected, updating approved amount", claimData.getId());

                for (BenefitResult benefitResult : pmlResponseDTO.getBenefit_results()) {
                    BigDecimal updatedApprovedAmount = BigDecimal.ZERO;
                    benefitResult.setAmount_after_drop(updatedApprovedAmount);
                }

                for (BillItemResultDto benefitItemResult : pmlResponseDTO.getBill_item_result()) {
                    BigDecimal updatedApprovedAmount = BigDecimal.ZERO;
                    benefitItemResult.setFinal_amount(updatedApprovedAmount);
                    benefitItemResult.setFinal_amount_after_drop(updatedApprovedAmount);
                    benefitItemResult.setAmount_after_room_proportional_discount(updatedApprovedAmount);
                    benefitItemResult.setAmount_after_mou_discount(updatedApprovedAmount);
                }

            }
        }

        return pmlResponseDTO;

    }

    public HashMap<String, BigDecimal> getDeductibleMap(PMLResponse pmlResponse) {
        HashMap<String, BigDecimal> deductibleMap = new HashMap<>();
        BigDecimal patientPayableDeductable = BigDecimal.ZERO;
        BigDecimal hospitalPayableDeductable = BigDecimal.ZERO;
        if (pmlResponse != null && pmlResponse.getPmlResponseDTO() != null
                && pmlResponse.getPmlResponseDTO().getBenefit_results() != null
                && pmlResponse.getPmlResponseDTO().getBenefit_results().size() > 0) {
            for (BenefitResult benefitResult : pmlResponse.getPmlResponseDTO().getBenefit_results()) {
                if (benefitResult != null && benefitResult.getPatientPayableDeduction() != null) {
                    patientPayableDeductable = patientPayableDeductable.add(benefitResult.getPatientPayableDeduction());
                }

                if (benefitResult != null && benefitResult.getHospitalPayableDeduction() != null) {
                    hospitalPayableDeductable = hospitalPayableDeductable.add(benefitResult.getHospitalPayableDeduction());
                }
            }
        }

        deductibleMap.put("patientPayableDeductible", patientPayableDeductable);
        deductibleMap.put("hospitalPayableDeductible", hospitalPayableDeductable);
        return deductibleMap;
    }

}
