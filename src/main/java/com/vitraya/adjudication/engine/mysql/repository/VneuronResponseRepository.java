package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.VneuronResponse;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface VneuronResponseRepository extends CrudRepository<VneuronResponse, Long> {

    @Query("SELECT * FROM vneuron_response WHERE claim_data_id = :claimDataId ORDER BY id DESC LIMIT 1")
    VneuronResponse getVneuronResponseByClaimDataId(@Param("claimDataId") long claimDataId);

}
