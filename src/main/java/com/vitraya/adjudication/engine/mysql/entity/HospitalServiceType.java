package com.vitraya.adjudication.engine.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Table("hospital_service_type")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalServiceType {
    @Id
    private long id;

    @Column("hospital_id")
    private long hospitalId;

    @Column("hospital_room_name")
    private String hospitalRoomName;

    @Column("vitraya_room_type")
    private String vitrayaRoomType;

    @Column("room_category")
    private String insurerRoomCategory;

    @Column("room_tariff_day")
    private BigDecimal roomTariffDay;

    @Column("service_code")
    private String serviceCode;

    @JsonIgnore
    @Column("date_created")
    private Date createTime;

    @JsonIgnore
    @Column("date_updated")
    private Date updateTime;

    @Column("enabled")
    private boolean enabled;

    @Column("private_ac")
    private boolean privateAc;

    @Column("insurer_room_category_mapping")
    private String insurerRoomCategoryMapping;
}
