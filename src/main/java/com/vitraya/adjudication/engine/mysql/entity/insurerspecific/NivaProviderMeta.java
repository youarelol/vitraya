package com.vitraya.adjudication.engine.mysql.entity.insurerspecific;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("niva_provider_meta")
@Getter
@Setter
@ToString
public class NivaProviderMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column("provider_code")
    private String providerCode;

    @Column("provider_name")
    private String providerName;

    @Column("provider_region")
    private String providerRegion;

    @Column("network_type")
    private String networkType;

}
