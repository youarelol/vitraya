package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.insurerspecific.NivaProviderMeta;
import org.springframework.data.repository.CrudRepository;

public interface NivaProviderMetaRepository extends CrudRepository<NivaProviderMeta, Long> {

    NivaProviderMeta findNivaProviderMetaByProviderCode(String providerCode);
}
