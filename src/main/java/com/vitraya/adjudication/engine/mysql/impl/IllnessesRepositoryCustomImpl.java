package com.vitraya.adjudication.engine.mysql.impl;


import com.vitraya.adjudication.engine.dto.IllnessResultDTO;
import com.vitraya.adjudication.engine.mysql.IllnessesRepositoryCustom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class IllnessesRepositoryCustomImpl implements IllnessesRepositoryCustom {
    private final JdbcTemplate jdbcTemplate;

    public IllnessesRepositoryCustomImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<IllnessResultDTO> findByProcedureId(String procedureId) {
        String sql = "SELECT default_icd_code, category, name, illness_code FROM illnesses " +
                "WHERE JSON_CONTAINS(relevant_procedures, CAST(? AS JSON)) " +
                "AND active = 1 AND deleted = 0";

        String jsonValue =  procedureId ;

        return jdbcTemplate.query(sql, new Object[]{jsonValue}, (rs, rowNum) ->
                new IllnessResultDTO(
                        rs.getString("default_icd_code"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getString("illness_code")
                )
        );
    }
}
