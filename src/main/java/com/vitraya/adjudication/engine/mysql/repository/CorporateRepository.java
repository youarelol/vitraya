package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.dto.InsurerDTO;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CorporateRepository extends CrudRepository<Corporate, Long> {

    //    @Modifying
    @Query("select * from corporates where corporate_code = :corporateCode")
    Corporate findCorporateByCorporateCode(@Param("corporateCode") String corporateCode);

    @Query("select * from corporates where name = :name")
    Corporate findCorporateByName(@Param("name") String name);

    @Query("select * from corporates where id = :id")
    Corporate findCorporateById(@Param("id") long id);

    @Query("SELECT corporate_code, name from corporates where status='ACTIVE' AND type='INSURANCE'")
    List<InsurerDTO> findAllInsurer();

    @Query("SELECT c.corporate_code FROM corporates c JOIN claim_data cd ON cd.hospital_id=c.id WHERE cd.intimation_number = :intimationNumber")
    String findHospitalCodeByIntimationNumber(@Param("intimationNumber") String intimationNumber);


}
