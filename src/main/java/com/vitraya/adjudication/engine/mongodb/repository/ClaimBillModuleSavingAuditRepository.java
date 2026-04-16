package com.vitraya.adjudication.engine.mongodb.repository;

import com.vitraya.adjudication.engine.mysql.entity.ClaimBillModuleSavingAudit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimBillModuleSavingAuditRepository extends CrudRepository<ClaimBillModuleSavingAudit, Long> {

}
