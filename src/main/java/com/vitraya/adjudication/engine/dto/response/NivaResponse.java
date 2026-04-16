package com.vitraya.adjudication.engine.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class NivaResponse {
    private String Status;
    private String StatusMessage;
    private String PreauthId;
    private List<ErrorList> ErrorList;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    public static class ErrorList {
        private String ErrorMsg;
        private String ErrorCode;
    }
}
