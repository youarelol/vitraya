package com.vitraya.adjudication.engine.email.service;


import com.vitraya.adjudication.engine.dto.enums.ClaimFlowType;
import com.vitraya.adjudication.engine.dto.enums.DocClaimStage;
import com.vitraya.adjudication.engine.dto.response.PMLResponseDTO;
import com.vitraya.adjudication.engine.email.dto.EmailTemplate;
import com.vitraya.adjudication.engine.email.service.factory.EmailServiceFactory;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mongodb.entity.EmailFlowLogs;
import com.vitraya.adjudication.engine.mongodb.repository.EmailFlowLogsRepository;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.entity.DocumentMaster;
import com.vitraya.adjudication.engine.mysql.entity.Procedures;
import com.vitraya.adjudication.engine.mysql.repository.CorporateRepository;
import com.vitraya.adjudication.engine.mysql.repository.ProceduresRepository;
import com.vitraya.adjudication.engine.service.DocumentService;
import com.vitraya.adjudication.engine.service.S3FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private EmailServiceFactory emailServiceFactory;

    @Autowired
    private S3FileService s3FileService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    EmailFlowLogsRepository emailFlowLogsRepository;

    public boolean sendEmailToInsurer(ClaimData claim,
                                      PMLResponseDTO pmlResponseDTO,
                                      ClaimFlowType claimFlowType) throws Exception {

        EmailInterface emailInterface = emailServiceFactory.getImplementationOfClient(claim.getInsuranceAgencyId());

        EmailTemplate emailTemplate = new EmailTemplate();

        String body = emailInterface.buildEmailBody(claim, pmlResponseDTO,claimFlowType);
        emailTemplate = emailInterface.prepareEmailTemplate(claim,body,getFilesList(claim,claimFlowType),claimFlowType);

        boolean mailsent = emailInterface.sendClaimOverMail(emailTemplate);

        prepareEmailFlowLogObject(emailTemplate, claim, claimFlowType, mailsent);

        log.info("Email sent status: {}", mailsent);

        return mailsent;
    }


    public void prepareEmailFlowLogObject(EmailTemplate emailTemplate,
                                          ClaimData claim,
                                          ClaimFlowType claimFlowType,
                                          boolean isSent) {

        EmailFlowLogs emailFlowLogs = new EmailFlowLogs();

        emailFlowLogs.setEmailSent(isSent);
        emailFlowLogs.setIntiationNumber(claim.getIntimationNumber());
        emailFlowLogs.setEmailBody(emailTemplate.getBody());
        emailFlowLogs.setSubject(emailTemplate.getSubject());
        emailFlowLogs.setClaimFlowType(claimFlowType.toString());
        emailFlowLogs.setPreauthId(claim.getInsurerIdentifier());
        emailFlowLogs.setEmailSentTime(new java.util.Date().toString());
        List<String> filesSent = new ArrayList<>();
        if (emailTemplate.getFiles() != null && !emailTemplate.getFiles().isEmpty()) {
            for(File file : emailTemplate.getFiles()) {
                if (file != null && file.exists()) {
                    filesSent.add(file.getName());
                }
            }
            emailFlowLogs.setFilesSent(String.join(",", filesSent));
        } else {
            emailFlowLogs.setFilesSent("no files sent");
        }

        emailFlowLogsRepository.save(emailFlowLogs);

    }

    public ArrayList<File> getFilesList(ClaimData claim,ClaimFlowType claimFlowType) throws VitrayaException, IOException {

        List<DocumentMaster> documentMasterList = new ArrayList<>();
        if (claimFlowType.equals(ClaimFlowType.PRE_AUTH)){
            documentMasterList = documentService.getClaimDocumentMasterList(claim.getIntimationNumber(),DocClaimStage.PRE_AUTH);
        } else if (claimFlowType.equals(ClaimFlowType.DISCHARGE)) {
            documentMasterList = documentService.getClaimDocumentMasterList(claim.getIntimationNumber(),DocClaimStage.DISCHARGE);
        } else if(claimFlowType.equals(ClaimFlowType.INTERIM_ENHANCEMENT)){
            documentMasterList = documentService.getClaimDocumentMasterList(claim.getIntimationNumber(),DocClaimStage.INTERIM);
        }else if(claimFlowType.equals(ClaimFlowType.RECONSIDER)){
            documentMasterList = documentService.getClaimDocumentMasterList(claim.getIntimationNumber(),DocClaimStage.RECONSIDERATION);
        }else if(claimFlowType.equals(ClaimFlowType.QUERY)){
            documentMasterList = documentService.getClaimDocumentMasterList(claim.getIntimationNumber(),DocClaimStage.QUERY);
        }else if(claimFlowType.equals(ClaimFlowType.SETTLEMENT)){
            documentMasterList = documentService.getClaimDocumentMasterList(claim.getIntimationNumber(),DocClaimStage.SETTLEMENT);
        }


        ArrayList<File> files = s3FileService.getClaimFiles(documentMasterList);

        return files;
    }
}



