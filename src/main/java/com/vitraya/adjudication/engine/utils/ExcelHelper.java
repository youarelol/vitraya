package com.vitraya.adjudication.engine.utils;

import com.vitraya.adjudication.engine.dto.response.BillingReportRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class ExcelHelper {

    private static final String[] HEADERS = new String[]{
            "Claim received time date", "Claim created time date", "Claim sent to niva date and time",
            "Vitraya Claim ID", "Niva Preauth ID", "Hospital code", "Hospital name", "Patient name",
            "Policy name", "Policy number", "Member number", "Procedure", "Claim stage", "Vitraya Claim status",
            "Niva Claim status", "Modification Remark", "ICD code", "Hospital Requested amount",
            "Vitraya Approved amount", "Niva Approved amount", "Difference in Approved amount",
            "Vitraya Savings Amount", "Vitraya Savings percentage", "Niva realised Savings amount",
            "Niva realised savings percentage", "Difference in savings amount", "Difference in savings percentage",
            "Vitraya Total Tariff savings amount", "Vitraya Total Tariff savings percentage",
            "Niva Total tariff savings amount", "Niva total tariff savings percentage",
            "Vitraya Pure tariff savings amount", "Vitraya Pure tariff savings percentage",
            "Niva Pure tariff savings amount", "Niva Pure tariff savings percentage",
            "Vitraya NME savings amount", "Vitraya NME savings percentage",
            "Niva NME savings amount", "Niva NME savings percentage",
            "Vitraya Pharmacy savings amount", "Vitraya Pharmacy savings percentage",
            "Niva Pharmacy savings amount", "Niva Pharmacy savings percentage",
            "Vitraya PML savings amount", "Vitraya PML savings percentage",
            "Niva PML savings amount", "Niva PML savings percentage",
            "Vitraya vNeuron savings amount", "Vitraya vNeuron savings percentage",
            "Niva vNeuron savings amount", "Niva vNeuron savings percentage",
            "Anonymous Savings", "Hospital Payable deductions manually added by Insurer",
            "Patient Payable Deductions manually added by insured", "Inscope Claim", "Email Claim", "Tariff Applied",
            "Amount Match Percentage"
    };

    public static File buildBillingReport(List<BillingReportRow> rows, String filePath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Billing Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (BillingReportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                setCell(row, col++, r.getClaimReceivedDateTime());
                setCell(row, col++, r.getClaimCreatedDateTime());
                setCell(row, col++, r.getClaimSentToNivaDateTime());
                setCell(row, col++, r.getVitrayaClaimId());
                setCell(row, col++, r.getNivaPreauthId());
                setCell(row, col++, r.getHospitalCode());
                setCell(row, col++, r.getHospitalName());
                setCell(row, col++, r.getPatientName());
                setCell(row, col++, r.getPolicyName());
                setCell(row, col++, r.getPolicyNumber());
                setCell(row, col++, r.getMemberNumber());
                setCell(row, col++, r.getProcedure());
                setCell(row, col++, r.getClaimStage());
                setCell(row, col++, r.getVitrayaClaimStatus());
                setCell(row, col++, r.getNivaClaimStatus());
                setCell(row, col++, r.getModificationRemark());
                setCell(row, col++, r.getIcdCode());
                setCell(row, col++, r.getHospitalRequestedAmount());
                setCell(row, col++, r.getVitrayaApprovedAmount());
                setCell(row, col++, r.getNivaApprovedAmount());
                setCell(row, col++, r.getDifferenceInApprovedAmount());
                setCell(row, col++, r.getVitrayaSavingsAmount());
                setCell(row, col++, r.getVitrayaSavingsPercentage());
                setCell(row, col++, r.getNivaRealisedSavingsAmount());
                setCell(row, col++, r.getNivaRealisedSavingsPercentage());
                setCell(row, col++, r.getDifferenceInSavingsAmount());
                setCell(row, col++, r.getDifferenceInSavingsPercentage());
                setCell(row, col++, r.getVitrayaTotalTariffSavingsAmount());
                setCell(row, col++, r.getVitrayaTotalTariffSavingsPercentage());
                setCell(row, col++, r.getNivaTotalTariffSavingsAmount());
                setCell(row, col++, r.getNivaTotalTariffSavingsPercentage());
                setCell(row, col++, r.getVitrayaPureTariffSavingsAmount());
                setCell(row, col++, r.getVitrayaPureTariffSavingsPercentage());
                setCell(row, col++, r.getNivaPureTariffSavingsAmount());
                setCell(row, col++, r.getNivaPureTariffSavingsPercentage());
                setCell(row, col++, r.getVitrayaNMESavingsAmount());
                setCell(row, col++, r.getVitrayaNMESavingsPercentage());
                setCell(row, col++, r.getNivaNMESavingsAmount());
                setCell(row, col++, r.getNivaNMESavingsPercentage());
                setCell(row, col++, r.getVitrayaPharmacySavingsAmount());
                setCell(row, col++, r.getVitrayaPharmacySavingsPercentage());
                setCell(row, col++, r.getNivaPharmacySavingsAmount());
                setCell(row, col++, r.getNivaPharmacySavingsPercentage());
                setCell(row, col++, r.getVitrayaPmlSavingsAmount());
                setCell(row, col++, r.getVitrayaPmlSavingsPercentage());
                setCell(row, col++, r.getNivaPmlSavingsAmount());
                setCell(row, col++, r.getNivaPmlSavingsPercentage());
                setCell(row, col++, r.getVitrayaVneuronSavingsAmount());
                setCell(row, col++, r.getVitrayaVneuronSavingsPercentage());
                setCell(row, col++, r.getNivaVneuronSavingsAmount());
                setCell(row, col++, r.getNivaVneuronSavingsPercentage());
                setCell(row, col++, r.getAnonymousSavings());
                setCell(row, col++, r.getHospitalPayableDeductionsManuallyAddedByInsurer());
                setCell(row, col++, r.getPatientPayableDeductionsManuallyAddedByInsured());
                setCell(row, col++, r.getInscopeClaim());
                setCell(row, col++, String.valueOf(r.isEmailClaim()));
                setCell(row, col++, r.getTariffApplied());
                setCell(row, col++, r.getAmountMatchPercentage());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            File outFile = new File(filePath);
            File parent = outFile.getParentFile();
            if (parent != null) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                workbook.write(fos);
            }
            return outFile;
        }
    }

    private static void setCell(Row row, int colIdx, String value) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value == null ? "" : value);
    }

    private static void setCell(Row row, int colIdx, BigDecimal value) {
        Cell cell = row.createCell(colIdx);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        } else {
            cell.setBlank();
        }
    }
}




