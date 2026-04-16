package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.request.BillingReportRequest;
import com.vitraya.adjudication.engine.dto.response.BillingReportRow;
import com.vitraya.adjudication.engine.dto.response.ClaimDetailsResponseDTO;
import com.vitraya.adjudication.engine.mysql.entity.ClaimAdjudicationResult;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.ClaimTransition;
import com.vitraya.adjudication.engine.mysql.repository.ClaimAdjudicationRepository;
import com.vitraya.adjudication.engine.mysql.repository.ClaimDataRepository;
import com.vitraya.adjudication.engine.utils.DateUtil;
import com.vitraya.adjudication.engine.utils.ExcelHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingReportService {

    private final ClaimDataRepository claimDataRepository;
    private final ClaimService claimService;
    private final CommunicationService communicationService;
    private final ClaimAdjudicationService claimAdjudicationService;
    private final ClaimAdjudicationRepository claimAdjudicationRepository;

    @Value("${billing.report.email.to}")
    private String billingReportEmailTo;

    @Async
    public void generateAndEmailReportAsync(BillingReportRequest request) {
        // This method will be called asynchronously
        try {
            generateAndEmailReport(request);
        } catch (Exception e) {
            log.error("Error generating billing report: {}", e.getMessage(), e);
        }
    }

    private File generateAndEmailReport(BillingReportRequest request) throws IOException, ParseException {
        // Resolve date range
        Date startDate = resolveStartDate(request.getStartDate());
        Date endDate = resolveEndDate(request.getEndDate());

        // Fetch claim ids in range (using date_created)
        List<Long> claimIds = claimDataRepository.findIdsByDateCreatedBetween(startDate, endDate);

        // Build rows via prepareClaimData (caller will fill values later as needed)
        List<BillingReportRow> rows = new ArrayList<>();
        for (Long id : claimIds) {
            ClaimDetailsResponseDTO details = claimService.prepareClaimData(id);
            ClaimAdjudicationResult claimAdjudicationResult = claimAdjudicationRepository.findByClaimDataId(id);
//            if (id == 765) {
//                log.debug("Debug claim data found for id {}", id);
//            }
            BillingReportRow row = mapToRow(details, claimAdjudicationResult);
            rows.add(row);
        }

        // Excel file path
        String fileName = "billing-report-" + DateUtil.dateToString(new Date(), "yyyyMMdd-HHmmss") + ".xlsx";
        String outputPath = "/home/himalaya/Downloads/" + fileName;
//        String outputPath = System.getProperty("java.io.tmpdir") + "/reports/" + fileName;
        File excel = ExcelHelper.buildBillingReport(rows, outputPath);

        // Email with attachment using property
        if (billingReportEmailTo != null && !billingReportEmailTo.isBlank()) {
            String body = "Billing report generated for range: "
                    + DateUtil.dateToString(startDate) + " to " + DateUtil.dateToString(endDate);
            communicationService.sendEmailWithAttachment("Billing Report", body, billingReportEmailTo, excel);
        }

        return excel;
    }

    private Date resolveStartDate(String start) throws ParseException {
        if (start != null && !start.isBlank()) return DateUtil.stringToDate(start);
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate firstOfLastMonth = lastMonth.atDay(1);
        return java.sql.Date.valueOf(firstOfLastMonth);
    }

    private Date resolveEndDate(String end) throws ParseException {
        if (end != null && !end.isBlank()) return DateUtil.stringToDate(end);
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate lastOfLastMonth = lastMonth.atEndOfMonth();
        return java.sql.Date.valueOf(lastOfLastMonth);
    }

    // fetchClaimIdsByDateRange no longer needed due to repository method

    private BillingReportRow mapToRow(ClaimDetailsResponseDTO d, ClaimAdjudicationResult adjudicationResult) {
        // Prepare chronological transitions (oldest first)
        List<ClaimTransition> chronologicalTransitions = getChronologicalTransitions(d.getClaimTransitions());
        // Extract transition data for specific columns
        String claimSentToNivaDateTime = extractClaimSentToNivaDateTime(chronologicalTransitions);
        VitrayaStatusResult vitrayaStatusResult = extractVitrayaClaimStatus(chronologicalTransitions);
        String vitrayaClaimStatus = vitrayaStatusResult.status;
        String nivaClaimStatus = extractNivaClaimStatus(chronologicalTransitions, vitrayaStatusResult.index);
        BigDecimal billApprovedAmount = d.getBillApprovedAmount() != null ? d.getBillApprovedAmount() : BigDecimal.ZERO;
        BigDecimal tariffSaving = d.getClaimBillAmountRequested() != null ? d.getClaimBillAmountRequested().subtract(billApprovedAmount) : BigDecimal.ZERO;
        BigDecimal totalSaving = d.getSavings();
        BigDecimal irdaiNonPayableSaving = d.getTariffPmlDataMerged().getIrdaiNonPayableDeductionAmount() != null
                ? d.getTariffPmlDataMerged().getIrdaiNonPayableDeductionAmount() : BigDecimal.ZERO;
        BigDecimal nmeTariffSaving = d.getTariffPmlDataMerged().getNmeTariffDeductionAmount() != null
                ? d.getTariffPmlDataMerged().getNmeTariffDeductionAmount() :  BigDecimal.ZERO;

        // Safe calculations with null checks
        BigDecimal billRequestedAmount = safeGetBigDecimal(d.getBillRequestedAmount());
        BigDecimal amountAfterTariff = safeGetBigDecimal(d.getAmountAfterTariffApplication());
        BigDecimal amountAfterPolicyRules = safeGetBigDecimal(d.getAmountAfterPolicyrulesApplication());
        BigDecimal hospitalPayableDeductable = safeGetBigDecimal(d.getHospitalPayableDeductable());
        BigDecimal patientPayableDeductable = safeGetBigDecimal(d.getPatientPayableDeductable());
        BigDecimal tariffMatchPercentage = safeGetBigDecimal(d.getTariffMatchPercentage());
        
        // Calculate percentages safely
        BigDecimal vitrayaPureTariffSavingsPercentage = calculatePercentage(tariffSaving, billRequestedAmount);
        BigDecimal vitrayaNMESavingsPercentage = calculatePercentage(irdaiNonPayableSaving, totalSaving);
        BigDecimal vitrayaPharmacySavingsPercentage = calculatePercentage(nmeTariffSaving, totalSaving);
        BigDecimal vitrayaPmlSavingsAmount = amountAfterTariff.subtract(amountAfterPolicyRules);
        BigDecimal vitrayaPmlSavingsPercentage = calculatePercentage(vitrayaPmlSavingsAmount, totalSaving);
        
        // Build in-scope claim status safely
        String inscopeClaim = buildInscopeClaimStatus(d);
        
        return BillingReportRow.builder()
                .claimReceivedDateTime(formatDateTime(d.getDateCreated()))
                .claimCreatedDateTime(formatDateTime(d.getDateCreated()))
                .claimSentToNivaDateTime(claimSentToNivaDateTime)
                .vitrayaClaimId(safeGetString(d.getId()))
                .nivaPreauthId(safeGetString(d.getPreAuthId()))
                .hospitalCode(safeGetString(d.getHospitalCode()))
                .hospitalName(safeGetString(d.getHospitalName()))
                .patientName(safeGetString(d.getPatientName()))
                .policyName(safeGetString(d.getProductName()))
                .policyNumber(safeGetString(d.getPolicyNumber()))
                .procedure(safeGetString(d.getProcedureName()))
                .claimStage(safeGetString(d.getClaimStage()))
                .vitrayaClaimStatus(safeGetString(vitrayaClaimStatus))
                .nivaClaimStatus(safeGetString(nivaClaimStatus))
                .icdCode(safeGetString(d.getIcdCode()))
                .vitrayaApprovedAmount(amountAfterPolicyRules)
                .nivaApprovedAmount(adjudicationResult.getFinalInsurerAmountReversefeed())
                .differenceInApprovedAmount(BigDecimal.ZERO)
                .vitrayaSavingsAmount(totalSaving)
                .vitrayaSavingsPercentage(safeGetString(String.valueOf(d.getAmountDeductedInPercent())) + "%")
                .vitrayaPureTariffSavingsAmount(tariffSaving)
                .vitrayaPureTariffSavingsPercentage(vitrayaPureTariffSavingsPercentage)
                .vitrayaNMESavingsAmount(irdaiNonPayableSaving)
                .vitrayaNMESavingsPercentage(vitrayaNMESavingsPercentage)
                .vitrayaPharmacySavingsAmount(nmeTariffSaving)
                .vitrayaPharmacySavingsPercentage(vitrayaPharmacySavingsPercentage)
                .vitrayaPmlSavingsAmount(vitrayaPmlSavingsAmount)
                .vitrayaPmlSavingsPercentage(vitrayaPmlSavingsPercentage)
                .hospitalPayableDeductionsManuallyAddedByInsurer(hospitalPayableDeductable)
                .patientPayableDeductionsManuallyAddedByInsured(patientPayableDeductable)
                .inscopeClaim(inscopeClaim)
                .emailClaim(d.isEmailClaim())
                .amountMatchPercentage(tariffMatchPercentage)
                .hospitalRequestedAmount(billRequestedAmount)
                .build();
    }

    /**
     * Extract "Sent to Niva Successfully" date from claim transitions.
     * Look for the first occurrence after "Initial Request Received" in chronological order.
     */
    private String extractClaimSentToNivaDateTime(List<ClaimTransition> transitions) {
        if (transitions == null || transitions.isEmpty()) {
            return null;
        }

        boolean foundInitialRequest = false;
        for (ClaimTransition transition : transitions) {
            String status = transition.getStatus();
            if (status != null) {
                if (status.contains("Initial Request Received")) {
                    foundInitialRequest = true;
                } else if (foundInitialRequest && status.contains("Sent to Niva Successfully")) {
                    return transition.getDateCreated() != null ? 
                           DateUtil.dateToString(transition.getDateCreated(), "yyyy-MM-dd HH:mm:ss") : null;
                }
            }
        }
        return null;
    }

    /**
     * Extract Vitraya claim status from transitions.
     * Look for status containing "By vitraya" before the last "Sent to Niva Successfully".
     * Returns the status and its index to be used for subsequent insurer status search.
     */
    private VitrayaStatusResult extractVitrayaClaimStatus(List<ClaimTransition> transitions) {
        if (transitions == null || transitions.isEmpty()) {
            return new VitrayaStatusResult(null, -1);
        }

        // Find the last "Sent to Niva Successfully" position in chronological list
        int lastSentToNivaIndex = -1;
        for (int i = 0; i < transitions.size(); i++) {
            String status = transitions.get(i).getStatus();
            if (status != null && status.contains("Sent to Niva Successfully")) {
                lastSentToNivaIndex = i;
            }
        }

        if (lastSentToNivaIndex == -1) {
            return new VitrayaStatusResult(null, -1);
        }

        // Search backwards from the last "Sent to Niva Successfully" for the most recent "By vitraya"
        for (int i = lastSentToNivaIndex - 1; i >= 0; i--) {
            String status = transitions.get(i).getStatus();
            if (status != null && (status.contains("By Vitraya") || status.contains("By Niva"))) {
                return new VitrayaStatusResult(status, i);
            }
        }
        return new VitrayaStatusResult(null, -1);
    }

    /**
     * Extract Niva claim status from transitions.
     * Look for last status containing "(Insurer)" after the Vitraya claim status index.
     */
    private String extractNivaClaimStatus(List<ClaimTransition> transitions, int vitrayaStatusIndex) {
        if (transitions == null || transitions.isEmpty() || vitrayaStatusIndex < 0) {
            return "Pending";
        }

        String latestInsurerStatus = null;
        for (int i = vitrayaStatusIndex + 1; i < transitions.size(); i++) {
            String status = transitions.get(i).getStatus();
            if (status != null && status.contains("(Insurer)") && !status.contains("Acknowledgement")) {
                latestInsurerStatus = status;
            }
        }
        return latestInsurerStatus != null ? latestInsurerStatus : "Pending";
    }

    private List<ClaimTransition> getChronologicalTransitions(List<ClaimTransition> transitions) {
        if (transitions == null || transitions.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClaimTransition> list = new ArrayList<>(transitions);
        Collections.reverse(list);
        return list;
    }

    private static class VitrayaStatusResult {
        final String status;
        final int index;
        VitrayaStatusResult(String status, int index) {
            this.status = status;
            this.index = index;
        }
    }

    // Helper methods for safe operations
    private String safeGetString(String value) {
        return value != null ? value : "";
    }

    private BigDecimal safeGetBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatDateTime(Date date) {
        return date != null ? DateUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss") : null;
    }

    private BigDecimal calculatePercentage(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        try {
            return numerator.divide(denominator, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        } catch (Exception e) {
            log.warn("Error calculating percentage: numerator={}, denominator={}", numerator, denominator, e);
            return BigDecimal.ZERO;
        }
    }

    private String buildInscopeClaimStatus(ClaimDetailsResponseDTO d) {
        try {
            boolean inScopeHospital = d.isInScopeHospital();
            boolean inScopePolicy = d.isInScopePolicy();
            boolean inScopeProcedure = d.isInScopeProcedure();
            return (inScopeHospital && inScopePolicy && inScopeProcedure) ? "In Scope" : "Out Of Scope";
        } catch (Exception e) {
            log.warn("Error determining in-scope status", e);
            return "Unknown";
        }
    }
}


