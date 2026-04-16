package com.vitraya.adjudication.engine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
public class UserDto {
    private String username;
    private String role; // Or use RoleDto/Role entity if needed
    private Date dateCreated;
    private Date dateUpdated;
}

