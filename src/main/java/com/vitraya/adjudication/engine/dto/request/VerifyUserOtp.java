package com.vitraya.adjudication.engine.dto.request;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class VerifyUserOtp {
    @Value("${otp.length}")
    private int otpLength;
    private String otp;
    private String otpIdentifier;
    private String username;

    @Override
    public String toString() {
        return "VerifyUserOtp{" +
                "otp='" + otp + '\'' +
                ", otpIdentifier='" + otpIdentifier + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    public void validate() throws VitrayaException {
        if (otp == null || otp.isEmpty() || otp.length() < otpLength) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_OTP_RECEIVED);
        }

        if (otpIdentifier == null || otpIdentifier.isEmpty()) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_OTP_VERIFICATION_REQUEST);
        }

        if (username == null || username.isEmpty()) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_OTP_VERIFICATION_REQUEST);
        }
    }
}
