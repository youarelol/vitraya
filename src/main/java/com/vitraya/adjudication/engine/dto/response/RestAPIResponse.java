package com.vitraya.adjudication.engine.dto.response;

import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
public class RestAPIResponse {
    private boolean success;
    private Date timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private Object data;

    public RestAPIResponse() {
        super();
    }

    private RestAPIResponse(boolean success) {
        this.success = success;
        this.status = success ? 200 : 400;
        this.timestamp = new Date();
        this.message = success ? "Success" : "Failed";
    }

    public RestAPIResponse(boolean success, LocalDateTime timestamp, int status, String error, String code,
                           String message, Object data) {
        this.success = success;
        this.timestamp = new Date();
        this.status = status;
        this.error = error;
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static RestAPIResponse buildSuccess(Object data) {
        return new RestAPIResponse(Boolean.TRUE, LocalDateTime.now(), 200, null, null, null, data);
    }

    public static RestAPIResponse buildSuccess() {
        return new RestAPIResponse(Boolean.TRUE);
    }


    public static RestAPIResponse buildFail(int status, String error, String code, String message) {
        return new RestAPIResponse(Boolean.FALSE, LocalDateTime.now(), status, error, code, message, null);
    }


    @Override
    public String toString() {
        return GsonUtils.toJson(this);
    }
}
