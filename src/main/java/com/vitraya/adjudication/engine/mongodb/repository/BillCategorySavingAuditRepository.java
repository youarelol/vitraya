package com.vitraya.adjudication.engine.mongodb.repository;

import com.vitraya.adjudication.engine.mysql.entity.BillCategorySavingAudit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BillCategorySavingAuditRepository extends CrudRepository<BillCategorySavingAudit, Long> {
//
//    @Modifying
//    @Query(value = "UPDATE bill_category_savings_audit SET status = 'EXPIRED' WHERE bill_module_id = :billModuleId")
//    int markExpiredForModuleId(@Param("billModuleId") Long billModuleId);
}
