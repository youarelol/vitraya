package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.InsurerFetchResponses;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsurerFetchResponsesRepository extends CrudRepository<InsurerFetchResponses, Long> {
    InsurerFetchResponses getInsurerFetchResponsesByClaimDataId(long id);

    @Query("SELECT * FROM insurer_fetch_responses WHERE claim_intimation_number = :intimationNumber")
    InsurerFetchResponses getInsurerFetchResponsesByClaimIntimationNumber(String intimationNumber);
}