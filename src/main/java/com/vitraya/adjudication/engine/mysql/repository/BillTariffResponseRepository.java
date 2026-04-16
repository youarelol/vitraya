package com.vitraya.adjudication.engine.mysql.repository;


import com.vitraya.adjudication.engine.mysql.entity.BillTariffResponse;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface BillTariffResponseRepository extends CrudRepository<BillTariffResponse, Long> {

    @Query("SELECT * FROM bill_tariff_response WHERE claim_data_id = :claimDataId ORDER BY id DESC LIMIT 1")
    BillTariffResponse getBillTariffResponseByClaimDataIdOrderByIdDesc(@Param("claimDataId") long claimDataId);

    @Query("SELECT * FROM bill_tariff_response WHERE bill_identifier = :billIdentifier ORDER BY id DESC LIMIT 1")
    BillTariffResponse getBillTariffResponseByBillIdentifier(@Param("billIdentifier") String billIdentifier);

    @Query("SELECT * FROM bill_tariff_response WHERE date_created > :startDate and date_created < :thresholdDate " +
            "and bill_identifier is null")
    List<BillTariffResponse> findAllByPendingBillPendingClaims(@Param("startDate") Date startDate,
                                                               @Param("thresholdDate") Date thresholdDate);

    @Modifying
    @Query("update bill_tariff_response set date_created = :dateCreated where id = :id")
    void updateDateCreated(@Param("id") long id, @Param("dateCreated") Date dateCreated);
}
