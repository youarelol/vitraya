package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class DocumentMasterListItem{
	private int id;
	private String note;
	private String fileName;
	private boolean fileSupported;
	private int documentStatus;
	private String storageFileName;
	private String fileType;
	private String docPath;
}