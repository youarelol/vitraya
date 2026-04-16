package com.vitraya.adjudication.engine.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class HospitalClaimMetadata{
	private DocumentsNameWithLocation documents_name_with_location;
	private DocumentsNameWithLocation full_doctype_path;
	private List<String> doc_types;
}