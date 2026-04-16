package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.response.PMLResponseDTO;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Data
@Table(name = "pml_response")
public class PMLResponse {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("pml_identifier")
    private String pmlIdentifier;

    @Column("pml_response")
    private String pmlResponse;

    @Column("pml_tat")
    private String pmlTat;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;

    @Column("pml_decision")
    private String pmlDecision;

    @Transient
    private PMLResponseDTO pmlResponseDTO;

    public PMLResponseDTO getPmlResponseDTO() {
        if (pmlResponseDTO != null) {
            return pmlResponseDTO;
        } else {
            return pmlResponseDTO = GsonUtils.fromJson(pmlResponse, PMLResponseDTO.class);
        }
    }

    public BigDecimal getApprovedAmount() {
        BigDecimal approvedAmount = null;
        if (getPmlResponseDTO() != null && pmlResponseDTO != null && pmlResponseDTO.getClaim_result() != null
                && pmlResponseDTO.getClaim_result().getClaim_approved_amount() != null) {
            approvedAmount = pmlResponseDTO.getClaim_result().getClaim_approved_amount();
        }
        log.info("In getApprovedAmount: pmlResponseDTO is: {}", pmlResponseDTO);

        return approvedAmount;
    }
}
