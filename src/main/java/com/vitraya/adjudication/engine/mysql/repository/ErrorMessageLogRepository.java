package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ErrorMessageLogs;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ErrorMessageLogRepository extends CrudRepository<ErrorMessageLogs, Long> {

    @Query("SELECT * FROM error_message_logs WHERE claim_data_id = :claimDataId")
    List<ErrorMessageLogs> findByClaimDataId(@Param("claimDataId") long claimDataId);

    @Query("SELECT * FROM error_message_logs WHERE claim_data_id = :claimDataId AND failure_engine = :failureEngine AND claim_stage = :claimStage")
    Optional<ErrorMessageLogs> findByClaimIdErrorReasonClaimStage(@Param("claimDataId") long claimDataId, @Param("failureEngine") String failureEngine, @Param("claimStage") String claimStage);

}
