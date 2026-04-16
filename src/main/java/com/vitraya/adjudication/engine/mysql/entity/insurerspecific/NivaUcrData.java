package com.vitraya.adjudication.engine.mysql.entity.insurerspecific;

import jakarta.persistence.EmbeddedId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.Date;

@Table("niva_ucr_rates")
@Getter
@Setter
@Data
public class NivaUcrData {


    @Id
    @EmbeddedId
    @UniqueElements
    private CompositeKeyUcr compositeKeyUcr;

     @Column("amount")
     private double amount;

     @Column("create_time")
     private Date createTime;

     @Column("update_time")
     private Date updateTime;

     @Column("enabled")
     private boolean enable;


}
