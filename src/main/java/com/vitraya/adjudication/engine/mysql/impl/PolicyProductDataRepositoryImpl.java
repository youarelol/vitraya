package com.vitraya.adjudication.engine.mysql.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class PolicyProductDataRepositoryImpl {

    private final JdbcTemplate jdbcTemplate;

    public PolicyProductDataRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //GET ALL POLICIES
    public Optional<Map<String, Object>> getAllPolicies() {
        try {
            String sql = "SELECT COALESCE(JSON_ARRAYAGG(JSON_OBJECT('value',product_code, 'label',policy_name)),JSON_ARRAY()) AS policies " +
                    " from policy_product WHERE active='1'ORDER BY policy_name" ;
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String json = rs.getString("policies");
                return Optional.of(Map.of("policies", json));
            });
        }catch (Exception ex){
            return Optional.empty();
        }

    }

}
