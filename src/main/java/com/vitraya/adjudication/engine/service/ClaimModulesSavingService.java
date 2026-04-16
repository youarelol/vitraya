package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.dto.enums.BillSavingStatus;
import com.vitraya.adjudication.engine.dto.enums.ClaimRequestTypeEnum;
import com.vitraya.adjudication.engine.dto.enums.ClaimRunIdentifier;
import com.vitraya.adjudication.engine.dto.enums.ClaimStatus;
import com.vitraya.adjudication.engine.dto.response.*;
import com.vitraya.adjudication.engine.mongodb.repository.BillCategorySavingAuditRepository;
import com.vitraya.adjudication.engine.mongodb.repository.BillLineItemSavingAuditRepository;
import com.vitraya.adjudication.engine.mongodb.repository.ClaimBillModuleSavingAuditRepository;
import com.vitraya.adjudication.engine.mysql.entity.*;
import com.vitraya.adjudication.engine.mysql.entity.PMLResponse;
import com.vitraya.adjudication.engine.mysql.repository.BillLineItemSavingRepository;
import com.vitraya.adjudication.engine.mysql.repository.ClaimBillModuleSavingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClaimModulesSavingService {
    private final ClaimBillModuleSavingRepository billModuleSavingRepository;
    private final ClaimBillModuleSavingAuditRepository claimBillModuleSavingAuditRepository;
    private final UserService userService;
    private final BillCategorySavingAuditRepository billCategorySavingAuditRepository;
    private final BillLineItemSavingRepository billLineItemSavingRepository;
    private final BillLineItemSavingAuditRepository billLineItemSavingAuditRepository;
    private final HelperService helperService;
    private final ClaimBillModuleSavingRepository claimBillModuleSavingRepository;

    public ClaimModulesSavingService(ClaimBillModuleSavingRepository billModuleSavingRepository,
                                     ClaimBillModuleSavingAuditRepository claimBillModuleSavingAuditRepository,
                                     UserService userService,
                                     BillCategorySavingAuditRepository billCategorySavingAuditRepository,
                                     BillLineItemSavingRepository billLineItemSavingRepository,
                                     BillLineItemSavingAuditRepository billLineItemSavingAuditRepository,
                                     HelperService helperService, ClaimBillModuleSavingRepository claimBillModuleSavingRepository) {
        this.billModuleSavingRepository = billModuleSavingRepository;
        this.claimBillModuleSavingAuditRepository = claimBillModuleSavingAuditRepository;
        this.claimBillModuleSavingRepository = claimBillModuleSavingRepository;
        this.userService = userService;
        this.billCategorySavingAuditRepository = billCategorySavingAuditRepository;
        this.billLineItemSavingRepository = billLineItemSavingRepository;
        this.billLineItemSavingAuditRepository = billLineItemSavingAuditRepository;
        this.helperService = helperService;
    }

    /**
     * This method is used to prepare the module savings data for a claim.
     *
     * @param claimData          The claim data object.
     * @param billTariffResponse The bill tariff response object.
     * @param isEditFlow         Flag to indicate if it's an edit flow.
     */

//    @Transactional
//    public void prepareModuleSavingsData(ClaimData claimData, BillTariffResponse billTariffResponse, boolean isEditFlow) {
//        if (claimData == null) return;
//        ClaimRequestTypeEnum claimStage = determineClaimStage(claimData.getClaimStatus());
//
//        ClaimBillModuleSaving claimBillModuleSaving = (claimStage != null)
//                ? billModuleSavingRepository.getClaimBillModuleSaving(claimData.getId(), claimStage.toString())
//                : null;
//
//        // This flag is used to determine do we need to take the complete backup or the limited backup in audit table.
//        boolean isClaimRerun = claimBillModuleSaving != null;
//
////        List<BillCategorySaving> billCategorySavingList = getBillCategorySavingList(isClaimRerun, claimBillModuleSaving);
//
//        List<BillLineItemSaving> billLineItemSavingList = getBillLineItemSavingList(isClaimRerun, claimBillModuleSaving);
//
//        List<BillCategorySavingAudit> billCategorySavingAudits = new ArrayList<>();
//        List<BillLineItemSavingAudit> billLineItemSavingAudits = new ArrayList<>();
//        ClaimBillModuleSavingAudit claimBillModuleSavingAudit = null;
//
//        if (isEditFlow && claimBillModuleSaving != null) {
//            claimBillModuleSavingAudit = ClaimBillModuleSavingAudit.from(claimBillModuleSaving);
//            // ToDo: We need to add the logic to identify the specific line items where we have made changes.
//            prepareEditedLineItemData(billTariffResponse, billLineItemSavingList,
//                    billLineItemSavingAudits, billCategorySavingAudits, billCategorySavingList, claimData.getId());
//
//        } else if (isClaimRerun) {
//            claimBillModuleSavingAudit = ClaimBillModuleSavingAudit.from(claimBillModuleSaving);
//            claimBillModuleSaving.setRerunCount(claimBillModuleSaving.getRerunCount() + 1);
//
//            billCategorySavingAudits = getBillCategorySavingAuditData(billCategorySavingList);
//            billLineItemSavingAudits = getBillLineItemSavingAuditData(billLineItemSavingList);
//        } else {
//            claimBillModuleSaving = new ClaimBillModuleSaving();
//            claimBillModuleSaving.setClaimDataId(claimData.getId());
//        }
//
//        try {
//            prepareSavingData(billTariffResponse, claimBillModuleSaving, billCategorySavingList, billLineItemSavingList,
//                    isClaimRerun, isEditFlow, claimStage);
//            saveClaimSavingDataAndAuditData(claimBillModuleSaving, billCategorySavingList, billLineItemSavingList,
//                    claimBillModuleSavingAudit, billCategorySavingAudits, billLineItemSavingAudits);
//        } catch (Exception e) {
//            log.error("Exception occurred while preparing module savings data: {}", e.getMessage());
//            throw new RuntimeException("Error while preparing module savings data", e);
//        }
//    }

//    private ClaimSavingDTO prepareEditedLineItemData(BillTariffResponse billTariffResponse,
//                                                     List<BillLineItemSaving> billLineItemSavingList,
//                                                     List<BillLineItemSavingAudit> billLineItemSavingAudits,
//                                                     List<BillCategorySavingAudit> billCategorySavingAudits,
//                                                     List<BillCategorySaving> billCategorySavingList,
//                                                     long claimDataId) {
//        try {
//            ClaimSavingDTO claimSavingDTO = new ClaimSavingDTO();
//            expiredLineItemSavings(claimDataId);
//            List<LineItemsItem> lineItemsItems = billTariffResponse.getBillTariffResponseDTO().getData().getLine_items();
//            int rerunCount = billLineItemSavingList.getFirst().getRerunCount();
//            String changeCode = helperService.generateRandomString();
//            String userId = userService.getLoggedCurrentUser();
//            HashMap<String, BillSavingsDTO> categorySavingDTO = new HashMap<>();
//            BillSavingsDTO billSavingsDTO = new BillSavingsDTO();
//
//            for (LineItemsItem lineItem : lineItemsItems) {
//                int rowId = lineItem.getData().getTariff().getRow_id();
//                String itemName = lineItem.getData().getDescription().getValue();
//                if (itemName == null) continue;
//
//                Optional<BillLineItemSaving> existingLineItemOpt = getLineItemIfExist(billLineItemSavingList, rowId, itemName);
//                BillLineItemSaving updatedLineItemSaving = null;
//                if (existingLineItemOpt.isPresent()) {
//                    String internalRemark = isLineItemEdited(existingLineItemOpt.get(), lineItem);
//                    if (internalRemark != null) {
//                        BillLineItemSaving lineItemSaving = existingLineItemOpt.get();
//                        billLineItemSavingAudits.add(BillLineItemSavingAudit.from(lineItemSaving));
//                        addCategoryAuditIgnoreDuplicate(billCategorySavingList,lineItemSaving,billCategorySavingAudits);
//                        // Update the line item saving
//                        lineItemSaving.updateLineItemDetails(lineItem.getData(), rerunCount, changeCode, userId,
//                                true, internalRemark);
//                        updatedLineItemSaving = lineItemSaving;
//                    } else {
//                        updatedLineItemSaving = existingLineItemOpt.get();
//                    }
//                } else {
//                    if (lineItem.getData().getTariff().isAdded()) {
//
//                        BillLineItemSaving lineItemSaving = new BillLineItemSaving(lineItem, rerunCount, changeCode, userId,
//                                true);
//                        addCategoryAuditIgnoreDuplicate(billCategorySavingList,lineItemSaving,billCategorySavingAudits);
//                        billLineItemSavingList.add(lineItemSaving);
//                        updatedLineItemSaving = lineItemSaving;
//                    } else {
//                        log.error("Please debug this case, this is not supposed to be executed for line item: {}", lineItem);
//                    }
//
//                }
//            /*
//             Now we have made the required changes in the line item. Now based on the values we need to prepare the
//             category and claim level savings.
//             */
//
//                BillSavingsDTO billSavingsDTOCurrent;
//                if (updatedLineItemSaving != null) {
//                    billSavingsDTOCurrent = getBillSavingsDTO(updatedLineItemSaving, categorySavingDTO);
//                    categorySavingDTO.put(updatedLineItemSaving.getCategoryName(), billSavingsDTOCurrent);
//                    prepareBillSavingDTO(billSavingsDTO, updatedLineItemSaving);
//                }
//
//            }
//            return claimSavingDTO;
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("Exception Occurring while running prepareEditingData : {}", e.getMessage());
//            return null;
//        }
//    }

//    private void addCategoryAuditIgnoreDuplicate(List<BillCategorySaving> billCategorySavingList,BillLineItemSaving lineItemSaving,List<BillCategorySavingAudit> billCategorySavingAudits){
//        billCategorySavingList.stream()
//                .filter(item -> item.getCategoryName().equals(lineItemSaving.getCategoryName()))
//                .findFirst()
//                .map(BillCategorySavingAudit::from)
//                .ifPresent(audit -> {
//                    if (billCategorySavingAudits.stream().noneMatch(a -> a.getBillCatgorySavingId().equals(audit.getBillCatgorySavingId()))) {
//                        billCategorySavingAudits.add(audit);
//                    }
//                });
//    }

//    private void prepareBillSavingDTO(BillSavingsDTO billSavingsDTO, BillLineItemSaving updatedLineItemSaving) {
//
//        billSavingsDTO.setRequestedAmount(helperService.safeAdd(billSavingsDTO.getRequestedAmount(), updatedLineItemSaving.getRequestedAmount()));
//        billSavingsDTO.setApprovedAmount(helperService.safeAdd(billSavingsDTO.getApprovedAmount(), updatedLineItemSaving.getApprovedAmount()));
//        billSavingsDTO.setIrdaiPayableSaving(helperService.safeAdd(billSavingsDTO.getIrdaiPayableSaving(), updatedLineItemSaving.getNonPayableSavings()));
//        billSavingsDTO.setProcedureConstructSaving(helperService.safeAdd(billSavingsDTO.getProcedureConstructSaving(), updatedLineItemSaving.getConstructSavings()));
//        billSavingsDTO.setPharmacySaving(helperService.safeAdd(billSavingsDTO.getPharmacySaving(), updatedLineItemSaving.getPharmacySavings()));
//        billSavingsDTO.setPureTariffSaving(helperService.safeAdd(billSavingsDTO.getPureTariffSaving(), updatedLineItemSaving.getTariffSavings()));
//
//        billSavingsDTO.setInsurerRequestedAmount(helperService.safeAdd(billSavingsDTO.getInsurerRequestedAmount(), updatedLineItemSaving.getInsurerRequestedAmount()));
//        billSavingsDTO.setInsurerApprovedAmount(helperService.safeAdd(billSavingsDTO.getInsurerApprovedAmount(), updatedLineItemSaving.getInsurerApprovedAmount()));
//        billSavingsDTO.setInsurerIrdaiPayableSaving(helperService.safeAdd(billSavingsDTO.getInsurerIrdaiPayableSaving(), updatedLineItemSaving.getInsurerNonPayableSavings()));
//        billSavingsDTO.setInsurerProcedureConstructSaving(helperService.safeAdd(billSavingsDTO.getInsurerProcedureConstructSaving(), updatedLineItemSaving.getInsurerConstructSavings()));
//        billSavingsDTO.setInsurerPharmacySaving(helperService.safeAdd(billSavingsDTO.getInsurerPharmacySaving(), updatedLineItemSaving.getInsurerPharmacySavings()));
//        billSavingsDTO.setInsurerPureTariffSaving(helperService.safeAdd(billSavingsDTO.getInsurerPureTariffSaving(), updatedLineItemSaving.getInsurerTariffSavings()));
//
//    }
//
//    private BillSavingsDTO getBillSavingsDTO(BillLineItemSaving updatedLineItemSaving, HashMap<String, BillSavingsDTO> categorySavingDTO) {
//        BillSavingsDTO billSavingsDTOCurrent = new BillSavingsDTO();
//        try {
//            if (updatedLineItemSaving != null
//                    && categorySavingDTO.containsKey(updatedLineItemSaving.getCategoryName())) {
//                billSavingsDTOCurrent = categorySavingDTO.get(updatedLineItemSaving.getCategoryName());
//            } else {
//
//                return billSavingsDTOCurrent;
//            }
//
//            billSavingsDTOCurrent.setRequestedAmount(helperService.safeAdd(billSavingsDTOCurrent.getRequestedAmount(), updatedLineItemSaving.getRequestedAmount()));
//            billSavingsDTOCurrent.setApprovedAmount(helperService.safeAdd(billSavingsDTOCurrent.getApprovedAmount(), updatedLineItemSaving.getApprovedAmount()));
//            billSavingsDTOCurrent.setIrdaiPayableSaving(helperService.safeAdd(billSavingsDTOCurrent.getIrdaiPayableSaving(), updatedLineItemSaving.getNonPayableSavings()));
//            billSavingsDTOCurrent.setProcedureConstructSaving(helperService.safeAdd(billSavingsDTOCurrent.getProcedureConstructSaving(), updatedLineItemSaving.getConstructSavings()));
//            billSavingsDTOCurrent.setPharmacySaving(helperService.safeAdd(billSavingsDTOCurrent.getPharmacySaving(), updatedLineItemSaving.getPharmacySavings()));
//            billSavingsDTOCurrent.setPureTariffSaving(helperService.safeAdd(billSavingsDTOCurrent.getPureTariffSaving(), updatedLineItemSaving.getTariffSavings()));
//
//            billSavingsDTOCurrent.setInsurerRequestedAmount(helperService.safeAdd(billSavingsDTOCurrent.getInsurerRequestedAmount(), updatedLineItemSaving.getInsurerRequestedAmount()));
//            billSavingsDTOCurrent.setInsurerApprovedAmount(helperService.safeAdd(billSavingsDTOCurrent.getInsurerApprovedAmount(), updatedLineItemSaving.getInsurerApprovedAmount()));
//            billSavingsDTOCurrent.setInsurerIrdaiPayableSaving(helperService.safeAdd(billSavingsDTOCurrent.getInsurerIrdaiPayableSaving(), updatedLineItemSaving.getInsurerNonPayableSavings()));
//            billSavingsDTOCurrent.setInsurerProcedureConstructSaving(helperService.safeAdd(billSavingsDTOCurrent.getInsurerProcedureConstructSaving(), updatedLineItemSaving.getInsurerConstructSavings()));
//            billSavingsDTOCurrent.setInsurerPharmacySaving(helperService.safeAdd(billSavingsDTOCurrent.getInsurerPharmacySaving(), updatedLineItemSaving.getInsurerPharmacySavings()));
//            billSavingsDTOCurrent.setInsurerPureTariffSaving(helperService.safeAdd(billSavingsDTOCurrent.getInsurerPureTariffSaving(), updatedLineItemSaving.getInsurerTariffSavings()));
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            log.error("Exception Occuring while executing getBillSavingsDTO : {}", ex.getMessage());
//        }
//
//        return billSavingsDTOCurrent;
//    }
//
//    private String isLineItemEdited(BillLineItemSaving billLineItemSaving, LineItemsItem lineItemNew) {
//        String internalRemark = null;
//        try {
//            if (lineItemNew != null
//                    && lineItemNew.getData() != null
//                    && lineItemNew.getData().getTariff() != null
//                    && billLineItemSaving.getRowId() == lineItemNew.getData().getTariff().getRow_id()
//                    && billLineItemSaving.getLineItem().trim().equalsIgnoreCase(lineItemNew.getData().getDescription().getValue().trim())) {
//
//                if (billLineItemSaving.isDeleted() != lineItemNew.getData().getTariff().isDeleted()) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Item is Deleted");
//                }
//                if (!billLineItemSaving.getCategoryName().equalsIgnoreCase(lineItemNew.getData().getVitraya_master_category().getValue())) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Category has changed from " + billLineItemSaving.getCategoryName() + " to " + lineItemNew.getData().getVitraya_master_category().getValue());
//                }
//                if (billLineItemSaving.getUnit() != Double.parseDouble(lineItemNew.getData().getQuantity().getValue())) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Unit has changed from " + billLineItemSaving.getUnit() + " to " + (int) Float.parseFloat(lineItemNew.getData().getQuantity().getValue()));
//                }
//                if (billLineItemSaving.getInsurerUnit() != lineItemNew.getData().getTariff().getInsurer_unit()) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Insurer unit has changed from " + billLineItemSaving.getInsurerUnit() + " to " + lineItemNew.getData().getTariff().getInsurer_unit());
//                }
//                if (!helperService.areEqualTreatNullAsZero(billLineItemSaving.getRequestedAmount(), lineItemNew.getData().getAmount().getValue() != null ? new BigDecimal(lineItemNew.getData().getAmount().getValue()) : BigDecimal.ZERO)) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Request Amount has changed from " + billLineItemSaving.getRequestedAmount() + " to " + lineItemNew.getData().getAmount().getValue());
//
//                }
//                if (!helperService.areEqualTreatNullAsZero(billLineItemSaving.getInsurerRequestedAmount(), lineItemNew.getData().getTariff().getInsurer_bill_amount())) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Insurer Request Amount has changed from " + billLineItemSaving.getInsurerRequestedAmount() + " to " + lineItemNew.getData().getTariff().getInsurer_bill_amount());
//                }
//                if (!helperService.areEqualTreatNullAsZero(billLineItemSaving.getApprovedAmount(), lineItemNew.getData().getTariff().getAdmissible_amount())) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Approved Amount has changed from " + billLineItemSaving.getApprovedAmount() + " to " + lineItemNew.getData().getTariff().getAdmissible_amount());
//                }
//                if (!helperService.areEqualTreatNullAsZero(billLineItemSaving.getInsurerApprovedAmount(), lineItemNew.getData().getTariff().getInsurer_tariff_amount())) {
//                    internalRemark = concatCommentInInternalRemark(internalRemark, "Insurer Approved Amount has changed from " + billLineItemSaving.getInsurerApprovedAmount() + " to " + lineItemNew.getData().getTariff().getInsurer_tariff_amount());
//                }
//
//
//                if (internalRemark != null) {
//                    log.info("Line Item has been edited with internal remark: {}", internalRemark);
//                }
//
//            }
//
//
//            return internalRemark;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            log.error("Exception while comparing line item: {} with exception message: {}", billLineItemSaving.getLineItem(), ex.getMessage());
//            return null;
//        }
//
//    }

//    private String concatCommentInInternalRemark(String internalRemark, String s) {
//        if (internalRemark != null) {
//            return internalRemark + ", " + s;
//        } else {
//            return s;
//        }
//
//    }
//
//
//    private void expiredLineItemSavings(long claimDataId) {
//        billLineItemSavingRepository.markExpiredForModuleId(claimDataId);
//    }

//    private List<BillLineItemSaving> getBillLineItemSavingList(boolean isClaimRerun, ClaimBillModuleSaving claimBillModuleSaving) {
//        return isClaimRerun
//                ? billLineItemSavingRepository.findByClaimBillModuleIdActive(claimBillModuleSaving.getId())
//                : new ArrayList<>();
//    }
//
//    private List<BillCategorySaving> getBillCategorySavingList(boolean isClaimRerun, ClaimBillModuleSaving claimBillModuleSaving) {
//        return isClaimRerun
//                ? billCategorySavingRepository.findByBillModuleIdActive(claimBillModuleSaving.getId())
//                : new ArrayList<>();
//    }

//    protected void saveClaimSavingDataAndAuditData(ClaimBillModuleSaving claimBillModuleSaving,
//                                                   List<BillCategorySaving> billCategorySavingList,
//                                                   List<BillLineItemSaving> billLineItemSavingList,
//                                                   ClaimBillModuleSavingAudit claimBillModuleSavingAudit,
//                                                   List<BillCategorySavingAudit> billCategorySavingAudits,
//                                                   List<BillLineItemSavingAudit> billLineItemSavingAudits) {
//        claimBillModuleSaving = claimBillModuleSavingRepository.save(claimBillModuleSaving);
//
//        // ToDo: We need to update the logic and update the claimBillModule id only in case when we are creating it freshly
//        long claimBillModuleId = claimBillModuleSaving.getId();
//        billCategorySavingList.forEach(billCategorySaving -> billCategorySaving.setBillModuleId(claimBillModuleId));
//        billLineItemSavingList.forEach(billLineItemSaving -> billLineItemSaving.setBillModuleId(claimBillModuleId));
//
//        billCategorySavingRepository.saveAll(billCategorySavingList);
//        billLineItemSavingRepository.saveAll(billLineItemSavingList);
//
////        if (claimBillModuleSavingAudit != null) {
////            claimBillModuleSavingAuditRepository.save(claimBillModuleSavingAudit);
////        }
////
////        if (billCategorySavingAudits != null) {
////            billCategorySavingAuditRepository.saveAll(billCategorySavingAudits);
////        }
////
////        if (billLineItemSavingAudits != null) {
////            billLineItemSavingAuditRepository.saveAll(billLineItemSavingAudits);
////        }
//    }

//    private ClaimRequestTypeEnum determineClaimStage(ClaimStatus claimStatus) {
//        if (claimStatus == null) return ClaimRequestTypeEnum.preauth_request;
//        if (ClaimStatus.isInterimStage(claimStatus)) {
//            return ClaimRequestTypeEnum.interim_enhancement_request;
//        } else if (ClaimStatus.isDischargeStage(claimStatus)) {
//            return ClaimRequestTypeEnum.final_enhancement_request;
//        }
//        return ClaimRequestTypeEnum.preauth_request;
//    }

//    private static List<BillLineItemSavingAudit> getBillLineItemSavingAuditData(List<BillLineItemSaving> billLineItemSavingList) {
//        if (billLineItemSavingList == null) return Collections.emptyList();
//        return billLineItemSavingList.stream()
//                .filter(Objects::nonNull)
//                .map(BillLineItemSavingAudit::from)
//                .collect(Collectors.toList());
//    }

//    private static List<BillCategorySavingAudit> getBillCategorySavingAuditData(List<BillCategorySaving> billCategorySavingList) {
//        if (billCategorySavingList == null) return Collections.emptyList();
//        return billCategorySavingList.stream()
//                .filter(Objects::nonNull)
//                .map(BillCategorySavingAudit::from)
//                .collect(Collectors.toList());
//    }

//    private void prepareSavingData(BillTariffResponse billTariffResponse, ClaimBillModuleSaving claimBillModuleSaving,
//                                   List<BillCategorySaving> billCategorySavingList, List<BillLineItemSaving> billLineItemSavingList,
//                                   boolean isClaimRerun, boolean isEditFlow, ClaimRequestTypeEnum claimStage) {
//        if (claimBillModuleSaving == null) return;
//        String changeCode = helperService.generateRandomString();
//        if (billTariffResponse == null
//                || billTariffResponse.getBillTariffResponseDTO() == null
//                || billTariffResponse.getBillTariffResponseDTO().getData() == null) {
//            return;
//        }
//
//        BillTariffResponseData billTariffResponseData = billTariffResponse.getBillTariffResponseDTO().getData();
//
//        List<LineItemsItem> lineItems = billTariffResponseData.getLine_items();
//        if (lineItems == null || lineItems.isEmpty()) return;
//
//        // ToDo: We need to reset only in case when we are not in edit flow
//
//        if (isClaimRerun)
//            resetClaimLevelAndCategoryLevelSavings(claimBillModuleSaving, billCategorySavingList, billLineItemSavingList);
//        String userId = userService.getLoggedCurrentUser();
//        int lineItemCount = 0;
//        for (LineItemsItem lineItem : lineItems) {
//            if (lineItem == null || lineItem.getData() == null || lineItem.getData().getTariff() == null
//                    || lineItem.getData().getDescription() == null) {
//                continue;
//            }
//
//            if (!lineItem.getData().getTariff().isDeleted()) {
//                lineItemCount++;
//            }
//
//            int rowId = lineItem.getData().getTariff().getRow_id();
//            String itemName = lineItem.getData().getDescription().getValue();
//            if (itemName == null) continue;
//
//            Optional<BillLineItemSaving> existingLineItemOpt = getLineItemIfExist(billLineItemSavingList, rowId, itemName);
//
//            BillLineItemSaving lineItemFresh;
//
//            if (existingLineItemOpt.isPresent()) {
//                BillLineItemSaving existingLineItem = existingLineItemOpt.get();
//                existingLineItem.updateLineItemDetails(lineItem.getData(), claimBillModuleSaving.getRerunCount(),
//                        changeCode, userId, isEditFlow, null);
//                lineItemFresh = existingLineItem;
//            } else {
//                BillLineItemSaving newLineItem = new BillLineItemSaving(lineItem,
//                        claimBillModuleSaving.getRerunCount(), changeCode, userId, isEditFlow);
//                billLineItemSavingList.add(newLineItem);
//                lineItemFresh = newLineItem;
//            }
//            log.info("Line item Name: {}, Requested Amount: {}, Approved Amount: {} and Saving: {}", lineItemFresh.getLineItem(), lineItemFresh.getRequestedAmount(), lineItemFresh.getApprovedAmount(), getSaving(lineItemFresh));
//            updateCategorySavingData(billCategorySavingList, lineItemFresh, isClaimRerun);
//            updateClaimBillModuleSaving(claimBillModuleSaving, lineItemFresh, isEditFlow);
//        }
//
//        claimBillModuleSaving.setNoOfLineItems(lineItemCount);
//        claimBillModuleSaving.setStage(claimStage != null ? claimStage.toString() : null);
//        claimBillModuleSaving.setParsedPercentage(billTariffResponseData.getBill_amounts().getAmount_match_percentage());
//        claimBillModuleSaving.setChangeCode(changeCode);
//        claimBillModuleSaving.setIsPackageClaim(false);
//        if (!isClaimRerun) {
//            claimBillModuleSaving.setDateCreated(new Date());
//            claimBillModuleSaving.setCreated_by(userId);
//        } else {
//            claimBillModuleSaving.setDateUpdated(new Date());
//            claimBillModuleSaving.setUpdated_by(userId);
//        }
//    }

//    private static Optional<BillLineItemSaving> getLineItemIfExist(List<BillLineItemSaving> billLineItemSavingList, int rowId, String itemName) {
//        return billLineItemSavingList.stream()
//                .filter(billLineItem -> billLineItem != null
//                        && billLineItem.getRowId() == rowId
//                        && itemName.trim().equalsIgnoreCase(billLineItem.getLineItem().trim()))
//                .findFirst();
//    }
//
//    private BigDecimal getSaving(BillLineItemSaving lineItemFresh) {
//        return lineItemFresh.getNonPayableSavings().add(lineItemFresh.getConstructSavings()).add(lineItemFresh.getPharmacySavings()).add(lineItemFresh.getTariffSavings());
//    }

//    private void updateClaimBillModuleSaving(ClaimBillModuleSaving claimBillModuleSaving, BillLineItemSaving lineItem,
//                                             boolean isEditFlow) {
//
//        if (claimBillModuleSaving == null || lineItem == null) return;
//        claimBillModuleSaving.setRequestedAmount(
//                HelperService.safeAdd(claimBillModuleSaving.getRequestedAmount(), lineItem.getRequestedAmount()));
//        claimBillModuleSaving.setApprovedAmount(
//                HelperService.safeAdd(claimBillModuleSaving.getApprovedAmount(), lineItem.getApprovedAmount()));
//        claimBillModuleSaving.setNonPayableSavings(
//                HelperService.safeAdd(claimBillModuleSaving.getNonPayableSavings(), lineItem.getNonPayableSavings()));
//        claimBillModuleSaving.setConstructSavings(
//                HelperService.safeAdd(claimBillModuleSaving.getConstructSavings(), lineItem.getConstructSavings()));
//        claimBillModuleSaving.setPharmacySavings(
//                HelperService.safeAdd(claimBillModuleSaving.getPharmacySavings(), lineItem.getPharmacySavings()));
//        claimBillModuleSaving.setTariffSavings(
//                HelperService.safeAdd(claimBillModuleSaving.getTariffSavings(), lineItem.getTariffSavings()));
//        claimBillModuleSaving.setVneuronSavings(
//                HelperService.safeAdd(claimBillModuleSaving.getVneuronSavings(), lineItem.getVneuronSavings()));
//
//
//        //non insurer
//        claimBillModuleSaving.setMouSavings(
//                HelperService.safeAdd(claimBillModuleSaving.getMouSavings(), lineItem.getMouSavings()));
//
//        claimBillModuleSaving.setSublimitSavings(
//                HelperService.safeAdd(claimBillModuleSaving.getSublimitSavings(), lineItem.getSublimitSavings()));
//
//        claimBillModuleSaving.setCopaySavings(
//                HelperService.safeAdd(claimBillModuleSaving.getCopaySavings(), lineItem.getCopaySavings()));
//
//        claimBillModuleSaving.setProportionalDiscount(
//                HelperService.safeAdd(claimBillModuleSaving.getProportionalDiscount(), lineItem.getProportionalDiscount()));
//
//        //only Insurer
//        BigDecimal savedInsurerRequestedAmt;
//        BigDecimal savedInsurerApprovedAmt;
//        BigDecimal savedInsurerNonPaybleSavings;
//        BigDecimal savedInsurerConstructSavings;
//        BigDecimal savedInsurerPharmacySavings;
//        BigDecimal savedInsurerTariffSavings;
//        BigDecimal savedInsurerVneuronSavings;
//
//        if (lineItem.getInsurerRequestedAmount() == null) {
//            savedInsurerRequestedAmt = lineItem.getRequestedAmount();
//            savedInsurerApprovedAmt = lineItem.getApprovedAmount();
//            savedInsurerNonPaybleSavings = lineItem.getNonPayableSavings();
//            savedInsurerConstructSavings = lineItem.getConstructSavings();
//            savedInsurerPharmacySavings = lineItem.getPharmacySavings();
//            savedInsurerTariffSavings = lineItem.getTariffSavings();
//            savedInsurerVneuronSavings = lineItem.getVneuronSavings();
//
//        } else {
//            savedInsurerRequestedAmt = lineItem.getInsurerRequestedAmount();
//            savedInsurerApprovedAmt = lineItem.getInsurerApprovedAmount();
//            savedInsurerNonPaybleSavings = lineItem.getInsurerNonPayableSavings();
//            savedInsurerConstructSavings = lineItem.getInsurerConstructSavings();
//            savedInsurerPharmacySavings = lineItem.getInsurerPharmacySavings();
//            savedInsurerTariffSavings = lineItem.getInsurerTariffSavings();
//            savedInsurerVneuronSavings = lineItem.getInsurerVneuronSavings();
//
//        }
//        claimBillModuleSaving.setInsurerRequestedAmount(HelperService.safeAdd(claimBillModuleSaving.getInsurerRequestedAmount(), savedInsurerRequestedAmt));
//        claimBillModuleSaving.setInsurerApprovedAmount(HelperService.safeAdd(claimBillModuleSaving.getInsurerApprovedAmount(), savedInsurerApprovedAmt));
//        claimBillModuleSaving.setInsurerNonPayableSavings(HelperService.safeAdd(claimBillModuleSaving.getInsurerNonPayableSavings(), savedInsurerNonPaybleSavings));
//        claimBillModuleSaving.setInsurerConstructSavings(HelperService.safeAdd(claimBillModuleSaving.getInsurerConstructSavings(), savedInsurerConstructSavings));
//        claimBillModuleSaving.setInsurerPharmacySavings(HelperService.safeAdd(claimBillModuleSaving.getInsurerPharmacySavings(), savedInsurerPharmacySavings));
//        claimBillModuleSaving.setInsurerTariffSavings(HelperService.safeAdd(claimBillModuleSaving.getInsurerTariffSavings(), savedInsurerTariffSavings));
//        claimBillModuleSaving.setInsurerVneuronSavings(HelperService.safeAdd(claimBillModuleSaving.getInsurerVneuronSavings(), savedInsurerVneuronSavings));
//
//        claimBillModuleSaving.setInsurerEdit(isEditFlow);
//    }

//    private void updateCategorySavingData(List<BillCategorySaving> billCategorySavingList, BillLineItemSaving lineItem, boolean isClaimRerun) {
//        if (billCategorySavingList == null || lineItem == null || lineItem.getCategoryName() == null) return;
//        Optional<BillCategorySaving> existingCategoryOpt = billCategorySavingList.stream()
//                .filter(category -> category != null && lineItem.getCategoryName().equalsIgnoreCase(category.getCategoryName()))
//                .findFirst();
//
//        BillCategorySaving billCategorySaving = existingCategoryOpt.orElseGet(BillCategorySaving::new);
//
//        // Now we have updated the category saving, we need to check if it already exists in the list,
//        // if yes then update else add
//        updateBillCategorySaving(billCategorySaving, lineItem);
//        billCategorySaving.setBillModuleId(lineItem.getBillModuleId());
//        billCategorySaving.setRerunCount(lineItem.getRerunCount());
//        billCategorySaving.setChangeCode(lineItem.getChangeCode());
//
//        if (existingCategoryOpt.isPresent()) {
//            billCategorySaving.setStatus(isClaimRerun ? BillSavingStatus.UPDATED.toString() : BillSavingStatus.ADDED.toString());
//            billCategorySaving.setDateUpdated(new Date());
//            billCategorySaving.setUpdated_by(lineItem.getUpdated_by());
//        } else {
//            billCategorySaving.setStatus(BillSavingStatus.ADDED.toString());
//            billCategorySaving.setCategoryName(lineItem.getCategoryName());
//            billCategorySaving.setDateCreated(new Date());
//            billCategorySaving.setCreated_by(lineItem.getCreated_by());
//            billCategorySavingList.add(billCategorySaving);
//        }
//    }

//    private void resetClaimLevelAndCategoryLevelSavings(ClaimBillModuleSaving claimBillModuleSaving,
//                                                        List<BillCategorySaving> billCategorySavingList,
//                                                        List<BillLineItemSaving> billLineItemSavingList) {
//        resetBillCategorySaving(billCategorySavingList);
//        resetLineItemSavings(billLineItemSavingList);
//        resetClaimLevelSavings(claimBillModuleSaving);
//    }

//    private void resetClaimLevelSavings(ClaimBillModuleSaving claimBillModuleSaving) {
//        if (claimBillModuleSaving == null) return;
//        claimBillModuleSaving.setInsurerApprovedAmount(BigDecimal.ZERO);
//        claimBillModuleSaving.setInsurerRequestedAmount(BigDecimal.ZERO);
//        claimBillModuleSaving.setApprovedAmount(BigDecimal.ZERO);
//        claimBillModuleSaving.setRequestedAmount(BigDecimal.ZERO);
//        claimBillModuleSaving.setTariffSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setNonPayableSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setConstructSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setPharmacySavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setVneuronSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setInsurerTariffSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setInsurerNonPayableSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setInsurerConstructSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setInsurerPharmacySavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setInsurerVneuronSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setMouSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setSublimitSavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setCopaySavings(BigDecimal.ZERO);
//        claimBillModuleSaving.setProportionalDiscount(BigDecimal.ZERO);
//    }
//
//    private static void resetLineItemSavings(List<BillLineItemSaving> billLineItemSavingList) {
//        if (billLineItemSavingList == null) return;
//        for (BillLineItemSaving lineItem : billLineItemSavingList) {
//            if (lineItem == null) continue;
//            lineItem.setRequestedAmount(BigDecimal.ZERO);
//            lineItem.setApprovedAmount(BigDecimal.ZERO);
//            lineItem.setNonPayableSavings(BigDecimal.ZERO);
//            lineItem.setConstructSavings(BigDecimal.ZERO);
//            lineItem.setPharmacySavings(BigDecimal.ZERO);
//            lineItem.setTariffSavings(BigDecimal.ZERO);
//            lineItem.setInsurerRequestedAmount(BigDecimal.ZERO);
//            lineItem.setInsurerApprovedAmount(BigDecimal.ZERO);
//            lineItem.setInsurerNonPayableSavings(BigDecimal.ZERO);
//            lineItem.setInsurerConstructSavings(BigDecimal.ZERO);
//            lineItem.setInsurerPharmacySavings(BigDecimal.ZERO);
//            lineItem.setInsurerTariffSavings(BigDecimal.ZERO);
//            lineItem.setMouSavings(BigDecimal.ZERO);
//            lineItem.setSublimitSavings(BigDecimal.ZERO);
//            lineItem.setCopaySavings(BigDecimal.ZERO);
//            lineItem.setProportionalDiscount(BigDecimal.ZERO);
//            lineItem.setVneuronSavings(BigDecimal.ZERO);
//            lineItem.setInsurerVneuronSavings(BigDecimal.ZERO);
//            lineItem.setStatus(BillSavingStatus.EXPIRED.toString());
//        }
//    }
//
//    private static void resetBillCategorySaving(List<BillCategorySaving> billCategorySavingList) {
//        if (billCategorySavingList == null) return;
//        for (BillCategorySaving categorySaving : billCategorySavingList) {
//            if (categorySaving == null) continue;
//            categorySaving.setRequestedAmount(BigDecimal.ZERO);
//            categorySaving.setApprovedAmount(BigDecimal.ZERO);
//            categorySaving.setNonPayableSavings(BigDecimal.ZERO);
//            categorySaving.setConstructSavings(BigDecimal.ZERO);
//            categorySaving.setPharmacySavings(BigDecimal.ZERO);
//            categorySaving.setTariffSavings(BigDecimal.ZERO);
//            categorySaving.setInsurerRequestedAmount(BigDecimal.ZERO);
//            categorySaving.setInsurerApprovedAmount(BigDecimal.ZERO);
//            categorySaving.setInsurerNonPayableSavings(BigDecimal.ZERO);
//            categorySaving.setInsurerConstructSavings(BigDecimal.ZERO);
//            categorySaving.setInsurerPharmacySavings(BigDecimal.ZERO);
//            categorySaving.setInsurerTariffSavings(BigDecimal.ZERO);
//            categorySaving.setMouSavings(BigDecimal.ZERO);
//            categorySaving.setSublimitSavings(BigDecimal.ZERO);
//            categorySaving.setCopaySavings(BigDecimal.ZERO);
//            categorySaving.setProportionalDiscount(BigDecimal.ZERO);
//            categorySaving.setVneuronSavings(BigDecimal.ZERO);
//            categorySaving.setInsurerVneuronSavings(BigDecimal.ZERO);
//            categorySaving.setStatus(BillSavingStatus.EXPIRED.toString());
//            categorySaving.setRemark(null);
//            categorySaving.setInsurerRemark(null);
//            categorySaving.setInternalRemark(null);
//        }
//    }
//
//    private static void updateBillCategorySaving(BillCategorySaving billCategorySaving, BillLineItemSaving existingLineItem) {
//
//        if (billCategorySaving == null || existingLineItem == null) return;
//        billCategorySaving.setRequestedAmount(
//                HelperService.safeAdd(billCategorySaving.getRequestedAmount(), existingLineItem.getRequestedAmount()));
//        billCategorySaving.setApprovedAmount(
//                HelperService.safeAdd(billCategorySaving.getApprovedAmount(), existingLineItem.getApprovedAmount()));
//        billCategorySaving.setNonPayableSavings(
//                HelperService.safeAdd(billCategorySaving.getNonPayableSavings(), existingLineItem.getNonPayableSavings()));
//        billCategorySaving.setConstructSavings(
//                HelperService.safeAdd(billCategorySaving.getConstructSavings(), existingLineItem.getConstructSavings()));
//        billCategorySaving.setPharmacySavings(
//                HelperService.safeAdd(billCategorySaving.getPharmacySavings(), existingLineItem.getPharmacySavings()));
//        billCategorySaving.setTariffSavings(
//                HelperService.safeAdd(billCategorySaving.getTariffSavings(), existingLineItem.getTariffSavings()));
//        billCategorySaving.setMouSavings(
//                HelperService.safeAdd(billCategorySaving.getMouSavings(), existingLineItem.getMouSavings()));
//        billCategorySaving.setSublimitSavings(
//                HelperService.safeAdd(billCategorySaving.getSublimitSavings(), existingLineItem.getSublimitSavings()));
//        billCategorySaving.setCopaySavings(
//                HelperService.safeAdd(billCategorySaving.getCopaySavings(), existingLineItem.getCopaySavings()));
//        billCategorySaving.setProportionalDiscount(
//                HelperService.safeAdd(billCategorySaving.getProportionalDiscount(), existingLineItem.getProportionalDiscount()));
//        billCategorySaving.setVneuronSavings(
//                HelperService.safeAdd(billCategorySaving.getVneuronSavings(), existingLineItem.getVneuronSavings()));
//
//        if (existingLineItem.getInternalRemark() != null) {
//            billCategorySaving.setInternalRemark(
//                    billCategorySaving.getInternalRemark() == null ? existingLineItem.getChangeCode() + " - " + existingLineItem.getLineItem().trim() + " - " + existingLineItem.getInternalRemark() : billCategorySaving.getInternalRemark() + " || " + existingLineItem.getChangeCode() + " - " + existingLineItem.getLineItem().trim() + " - " + existingLineItem.getInternalRemark()
//            );
//        }
//
//        BigDecimal savedInsurerRequestedAmt;
//        BigDecimal savedInsurerApprovedAmt;
//        BigDecimal savedInsurerNonPaybleSavings;
//        BigDecimal savedInsurerConstructSavings;
//        BigDecimal savedInsurerPharmacySavings;
//        BigDecimal savedInsurerTariffSavings;
//        BigDecimal savedInsurerVneuronSavings;
//
//        if (existingLineItem.getInsurerRequestedAmount() == null) {
//            savedInsurerRequestedAmt = existingLineItem.getRequestedAmount();
//            savedInsurerApprovedAmt = existingLineItem.getApprovedAmount();
//            savedInsurerNonPaybleSavings = existingLineItem.getNonPayableSavings();
//            savedInsurerConstructSavings = existingLineItem.getConstructSavings();
//            savedInsurerPharmacySavings = existingLineItem.getPharmacySavings();
//            savedInsurerTariffSavings = existingLineItem.getTariffSavings();
//            savedInsurerVneuronSavings = existingLineItem.getVneuronSavings();
//
//        } else {
//            savedInsurerRequestedAmt = existingLineItem.getInsurerRequestedAmount();
//            savedInsurerApprovedAmt = existingLineItem.getInsurerApprovedAmount();
//            savedInsurerNonPaybleSavings = existingLineItem.getInsurerNonPayableSavings();
//            savedInsurerConstructSavings = existingLineItem.getInsurerConstructSavings();
//            savedInsurerPharmacySavings = existingLineItem.getInsurerPharmacySavings();
//            savedInsurerTariffSavings = existingLineItem.getInsurerTariffSavings();
//            savedInsurerVneuronSavings = existingLineItem.getInsurerVneuronSavings();
//
//        }
//
//
//        billCategorySaving.setInsurerRequestedAmount(
//                HelperService.safeAdd(billCategorySaving.getInsurerRequestedAmount(), savedInsurerRequestedAmt));
//        billCategorySaving.setInsurerApprovedAmount(
//                HelperService.safeAdd(billCategorySaving.getInsurerApprovedAmount(), savedInsurerApprovedAmt));
//        billCategorySaving.setInsurerNonPayableSavings(
//                HelperService.safeAdd(billCategorySaving.getInsurerNonPayableSavings(), savedInsurerNonPaybleSavings));
//        billCategorySaving.setInsurerConstructSavings(
//                HelperService.safeAdd(billCategorySaving.getInsurerConstructSavings(), savedInsurerConstructSavings));
//        billCategorySaving.setInsurerPharmacySavings(
//                HelperService.safeAdd(billCategorySaving.getInsurerPharmacySavings(), savedInsurerPharmacySavings));
//        billCategorySaving.setInsurerTariffSavings(
//                HelperService.safeAdd(billCategorySaving.getInsurerTariffSavings(), savedInsurerTariffSavings));
//        billCategorySaving.setVneuronSavings(
//                HelperService.safeAdd(billCategorySaving.getVneuronSavings(), savedInsurerVneuronSavings));
//
//    }

//    public void updatePMLSaving(ClaimData claimData, PMLResponse pmlResponse, ClaimRunDTO claimRunDTO) {
//        log.info("PML saving called ...");
//        if (pmlResponse == null || pmlResponse.getPmlResponseDTO() == null) {
//            log.error("PML response is null for claim data id: " + claimData.getIntimationNumber());
//            return;
//        }
//
//        ClaimRequestTypeEnum claimRequestTypeEnum = determineClaimStage(claimData.getClaimStatus());
//        ClaimBillModuleSaving claimBillModuleSaving = billModuleSavingRepository.getClaimBillModuleSaving(
//                claimData.getId(), claimRequestTypeEnum.toString());
//
//        if (claimBillModuleSaving == null) {
//            log.error("Claim Saving does not exist: " + claimData.getIntimationNumber());
//            return;
//        }
//
//        List<BillCategorySaving> billCategorySavingExistList =
//                billCategorySavingRepository.findByBillModuleIdActive(claimBillModuleSaving.getId());
//        List<BillLineItemSaving> billLineItemSavingList = billLineItemSavingRepository
//                .findByClaimBillModuleIdActive(claimBillModuleSaving.getId());
//        prepareAuditDataAndUpdatePMLSavings(claimBillModuleSaving, billCategorySavingExistList, billLineItemSavingList,
//                pmlResponse, claimRunDTO);
//    }

//    protected void prepareAuditDataAndUpdatePMLSavings(ClaimBillModuleSaving claimBillModuleSaving,
//                                                       List<BillCategorySaving> billCategorySavingExistList,
//                                                       List<BillLineItemSaving> billLineItemSavingList, PMLResponse pmlResponse,
//                                                       ClaimRunDTO claimRunDTO) {
//        boolean isOnlyPMLRerun = claimRunDTO != null
//                && (ClaimRunIdentifier.RERUN_PML.equals(claimRunDTO.getClaimRunIdentifier())
//                || ClaimRunIdentifier.RERUN_TARIFF_PML.equals(claimRunDTO.getClaimRunIdentifier()));
//        if (isOnlyPMLRerun) {
//            prepareAuditData(claimBillModuleSaving, billCategorySavingExistList, billLineItemSavingList);
//        }
//
//        PMLSavingsDTO pmlSavings = this.calculatePMLSavings(pmlResponse.getPmlResponseDTO().getBill_item_result());
//        updateLineItemSavings(claimBillModuleSaving, pmlResponse);
//        updateCategorySavings(billCategorySavingExistList, pmlResponse);
//        updateClaimLevelSavings(claimBillModuleSaving, pmlSavings, isOnlyPMLRerun);
//    }
//
//    protected void prepareAuditData(ClaimBillModuleSaving claimBillModuleSaving, List<BillCategorySaving> billCategorySavingList,
//                                    List<BillLineItemSaving> billLineItemSavingList) {
//        List<BillCategorySavingAudit> billCategorySavingAudits = getBillCategorySavingAuditData(billCategorySavingList);
//        List<BillLineItemSavingAudit> billLineItemSavingAudits = getBillLineItemSavingAuditData(billLineItemSavingList);
//        ClaimBillModuleSavingAudit claimBillModuleSavingAudit = ClaimBillModuleSavingAudit.from(claimBillModuleSaving);
//
//        if (claimBillModuleSavingAudit != null) {
//            claimBillModuleSavingAuditRepository.save(claimBillModuleSavingAudit);
//        }
//
//        if (billCategorySavingAudits != null) {
//            billCategorySavingAuditRepository.saveAll(billCategorySavingAudits);
//        }
//
//        if (billLineItemSavingAudits != null) {
//            billLineItemSavingAuditRepository.saveAll(billLineItemSavingAudits);
//        }
//    }
//
//    private void updateLineItemSavings(ClaimBillModuleSaving billModuleSavingCurrent, PMLResponse pmlResponse) {
//        List<BillLineItemSaving> billLineItemSavingList = new ArrayList<>();
//        pmlResponse.getPmlResponseDTO().getBill_item_result().forEach(item -> {
//            BillLineItemSaving billLineSavingExist = billLineItemSavingRepository
//                    .findFirstByBillModuleIdAndRowId(billModuleSavingCurrent.getId(), item.getRow_id());
//            if (billLineSavingExist != null && billLineSavingExist.getLineItem().equalsIgnoreCase(item.getBill_item_name())) {
//                PMLSavingsDTO pmlLineItemSavings = this.calculatePMLSavings(List.of(item));
//                billLineSavingExist.setMouSavings(pmlLineItemSavings.getMouSavings());
//                billLineSavingExist.setCopaySavings(pmlLineItemSavings.getCopaySavings());
//                billLineSavingExist.setSublimitSavings(pmlLineItemSavings.getSublimitSavings());
//                billLineSavingExist.setProportionalDiscount(pmlLineItemSavings.getProportionalSavings());
//                billLineItemSavingList.add(billLineSavingExist);
//            } else {
//                log.error("Line item does not exist: " + item.getBill_item_name());
//            }
//        });
//
//        billLineItemSavingRepository.saveAll(billLineItemSavingList);
//    }
//
//    private void updateCategorySavings(List<BillCategorySaving> billCategorySavingExistList, PMLResponse pmlResponse) {
//        List<BillCategorySaving> billCategorySavingList = new ArrayList<>();
//        billCategorySavingExistList.forEach(category -> {
//            List<BillItemResultDto> categoryLineItems = pmlResponse.getPmlResponseDTO().getBill_item_result()
//                    .stream()
//                    .filter(item -> item.getMaster_category().equals(category.getCategoryName()))
//                    .collect(Collectors.toList());
//            PMLSavingsDTO pmlCategorySavings = this.calculatePMLSavings(categoryLineItems);
//            category.setMouSavings(pmlCategorySavings.getMouSavings());
//            category.setCopaySavings(pmlCategorySavings.getCopaySavings());
//            category.setSublimitSavings(pmlCategorySavings.getSublimitSavings());
//            category.setProportionalDiscount(pmlCategorySavings.getProportionalSavings());
//            billCategorySavingList.add(category);
//        });
//        billCategorySavingRepository.saveAll(billCategorySavingList);
//    }
//
//    private void updateClaimLevelSavings(ClaimBillModuleSaving billModuleSavingCurrent, PMLSavingsDTO pmlSavings, boolean isRerun) {
//        if (isRerun) {
//            billModuleSavingCurrent.setRerunCount(billModuleSavingCurrent.getRerunCount() + 1);
//        }
//
//        billModuleSavingCurrent.setDateUpdated(new Date());
//        billModuleSavingCurrent.setMouSavings(pmlSavings.getMouSavings());
//        billModuleSavingCurrent.setCopaySavings(pmlSavings.getCopaySavings());
//        billModuleSavingCurrent.setSublimitSavings(pmlSavings.getSublimitSavings());
//        billModuleSavingCurrent.setProportionalDiscount(pmlSavings.getProportionalSavings());
//        billModuleSavingRepository.save(billModuleSavingCurrent);
//    }

//    public PMLSavingsDTO calculatePMLSavings(List<BillItemResultDto> items) {
//        PMLSavingsDTO summary = new PMLSavingsDTO();
//
//        for (BillItemResultDto item : items) {
//            BigDecimal mou = item.getMou_discount_applied() != null ? item.getMou_discount_applied() : BigDecimal.ZERO;
//            BigDecimal copay = item.getCopay_applied() != null ? item.getCopay_applied() : BigDecimal.ZERO;
//            BigDecimal proportional = item.getRoom_proportional_discount_applied() != null
//                    ? item.getRoom_proportional_discount_applied() : BigDecimal.ZERO;
//            BigDecimal finalAmount = item.getFinal_amount() != null ? item.getFinal_amount() : BigDecimal.ZERO;
//            BigDecimal finalAfterDrop = item.getFinal_amount_after_drop() != null ? item.getFinal_amount_after_drop() : BigDecimal.ZERO;
//            BigDecimal sublimitDrop = finalAmount.subtract(finalAfterDrop);
//
//            summary.setMouSavings(summary.getMouSavings().add(mou));
//            summary.setCopaySavings(summary.getCopaySavings().add(copay));
//            summary.setSublimitSavings(summary.getSublimitSavings().add(sublimitDrop));
//            summary.setProportionalSavings(summary.getSublimitSavings().add(proportional));
//        }
//
//        return summary;
//    }
//
//    public void calculateVNeuronSaving(ClaimData claimData, VNeuronResponseDTO vNeuronResponseDTO) {
//        log.info("Vneuron saving called ...");
//        if (vNeuronResponseDTO == null) {
//            log.error("Vneuron Response is Null");
//            return;
//        }
//
//        ClaimRequestTypeEnum claimRequestTypeEnum = determineClaimStage(claimData.getClaimStatus());
//        ClaimBillModuleSaving claimBillModuleSaving = billModuleSavingRepository.getClaimBillModuleSaving(
//                claimData.getId(), claimRequestTypeEnum.toString());
//
//        if (claimBillModuleSaving == null) {
//            log.error("Claim Saving does not exist: " + claimData.getIntimationNumber());
//            return;
//        }
//    }

}