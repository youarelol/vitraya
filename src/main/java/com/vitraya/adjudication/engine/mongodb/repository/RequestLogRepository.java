package com.vitraya.adjudication.engine.mongodb.repository;

import com.vitraya.adjudication.engine.mongodb.entity.RequestLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLogRepository extends MongoRepository<RequestLogDocument, String> {

}
