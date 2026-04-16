package com.vitraya.adjudication.engine.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class UserRequest {
    private String username;

    @NotNull(message = "Password is required")
    private String password;

    @NotNull
    private String email;
    private String mobile_number;
    private String status;
    private String corporate_id;
    private String role;
}
