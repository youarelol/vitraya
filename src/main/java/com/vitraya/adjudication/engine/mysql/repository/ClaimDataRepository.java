package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ClaimDataRepository extends CrudRepository<ClaimData, Long> {

    @Query("SELECT * FROM claim_data WHERE intimation_number = :intimationNumber")
    Optional<ClaimData> findByIntimationNumber(@Param("intimationNumber") String intimationNumber);

    @Query("SELECT * FROM claim_data WHERE id = :id")
    ClaimData findByClaimDataId(@Param("id") long id);

    @Modifying
    @Query("update claim_data set status = :status where id = :id")
    int updateClaimStatus(@Param("status") String status, @Param("id") long id);

    @Query("SELECT * FROM claim_data WHERE id = :id and deleted = 0")
    ClaimData findByClaimDataIdAndNonDelete(@Param("id") long id);

    @Query("SELECT * FROM claim_data WHERE date_updated >= :thresholdDate and status " +
            "not in ('ENHANCEMENT_SUCCESSFUL', 'ENHANCEMENT_COMPLETED', 'ENHANCEMENT_FAILED') " +
            "and adjudication_status = 'Pending' and deleted = 0")
    List<ClaimData> getClaimsOfPendingStateByTime(@Param("thresholdDate") Date thresholdDate);

    @Query("SELECT * FROM claim_data WHERE date_created <= :thresholdDate and status " +
            "in ('ENHANCEMENT_IN_PROGRESS') " +
            "and adjudication_status <> 'Pending' and  pushed_to_insurer = 0 and deleted = 0")
    List<ClaimData> getClaimsToBePushToInsurer(@Param("thresholdDate") Date thresholdDate);

    @Query("SELECT * FROM claim_data WHERE insurer_identifier is null and is_email_flow =false and  deleted = 0")
    List<ClaimData> findAllPendingPreAuthClaimData();

    @Query("SELECT * FROM claim_data WHERE (claim_status = 'INTERIM_RAISED' || claim_status = 'DISCHARGE_RAISED') " +
            "and deleted = 0")
    List<ClaimData> findAllPendingExtensionClaimData();

    @Modifying
    @Query("update claim_data set pushed_to_insurer = :pushedToInsurerFlag where id = :id")
    void updatePushToInsurerFlag(@Param("id") long id, @Param("pushedToInsurerFlag") boolean pushedToInsurerFlag);

    @Query("SELECT id FROM claim_data WHERE deleted = 0 and date_created >= :startDate and date_created <= :endDate")
    List<Long> findIdsByDateCreatedBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
