package com.vitraya.adjudication.engine.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DocumentsItem {
    private String fileName;
    private String check_sum;
    private String original_document_url;
    private String ocr_output_url;
    private boolean docAvailable;


    @Data
    public static class DocumentDetails{
        private String name;
        private List<String> alias;
        private String parent_category;
    }
}
