package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimModuleStats;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ClaimModuleStatsRepository extends CrudRepository<ClaimModuleStats, Long> {
    @Query("select * from claim_module_stats where claim_data_id = :claimDataId order by id desc limit 1")
    ClaimModuleStats findLatestByClaimDataId(@Param("claimDataId") long claimDataId);

    @Query("select * from claim_module_stats where claim_data_id = :claimDataId and claim_stage = :claimStage order by id desc limit 1")
    ClaimModuleStats findLatestByClaimStageAndClaimDataId(@Param("claimDataId") long claimDataId, @Param("claimStage") String claimStage);

    @Query("select * from claim_module_stats where id = :id")
    ClaimModuleStats findClaimModuleStatsById(@Param("id") long id);

    @Modifying
    @Query("update claim_module_stats set medical_identifier = :identifier, medical_tat = :tat, date_updated = now()" +
            " where id = :id")
    int updateVneuronStats(@Param("identifier") String identifier, @Param("tat") int tat, @Param("id") long id);

    @Modifying
    @Query("update claim_module_stats set bill_identifier = :identifier, bill_tariff_tat = :tat, date_updated = now()" +
            " where id = :id")
    int updateBillStats(String identifier, int tat, long id);

    @Modifying
    @Query("update claim_module_stats set pml_identifier = :identifier, pml_tat = :tat, date_updated = now()" +
            " where id = :id")
    int updatePMLStats(String identifier, int tat, long id);
}
