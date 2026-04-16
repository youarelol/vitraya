package com.vitraya.adjudication.engine.mysql.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitraya.adjudication.engine.dto.enums.DocClaimStage;
import com.vitraya.adjudication.engine.dto.enums.DocumentStatusEnum;
import com.vitraya.adjudication.engine.dto.enums.UploadFilePrefix;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("document_master")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DocumentMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;

    @JsonIgnore
    @Column("parent_table_id")
    private long parentTableId;

    @JsonIgnore
    @Column("parent_table_intimation")
    private String parentTableIntimation;

    @JsonIgnore
    @Column("document_type")
    @Enumerated(value = EnumType.STRING)
    private UploadFilePrefix documentType;

    @Column("file_name")
    private String fileName;

    @Column("storage_file_name")
    @JsonIgnore
    private String storageFileName;

    @Column("file_type")
    @JsonIgnore
    private String fileType;

    @Column("file_supported")
    private boolean fileSupported;

    @Column("doc_path")
    @JsonIgnore
    private String docPath;

    @Column("document_status")
    @JsonIgnore
    private int documentStatus;

    @Column("omni_docs_image_index")
    @JsonIgnore
    private String omniDocsImageIndex;

    @Column("date_created")
    @JsonIgnore
    private Date dateCreated;

    @Column("date_updated")
    @JsonIgnore
    private Date dateUpdated;

    @Column("stage")
    @Enumerated(value = EnumType.STRING)
    private DocClaimStage stage;

    @Column("latest_bill_documents")
    @JsonIgnore
    private boolean latestBillDocuments;

    @Column("pre_signed_url")
    private String preSignedUrl;

    @Column("txn_id")
    private String txnId;

    @Column("doc_available")
    private boolean docAvailable;

    public void setDocumentStatus(DocumentStatusEnum status) {
        this.documentStatus = status.getState();
    }
}
