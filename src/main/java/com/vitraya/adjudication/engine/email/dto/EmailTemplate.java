package com.vitraya.adjudication.engine.email.dto;

import com.vitraya.adjudication.engine.dto.enums.ClaimFlowType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
public class EmailTemplate {


    private String subject;
    private String body;
    private ClaimFlowType claimFlowType;
    private String fromEmail;
    private List<String> ackEmails;

    private boolean hasCCEmails;
    private List<String> ccEmails;

    private boolean hasAttachement;
    private ArrayList<File> files;


}
