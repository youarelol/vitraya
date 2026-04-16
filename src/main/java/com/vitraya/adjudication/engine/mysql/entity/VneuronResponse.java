package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.response.VNeuronResponseDTO;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "vneuron_response")
public class VneuronResponse {
    @Id
    private long id;

    @Column("claim_data_id")
    private long claimDataId;

    @Column("medical_identifier")
    private String medicalIdentifier;

    @Column("vneuron_response")
    private String vnueronResponse;

    @CreationTimestamp
    @Column("date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column("date_updated")
    private Date dateUpdated;

    @Column("vneuron_tat")
    private String vneuronTat;

    @Column("vneuron_decision")
    private String vneuronDecision;

    @Transient
    private VNeuronResponseDTO vNeuronResponseDTO;

    public VNeuronResponseDTO getVNeuronResponseDTO() {
        if (vNeuronResponseDTO != null) {
            return vNeuronResponseDTO;
        } else if (vnueronResponse != null) {
            vNeuronResponseDTO = GsonUtils.fromJson(vnueronResponse, VNeuronResponseDTO.class);
        }

        return null;
    }
}
