package com.vitraya.adjudication.engine.dto.request;

import lombok.Data;

@Data
public class DocumentsItem{
	private String docId;
	private String document;
	private String docUrl;
	private String documentName;

	public String getDocId(){
		return docId;
	}

	public String getDocument(){
		return document;
	}

	public String getDocUrl(){
		return docUrl;
	}

	public String getDocumentName(){
		return documentName;
	}
}
