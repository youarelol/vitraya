package com.vitraya.adjudication.engine.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import org.apache.commons.mail.MultiPartEmail;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
@Slf4j
public class CommunicationService {
    @Value("${exception.email.sender}")
    private String emailSender;

    @Value("${email.smtp.host}")
    private String smtpHost;

    @Value("${email.smtp.port}")
    private int smtpPort;

    @Value("${email.smtp.auth}")
    private String smtpEnableAuthentication;

    @Value("${email.smtp.tls.enable}")
    private String smtpEnableTls;

    @Value("${smtp.connection.username}")
    private String CONNECTION_USERNAME;

    @Value("${smtp.connection.password}")
    private String CONNECTION_PASSWORD;

    @Value("${environment.name}")
    private String ENVIRONMENT_NAME;

    @Value("${mail.communication.enable}")
    private boolean MAIL_COMMUNICATION_ENABLE;

    @Value("${otp.validity.min}")
    private int otpValidityMin;

    @Value("${aws.sns.access.key.id}")
    private String awsSnsAccessKeyId;

    @Value("${aws.sns.secret.access.key.id}")
    private String awsSnsSecretAccessKeyId;

    @Value("${vitraya.env}")
    private String vitrayaEnv;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationService.class);

    public void sendEmail(String subject, String content, String emailRecipients) {
        if (!MAIL_COMMUNICATION_ENABLE) {
            return;
        }

        try {
            MultiPartEmail email = new MultiPartEmail();
            email.setHostName(smtpHost);
            email.setSmtpPort(smtpPort);
            email.setAuthenticator(new DefaultAuthenticator(CONNECTION_USERNAME, CONNECTION_PASSWORD));
            email.setSSLOnConnect(true);
            email.setFrom("Vitraya <" + emailSender + ">");
            email.setSubject(subject + " | " + vitrayaEnv);
            email.addPart(content, "text/html; charset=UTF-8");
            for (String recipient : emailRecipients.split(",")) {
                email.addTo(recipient.trim());
            }
            email.send();
            log.info("Email sent successfully for subject {}", subject);
        } catch (EmailException e) {
            log.error("Caught exception while sending email: ", e);
        }
    }

    public void sendEmailWithAttachment(String subject, String content, String emailRecipients, java.io.File attachment) {
        if (!MAIL_COMMUNICATION_ENABLE) {
            return;
        }

        try {
            MultiPartEmail email = new MultiPartEmail();
            email.setHostName(smtpHost);
            email.setSmtpPort(smtpPort);
            email.setAuthenticator(new DefaultAuthenticator(CONNECTION_USERNAME, CONNECTION_PASSWORD));
            email.setSSLOnConnect(true);
            email.setFrom("Vitraya <" + emailSender + ">");
            email.setSubject(subject + " | " + vitrayaEnv);
            email.addPart(content, "text/html; charset=UTF-8");
            for (String recipient : emailRecipients.split(",")) {
                email.addTo(recipient.trim());
            }
            if (attachment != null && attachment.exists()) {
                org.apache.commons.mail.EmailAttachment att = new org.apache.commons.mail.EmailAttachment();
                att.setPath(attachment.getAbsolutePath());
                att.setDisposition(org.apache.commons.mail.EmailAttachment.ATTACHMENT);
                att.setName(attachment.getName());
                email.attach(att);
            }
            email.send();
            log.info("Email with attachment sent successfully for subject {}", subject);
        } catch (org.apache.commons.mail.EmailException e) {
            log.error("Caught exception while sending email with attachment: ", e);
        }
    }

    public String sendSMS(String mobileNumber, String otp) {
        // AWS credentials
        String accessKeyId = awsSnsAccessKeyId;
        String secretAccessKey = awsSnsSecretAccessKeyId;
        Region region = Region.AP_SOUTH_1; // Set your appropriate region

        // Create SNS client
        SnsClient snsClient = SnsClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();

        // Send SMS
        PublishRequest request = PublishRequest.builder()
                .phoneNumber("+91" + mobileNumber) // Include country code
                .message("Your OTP for Login is: " + otp + ". OTP is valid for " + otpValidityMin + " min. Please do not share with anyone. Thanks Vitraya.")
                .build();

        PublishResponse response = snsClient.publish(request);
        LOGGER.info("SMS Message ID: " + response.messageId());
        // Close the SNS client
        snsClient.close();

        return response.messageId();
    }
}
