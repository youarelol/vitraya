package com.vitraya.adjudication.engine.mysql.mapper;

import com.vitraya.adjudication.engine.dto.response.ClaimDataListDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClaimDataRowMapper implements RowMapper<ClaimDataListDTO.ClaimDataDto> {

    @Override
    public ClaimDataListDTO.ClaimDataDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClaimDataListDTO.ClaimDataDto claimDataDto = new ClaimDataListDTO.ClaimDataDto();
        claimDataDto.setId(String.valueOf(rs.getInt("id")));
        claimDataDto.setIntimationNumber(rs.getString("intimation_number"));
        claimDataDto.setInsurerIdentifier(rs.getString("insurer_identifier"));
        claimDataDto.setPatientName(rs.getString("patient_name"));
        claimDataDto.setHospitalName(rs.getString("name"));
        claimDataDto.setClaimStatus(rs.getString("claim_status"));
        claimDataDto.setAdjudicationStatus(rs.getString("adjudication_status"));
        claimDataDto.setStatus(rs.getString("status"));
        claimDataDto.setDateCreated(rs.getDate("date_created"));
        claimDataDto.setDateUpdated(rs.getDate("date_updated"));
        claimDataDto.setInitialTat(rs.getLong("initial_tat"));
        claimDataDto.setDischargeTat(rs.getLong("discharge_tat"));
        claimDataDto.setPreAuthBillAmount(rs.getBigDecimal("pre_auth_bill_amount"));
        claimDataDto.setDischargeBillAmount(rs.getBigDecimal("discharge_bill_amount"));
        claimDataDto.setPreAuthAmountApproved(rs.getBigDecimal("pre_auth_amount_approved"));
        claimDataDto.setDischargeAmountApproved(rs.getBigDecimal("discharge_amount_approved"));
        claimDataDto.setPreAuthDecision(rs.getString("pre_auth_decision"));
        claimDataDto.setDischargeClaimDecision(rs.getString("discharge_claim_decision"));
        claimDataDto.setBillAmount(claimDataDto.getDischargeBillAmount() != null
                ? claimDataDto.getDischargeBillAmount() : claimDataDto.getPreAuthBillAmount());
        claimDataDto.setApprovedAmount(claimDataDto.getDischargeAmountApproved() != null
                ? claimDataDto.getDischargeAmountApproved() : claimDataDto.getPreAuthAmountApproved());
        claimDataDto.setClaimStatusText(rs.getString("claim_status_text"));
        return claimDataDto;
    }
}
