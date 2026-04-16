package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.enums.BillCodeEnum;
import com.vitraya.adjudication.engine.dto.request.CostEstimateDTO;
import com.vitraya.adjudication.engine.dto.request.EstimatesItem;
import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.mysql.entity.BillTariffResponse;
import com.vitraya.adjudication.engine.mysql.entity.ClaimAdmissionDetails;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import com.vitraya.adjudication.engine.mysql.repository.ClaimAdmissionDetailsRepository;
import com.vitraya.adjudication.engine.utils.DateUtil;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClaimAdmissionService {

    private final ClaimAdmissionDetailsRepository claimAdmissionDetailsRepository;
    private final DocumentService documentService;

    public ClaimAdmissionService(ClaimAdmissionDetailsRepository claimAdmissionDetailsRepository, DocumentService documentService) {
        this.claimAdmissionDetailsRepository = claimAdmissionDetailsRepository;
        this.documentService = documentService;
    }

    public void saveClaimAdmissionDetails(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData,
                                          ClaimData claimData) throws ParseException {
        if (isClaimAdmissionDetailsExist(vitrayaInsurerClaimData)) {
            boolean isAPackage = vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getCostEstimate() != null && vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getCostEstimate().getPackageAmount() != null
                    && BigDecimal.ZERO.compareTo(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getCostEstimate().getPackageAmount()) < 0;
            ClaimAdmissionDetails claimAdmissionDetails = ClaimAdmissionDetails.builder()
                    .claimDataId(claimData.getId())
                    .admissionDate(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getAdmissionDate()))
                    .dischargeDate(DateUtil.stringToDate(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getDischargeDate()))
                    .costEstimation(GsonUtils.toJson(vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getCostEstimate()))
                    .isPackage(isAPackage)
                    .packageAmount(isAPackage ? vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails().getCostEstimate().getPackageAmount() : new BigDecimal(0))
                    .dateCreated(new Date())
                    .txnId(vitrayaInsurerClaimData.getTxnId())
                    .build();

            claimAdmissionDetailsRepository.save(claimAdmissionDetails);
        }

    }

    private boolean isClaimAdmissionDetailsExist(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData) {
        return vitrayaInsurerClaimData != null
                && vitrayaInsurerClaimData.getRequest() != null
                && vitrayaInsurerClaimData.getRequest().getClaimAdmissionDetails() != null;
    }

    public BillTariffResponse getBillTariffResponseFromAdmission(ClaimData claimData, BillTariffResponse billTariffResponse) {
        ClaimAdmissionDetails claimAdmissionDetails = claimAdmissionDetailsRepository.findByClaimId(claimData.getId());
        if (claimAdmissionDetails != null) {
            if (billTariffResponse == null) {
                billTariffResponse = new BillTariffResponse();
            }

            BillTariffResponseDTO billTariffResponseDTO = new BillTariffResponseDTO();
            billTariffResponseDTO.setMessage("Hospital claimed line items details.");
            billTariffResponseDTO.setUnique_identifier(claimData.getIntimationNumber());
            billTariffResponseDTO.setData(getBillTariffResponseData(claimAdmissionDetails, claimData));
            billTariffResponseDTO.setBill_code(BillCodeEnum.BILL_RESPONSE_NOT_FOUND.name());
            billTariffResponseDTO.setResponse_code("DEFAULT_TARIFF_APPLIED");

            billTariffResponse.setBillTariffResponseDTO(billTariffResponseDTO);
            billTariffResponse.setBillTariffResponse(GsonUtils.toJson(billTariffResponseDTO));
        }

        return billTariffResponse;
    }

    private BillTariffResponseData getBillTariffResponseData(ClaimAdmissionDetails claimAdmissionDetails, ClaimData claimData) {
        BillTariffResponseData billTariffResponseData = new BillTariffResponseData();
        CostEstimateDTO costEstimateDTO = GsonUtils.fromJson(claimAdmissionDetails.getCostEstimation(), CostEstimateDTO.class);
        if (costEstimateDTO != null) {
            billTariffResponseData.setMetadata(getMetaData(claimData));
            billTariffResponseData.setLine_items(getLineItems(claimAdmissionDetails, costEstimateDTO));
            billTariffResponseData.setBill_amounts(getBillAmounts(costEstimateDTO));
            billTariffResponseData.setTariff_amounts(getTariffAmount(claimAdmissionDetails, costEstimateDTO));
            billTariffResponseData.setCategory_summary(getCategorySummary(billTariffResponseData.getLine_items()));
            billTariffResponseData.setAmounts_after_payables(null);
        }

        return billTariffResponseData;
    }

    private List<CategorySummaryItem> getCategorySummary(List<LineItemsItem> lineItems) {
        List<CategorySummaryItem> categorySummaryItems = new ArrayList<>();
        // Iterate over the lineItems and group them using VitrayaMasterCategory.value
        Map<String, List<LineItemsItem>> groupedLineItems = lineItems.stream()
                .collect(Collectors.groupingBy(item -> item.getData().getVitraya_master_category().getValue()));

        for (Map.Entry<String, List<LineItemsItem>> entry : groupedLineItems.entrySet()) {
            CategorySummaryItem categorySummaryItem = new CategorySummaryItem();
            List<LineItemsItem> lineItemsItems = entry.getValue();
            categorySummaryItem.setCategory_name(entry.getKey());
            categorySummaryItem.setLine_items(entry.getValue());

            BigDecimal requestedAmount = BigDecimal.ZERO;
            BigDecimal admissibleAmount = BigDecimal.ZERO;
            BigDecimal admissibleAmountWithoutProcedureConstruct = BigDecimal.ZERO;
            BigDecimal amountForIrdaiPayable = BigDecimal.ZERO;
            BigDecimal amountAfterProcedureConstruct = BigDecimal.ZERO;
            BigDecimal deductions = BigDecimal.ZERO;
            for (LineItemsItem lineItemsItem : lineItemsItems) {
                requestedAmount = requestedAmount.add(new BigDecimal(lineItemsItem.getData().getAmount().getValue()));
                admissibleAmount = admissibleAmount.add(lineItemsItem.getData().getTariff().getAdmissible_amount());
                admissibleAmountWithoutProcedureConstruct = admissibleAmountWithoutProcedureConstruct.add(lineItemsItem.getData().getTariff().getAdmissible_amount_without_procedure_construct());
                amountForIrdaiPayable = amountForIrdaiPayable.add(lineItemsItem.getData().getTariff().getAdmissible_amount());
                amountAfterProcedureConstruct = amountAfterProcedureConstruct.add(lineItemsItem.getData().getTariff().getAdmissible_amount());
                deductions = deductions.add(new BigDecimal(lineItemsItem.getData().getAmount().getValue()).add(lineItemsItem.getData().getTariff().getAdmissible_amount()));
            }

            categorySummaryItem.setRequested_amount(requestedAmount);
            categorySummaryItem.setAdmissible_amount(admissibleAmount);
            categorySummaryItem.setAdmissible_amount_without_procedure_construct(admissibleAmountWithoutProcedureConstruct);
            categorySummaryItem.setAmount_for_irdai_payable(amountForIrdaiPayable);
            categorySummaryItem.setAmount_after_procedure_construct(amountAfterProcedureConstruct);
            categorySummaryItem.setDeductions(deductions);

            categorySummaryItems.add(categorySummaryItem);
        }

        return categorySummaryItems;
    }

    private TariffAmounts getTariffAmount(ClaimAdmissionDetails claimAdmissionDetails, CostEstimateDTO costEstimateDTO) {
        TariffAmounts tariffAmounts = new TariffAmounts();
        BigDecimal authorisedAmount = BigDecimal.ZERO;
        if (claimAdmissionDetails.isPackage()) {
            authorisedAmount = costEstimateDTO.getTotalCost();
        } else {
            for (EstimatesItem estimatesItem : costEstimateDTO.getEstimates()) {
                authorisedAmount = authorisedAmount.add(estimatesItem.getEstimateAmount());
            }
        }

        tariffAmounts.setTotal_admissible_amount(authorisedAmount);
        return tariffAmounts;
    }

    private List<LineItemsItem> getLineItems(ClaimAdmissionDetails claimAdmissionDetails, CostEstimateDTO costEstimateDTO) {
        List<LineItemsItem> lineItemsItems = new ArrayList<>();
        if (costEstimateDTO != null) {
            int rowId = 0;
            // Now check do we have the package in the cost estimates.
            if (claimAdmissionDetails.isPackage()) {
                lineItemsItems.add(getPackageLineItemObj(claimAdmissionDetails.getPackageAmount(), rowId));
                rowId++;
            }

            if (costEstimateDTO.getEstimates() != null) {
                for (EstimatesItem estimatesItem : costEstimateDTO.getEstimates()) {
                    lineItemsItems.add(getLineItemsObj(estimatesItem, claimAdmissionDetails.getPackageAmount(),
                            false, rowId));
                    rowId++;
                }
            }
        }

        return lineItemsItems;
    }

    private LineItemsItem getPackageLineItemObj(BigDecimal packageAmount, int rowId) {
        LineItemsItem lineItemsItem = new LineItemsItem();
        LineItemData lineItemData = new LineItemData();
        Tariff tariff = new Tariff();
        tariff.setRemarks("Approved as per agreed tariff");
        tariff.setRow_id(rowId);
        tariff.setConfidence(100);
        tariff.setIrdai_payable(true);
        tariff.setTariff_amount(packageAmount);
        tariff.setTariff_matched(true);
        tariff.setTariff_line_item("Procedure Charges");
        tariff.setAdmissible_amount(packageAmount);
        tariff.setIncluded_in_package(true);
        tariff.setIncluded_in_procedure(true);
        tariff.setTariff_per_unit_amount(packageAmount);
        tariff.setAllowed_quantity_by_construct(BigDecimal.ONE);
        tariff.setAdmissible_amount_without_procedure_construct(packageAmount);
        tariff.setDeleted(false);
        tariff.setCost_depends_on_room_type(true);
        VitrayaMasterCategory vitrayaMasterCategory = new VitrayaMasterCategory();
        vitrayaMasterCategory.setValue("Package Charges");
        lineItemData.setRow_id(rowId);
        lineItemData.setVitraya_master_category(vitrayaMasterCategory);
        lineItemData.setTariff(tariff);
        ValueConfidenceDTO valueConfidenceDTODescription = new ValueConfidenceDTO("Package Charges", 100);
        lineItemData.setDescription(valueConfidenceDTODescription);

        ValueConfidenceDTO valueConfidenceDTOAmount = new ValueConfidenceDTO(packageAmount.toString(), 100);
        lineItemData.setAmount(valueConfidenceDTOAmount);

        ValueConfidenceDTO valueConfidenceDTOQuantity = new ValueConfidenceDTO("1", 100);
        lineItemData.setQuantity(valueConfidenceDTOQuantity);

        ValueConfidenceDTO valueConfidenceDTORate = new ValueConfidenceDTO(packageAmount.toString(), 100);
        lineItemData.setRate(valueConfidenceDTORate);

        lineItemsItem.setData(lineItemData);
        return lineItemsItem;
    }

    private LineItemsItem getLineItemsObj(EstimatesItem estimatesItem, BigDecimal packageAmount,
                                          boolean isPackage, int rowId) {
        BigDecimal tariffAmount = isPackage ? packageAmount : estimatesItem.getEstimateAmount();

        LineItemsItem lineItemsItem = new LineItemsItem();
        LineItemData lineItemData = new LineItemData();
        Tariff tariff = new Tariff();
        tariff.setRemarks(isPackage ? "Package charges" : "Approved as per agreed tariff");
        tariff.setRow_id(rowId);
        tariff.setConfidence(100);
        tariff.setIrdai_payable(true);
        tariff.setTariff_amount(tariffAmount);
        tariff.setTariff_matched(true);
        tariff.setTariff_line_item(isPackage ? "Procedure Charges" : estimatesItem.getCategoryName());
        tariff.setAdmissible_amount(isPackage ? packageAmount : tariffAmount);
        tariff.setIncluded_in_package(true);
        tariff.setIncluded_in_procedure(true);
        tariff.setTariff_per_unit_amount(getCostPerUnitAmount(estimatesItem));
        tariff.setAllowed_quantity_by_construct(isPackage ? BigDecimal.ONE : BigDecimal.valueOf(estimatesItem.getNumberOfUnits()));
        tariff.setAdmissible_amount_without_procedure_construct(tariff.getAdmissible_amount());
        tariff.setProcedure_construct_payable_boolean(true);
        tariff.setDeleted(false);
        tariff.setCost_depends_on_room_type(true);
        VitrayaMasterCategory vitrayaMasterCategory = new VitrayaMasterCategory();
        vitrayaMasterCategory.setValue(isPackage ? "Package Charges" : estimatesItem.getMasterCategoryName());
        lineItemData.setRow_id(rowId);
        lineItemData.setVitraya_master_category(vitrayaMasterCategory);
        lineItemData.setTariff(tariff);
        ValueConfidenceDTO valueConfidenceDTODescription = new ValueConfidenceDTO(isPackage
                ? "Package Charges" : estimatesItem.getCategoryName(), 100);
        lineItemData.setDescription(valueConfidenceDTODescription);

        ValueConfidenceDTO valueConfidenceDTOAmount = new ValueConfidenceDTO(estimatesItem.getEstimateAmount().toString(), 100);
        lineItemData.setAmount(valueConfidenceDTOAmount);

        ValueConfidenceDTO valueConfidenceDTOQuantity = new ValueConfidenceDTO(String.valueOf(estimatesItem.getNumberOfUnits()), 100);
        lineItemData.setQuantity(valueConfidenceDTOQuantity);

        ValueConfidenceDTO valueConfidenceDTORate = new ValueConfidenceDTO(isPackage
                ? "Package Charges" : tariff.getTariff_per_unit_amount().toString(), 100);
        lineItemData.setRate(valueConfidenceDTORate);

        lineItemsItem.setData(lineItemData);
        return lineItemsItem;
    }

    private BigDecimal getCostPerUnitAmount(EstimatesItem estimatesItem) {
        BigDecimal amountPerUnit = estimatesItem.getEstimateAmount();
        try {
            return (estimatesItem.getAuthorizedAmount() != null ? estimatesItem.getAuthorizedAmount() : estimatesItem.getEstimateAmount())
                    .divide(BigDecimal.valueOf(estimatesItem.getNumberOfUnits()),
                            RoundingMode.HALF_DOWN);
        } catch (Exception e) {
            log.error("Error while getting the cost per unit amount", e);
        }

        return amountPerUnit;
    }

    private BillAmounts getBillAmounts(CostEstimateDTO costEstimateDTO) {
        BillAmounts billAmounts = new BillAmounts();
        billAmounts.setTotal_bill_amount(costEstimateDTO.getTotalCost());
        billAmounts.setTotal_sum_of_line_items(costEstimateDTO.getTotalCost());
        billAmounts.setDifference_in_amount(BigDecimal.ZERO);
        return billAmounts;
    }

    private Metadata getMetaData(ClaimData claimData) {
        Metadata metadata = new Metadata();
        List<DocumentMaster> documentMasterList = documentService.getClaimDocumentMasterList(claimData.getIntimationNumber());
        // Iterate over documentMasterList and get the preSignedUrl and set to the metadata
        metadata.setBillS3UrlList(documentMasterList.stream().map(DocumentMaster::getPreSignedUrl).toList());
        metadata.setBill_s3_url(metadata.getBillS3UrlList().get(0));
        return metadata;
    }

    public ClaimAdmissionDetails getClaimAdmissionDetails(long id) {
        return claimAdmissionDetailsRepository.findByClaimId(id);
    }
}
