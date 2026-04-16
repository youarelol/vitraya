package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimRiderDetails;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ClaimRidersRepository extends CrudRepository<ClaimRiderDetails, Long> {

  @Query("SELECT * FROM claim_riders_details WHERE claim_data_id = :claimDataId")
  ClaimRiderDetails findByClaimDataId(@Param("claimDataId") long claimDataId);


}
