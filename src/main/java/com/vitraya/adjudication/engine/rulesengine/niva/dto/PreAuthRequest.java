package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.enums.AdjudicationStatus;
import com.vitraya.adjudication.engine.dto.response.BillTariffPmlDto;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrClaims;
import com.vitraya.adjudication.engine.utils.DateUtil;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Data
@Slf4j
public class PreAuthRequest {
    private ClaimDetails ClaimDetails;
    private WorkItemChecks WorkItemChecks;
    private List<EpisodeDetails> EpisodeDetails;
    private List<QuestionnaireDetails> QuestionnaireDetails;
    private PreAuthRequest.PEDDetails PEDDetails;
    private AdjudicationResult AdjudicationResult;

    @Getter
    @Setter
    @ToString
    public static class ClaimDetails {
        private String PreauthId;
        private String PolicyNo_COI;
        private String MemberNo;
        private String PlanID;
        private String WDMSUniqueNo;
        private String VitrayaClaimID;
        private String EpisodeNo;
        private String ProviderCode;
        private String DeptIdFaxnumber;
        private String SourceSystem;
        private String IcdCode;
        private String ActualRoomType;
        private NivaRequestTypeEnum RequestType;
        private String ClaimType;
        private String TariffDifferential;
    }

    @Getter
    @Setter
    @ToString
    static class WorkItemChecks {
        private String Diagnosis; //checkbox
        private String DiagnosisName;
        private String TreatmentType; //checkbox
        private String TreatmentTypeName; //List<String>
        private String ProposedDateofAdmission;
        private String MedicalDataPresent; //checkbox
        private String ServiceCodeandDescription; //checkbox
        private String ServiceType; //checkbox
        private String RequestedAmount;
        private String DoctorSignature; //checkbox
        private String MemberSignature; //checkbox
        private String AlternatePhoneNo;
        private String AlternateEmail;
        private String InternationalCoverage; //checkbox
        private String DocumentsReceived; //checkbox
    }

    @Getter
    @Setter
    @ToString
    public static class QuestionnaireDetails {
        private String Question;
        private String Answer;
    }

    @Getter
    @Setter
    @ToString
    private static class PEDDetails {
        private String DateofFirstDiagnosis;
        private List<PastIllnessHistory> pastIllnessHistory;

        @Getter
        @Setter
        @ToString
        private static class PastIllnessHistory {
            private String ChronicIllnessName;
            private String DateofDiagnosis;
            private String Months;
        }
    }

    @Getter
    @Setter
    @ToString
    public static class AdjudicationResult {
        private String AdjudicationCategory;
        private String AdjudicationDecision;
        private String AdjudicationCategoryRemarks;
        private String AdjudicationRemarks;
        private String AdjudicationApprovedAmount;
    }

    public static PreAuthRequest buildPreAuthRequest(String wdmsUniqueNo, ClaimData claimData, BillTariffPmlDto billTariffPmlDto,
                                                     ClaimAdjudicationResult claimAdjudicationResult, List<EpisodeDetails> episodeDetails,
                                                     ServiceType serviceType, String roomType, boolean isUpdateFlow, NivaPolicyDataDTO nivaPolicyDataDTO,
                                                     Corporate hospital, Illnesses illnesses, NivaRequestData previousNivaRequestData, boolean isPreAuth, boolean isInterim, boolean isDischarge,
                                                     boolean isQueryReply, boolean isReconsideration, boolean isSettlement, boolean isRepush) {
        log.info("claim -{} preparing preauth request ", claimData.getId());
        PreAuthRequest preAuthRequest = new PreAuthRequest();

        preAuthRequest.setClaimDetails(getClaimDetailsDto(claimData, wdmsUniqueNo, claimData.getIntimationNumber(),
                roomType, nivaPolicyDataDTO, hospital));


        preAuthRequest.setWorkItemChecks(getWorkItemChecksDto(claimData, wdmsUniqueNo, isUpdateFlow, billTariffPmlDto,
                illnesses));

        preAuthRequest.setQuestionnaireDetails(null);
        List<ChronicIllnessDTO> chronicIllnessDTO = claimData.getPedList() != null ?
                GsonUtils.fromJsonToList(claimData.getPedList(), ChronicIllnessDTO.class) : null;
        preAuthRequest.setPEDDetails(getPedDetailsDto(claimData.getId(), chronicIllnessDTO, claimData.getDateOfFirstDiagnosis()));
        String upperLimitRuleMessage = "";

        if (isQueryReply || isReconsideration) {
            if (previousNivaRequestData != null) {
                PreAuthRequest previousPreAuthRequest = GsonUtils.fromJson(previousNivaRequestData.getRequestData(), PreAuthRequest.class);
                preAuthRequest.setAdjudicationResult(previousPreAuthRequest.getAdjudicationResult());
                preAuthRequest.setEpisodeDetails(previousPreAuthRequest.getEpisodeDetails());
            } else {
                log.info("Preauth request {} before episode details {}", preAuthRequest, episodeDetails);
                preAuthRequest.setEpisodeDetails(episodeDetails);
                log.info("Preauth request {} after episode details {}", preAuthRequest, episodeDetails);
                preAuthRequest.setAdjudicationResult(new AdjudicationResult());
            }
        } else {
            log.info("Preauth request {} before episode details {}", preAuthRequest, episodeDetails);
            preAuthRequest.setEpisodeDetails(episodeDetails);
            log.info("Preauth request {} after episode details {}", preAuthRequest, episodeDetails);
            preAuthRequest.setAdjudicationResult(getAdjudicationResultDto(claimAdjudicationResult, upperLimitRuleMessage, isPreAuth, isDischarge));
        }


        if (isRepush) {
            preAuthRequest.getClaimDetails().setRequestType(NivaRequestTypeEnum.Repush);
            if (isPreAuth) {
                preAuthRequest.getClaimDetails().setClaimType(String.valueOf(NivaClaimTypeEnum.NEW_PRE_AUTH.getClaimTypeValue()));
            } else if (isInterim) {
                preAuthRequest.getClaimDetails().setClaimType(String.valueOf(NivaClaimTypeEnum.EXTENSION_PRE_AUTH.getClaimTypeValue()));
            } else if (isDischarge) {
                preAuthRequest.getClaimDetails().setClaimType(String.valueOf(NivaClaimTypeEnum.DISCHARGE_PRE_AUTH.getClaimTypeValue()));
            } else if (isQueryReply) {
                preAuthRequest.getClaimDetails().setClaimType(String.valueOf(NivaClaimTypeEnum.PENDING_PRE_AUTH.getClaimTypeValue()));
            } else if (isReconsideration) {
                preAuthRequest.getClaimDetails().setClaimType(String.valueOf(NivaClaimTypeEnum.EXTENSION_PRE_AUTH.getClaimTypeValue()));
            }
        }

        log.info("claim -{} preauth request - {}", claimData.getId(), new Gson().toJson(preAuthRequest));
        return preAuthRequest;
    }

    private static ClaimDetails getClaimDetailsDto(ClaimData claimData, String wdmsUniqueNo, String intimationNumber,
                                                   String roomType, NivaPolicyDataDTO nivaPolicyDataDTO, Corporate hospital) {
        log.info("claim -{} preparing claimDetailsDto ", claimData.getId());

        ClaimDetails claimDetails = new ClaimDetails();
        claimDetails.setPreauthId(claimData.getInsurerIdentifier());
        claimDetails.setPolicyNo_COI(nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getPolicy_Number().substring(0, 8));
        claimDetails.setMemberNo(String.valueOf(claimData.getMemberNo()));
        claimDetails.setPlanID(nivaPolicyDataDTO.getPolicy().getPolicyHolderDetails().getPlanID());
        claimDetails.setWDMSUniqueNo(wdmsUniqueNo); // integrate WDMS api
        claimDetails.setVitrayaClaimID(intimationNumber); // integrate WDMS api
        claimDetails.setEpisodeNo("1");
        claimDetails.setProviderCode(hospital.getCorporateCode());
        claimDetails.setDeptIdFaxnumber("21/01244834111");
        claimDetails.setSourceSystem("M");
        if (claimData.getIcdCode() != null) {
            if (claimData.getIcdCode().contains("/")) {
                claimDetails.setIcdCode(claimData.getIcdCode().split("/")[0]);
            } else {
                claimDetails.setIcdCode(claimData.getIcdCode());
            }
        } else {
            claimDetails.setIcdCode("R50");
        }
        claimDetails.setActualRoomType(roomType);

        log.info("claim -{} claim details prepared for  preauth request data -{} ", claimData.getId(), new Gson().toJson(claimDetails));
        return claimDetails;
    }


    private static WorkItemChecks getWorkItemChecksDto(ClaimData claimData, String WDMSUniqueNo, boolean isUpdateFlow,
                                                       BillTariffPmlDto billTariffPmlDto, Illnesses illnesses) {
        log.info("claim -{} Preparing workitem checks  ", claimData.getId());


        WorkItemChecks workItemChecks = new WorkItemChecks();
        workItemChecks.setDiagnosis("1");
        workItemChecks.setDiagnosisName(illnesses != null && illnesses.getIllnessName() != null ? illnesses.getIllnessName()
                : "Other");
        workItemChecks.setTreatmentType("1");
        workItemChecks.setTreatmentTypeName("I");
        workItemChecks.setProposedDateofAdmission(DateUtil.parseDateInNivaFormat(claimData.getDateOfAdmission()));
        workItemChecks.setMedicalDataPresent("1");
        workItemChecks.setServiceCodeandDescription("0"); // non mandatory
        workItemChecks.setServiceType("0"); // non mandatory
        workItemChecks.setRequestedAmount(billTariffPmlDto.getTotal_bill_amount() != null ?
                String.valueOf(billTariffPmlDto.getTotal_bill_amount()) : "0");
        workItemChecks.setDoctorSignature("1");
        workItemChecks.setMemberSignature("1");
        workItemChecks.setAlternatePhoneNo(claimData.getAttendantMobileNumber());
        workItemChecks.setAlternateEmail("xyz@gmail.com");
        workItemChecks.setInternationalCoverage("0");
        workItemChecks.setDocumentsReceived(WDMSUniqueNo != null && !WDMSUniqueNo.equalsIgnoreCase("") ? "1" : "0"); // Validation - Document falg need to be verify with WDMS call
        log.info("claim -{} workItemChecks request data - {} ", claimData.getId(), new Gson().toJson(workItemChecks));

        return workItemChecks;
    }

    private static PreAuthRequest.PEDDetails getPedDetailsDto(long claimDataId, List<ChronicIllnessDTO> chronicIllnessDTO,
                                                              Date dateOfFirstDiagnosis) {
        PreAuthRequest.PEDDetails PEDDetails = new PEDDetails();
        PEDDetails.setDateofFirstDiagnosis(DateUtil.parseDateInNivaFormat(dateOfFirstDiagnosis));
        List<PreAuthRequest.PEDDetails.PastIllnessHistory> pastIllnessHistoryList = getPastIllnessHistoryDtoList(chronicIllnessDTO);

        PEDDetails.setPastIllnessHistory(pastIllnessHistoryList);
        log.info("claim -{} pedDetails request data {}", claimDataId, new Gson().toJson(PEDDetails));

        return PEDDetails;
    }

    private static List<PreAuthRequest.PEDDetails.PastIllnessHistory> getPastIllnessHistoryDtoList(List<ChronicIllnessDTO> chronicIllnessList) {
        List<PreAuthRequest.PEDDetails.PastIllnessHistory> pastIllnessHistoryList = new ArrayList<>();
        if (chronicIllnessList != null && !chronicIllnessList.isEmpty()) {
            for (ChronicIllnessDTO chronicIllnessDTO : chronicIllnessList) {
                PreAuthRequest.PEDDetails.PastIllnessHistory pastIllnessHistory = new PEDDetails.PastIllnessHistory();
                pastIllnessHistory.setChronicIllnessName(chronicIllnessDTO.getIllnessName());
                pastIllnessHistory.setDateofDiagnosis(chronicIllnessDTO.getDiagnosisDate() != null
                        ? DateUtil.parseDateInNivaFormat(chronicIllnessDTO.getDiagnosisDate()) : null);
                pastIllnessHistory.setMonths(String.valueOf(chronicIllnessDTO.getNumberOfMonths()));
                pastIllnessHistoryList.add(pastIllnessHistory);
            }
        }
        return pastIllnessHistoryList;
    }

    private static AdjudicationResult getAdjudicationResultDto(ClaimAdjudicationResult claimAdjudicationResult, String upperLimitRuleMessage,
                                                               boolean isPreAuth, boolean isDischarge) {
        String claimDecision = null;
        if (isPreAuth) {
            claimDecision = claimAdjudicationResult.getPreAuthInsurerDecision() != null
                    ? claimAdjudicationResult.getPreAuthInsurerDecision() : claimAdjudicationResult.getClaimDecision();

        } else if (isDischarge) {
            claimDecision = claimAdjudicationResult.getDischargeInsurerDecision() != null
                    ? claimAdjudicationResult.getDischargeInsurerDecision() : claimAdjudicationResult.getClaimDecision();

        }
        // If the option was chosen to push to insurer, then the decision should be "MANUAL" if we have not received a decision
        claimDecision = claimDecision != null ? claimDecision : AdjudicationStatus.MANUAL.getAdjudicationStatus();
        AdjudicationStatus adjudicationStatus = AdjudicationStatus.valueOf(claimDecision);
        PreAuthRequest.AdjudicationResult adjudicationResult = getAdjudicationResult(claimAdjudicationResult, adjudicationStatus, claimDecision, isPreAuth, isDischarge);
        log.info("adjudicationResult request data {}", adjudicationResult);
        return adjudicationResult;
    }

    private static PreAuthRequest.AdjudicationResult getAdjudicationResult(ClaimAdjudicationResult claimAdjudicationResult,
                                                                           AdjudicationStatus adjudicationStatus, String claimDecision,
                                                                           boolean isPreAuth, boolean isDischarge) {
        AdjudicationResult adjudicationResult = new AdjudicationResult();


        if (!(AdjudicationStatus.APPROVED.equals(adjudicationStatus)
                || AdjudicationStatus.REJECTED.equals(adjudicationStatus))) {
            adjudicationStatus = AdjudicationStatus.MANUAL;
        }

        adjudicationResult.setAdjudicationCategory(adjudicationStatus.getAdjudicationDecision());
        adjudicationResult.setAdjudicationDecision(claimDecision);
        adjudicationResult.setAdjudicationCategoryRemarks("-");
        adjudicationResult.setAdjudicationRemarks("-");

        if (AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimDecision)) {
            adjudicationResult.setAdjudicationApprovedAmount("0");
        } else {
            if (isPreAuth) {
                adjudicationResult.setAdjudicationApprovedAmount(
                        claimAdjudicationResult.getPreAuthInsurerAmountApproved() != null
                                ? claimAdjudicationResult.getPreAuthInsurerAmountApproved().toString()
                                : (claimAdjudicationResult.getPreAuthAmountApproved() != null
                                ? claimAdjudicationResult.getPreAuthAmountApproved().toString() : "0"));
            } else if (isDischarge) {
                adjudicationResult.setAdjudicationApprovedAmount(
                        claimAdjudicationResult.getInsurerDischargeAmountApproved() != null
                                ? claimAdjudicationResult.getInsurerDischargeAmountApproved().toString()
                                : (claimAdjudicationResult.getDischargeAmountApproved() != null
                                ? claimAdjudicationResult.getDischargeAmountApproved().toString() : "0"));
            }
        }

        return adjudicationResult;
    }

    public void groupEpisodeDetailsIntoSingleRow(BigDecimal preAuthAmountApproved,
                                                 ClaimAdjudicationResult claimAdjudicationResult, NivaUcrClaims nivaUcrClaims) {
        // Now group the episode details by service type, we need to add up the amount details.
        if (EpisodeDetails == null || EpisodeDetails.isEmpty()) {
            return;
        }

        BigDecimal calculatedApprovedAmount = BigDecimal.ZERO;
        EpisodeDetails groupedEpisodes = null;
        HashSet<String> noteSet = null;

        for (EpisodeDetails episode : EpisodeDetails) {
            if (groupedEpisodes == null) {
                groupedEpisodes = episode;
                getInPatientHospitalisationEpisodeDetails(groupedEpisodes);
            } else {
                updateEpisodeDetails(episode, groupedEpisodes);
            }

            if (episode.getNotes() != null && !episode.getNotes().isEmpty()) {
                if (noteSet == null) {
                    noteSet = new HashSet<>();
                }

                cleanNotes(episode);
                noteSet.add(episode.getNotes());
            }

            calculatedApprovedAmount = calculatedApprovedAmount.add(getBigDecimalValue(episode.getApprovedAmt()));
        }

        // Set noteSet values with comma separated values
        groupedEpisodes.setNotes(noteSet != null && !noteSet.isEmpty() ? String.join(", ", noteSet) : "");

        /*
        Check the below mentioned conditions.
        1. If the Insurer claim decision is REJECTED, then set the approved amount to 0 and reject amount to requested amount.
        2. If the Engine claim decision is REJECTED, then set the approved amount to 0 and reject amount to requested amount.
        3. If the Insurer claim decision is other than REJECTED, then check the requested amount, approved amount and reject amount.
           If the requested amount is not equal to the sum of approved and reject amounts, then adjust the difference to the approved amount.
         */

        boolean isRejected = false;
        if (claimAdjudicationResult.getPreAuthInsurerDecision() != null
                && AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getPreAuthInsurerDecision())) {
            isRejected = true;
            groupedEpisodes.setApprovedAmt("0");
            groupedEpisodes.setRejectAmt(groupedEpisodes.getRequestedAmt());
        } else if (claimAdjudicationResult.getPreAuthInsurerDecision() == null
                && AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getPreAuthDecision())) {
            isRejected = true;
            groupedEpisodes.setApprovedAmt("0");
            groupedEpisodes.setRejectAmt(groupedEpisodes.getRequestedAmt());
        }

        // If the requested amount is not equal to the sum of approved and reject amounts, then adjust the difference to the approved amount.
        BigDecimal requestedAmt = getBigDecimalFloorValue(groupedEpisodes.getRequestedAmt());
        BigDecimal approvedAmt = getBigDecimalFloorValue(groupedEpisodes.getApprovedAmt());
        BigDecimal rejectAmt = getBigDecimalFloorValue(groupedEpisodes.getRejectAmt());

        if (nivaUcrClaims != null
                && nivaUcrClaims.getFinalApprovedAmount() != null
                && nivaUcrClaims.getFinalApprovedAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("claim -{} is a ucr claim", claimAdjudicationResult.getClaimDataId());
            approvedAmt = getBigDecimalFloorValue(nivaUcrClaims.getFinalApprovedAmount().toString());
            rejectAmt = requestedAmt.compareTo(approvedAmt) > 0 ? requestedAmt.subtract(approvedAmt) : BigDecimal.ZERO;
            groupedEpisodes.setNotes(noteSet != null && !noteSet.isEmpty() ? String.join(", ", "recommended ucr applied.") : "recommended ucr applied.");
            calculatedApprovedAmount = approvedAmt;
        }

        if (requestedAmt.compareTo(approvedAmt.add(rejectAmt)) != 0) {
            approvedAmt = approvedAmt.add(requestedAmt.subtract(approvedAmt.add(rejectAmt)));
        }

        groupedEpisodes.setApprovedAmt(approvedAmt.toString());
        groupedEpisodes.setRejectAmt(rejectAmt.toString());

        // If the preAuthAmountApproved is not null and greater than 0, then ceil the preAuthAmountApproved value
        preAuthAmountApproved = preAuthAmountApproved.compareTo(BigDecimal.ZERO) == 0
                ? calculatedApprovedAmount
                : getBigDecimalFloorValue(preAuthAmountApproved.toString());

        if (!isRejected && preAuthAmountApproved != null && preAuthAmountApproved.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rejectAmtBD = requestedAmt.subtract(preAuthAmountApproved);
            groupedEpisodes.setApprovedAmt(preAuthAmountApproved.toString());
            groupedEpisodes.setRejectAmt(rejectAmtBD.toString());

            if (rejectAmt.compareTo(BigDecimal.ZERO) > 0) {
                if (groupedEpisodes.getNotes() == null || groupedEpisodes.getNotes().isEmpty()) {
                    groupedEpisodes.setNotes("Approved as per agreed tariff");
                }

                groupedEpisodes.setMedical("1");
                groupedEpisodes.setRejectQty(groupedEpisodes.getQTY());
                groupedEpisodes.setRejectionReason("116");
            }
        }

        if (groupedEpisodes.getNotes() == null || groupedEpisodes.getNotes().isEmpty()) {
            groupedEpisodes.setNotes("Approved as per agreed tariff");
        }

        // Assign groupedEpisodes to EpisodeDetails
        EpisodeDetails = new ArrayList<>();
        EpisodeDetails.add(groupedEpisodes);
    }

    private void getInPatientHospitalisationEpisodeDetails(EpisodeDetails groupedEpisodes) {
        // For In-Patient Hospitalisation, we need to set the below-mentioned values
        groupedEpisodes.setServiceType("10");
        groupedEpisodes.setBenType("A");
        groupedEpisodes.setBenHead("A9");
    }

    public void groupEpisodeDetailsByServiceType(BigDecimal dischargeAmountApproved, ClaimAdjudicationResult claimAdjudicationResult) {
        // Now group the episode details by service type, we need to add up the amount details.
        if (EpisodeDetails == null || EpisodeDetails.isEmpty()) {
            return;
        }
        // Grouping by ServiceType
        Map<String, EpisodeDetails> groupedEpisodes = new HashMap<>();
        Map<String, HashSet<String>> noteSetMap = new HashMap<>();

        for (EpisodeDetails episode : EpisodeDetails) {
            String serviceType = episode.getServiceType();
            if (!groupedEpisodes.containsKey(serviceType)) {
                EpisodeDetails episodeDetails = new EpisodeDetails(
                        serviceType,
                        episode.getRequestedAmt(),
                        episode.getQTY(),
                        episode.getApprovedAmt(),
                        episode.getCopayAmt(),
                        episode.getDeductibleAmt(),
                        episode.getDiscountAmt(),
                        "1".equalsIgnoreCase(episode.getMedical()) ? episode.getRejectQty() : "",
                        "1".equalsIgnoreCase(episode.getMedical()) ? episode.getRejectAmt() : "");

                episodeDetails.setServiceCode(episode.getServiceCode());
//                episodeDetails.setServiceTypeDescription(episode.getServiceTypeDescription());
                episodeDetails.setBedType(episode.getBedType());
                episodeDetails.setBenType(episode.getBenType());
                episodeDetails.setBenHead(episode.getBenHead());
                episodeDetails.setDateFrom(episode.getDateFrom());
                episodeDetails.setDateTo(episode.getDateTo());
                episodeDetails.setMPolicy(episode.getMPolicy());
                episodeDetails.setMedical(episode.getMedical());
                episodeDetails.setIneligible(episode.getIneligible());
                episodeDetails.setStatus(episode.getStatus());
                episodeDetails.setRejectionReason(episode.getRejectionReason());
                episodeDetails.setNotes(episode.getNotes());
                episodeDetails.setProcedure(episode.getProcedure());
                episodeDetails.setRejectQty(episode.getRejectQty());
                episodeDetails.setRejectAmt(episode.getRejectAmt());
                groupedEpisodes.put(serviceType, episodeDetails);

                HashSet<String> noteSet = new HashSet<String>();
                noteSet.add(episode.getNotes());
                noteSetMap.put(serviceType, noteSet);
            } else {
                // Merge values by summing up numeric fields
                EpisodeDetails existing = groupedEpisodes.get(serviceType);
                updateEpisodeDetails(episode, existing);

                HashSet<String> noteSet = noteSetMap.get(serviceType);
                cleanNotes(episode);
                noteSet.add(episode.getNotes());
                noteSetMap.put(serviceType, noteSet);
            }
        }

        for (Map.Entry<String, HashSet<String>> entry : noteSetMap.entrySet()) {
            String serviceType = entry.getKey();
            HashSet<String> noteSet = entry.getValue();
            groupedEpisodes.get(serviceType).setNotes(noteSet != null && !noteSet.isEmpty()
                    ? String.join(", ", noteSet) : "Approved as per agreed tariff");
        }

        // Now we need to loop over the groupedEpisodes map values and round off the values to floor
        BigDecimal requestedAmt = getBigDecimalFloorValue(this.getWorkItemChecks().getRequestedAmount());
        BigDecimal calculatedBillReqAmt = BigDecimal.ZERO;
        String serviceTypeWithHighestRequestAmount = null;
        BigDecimal highestRequestAmount = BigDecimal.ZERO;
        boolean isRejected = false;
        for (EpisodeDetails episode : groupedEpisodes.values()) {
            BigDecimal requestedAmtEpisode = getBigDecimalFloorValue(episode.getRequestedAmt());
            BigDecimal approvedAmtEpisode = getBigDecimalFloorValue(episode.getApprovedAmt());
            BigDecimal rejectedAmtEpisode = getBigDecimalFloorValue(episode.getRejectAmt());
            BigDecimal calculatedReqAmt = approvedAmtEpisode.add(rejectedAmtEpisode);

            /*
            Check the below-mentioned conditions.
            1. If the Insurer claim decision is REJECTED, then set the approved amount to 0 and reject amount to requested amount.
            2. If the Engine claim decision is REJECTED, then set the approved amount to 0 and reject amount to requested amount.
            3. If the Insurer claim decision is other than REJECTED, then check the requested amount, approved amount and reject amount.
               If the requested amount is not equal to the sum of approved and reject amounts, then adjust the difference to the approved amount.
             */

            if (claimAdjudicationResult.getDischargeInsurerDecision() != null
                    && AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getDischargeInsurerDecision())) {
                isRejected = true;
                episode.setApprovedAmt("0");
                episode.setRejectAmt(requestedAmtEpisode.toString());
                episode.setNotes("Deducted as per agreed terms and conditions");
            } else if (claimAdjudicationResult.getPreAuthInsurerDecision() == null
                    && AdjudicationStatus.REJECTED.getAdjudicationStatus().equalsIgnoreCase(claimAdjudicationResult.getPreAuthDecision())) {
                isRejected = true;
                episode.setApprovedAmt("0");
                episode.setRejectAmt(requestedAmtEpisode.toString());
                episode.setNotes("Deducted as per agreed terms and conditions");
            } else {
                if (requestedAmtEpisode.compareTo(calculatedReqAmt) != 0) {
                    approvedAmtEpisode = approvedAmtEpisode.add(requestedAmtEpisode.subtract(calculatedReqAmt));
                }

                episode.setRequestedAmt(requestedAmtEpisode.toString());
                episode.setApprovedAmt(approvedAmtEpisode.toString());
                episode.setRejectAmt(rejectedAmtEpisode.toString());
                calculatedBillReqAmt = calculatedBillReqAmt.add(requestedAmtEpisode);
                if (rejectedAmtEpisode.compareTo(BigDecimal.ZERO) < 0 &&
                        (episode.getNotes() == null || episode.getNotes().isEmpty())) {
                    episode.setNotes("Approved as per agreed tariff");
                }

                if (requestedAmtEpisode.compareTo(highestRequestAmount) > 0) {
                    highestRequestAmount = requestedAmtEpisode;
                    serviceTypeWithHighestRequestAmount = episode.getServiceType();
                }
            }
        }

        /*
         Now check if the calculated bill requested amount is equal to the requested amount in work item checks or not.
         If not then, adjust the difference to the episode details with ServiceType value as 07 approved amount.
         */
        if (!isRejected && calculatedBillReqAmt.compareTo(requestedAmt) != 0) {
            BigDecimal difference = requestedAmt.subtract(calculatedBillReqAmt);
            EpisodeDetails episodeDetails;
            if (groupedEpisodes.containsKey("07")) {
                episodeDetails = groupedEpisodes.get("07");
            } else {
                episodeDetails = groupedEpisodes.get(serviceTypeWithHighestRequestAmount);
            }

            /*
             Add the difference to the requested amount, and approved amount as difference always be positive
             */
            episodeDetails.setRequestedAmt(getBigDecimalValue(episodeDetails.getRequestedAmt()).add(difference).toString());
        }

        EpisodeDetails = new ArrayList<>(groupedEpisodes.values());
    }

    private void cleanNotes(EpisodeDetails episode) {
        if (episode.getNotes().equals("Approved as per agreed tariff.")) {
            episode.setNotes("Approved as per agreed tariff");
        }
    }

    private BigDecimal getBigDecimalFloorValue(String value) {
        BigDecimal valueBD = getBigDecimalValue(value);
        return BigDecimal.valueOf(Math.floor(valueBD.doubleValue())).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getBigDecimalValue(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? new BigDecimal(value) : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("Error parsing value: {}", value, e);
            return BigDecimal.ZERO;
        }
    }

    private void updateEpisodeDetails(EpisodeDetails episode, EpisodeDetails existing) {
        existing.setQTY(getSumOfNumbers(existing.getQTY(), episode.getQTY()));
        existing.setCopayAmt(getSumOfNumbers(existing.getCopayAmt(), episode.getCopayAmt()));
        existing.setDeductibleAmt(getSumOfNumbers(existing.getDeductibleAmt(), episode.getDeductibleAmt()));
        existing.setDiscountAmt(getSumOfNumbers(existing.getDiscountAmt(), episode.getDiscountAmt()));
        existing.setRejectQty(
                ("1".equalsIgnoreCase(episode.getMedical()) || !existing.getRejectQty().isEmpty())
                        ? getSumOfNumbers(existing.getRejectQty(), episode.getRejectQty()) : "");
        if (!existing.getQTY().isEmpty() && getIntValue(existing.getQTY()) > 1) {
            existing.setQTY("1");
        }
        if (!existing.getRejectQty().isEmpty() && getIntValue(existing.getRejectQty()) > 1) {
            existing.setRejectQty("1");
        }
        existing.setMedical(
                "1".equalsIgnoreCase(episode.getMedical()) || "1".equalsIgnoreCase(existing.getMedical()) ? "1" : "0");

        existing.setRequestedAmt(getSumOfNumbers(existing.getRequestedAmt(), episode.getRequestedAmt()));
        existing.setApprovedAmt(getSumOfNumbers(existing.getApprovedAmt(), episode.getApprovedAmt()));
        existing.setRejectAmt(
                "1".equalsIgnoreCase(episode.getMedical()) || !existing.getRejectQty().isEmpty()
                        ? getSumOfNumbers(existing.getRejectAmt(), episode.getRejectAmt()) : "");
        if (existing.getRejectionReason() == null || existing.getRejectionReason().isEmpty()
                && episode.getRejectionReason() != null && !episode.getRejectionReason().isEmpty()) {
            existing.setRejectionReason(episode.getRejectionReason());
        }
    }

    private int getIntValue(String qty) {
        int number = 0;
        try {
            number = Integer.parseInt(qty);
        } catch (Exception e) {
            log.error("Error parsing qty number: {}", qty);
        }

        return number;
    }

    private String getSumOfNumbers(String requestedAmt, String currentEpisodeAmount) {
        BigDecimal firstNumber = getValidBigDecimalValue(requestedAmt);
        BigDecimal secondNumber = getValidBigDecimalValue(currentEpisodeAmount);

        return (firstNumber.add(secondNumber)).toString();
    }

    private BigDecimal getValidBigDecimalValue(String requestedAmt) {
        try {
            return requestedAmt != null && !requestedAmt.trim().isEmpty() ? new BigDecimal(requestedAmt) : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public void checkAndCorrectData() {
        double totalRequestedAmount = 0.0;
        double totalApprovedAmount = 0.0;
        double totalRejectedAmount = 0.0;

        for (EpisodeDetails episode : this.EpisodeDetails) {
            double episodeRequestedAmount = Math.floor(episode.getRequestedAmt() != null ? getValidBigDecimalValue(episode.getRequestedAmt()).doubleValue() : 0.0);
            double episodeApprovedAmount = Math.floor(episode.getApprovedAmt() != null ? getValidBigDecimalValue(episode.getApprovedAmt()).doubleValue() : 0.0);
            double episodeRejectedAmount = Math.floor(episode.getRejectAmt() != null ? getValidBigDecimalValue(episode.getRejectAmt()).doubleValue() : 0.0);

            if (episode.getRejectAmt() != null && episode.getRejectAmt().equalsIgnoreCase("0.00")) {
                episode.setRejectAmt("");
                episode.setRejectQty("");
            }

            if (getValidBigDecimalValue(episode.getRequestedAmt()).doubleValue() != episodeRequestedAmount) {
                episode.setRequestedAmt(String.valueOf(Math.floor(episodeRequestedAmount)));
            }

            // We need to handle -ve request amount secenerio here.
            if (episodeRejectedAmount < 0) {
                log.info("Negative reject amount detected for episode: {}", episode);
                episodeRejectedAmount = episodeRequestedAmount - episodeApprovedAmount;
                if (episodeRejectedAmount > 0) {
                    episode.setRejectAmt(String.valueOf(Math.floor(episodeRejectedAmount)));
                    episode.setRejectQty(episode.getRejectQty() != null && !episode.getRejectQty().isEmpty()
                            ? episode.getRejectQty() : "1");
                } else {
                    episode.setRejectAmt("");
                    episode.setRejectQty("");
                }

                episode.setApprovedAmt(String.valueOf(Math.floor(episodeApprovedAmount)));
            }

            if (episodeRejectedAmount > 0 && episode.getRejectQty().equalsIgnoreCase("")) {
                episode.setRejectQty("1");
            }

            totalRequestedAmount += episodeRequestedAmount;
            totalRejectedAmount += episodeRejectedAmount;
            totalApprovedAmount += episodeApprovedAmount;

            // Check notes and set values if notes is an empty string
            if (episode.getNotes() == null || episode.getNotes().isEmpty()) {
                episode.setNotes("Approved as per agreed tariff");
            }
        }
        this.getWorkItemChecks().setRequestedAmount(String.valueOf(totalRequestedAmount));
        this.getAdjudicationResult().setAdjudicationApprovedAmount(String.valueOf(totalApprovedAmount));
    }
}
