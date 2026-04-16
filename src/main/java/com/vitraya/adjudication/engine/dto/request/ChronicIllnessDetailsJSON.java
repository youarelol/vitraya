package com.vitraya.adjudication.engine.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class ChronicIllnessDetailsJSON{
	private List<ChronicIllnessListItem> chronicIllnessList;
}