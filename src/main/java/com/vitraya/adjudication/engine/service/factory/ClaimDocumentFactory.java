package com.vitraya.adjudication.engine.service.factory;

import com.vitraya.adjudication.engine.mysql.entity.Corporate;
import com.vitraya.adjudication.engine.service.ClaimDocumentDownloadService;
import com.vitraya.adjudication.engine.service.VitrayaClaimDocumentDownloadMultipartService;
import com.vitraya.adjudication.engine.service.VitrayaClaimDocumentDownloadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClaimDocumentFactory {
    public final VitrayaClaimDocumentDownloadService vitrayaClaimDocumentDownloadService;
    public final VitrayaClaimDocumentDownloadMultipartService vitrayaClaimDocumentDownloadMultipartService;

    public ClaimDocumentFactory(VitrayaClaimDocumentDownloadService vitrayaClaimDocumentDownloadService,
                                VitrayaClaimDocumentDownloadMultipartService vitrayaClaimDocumentDownloadMultipartService) {
        this.vitrayaClaimDocumentDownloadService = vitrayaClaimDocumentDownloadService;
        this.vitrayaClaimDocumentDownloadMultipartService = vitrayaClaimDocumentDownloadMultipartService;
    }

    public ClaimDocumentDownloadService getDocumentDownloadFactory(String payorCode, MultipartFile[] files) {
        // ToDo: convert this to a insurer document download mapping. Instead of checking insurer code
        if (files != null && files.length > 0) {
            return vitrayaClaimDocumentDownloadMultipartService;
        }
        if (payorCode.equalsIgnoreCase("NIVA")) {
            return vitrayaClaimDocumentDownloadService;
        }

        return null;
    }
}
