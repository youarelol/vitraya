package com.vitraya.adjudication.engine.mongodb.repository;

import com.vitraya.adjudication.engine.mongodb.entity.IntegrationLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationLogRepository extends MongoRepository<IntegrationLogDocument, String> {
}


