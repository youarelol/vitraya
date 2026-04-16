package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("day_care_procedures_mapping")
public class DayCareProceduresMapping {
    @Id
    @Column("id")
    private long id;

    @Column("procedure_id")
    private long procedureId;

    @Column("procedure_name")
    private String procedureName;
}
