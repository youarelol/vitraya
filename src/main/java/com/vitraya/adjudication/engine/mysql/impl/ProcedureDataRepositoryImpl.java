package com.vitraya.adjudication.engine.mysql.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class ProcedureDataRepositoryImpl {

    private final JdbcTemplate jdbcTemplate;

    public ProcedureDataRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //GET ALL PROCEDURES
    public Optional<Map<String, Object>> getAllProcedures() {
        try {
            String sql = "SELECT COALESCE(JSON_ARRAYAGG(JSON_OBJECT('value',vneuron_sctid_code, 'label',name)),JSON_ARRAY()) AS result from procedures WHERE active='1' " +
                    " AND vneuron_sctid_code IS NOT NULL AND TRIM(vneuron_sctid_code) != '' ORDER BY name";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String json = rs.getString("result");
                return Optional.of(Map.of("procedures", json));
            });
        }catch (Exception ex){
            return Optional.empty();
        }
    }

    //GET ALL the illness and Procudures Map that can be cached once and can be load while Create-Claim page
    public Optional<Map<String, Object>> getProcedureIllnesses(long prcId) {
        try {
            StringBuilder queryString = new StringBuilder("SELECT JSON_OBJECTAGG(sub.pcd, sub.illness_list) AS prc_illnesses FROM ( SELECT p.vneuron_sctid_code as pcd, COALESCE(JSON_ARRAYAGG(CASE WHEN i.name IS NOT NULL THEN JSON_OBJECT('icd_code', i.default_icd_code,'name', i.name,'prc', i.relevant_procedures) END), JSON_ARRAY()) AS illness_list FROM procedures p " +
                    "INNER JOIN illnesses i ON  JSON_CONTAINS(i.relevant_procedures, CAST(p.id as CHAR) , '$') WHERE 1 ");

            if (prcId != 0)
                queryString.append(String.format("AND p.vneuron_sctid_code = %d", prcId));

            queryString.append(" AND p.vneuron_sctid_code != '' GROUP BY p.id) as sub");
            System.out.println(queryString.toString());

            return jdbcTemplate.queryForObject(queryString.toString(), (rs, rowNum) -> {
                String json = rs.getString("prc_illnesses");
                return Optional.of(Map.of("prc_illnesses", json));
            });
        }catch (Exception ex){
            return Optional.empty();
        }
    }

}
