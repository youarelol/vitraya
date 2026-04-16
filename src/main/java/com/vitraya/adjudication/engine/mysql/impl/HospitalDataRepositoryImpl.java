package com.vitraya.adjudication.engine.mysql.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class HospitalDataRepositoryImpl {

    private final JdbcTemplate jdbcTemplate;

    public HospitalDataRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // GET all HOSPITALS
    public Optional<Map<String, Object>> getAllHospitals() {
        try {
            String sql = "SELECT COALESCE( JSON_ARRAYAGG(JSON_OBJECT('value', corporate_code, 'label', name)), JSON_ARRAY()) AS hospitals FROM (SELECT DISTINCT c.corporate_code, c.name FROM corporates c INNER JOIN hospital_service_type i ON i.hospital_id = c.id WHERE c.status = 'ACTIVE' AND c.type='HOSPITAL' ORDER BY c.name) sub";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                String json = rs.getString("hospitals");
                return Optional.of(Map.of("hospitals", json));
            });
        }catch (Exception ex){
            return Optional.empty();
        }
    }

    //GET ALL Hospital-RoomType relations json Map that can be cached once and can be load while Create-Claim page
    public Optional<Map<String, Object>> getHospitalRoomTypes(String hospitalCd) {
        try {
            StringBuilder queryString = new StringBuilder("SELECT JSON_OBJECTAGG( sub.hospital_code, sub.room_types ) AS hospital_rooms FROM (SELECT c.corporate_code as hospital_code, COALESCE(JSON_ARRAYAGG(CASE WHEN i.hospital_room_name IS NOT NULL THEN JSON_OBJECT('room_name', i.hospital_room_name,'room_type', i.vitraya_room_type,'category', i.room_category) END),JSON_ARRAY()) AS room_types " +
                    "FROM corporates c INNER JOIN hospital_service_type i ON i.hospital_id = c.id WHERE 1");

            if (hospitalCd!= null && !hospitalCd.isEmpty())
                queryString.append(String.format(" AND c.corporate_code = %s", hospitalCd));

            queryString.append(" GROUP BY c.corporate_code) as sub");
            System.out.println(queryString.toString());
            return jdbcTemplate.queryForObject(queryString.toString(), (rs, rowNum) -> {
                String json = rs.getString("hospital_rooms");
                return Optional.of(Map.of("hospital_rooms", json));
            });
        }catch (Exception ex){
                return Optional.empty();
            }

    }

}
