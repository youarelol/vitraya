package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimAdmissionDetails;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimAdmissionDetailsRepository extends CrudRepository<ClaimAdmissionDetails, Long> {

    @Query("SELECT * FROM claim_admission_details WHERE claim_data_id = :id order by id desc limit 1")
    ClaimAdmissionDetails findByClaimId(long id);
}