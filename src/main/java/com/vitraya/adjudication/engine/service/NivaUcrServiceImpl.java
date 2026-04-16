package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaProviderMeta;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrData;
import com.vitraya.adjudication.engine.mysql.repository.NivaProviderMetaRepository;
import com.vitraya.adjudication.engine.mysql.repository.UcrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class NivaUcrServiceImpl implements InsurerSpecificLimitsService {

    @Autowired
    NivaProviderMetaRepository nivaProviderMetaRepository;

    @Autowired
    UcrRepository ucrRepository;

    @Override
    public BigDecimal getUpperLimit(String procedure_code, String hospital_code) {

        NivaUcrData ucrRates = null;
        NivaProviderMeta nivaProviderMeta=nivaProviderMetaRepository.findNivaProviderMetaByProviderCode(hospital_code);
        if (nivaProviderMeta == null) {
            return BigDecimal.ZERO;
        }
        ucrRates=ucrRepository.getAmount(nivaProviderMeta.getProviderRegion(), nivaProviderMeta.getNetworkType(), procedure_code);
        if(ucrRates == null){
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(ucrRates.getAmount());
    }
}
