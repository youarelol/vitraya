package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.CompositeKeyUcr;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrData;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface UcrRepository extends CrudRepository<NivaUcrData, CompositeKeyUcr>{


    @Query("select amount from niva_ucr_rates where procedure_code=:procedure_id and network_type=:networkType and region=:location ")
    public NivaUcrData getAmount(String location, String networkType, String procedure_id);
}
