package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimTransition;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimTransitionRepository extends CrudRepository<ClaimTransition, Long> {
    @Query("select * from claim_transition where claim_data_id = :claimId order by id desc")
    List<ClaimTransition> findAllClaimTransitionByClaimDataIdLatest(@Param("claimId") long claimId);
}