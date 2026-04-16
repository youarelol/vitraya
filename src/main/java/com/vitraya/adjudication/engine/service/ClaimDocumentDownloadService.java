package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.request.VitrayaInsurerClaimDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ClaimDocumentDownloadService {

    void saveDocuments(VitrayaInsurerClaimDataDTO vitrayaInsurerClaimData, MultipartFile[] files) throws IOException;
}
