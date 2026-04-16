package com.vitraya.adjudication.engine.rulesengine.niva.dto;

import com.vitraya.adjudication.engine.dto.response.BillItemResultDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Slf4j
public class EpisodeDetails {
    private String ServiceCode;
    private String ServiceType; //List<String>
    private String ServiceTypeDescription;
    private String BedType; //List<String>
    private String BenType; //List<String>
    private String BenHead; //List<String>
    private String RequestedAmt;
    private String DateFrom;
    private String DateTo;
    private String QTY;
    private String ApprovedAmt;
    private String CopayAmt;
    private String DeductibleAmt;
    private String DiscountAmt;
    private String MPolicy; //checkbox
    private String Medical; //checkbox
    private String Ineligible;
    private String Status;
    private String RejectQty;
    private String RejectAmt;
    private String RejectionReason; //List<String>
    private String Notes;
    private String Procedure; //List<String>

    // Constructor
    public EpisodeDetails(String serviceType, String requestedAmt, String qty,
                          String approvedAmt, String copayAmt, String deductibleAmt,
                          String discountAmt, String rejectQty, String rejectAmt) {
        this.ServiceType = serviceType;
        this.RequestedAmt = requestedAmt;
        this.QTY = qty;
        this.ApprovedAmt = approvedAmt;
        this.CopayAmt = copayAmt;
        this.DeductibleAmt = deductibleAmt;
        this.DiscountAmt = discountAmt;
        this.RejectQty = "";
        this.RejectAmt = "";
    }

    public void setAmountComponentsWithNote(long claimDataId, BillItemResultDto billItemResultDto, BigDecimal patientDeduction,
                                            BigDecimal hospitalDeduction, boolean isRejected, BigDecimal rejectedAmount) {
        BigDecimal requestedAmount = billItemResultDto.getRequested_amount() != null
                ? billItemResultDto.getRequested_amount() : BigDecimal.ZERO;
        BigDecimal approvedAmountInitial = billItemResultDto.getFinal_amount_after_drop();
        BigDecimal approvedAmountFinal = approvedAmountInitial.subtract(patientDeduction).subtract(hospitalDeduction);
        BigDecimal rejectAmount = isRejected ? rejectedAmount : BigDecimal.ZERO;


        // Check 1. First check the mathematics of the requested amount, approved amount, and rejected amount
        if (requestedAmount.compareTo(approvedAmountFinal.add(rejectAmount)) != 0) {
            log.error("[Claim Data Id: {}] Amount mismatch inconsistency detected: Requested Amount: {}, " +
                            "Approved Amount: {}, Rejected Amount: {}", claimDataId, requestedAmount, approvedAmountFinal,
                    rejectAmount);

            // Now we will settle the difference in the approved amount
            BigDecimal difference = requestedAmount.subtract(approvedAmountFinal.add(rejectAmount));
            approvedAmountFinal = approvedAmountFinal.add(difference);
        }

        // Check 2. Ensure that we have added the notes in case of rejection
        if (isRejected && (this.getNotes() == null || this.getNotes().isEmpty())) {
            log.warn("[Claim Data Id: {}] Rejection detected but no notes provided. Adding default rejection note.", claimDataId);
            this.setNotes("Deduction as per agreed policy conditions.");
        }

        this.setRequestedAmt(requestedAmount.compareTo(BigDecimal.ZERO) != 0 ? requestedAmount.toString() : "0"); // Check 1
        this.setApprovedAmt(approvedAmountFinal.compareTo(BigDecimal.ZERO) != 0 ? approvedAmountFinal.toString() : "0"); // Check 2
        this.setRejectAmt(rejectedAmount.compareTo(BigDecimal.ZERO) != 0 ? rejectAmount.toString() : ""); // Check 3
    }
}
