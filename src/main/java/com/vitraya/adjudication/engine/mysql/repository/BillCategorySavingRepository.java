package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.BillCategorySaving;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillCategorySavingRepository extends CrudRepository<BillCategorySaving, Long> {

//    @Query(value = "SELECT * FROM bill_category_savings WHERE bill_module_id = :billModuleId AND category_name = :categoryName AND (status != 'EXPIRED' || status is null) LIMIT 1")
//    BillCategorySaving findFirstByBillModuleIdAndCategoryName(@Param("billModuleId") Long billModuleId,
//                                                              @Param("categoryName") String categoryName);
//
//
//    @Query(value = "SELECT * FROM bill_category_savings WHERE bill_module_id = :billModuleId " +
//            "and status !='EXPIRED'")
//    List<BillCategorySaving> findByBillModuleIdActive(@Param("billModuleId") Long billModuleId);

}
