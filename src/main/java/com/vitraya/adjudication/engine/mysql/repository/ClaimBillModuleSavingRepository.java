package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimBillModuleSaving;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ClaimBillModuleSavingRepository extends CrudRepository<ClaimBillModuleSaving, Long> {

//    @Query("SELECT * FROM bill_module_saving WHERE claim_data_id = :claimDataId AND stage = :stage")
//    ClaimBillModuleSaving getClaimBillModuleSaving(@Param("claimDataId") long claimDataId,
//                                                   @Param("stage") String stage);
//
//    @Modifying
//    @Query("UPDATE bill_module_saving SET rerun_count = :rerunCount WHERE id = :id")
//    int updateRerunCount(@Param("id") int id, @Param("rerunCount") int rerunCount);
}
