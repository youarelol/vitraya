package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.BillLineItemSaving;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BillLineItemSavingRepository extends CrudRepository<BillLineItemSaving, Long> {

    @Query(value = "SELECT * FROM bill_line_item_saving WHERE bill_module_id = :bill_module_id AND row_id = :rowId " +
            "AND deleted = 0 AND status != 'EXPIRED' LIMIT 1")
    BillLineItemSaving findFirstByBillModuleIdAndRowId(@Param("bill_module_id") long bill_module_id,
                                                       @Param("rowId") long rowId);

    @Query(value = "SELECT * FROM bill_line_item_saving WHERE bill_module_id = :billModuleId " +
            "AND deleted = 0 and status !='EXPIRED'")
    List<BillLineItemSaving> findByClaimBillModuleIdActive(@Param("billModuleId") long billModuleId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE bill_line_item_saving SET status = 'EXPIRED' WHERE bill_module_id = :billModuleId")
    int markExpiredForModuleId(@Param("billModuleId") long billModuleId);
}
