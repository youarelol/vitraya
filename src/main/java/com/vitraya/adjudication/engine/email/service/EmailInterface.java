package com.vitraya.adjudication.engine.email.service;

import com.vitraya.adjudication.engine.dto.enums.ClaimFlowType;
import com.vitraya.adjudication.engine.dto.response.PMLResponseDTO;
import com.vitraya.adjudication.engine.email.dto.EmailTemplate;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.ClaimData;

import java.io.File;
import java.util.ArrayList;

public interface EmailInterface {

    boolean sendClaimOverMail(EmailTemplate emailTemplate) throws Exception;

    EmailTemplate prepareEmailTemplate(ClaimData claim,
                                       String body,
                                       ArrayList<File> files,
                                       ClaimFlowType claimFlowType);

    String buildEmailBody(ClaimData claim,
                          PMLResponseDTO pmlResponseDTO,
                          ClaimFlowType claimFlowType);
}
