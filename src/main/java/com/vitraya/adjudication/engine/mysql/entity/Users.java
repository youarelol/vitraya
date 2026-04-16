package com.vitraya.adjudication.engine.mysql.entity;

import com.vitraya.adjudication.engine.dto.enums.UserStatusEnum;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
@Table(name = "users")
public class Users {
    @Id
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column("mobile_number")
    private String mobileNumber;

    @Column("status")
    private UserStatusEnum status;

    @Column("password_attempts")
    private int passwordAttempts;

    @Column("corporate_id")
    private int corporateId;

    @Column("block_time")
    private Date blockTime;

    @Column("created_by")
    private String createdBy;

    @Column("updated_by")
    private String updatedBy;

    @Column("date_created")
    private Date createdOn;

    @Column("date_updated")
    private Date updatedOn;

    private int role;

    private String userid;

    @Column("token")
    private String token;

    @Column("refresh_token")
    private String refreshToken;

    @Column("otp")
    private String otp;

    @Column("otp_identifier")
    private String otpIdentifier;

    @Column("otp_created")
    private Date otpCreated;
}
