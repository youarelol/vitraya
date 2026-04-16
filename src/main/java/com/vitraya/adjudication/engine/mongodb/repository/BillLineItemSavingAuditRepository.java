package com.vitraya.adjudication.engine.mongodb.repository;

import com.vitraya.adjudication.engine.mysql.entity.BillLineItemSavingAudit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BillLineItemSavingAuditRepository extends CrudRepository<BillLineItemSavingAudit, Long> {
//
//    @Modifying
//    @Query(value = "UPDATE bill_line_item_saving_audit SET status = 'EXPIRED' WHERE bill_module_id = :billModuleId")
//    int markExpiredForModuleId(@Param("billModuleId") Long billModuleId);
}
