package com.vitraya.adjudication.engine.service;

import com.jcraft.jsch.*;
import com.vitraya.adjudication.engine.dto.enums.ExternalEntity;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;
import com.vitraya.adjudication.engine.mysql.entity.NivaPushEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SftpIntegrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpIntegrationService.class);

    @Value("${niva.wdms.sftp.host:1.6.8.22}")
    private String remoteHost;

    @Value("${niva.wdms.sftp.username:claims.vitraya_uat}")
    private String username;

    @Value("${niva.wdms.sftp.password:8ELbjqvpF2xB@#2023}")
    private String password;

    @Value("${niva.wdms.sftp.port:2811}")
    private int port;

    @Value("${niva.wdms.sftp.working.dir:UAT/Pre_Auth_Documents_UAT/}")
    private String remoteWorkingDir;

    @Value("${wdms.sftp.max.retry.count: 2}")
    private Integer sftpMaxRetryCountLimit;

    @Value("${niva.wdms.sftp.connection.timeout:30000}")
    private int conectionTimeout;

    @Value("${old.sftp.keys:true}")
    private boolean oldsftpkeys;

    @Value("${base.local.directory:/tmp/}")
    private String tmpDirectoryLocal;

    private final IntegrationLogService integrationLogService;

    public SftpIntegrationService(IntegrationLogService integrationLogService) {
        this.integrationLogService = integrationLogService;
    }


    public boolean connectWithSftp(String wdmsUniqueNo, ClaimData claimData,
                                   ArrayList<File> files, NivaPushEvent nivaPushEvent) {
        long startTime = System.currentTimeMillis();
        List<String> uploadedFileNames = new ArrayList<>();
        String SFTPHOST = remoteHost;
        int SFTPPORT = port;
        String SFTPUSER = username;
        String SFTPPASS = password;
        String SFTPWORKINGDIR = remoteWorkingDir;
        int timeout = conectionTimeout; //30 sec
//        String SFTPWORKINGDIR = "Claims/Others/";

        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        LOGGER.info("preparing the host information for sftp.");

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();

            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.setTimeout(timeout);
            session.connect();
            LOGGER.info("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            LOGGER.info("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            channelSftp.cd(SFTPWORKINGDIR);

            List<String> duplicateFileCheck = new ArrayList<>();

            for (File file : files) {
                if (duplicateFileCheck.contains(file.getName())) {
                    continue;
                }
                duplicateFileCheck.add(file.getName());
                String fileName = "Claims^Others^" + wdmsUniqueNo + "^" + file.getName();
                uploadedFileNames.add(fileName);
                LOGGER.info("claim -{} file {}", claimData.getId(), fileName);
                int retryCount = 0;
                while (retryCount < sftpMaxRetryCountLimit) {
                    retryCount = retryCount + 1;
                    LOGGER.info("claim -{} trying to upload file to sftp retry count : {}", claimData.getId(), retryCount);
                    FileInputStream fileupload = new FileInputStream(file);
                    channelSftp.put(fileupload, fileName);
//                    if (checkFileUploadedCompletely(channelSftp, fileName, file, claimData.getId())) {
//                        LOGGER.info("claim -{} file uploaded succesfully after {} retries", claimData.getId(), retryCount);
//                        break;
//                    }
                    fileupload.close();
//                    LOGGER.info("claim -{} file uploaded failed for retry count {} ", claimData.getId(), retryCount);
                }
            }
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();

            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.SFTP.toString(), "-", uploadedFileNames, "SUCCESS", endTime - startTime, "Files uploaded successfully");
        } catch (Exception ex) {
            long endTime = System.currentTimeMillis();
            integrationLogService.logInteraction(nivaPushEvent.getId(), claimData.getIntimationNumber(),
                    ExternalEntity.SFTP.toString(), "-", uploadedFileNames, ex.getLocalizedMessage(), endTime - startTime, "Files upload failed");
            LOGGER.error("claim -{} Exception found while transfer the response. {}", claimData.getId(), ex.getLocalizedMessage());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean checkFileUploadedCompletely(ChannelSftp sftpChannel, String fileName, File localFile, long claimId) {
        try {

            SftpATTRS remoteFileAttributes = sftpChannel.stat(fileName);
            long remoteFileSize = remoteFileAttributes.getSize();

            long localFileSize = localFile.length();

            LOGGER.info("claim -{} original file size : {} uploaded file size : {}", claimId, localFileSize, remoteFileSize);
            if (remoteFileSize == localFileSize) {
                LOGGER.info("claim -{} File upload successful.", claimId);
                return true;
            } else {
                LOGGER.info("claim -{} File upload incomplete.", claimId);
                return false;
            }
        } catch (Exception e) {
            LOGGER.info("claim -{} exception occured in comparing remote and local file sizes  {}", claimId, e.getLocalizedMessage());
            e.printStackTrace();
            // Handle the exception
        }
        return false;
    }
}
