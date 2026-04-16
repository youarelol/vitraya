package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.dto.enums.ClaimStatus;
import com.vitraya.adjudication.engine.mysql.entity.ClaimRejectionReason;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ClaimRejectionReasonRepo extends CrudRepository<ClaimRejectionReason, Long> {
    @Query("select * from claim_rejection_reason where claim_data_id = :id and claim_stage = :claimStatus and claim_status = :adjudicationStatus")
    ClaimRejectionReason findByClaimIdClaimStageAndStatus(long id, ClaimStatus claimStatus, String adjudicationStatus);
}
