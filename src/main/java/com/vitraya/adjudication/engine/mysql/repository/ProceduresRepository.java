package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.Procedures;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProceduresRepository extends CrudRepository<Procedures, Long> {

    // ToDo: we need to remove the limit from the query
    @Query("select * from procedures where vneuron_sctid_code = :snowmedCode limit 1")
    Procedures findProceduresByVneuronSctidCode(@Param("snowmedCode") String snowmedCode);

    @Query("Select * from procedures where deleted = false")
    List<Procedures> findAllNotDeleted();

    Procedures findTopById(Long id);
}