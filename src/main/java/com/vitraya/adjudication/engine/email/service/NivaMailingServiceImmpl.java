package com.vitraya.adjudication.engine.email.service;


import com.vitraya.adjudication.engine.dto.enums.ClaimFlowType;
import com.vitraya.adjudication.engine.dto.response.PMLResponseDTO;
import com.vitraya.adjudication.engine.email.dto.EmailTemplate;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mongodb.entity.EmailFlowLogs;
import com.vitraya.adjudication.engine.mongodb.repository.EmailFlowLogsRepository;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.mysql.entity.Procedures;
import com.vitraya.adjudication.engine.mysql.repository.CorporateRepository;
import com.vitraya.adjudication.engine.mysql.repository.ProceduresRepository;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NivaMailingServiceImmpl implements EmailInterface {

    @Autowired
    CorporateRepository corporateRepository;

    @Autowired
    ProceduresRepository proceduresRepository;



    @Value("${nivabupa.hostname:email-smtp.ap-south-1.amazonaws.com}")
    private String host;

    @Value("${nivabupa.smtp.port:465}")
    private int port;

    @Value("${nivabupa.host.username:AKIAXNWU2QGVKEXO27LH}")
    private String username;

    @Value("${nivabupa.host.password:BImH6BNbjn/aJRxnDGPdKwOFvWTwhryNX/Lb+Z9zEjla}")
    private String password;

    @Value("${nivabupa.host.email:claims.nbhi@vitrayatech.com}")
    private String hostemail;

    @Value("${nivabupa.target.email:devang.chavda@vitraya.com}")
    private String targetemail;

    @Value("${nivabupa.target.email.for.ds:devang.chavda@vitraya.com}")
    private String targetEmailForDS;

    @Value("${nivabupa.internal.monitor.email:vdiclientmax@gmail.com,himalaya.patwal@vitraya.com,rishi.vashisth@vitraya.com}")
    private String vitrayaMonitorEmail;

    @Value("${amazon.aws.accessKey:AKIAXCDDPMZL356BIG6V}")
    private String accessKey;

    @Value("${amazon.aws.secretKey:i2jHNzSzPDepUYsmNKAucNypY5Tv/fTNjivPvEe3}")
    private String secretKey;

    @Value("${amazon.aws.bucket.name:claim-docs-dev}")
    private String bucket_name;

    @Value("${amazon.aws.s3.base.directory:Claims/}")
    private String baseDirectoryInS3;

    @Value("${base.local.directory:/tmp/}")
    private String tmpDirectoryLocal;

    @Value("${os.specific.slash:/}")
    private String osSpecificSlash;

    @Override
    public boolean sendClaimOverMail(EmailTemplate emailTemplate) throws Exception {
        MultiPartEmail email = new MultiPartEmail();
        List<InternetAddress> bccList = Arrays.asList(InternetAddress.parse(vitrayaMonitorEmail));
        try {
            if (emailTemplate.isHasAttachement()) {
                for (File file : emailTemplate.getFiles()) {
                    EmailAttachment attachment = new EmailAttachment();
                    attachment.setPath(file.getAbsolutePath());
                    attachment.setDisposition(EmailAttachment.ATTACHMENT);
                    attachment.setDescription(file.getName());
                    attachment.setName(file.getName());
                    email.attach(attachment);
                }
            } else {
            }
            email.setHostName(host);
            email.setSmtpPort(port);
            email.setAuthenticator(new DefaultAuthenticator(username, password));
            email.setSSLOnConnect(true);
            email.setFrom("Vitraya Claims <" + hostemail + ">");
            email.setSubject(emailTemplate.getSubject());
            email.addPart(emailTemplate.getBody(), "text/html; charset=UTF-8");
            try {
                if (emailTemplate.getClaimFlowType() != null) {
                    if (emailTemplate.getClaimFlowType().equals(ClaimFlowType.SETTLEMENT)) {
                        email.addTo(targetEmailForDS);
                    } else {
                        email.addTo(targetemail);
                    }
                } else {
                    email.addTo(targetemail);
                }
            } catch (Exception e) {
                email.addTo(targetemail);
                e.printStackTrace();
            }

            email.setBcc(bccList);
            email.send();


//            deleteLocalFiles(emailTemplate.getFiles());

            return true;

        } catch (Exception e) {
            e.printStackTrace();

//            deleteLocalFiles(emailTemplate.getFiles());

            return false;
        }

    }



    public void deleteLocalFiles(ArrayList<File> files) {
        try {
            if (!files.isEmpty()) {
                for (File file : files) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public EmailTemplate prepareEmailTemplate(ClaimData claim, String body, ArrayList<File> files, ClaimFlowType claimFlowType) {


        EmailTemplate emailTemplate = new EmailTemplate();

        String claimType = "";

//        emailTemplate.setClaimFlowType(claim.getClaimType());

        if (claimFlowType.equals(ClaimFlowType.INTERIM_ENHANCEMENT)) {
            claimType = "Extension";
        } else if (claimFlowType.equals(ClaimFlowType.RECONSIDER)) {
            claimType = "Reconsideration";
        } else if (claimFlowType.equals(ClaimFlowType.PRE_AUTH)) {
            claimType = "Preauth";
        }
        // this is for after refactoring
        else if (claimFlowType.equals(ClaimFlowType.DISCHARGE)) {
            claimType = "Discharge";
        } else if (claimFlowType.equals(ClaimFlowType.SETTLEMENT)) {
            claimType = "Settlement";
        }else if (claimFlowType.equals(ClaimFlowType.QUERY)) {
            claimType = "Query reply";
        }
        String subject = claimType + "\t" + claim.getIntimationNumber();

        if (claimType.equalsIgnoreCase("Settlement")) {
            subject = claimType + "\t" + " Preauth_id- " + claim.getInsurerIdentifier() + "\t" + claim.getIntimationNumber();
            ;
        }

        body = body + "<br><br> In case of any escalations please contact the <b>" + hostemail + "</b><br><br>" +
                "Thanks & Regards<br><br>";

        if (!files.isEmpty()) {
            emailTemplate.setHasAttachement(true);
            emailTemplate.setFiles(files);
        }

        emailTemplate.setSubject(subject);
        emailTemplate.setBody(body);

        List<String> emptyList = new ArrayList<>();
        emailTemplate.setAckEmails(emptyList);

        return emailTemplate;
    }

    @Override
    public String buildEmailBody(ClaimData claim,
                                 PMLResponseDTO pmlResponseDTO,
                                 ClaimFlowType claimFlowType) throws VitrayaException {

        BigDecimal estimatedAmount = BigDecimal.ZERO;

        String claimType = "";
        if (claimFlowType.equals(ClaimFlowType.INTERIM_ENHANCEMENT)) {
            claimType = "Extension";
        } else if (claimFlowType.equals(ClaimFlowType.RECONSIDER)) {
            claimType = "Reconsideration";
        } else if (claimFlowType.equals(ClaimFlowType.SETTLEMENT)) {
            claimType = "Settlement";
        } else if (claimFlowType.equals(ClaimFlowType.PRE_AUTH)) {
            claimType = "Preauth";
        } else if (claimFlowType.equals(ClaimFlowType.DISCHARGE)) {
            claimType = "Discharge";
        }else if (claimFlowType.equals(ClaimFlowType.QUERY)) {
            claimType = "Query reply";
        }

        Corporate corporate = corporateRepository.findCorporateById(claim.getHospitalId());
        String hospitalName = corporate.getName();

        long procedureid = claim.getProcedureId();

        Procedures procedureDTO = proceduresRepository.findTopById(procedureid);
        String procedureName = "";
        if (procedureDTO != null) {
            procedureName = procedureDTO.getName();
        }

        String body = "";
        if (pmlResponseDTO == null || claimType.equalsIgnoreCase("Query reply")) {
            //this is manual claim
            if(claimType.equalsIgnoreCase("Query reply")){

                body = "Query reply sent from hospital.Files are attached in this email.";

            }else {
                body = "Respected Sir/Madam,<br><br><br>" +
                        "Greetings from <b>" + hospitalName + "</b>. Please note the " + claimType + " hospitalization " +
                        "intimation for <b>" + claim.getPatientName() + "</b> for <b>" + procedureName +
                        " </b>Claim supporting documents are as attached.<br><br>" +
                        "Kindly consider this as an intimation letter for the processing the Medi claim for <b>" + claim.getPatientName() + "</b><br><br>" +
                        "provider code : <b>" + corporate.getCorporateCode() + "</b><br><br>";
            }
        } else {
            try {
                estimatedAmount = pmlResponseDTO.getTotal_claim_amount();
            } catch (Exception e) {
                e.printStackTrace();
            }

            body = "Respected Sir/Madam,<br><br><br>" +
                    "Greetings from <b>" + hospitalName + "</b>. Please note the " + claimType + " hospitalization " +
                    "intimation for <b>" + claim.getPatientName() + "</b> for <b>" + procedureName + "</b>" +
                    "</b> claim, Intimation for <b>INR " + estimatedAmount + "</b><br><br>" +
                    "Claim supporting documents are as attached.<br><br>" +
                    "Kindly consider this as an intimation letter for the processing the Mediclaim for <b>" + claim.getPatientName() + "</b><br><br>" +
                    "Member id : <b>" + claim.getMedicalCardNumber() + "</b><br>" +
                    "provider code : <b>" + corporate.getCorporateCode() + "</b><br><br>";

            if (!claimType.equalsIgnoreCase("Preauth")) {
                body = body + "preauth ID : <b>" + claim.getInsurerIdentifier() + "</b><br><br>";
            }

            if (pmlResponseDTO.getClaim_result().isClaim_approved()) {
                body = body + "Claim is approved for <b>INR " + pmlResponseDTO.getClaim_result().getClaim_approved_amount() + "</b><br><br>";
            } else {
                body = body + "Claim is rejected </b><br><br>";
            }

            if (pmlResponseDTO.getClaim_result().isClaim_approved()) {
                body = body + pmlResponseDTO.getClaim_result().getCoverageReasons()
                        .stream().collect(Collectors.joining("<br><br>"));
            } else {
                body = body + pmlResponseDTO.getClaim_result().getCoverageFailedReasons()
                        .stream().collect(Collectors.joining("<br><br>"));
            }
        }

        return body;
    }
}
