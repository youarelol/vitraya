package com.vitraya.adjudication.engine.rulesengine.niva.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthenticationResponseDTO {
    private String Status;
    private String Token;
    private String ErrorMsg;
}
