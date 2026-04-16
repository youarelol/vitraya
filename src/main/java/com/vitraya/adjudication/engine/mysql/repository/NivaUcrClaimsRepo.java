package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrClaims;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;

public interface NivaUcrClaimsRepo extends CrudRepository<NivaUcrClaims,Long> {
    @Query("select * from niva_ucr_claims where claim_data_id = :claimDataId order by id desc limit 1")
    NivaUcrClaims findLatestByClaimDataId(@Param("claimDataId") long claimDataId);

    @Modifying
    @Query("update niva_ucr_claims set pml_approved_amount = :pmlApprovedAmount, final_approved_amount = :finalApprovedAmount, date_updated = now()" +
            " where id = :id")
    void updateUcrEntry(@Param("pmlApprovedAmount") BigDecimal pmlApprovedAmount, @Param("finalApprovedAmount") BigDecimal finalApprovedAmount,
                        @Param("id") long id);

}
