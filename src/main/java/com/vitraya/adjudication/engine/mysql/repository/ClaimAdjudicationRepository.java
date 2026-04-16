package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimAdjudicationResult;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ClaimAdjudicationRepository extends CrudRepository<ClaimAdjudicationResult, Long> {
    @Query("SELECT * FROM claim_adjudication_result WHERE claim_data_id = :id")
    ClaimAdjudicationResult findByClaimDataId(@Param("id") long id);

    @Query("SELECT * FROM claim_adjudication_result WHERE claim_data_id = :id and claim_stage = :claimStage")
    ClaimAdjudicationResult findByClaimStageAndClaimDataId(@Param("id") long id, @Param("claimStage") String claimStage);

    @Query("SELECT * FROM claim_adjudication_result WHERE date_created > :startDate and date_created < :thresholdDate " +
            "and pre_auth_bill_amount is null and claim_stage = 'preauth_request'")
    List<ClaimAdjudicationResult> getPendingClaimsListPreAuth(@Param("startDate") Date startDate, @Param("thresholdDate") Date thresholdDate);

    @Query("SELECT * FROM claim_adjudication_result WHERE date_created > :startDate and date_created < :thresholdDate " +
            "and discharge_bill_amount is null and claim_stage = 'final_enhancement_request'")
    List<ClaimAdjudicationResult> getPendingClaimsListDischarge(@Param("startDate") Date startDate, @Param("thresholdDate") Date thresholdDate);

    ClaimAdjudicationResult findTopByClaimDataIdOrderByIdDesc(@Param("claimDataId") long claimDataId);
}
