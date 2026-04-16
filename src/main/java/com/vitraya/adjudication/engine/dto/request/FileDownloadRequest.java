package com.vitraya.adjudication.engine.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDownloadRequest {
    private int id;
    private String hospitalCode;
}
