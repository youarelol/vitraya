package com.vitraya.adjudication.engine.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MessageApiResponse {

    private String code;
    private String text;

    public MessageApiResponse(String code, String text) {
        this.code = code;
        this.text = text;
    }
}
