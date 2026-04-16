package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import com.google.gson.Gson;
import com.vitraya.adjudication.engine.dto.request.ClaimIllnessTreatmentDetailsDTO;
import com.vitraya.adjudication.engine.mysql.entity.ClaimAdjudicationResult;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.print.Doc;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Getter
@Setter
@Data
public class SettlementRequest {

    private DSClaimDetails DSClaimDetails;
    private TreatingDoctorDetails TreatingDoctorDetails;

    @Getter
    @Setter
    @ToString
    @Data
    public static class DSClaimDetails {
        private String ClaimedAmount;
        private String WDMSUniqueNo;
        private String PreauthId;
        private String VitrayaClaimID;     // initimation number
        private String ClaimType;
        private String InvoiceNumber;
        private String ProviderCode;
        private String NoOfInvoices;
        private String ActualRoomType;
        private String Date_Claim_Intimation;
        private String Date_Original_Doc_Received;
        private String Receipt_Complete_Documents;
        private String SourceSystem;
        private String ProviderPresentAmount;
        private String StatementPresentAmount;
        private String claimMode;

    }

    @Getter
    @Setter
    @ToString
    public static class TreatingDoctorDetails {

        private String Name;
        private String Qualification;
        private String DateOfSurgery;
    }

    public static SettlementRequest buildSettlementRequest(ClaimData claimData, Corporate hospital, String wdmsUniqueNo, ClaimAdjudicationResult claimAdjudicationResult) {
        DSClaimDetails DSClaimDetails = new DSClaimDetails();
        BigDecimal approvedAmount = BigDecimal.valueOf(0);
        BigDecimal claimedAmount = BigDecimal.valueOf(0);

        if (claimAdjudicationResult != null) {
            if (claimAdjudicationResult.getInsurerDischargeAmountApproved() != null) {
                approvedAmount = claimAdjudicationResult.getInsurerDischargeAmountApproved();
            } else if (claimAdjudicationResult.getDischargeAmountApproved() != null) {
                approvedAmount = claimAdjudicationResult.getDischargeAmountApproved();
            } else if (claimAdjudicationResult.getPreAuthInsurerAmountApproved() != null) {
                approvedAmount = claimAdjudicationResult.getPreAuthInsurerAmountApproved();
            } else if (claimAdjudicationResult.getPreAuthAmountApproved() != null) {
                approvedAmount = claimAdjudicationResult.getPreAuthAmountApproved();
            }

            if (claimAdjudicationResult.getDischargeBillAmount() != null) {
                claimedAmount = claimAdjudicationResult.getDischargeBillAmount();
            } else if (claimAdjudicationResult.getPreAuthBillAmount() != null) {
                claimedAmount = claimAdjudicationResult.getPreAuthBillAmount();
            }
        }

        ClaimIllnessTreatmentDetailsDTO.DoctorDto doctorDto = new Gson().fromJson(claimData.getDoctorDetailForSettlement(),
                ClaimIllnessTreatmentDetailsDTO.DoctorDto.class);
        log.info("Doctor Details: {} for settlement case for claimId: {}", new Gson().toJson(doctorDto), claimData.getId());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String date = simpleDateFormat.format(new Date());
        DSClaimDetails.setClaimedAmount(doctorDto != null ? doctorDto.getAmountToClaim() : claimedAmount.toString());
        DSClaimDetails.setPreauthId(claimData.getInsurerIdentifier());
        DSClaimDetails.setWDMSUniqueNo(wdmsUniqueNo);
        DSClaimDetails.setProviderCode(hospital.getCorporateCode());
        DSClaimDetails.setVitrayaClaimID(claimData.getIntimationNumber());
        DSClaimDetails.setClaimType("I");
        DSClaimDetails.setSourceSystem("M");
        DSClaimDetails.setActualRoomType("A");
        DSClaimDetails.setDate_Claim_Intimation(date);
        DSClaimDetails.setDate_Original_Doc_Received(date);
        DSClaimDetails.setReceipt_Complete_Documents(date);
        DSClaimDetails.setNoOfInvoices("1");
        DSClaimDetails.setProviderPresentAmount(approvedAmount.toString());
        DSClaimDetails.setStatementPresentAmount(approvedAmount.toString());
        DSClaimDetails.setInvoiceNumber("945600");
        DSClaimDetails.setClaimMode("Non-NHCX");

        TreatingDoctorDetails treatingDoctorDetails = new TreatingDoctorDetails();
        treatingDoctorDetails.setName(doctorDto != null ? doctorDto.getDoctorName() : "Treating doctor");
        treatingDoctorDetails.setQualification(doctorDto != null ? doctorDto.getQualification() : "MBBS");
        Date surgeryDate = null;
        Date fallbackDate = new Date();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            if (doctorDto != null
                    && doctorDto.getDateOfSurgery() != null
                    && !doctorDto.getDateOfSurgery().isEmpty()) {
                surgeryDate = inputFormat.parse(doctorDto.getDateOfSurgery());
            }
        } catch (Exception e) {
            log.info("exception caught parsing surgery date");
        }
        treatingDoctorDetails.setDateOfSurgery(simpleDateFormat.format(surgeryDate != null ? surgeryDate : fallbackDate));
        SettlementRequest settlementRequest = new SettlementRequest();
        settlementRequest.setDSClaimDetails(DSClaimDetails);
        settlementRequest.setTreatingDoctorDetails(treatingDoctorDetails);

        return settlementRequest;
    }
}
