package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.dto.enums.DocClaimStage;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentMasterRepository extends CrudRepository<DocumentMaster, Long> {
    @Query("select * from document_master where parent_table_intimation = :intimationNumber")
    List<DocumentMaster> findByParentTableIntimation(String intimationNumber);

    @Query("select * from document_master where parent_table_intimation = :intimationNumber and stage = :docClaimStage")
    List<DocumentMaster> findByParentTableIntimation(String intimationNumber, DocClaimStage docClaimStage);

    @Query("select * from document_master where parent_table_intimation = :parentTableId and txn_id = :txnId")
    List<DocumentMaster> findByParentTableIdAndTxnId(String parentTableId, String txnId);
}
