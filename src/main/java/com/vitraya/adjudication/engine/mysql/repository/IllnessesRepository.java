package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.IllnessesRepositoryCustom;
import com.vitraya.adjudication.engine.mysql.entity.Illnesses;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IllnessesRepository extends CrudRepository<Illnesses, Long>, IllnessesRepositoryCustom {
    @Query("select * from illnesses where default_icd_code = :defaultIcdCode")
    List<Illnesses> findIllnessesByDefaultICDCode(@Param("defaultIcdCode") String defaultICDCode);

    @Query("select * from illnesses where name = :illnessName")
    Illnesses findIllnessesByIllnessName(@Param("illnessName") String illnessName);


}