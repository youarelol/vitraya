package com.vitraya.adjudication.engine.mysql.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table("niva_push_events")
public class NivaPushEvent {

    @Id
    private Long id;

    @Column("intimation_number")
    private String intimationNumber;

    @Column("status")
    private String status;

    @Column("message")
    private String message;

    @Column("time_taken_ms")
    private Long timeTakenMs;

    @Column("date_created")
    private Date dateCreated;

    @Column("date_updated")
    private Date dateUpdated;
}


