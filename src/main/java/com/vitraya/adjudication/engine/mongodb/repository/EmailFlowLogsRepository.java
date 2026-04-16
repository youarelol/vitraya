package com.vitraya.adjudication.engine.mongodb.repository;

import com.vitraya.adjudication.engine.mongodb.entity.EmailFlowLogs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailFlowLogsRepository extends MongoRepository<EmailFlowLogs, String> {

    List<EmailFlowLogs> findAllByIntiationNumber(String intiationNumber);
    List<EmailFlowLogs> findByPreauthId(String preauthId);

}
