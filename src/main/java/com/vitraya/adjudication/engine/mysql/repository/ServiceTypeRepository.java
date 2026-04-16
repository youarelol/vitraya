package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.ServiceType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceTypeRepository extends CrudRepository<ServiceType, Long> {
    @Query("select * from service_type where vitraya_master = :vitrayaMaster and benefit_type = :benefitType and is_enabled = :enabled")
    List<ServiceType> findByVitrayaMasterAndBenefitTypeAndIsEnabled(String vitrayaMaster, String benefitType, boolean enabled);
}