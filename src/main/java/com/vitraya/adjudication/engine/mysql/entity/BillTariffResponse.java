package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.BillTariffStatusResponseCode;
import com.vitraya.adjudication.engine.dto.response.BillTariffResponseDTO;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "bill_tariff_response")
public class BillTariffResponse {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("bill_identifier")
    private String billIdentifier;

    @Column("bill_tariff_response")
    private String billTariffResponse;

    @Column("bill_tariff_tat")
    private String billTariffTat;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

    @Column("bill_tariff_decision")
    private String billTariffDecision;

    @Column("doc_available")
    private boolean docAvailable;

    @Transient
    private BillTariffResponseDTO billTariffResponseDTO;

    @Transient
    private boolean isClaimEdited;

    public BillTariffResponseDTO getBillTariffResponseDTO() {
        if (billTariffResponseDTO != null) {
            return billTariffResponseDTO;
        } else if (billTariffResponse != null) {
            return billTariffResponseDTO = GsonUtils.fromJson(billTariffResponse, BillTariffResponseDTO.class);
        }

        return null;
    }

    public void setBillTariffResponseDTO(BillTariffResponseDTO billTariffResponseDTO) {
        this.billTariffResponseDTO = billTariffResponseDTO;
        this.billTariffResponse = GsonUtils.toJson(billTariffResponseDTO);
    }

    public BigDecimal getClaimBillAmountRequested() {
        BigDecimal claimBillAmountRequested = null;
        if (getBillTariffResponseDTO() != null && billTariffResponseDTO != null && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getBill_amounts() != null) {
            claimBillAmountRequested = billTariffResponseDTO.getData().getBill_amounts().getTotal_bill_amount();
        }

        return claimBillAmountRequested;
    }

    public BigDecimal getTariffMatchPercentage() {
        BigDecimal tariffMatchPercentage = null;
        if (billTariffResponseDTO != null && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getBill_amounts() != null) {
            tariffMatchPercentage = billTariffResponseDTO.getData().getBill_amounts().getAmount_match_percentage();
        }

        return tariffMatchPercentage;
    }

    public BigDecimal getAmountAfterTariffApplication() {
        BigDecimal amountAfterTariffApplication = null;

        if (billTariffResponseDTO != null && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getTariff_amounts() != null
                && billTariffResponseDTO.getData().getTariff_amounts().getTotal_admissible_amount() != null) {
            amountAfterTariffApplication = billTariffResponseDTO.getData().getTariff_amounts().getTotal_admissible_amount();
        }

        return amountAfterTariffApplication;
    }

    public BigDecimal getAmountDeductedInPercent() {
        BigDecimal amountDeductedInPercent = null;

        // tariffResponse.getData().getTariff_amounts().getDeduction()
        if (billTariffResponseDTO != null && billTariffResponseDTO.getData() != null
                && billTariffResponseDTO.getData().getTariff_amounts() != null
                && billTariffResponseDTO.getData().getTariff_amounts().getDeduction() != null) {
            amountDeductedInPercent = billTariffResponseDTO.getData().getTariff_amounts().getDeduction();
        }

        return amountDeductedInPercent;
    }

    public boolean isBillFound() {
        return billTariffResponseDTO != null && billTariffResponseDTO.getResponse_code() != null
                && billTariffResponseDTO.getResponse_code().equalsIgnoreCase(BillTariffStatusResponseCode.BILL_FAILED.toString());
    }

    public void setBillTariffResponse(String billTariffResponse) {
        this.billTariffResponse = billTariffResponse;
        this.billTariffResponseDTO = GsonUtils.fromJson(billTariffResponse, BillTariffResponseDTO.class);
    }
}
