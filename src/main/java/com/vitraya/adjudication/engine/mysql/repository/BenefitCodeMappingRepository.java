package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.BenefitCodeMapping;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface BenefitCodeMappingRepository extends CrudRepository<BenefitCodeMapping, Long> {

    @Query("select * from benefit_code_mapping where category = :categoryName")
    BenefitCodeMapping findByCategory(@Param("categoryName") String categoryName);
}
