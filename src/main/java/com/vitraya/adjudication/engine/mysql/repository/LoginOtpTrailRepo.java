package com.vitraya.adjudication.engine.mysql.repository;

import com.vitraya.adjudication.engine.mysql.entity.LoginOtpTrail;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginOtpTrailRepo extends CrudRepository<LoginOtpTrail, Long> {
    LoginOtpTrail findByMessageIdAndOtp(String otpIdentifier, String otp);


    @Query("select count(id) from login_otp_trail where mobile_number =:mobile_number and creation_time >:creation_time order by creation_time desc")
    int findTop3ForMobileNumber(@Param("mobile_number") String mobileNumber,@Param("creation_time") LocalDateTime creationTime);
}
