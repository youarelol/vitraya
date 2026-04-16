package com.vitraya.adjudication.engine.service;


import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaProviderMeta;
import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaUcrData;
import com.vitraya.adjudication.engine.mysql.repository.NivaProviderMetaRepository;
import com.vitraya.adjudication.engine.mysql.repository.UcrRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class UCRDataService {

      private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UCRDataService.class);
      @Autowired
      private UcrRepository ucrRepository;
      @Autowired
      private NivaProviderMetaRepository nivaProviderMetaRepository;

      public BigDecimal getProcedureAmount(String providerCode, String procedureCode) {
                  NivaUcrData ucrRates = null;
                  NivaProviderMeta nivaProviderMeta=nivaProviderMetaRepository.findNivaProviderMetaByProviderCode(providerCode);
                  if (nivaProviderMeta == null) {
                        return BigDecimal.ZERO;
                  }
                  ucrRates=ucrRepository.getAmount(nivaProviderMeta.getProviderRegion(), nivaProviderMeta.getNetworkType(), procedureCode);
                  if(ucrRates == null){
                        return BigDecimal.ZERO;
                  }

                  return BigDecimal.valueOf(ucrRates.getAmount());
      }
}
