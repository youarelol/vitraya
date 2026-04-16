package com.vitraya.adjudication.engine.mysql.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Table("login_otp_trail")
public class LoginOtpTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column("user_name")
    private String userName;

    @Column("mobile_number")
    private String mobileNumber;

    @Column("otp")
    private String otp;

    @Column("message_id")
    private String messageId;

    @Column("creation_time")
    private Date creationTime;

    @Column("expiry_time")
    private Date expiryTime;

    @Column("otp_consumed")
    private boolean otpConsumed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isOtpConsumed() {
        return otpConsumed;
    }

    public void setOtpConsumed(boolean otpConsumed) {
        this.otpConsumed = otpConsumed;
    }
}
