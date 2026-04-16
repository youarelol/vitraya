package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.EnhancementConfig;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface EnhancementConfigRepository extends CrudRepository<EnhancementConfig, Long> {

    @Query("select * from enhancement_config where hospital_id = :hospitalId and insurance_agency_id = :insuranceAgencyId")
    EnhancementConfig findByHospitalIdAndInsuranceAgencyId(@Param("hospitalId") long hospitalId,
                                                           @Param("insuranceAgencyId") long insuranceAgencyId);
}
