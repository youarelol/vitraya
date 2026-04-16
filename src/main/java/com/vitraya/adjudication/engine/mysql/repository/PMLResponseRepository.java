package com.vitraya.adjudication.engine.mysql.repository;


import com.vitraya.adjudication.engine.mysql.entity.PMLResponse;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface PMLResponseRepository extends CrudRepository<PMLResponse, Long> {
    @Query("select * from pml_response where claim_data_id = :claimId")
    PMLResponse findPMLResponseByClaimDataId(long claimId);

    @Query("select * from pml_response where claim_data_id = :claimId order by id desc limit 1")
    PMLResponse findPMLResponseByClaimDataIdLatest(long claimId);
}
