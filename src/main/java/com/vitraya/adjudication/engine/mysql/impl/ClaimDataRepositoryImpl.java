package com.vitraya.adjudication.engine.mysql.impl;

import com.vitraya.adjudication.engine.dto.enums.CorporateTypeEnum;
import com.vitraya.adjudication.engine.dto.enums.DataSortEnum;
import com.vitraya.adjudication.engine.dto.request.ClaimListRequestDTO;
import com.vitraya.adjudication.engine.dto.response.ClaimDataListDTO;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.mapper.ClaimDataRowMapper;
import com.vitraya.adjudication.engine.utils.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@Repository
public class ClaimDataRepositoryImpl {

    private final JdbcTemplate jdbcTemplate;
    private final EncryptionUtils EncryptionUtils;

    @Value("${API_PARAM_ENCRYPTION_DECRYTION_KEY}")
    private String apiParamEncryptiondecryptionKey;

    public ClaimDataRepositoryImpl(JdbcTemplate jdbcTemplate, EncryptionUtils encryptionUtils) {
        this.jdbcTemplate = jdbcTemplate;
        this.EncryptionUtils = encryptionUtils;
    }

    public ClaimDataListDTO findClaimData(Corporate corporate, ClaimListRequestDTO claimListRequestDTO)
            throws UnsupportedEncodingException {
        boolean dataByUpdatedDate = true;
        boolean isAttributeQuery = false;
        boolean isInsurer = false;

        if (corporate != null) {
            isInsurer = corporate.getType().equals(CorporateTypeEnum.INSURANCE);
        }

        StringBuilder queryString = new StringBuilder(" select cd.id, cd.intimation_number, cd.insurer_identifier, " +
                " cd.patient_name, c.name, cd.claim_status, cd.adjudication_status, cd.status, cd.date_created, cd.date_updated, " +
                " cd.initial_tat, cd.discharge_tat, car.pre_auth_bill_amount, car.discharge_bill_amount, car.pre_auth_amount_approved, " +
                " car.discharge_amount_approved, car.pre_auth_decision, car.discharge_claim_decision, cd.claim_status_text " +
                " from claim_data cd, corporates c, claim_adjudication_result car " +
                " where c.id = cd.hospital_id and cd.deleted = 0 and cd.id = car.claim_data_id and ");

        StringBuilder countQueryString = new StringBuilder("select count(1) " +
                " from claim_data cd, corporates c, claim_adjudication_result car " +
                " where c.id = cd.hospital_id and cd.deleted = 0 and cd.id = car.claim_data_id and ");

        if (claimListRequestDTO.getDataSortBy().equals(DataSortEnum.DATE_CREATED)) {
            dataByUpdatedDate = false;
            queryString.append(" cd.date_created >= ?").append(" and cd.date_created <= ? ");
            countQueryString.append(" cd.date_created >= ?").append(" and cd.date_created <= ? ");
        } else {
            queryString.append(" cd.date_updated >= ?").append(" and cd.date_updated <= ? ");
            countQueryString.append(" cd.date_updated >= ?").append(" and cd.date_updated <= ? ");
        }

        if (claimListRequestDTO.getAttributeName() != null && !claimListRequestDTO.getAttributeName().isEmpty() &&
                claimListRequestDTO.getAttributeValue() != null && !claimListRequestDTO.getAttributeValue().isEmpty()) {
            isAttributeQuery = true;
            queryString
                    .append(" and ")
                    .append(getAttributeTableColumnName(claimListRequestDTO.getAttributeName()))
                    .append(" LIKE ? ");
            countQueryString.append(" and ")
                    .append(getAttributeTableColumnName(claimListRequestDTO.getAttributeName()))
                    .append("  LIKE ?");
        }

        if (isInsurer) {
            queryString.append(" and cd.insurer_identifier is not null ");
            countQueryString.append(" and cd.insurer_identifier is not null ");
        }

        queryString.append(" order by cd.date_updated desc, cd.id desc ");
        List<ClaimDataListDTO.ClaimDataDto> claimDataListDTOList;
        long totalRecords;

        log.info("Generated query is {}", queryString);
        log.info("Claim list fetch request dto is {}", claimListRequestDTO);
        // First let's get the total count then we will get the results as well.
        if (isAttributeQuery) {
            totalRecords = jdbcTemplate.queryForObject(countQueryString.toString(), Long.class,
                    claimListRequestDTO.getStartDate(), claimListRequestDTO.getEndDate(), "%" + claimListRequestDTO.getAttributeValue() + "%");
        } else {
            totalRecords = jdbcTemplate.queryForObject(countQueryString.toString(), Long.class,
                    claimListRequestDTO.getStartDate(), claimListRequestDTO.getEndDate());
        }

        // Add limit and offset to queryString
        if (claimListRequestDTO.getPageNo() == 0) {
            claimListRequestDTO.setPageNo(1);
        }

        // Now it's time to get the results
        queryString.append(" limit ").append(claimListRequestDTO.getPageSize())
                .append(" offset ").append((claimListRequestDTO.getPageNo() - 1) * claimListRequestDTO.getPageSize());

        if (isAttributeQuery) {
            claimDataListDTOList = jdbcTemplate.query(queryString.toString(), new ClaimDataRowMapper(),
                    claimListRequestDTO.getStartDate(), claimListRequestDTO.getEndDate(), "%" + claimListRequestDTO.getAttributeValue() + "%");
        } else {
            claimDataListDTOList = jdbcTemplate.query(queryString.toString(), new ClaimDataRowMapper(),
                    claimListRequestDTO.getStartDate(), claimListRequestDTO.getEndDate());
        }
        //encryption of id so that claimdata id is not exposed do the encryption also
        for (ClaimDataListDTO.ClaimDataDto claimDataDto : claimDataListDTOList) {
            String id = claimDataDto.getId();
//                String idAsString = String.valueOf(id);
            String encryptedId = EncryptionUtils.encrypt(id, apiParamEncryptiondecryptionKey);
            claimDataDto.setId(encryptedId.replaceAll("/", "_"));
        }

        ClaimDataListDTO claimDataListDTO = new ClaimDataListDTO();
        claimDataListDTO.setTotalClaimCount((int) totalRecords);
        claimDataListDTO.setClaimDataList(claimDataListDTOList);
        return claimDataListDTO;
    }

    private String getAttributeTableColumnName(String attributeName) {
        if (attributeName.equalsIgnoreCase("intimationNumber")) {
            return "cd.intimation_number";
        } else if (attributeName.equalsIgnoreCase("patientName")) {
            return "cd.patient_name";
        } else if (attributeName.equalsIgnoreCase("hospitalName")) {
            return "c.name";
        } else if (attributeName.equalsIgnoreCase("adjudicationStatus")) {
            return "cd.adjudication_status";
        }

        return null;
    }
}
