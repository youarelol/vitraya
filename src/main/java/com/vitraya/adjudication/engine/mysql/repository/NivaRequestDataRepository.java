package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.NivaRequestData;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NivaRequestDataRepository extends CrudRepository<NivaRequestData, Long> {
    // ToDO: please check with and understand the requirement here.

    @Query("select * from niva_request_data where claim_data_id = :claimDataId and request_type = :requestType order by id desc limit 1")
    NivaRequestData findByClaimIdAndRequestType(long claimDataId, String requestType);

    NivaRequestData findTopByClaimDataIdOrderByIdDesc(long claimDataId);
}