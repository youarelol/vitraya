package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.DayCareProceduresMapping;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface DayCareProceduresMappingRepository extends CrudRepository<DayCareProceduresMapping, Long> {
    @Query("select * from day_care_procedures_mapping where procedure_id = :procedureId")
    DayCareProceduresMapping findByProcedureId(long procedureId);
}
