package com.vitraya.adjudication.engine.helper;

import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.MessageSource;

import java.util.Locale;


public class VitrayaException extends RuntimeException {
    // Static method to set MessageSource
    @Setter
    private static MessageSource messageSource; // Static for global use
    @Getter
    private final String code;
    private final String message;

    // Constructor with error code only
    public VitrayaException(String code) {
        this(code, null);
    }

    public VitrayaException(VitrayaErrorCodes vitrayaErrorCodes) {
        this(vitrayaErrorCodes.getCode(), null);
    }

    // Constructor with error code and arguments
    public VitrayaException(String code, Object[] args) {
        super(resolveMessage(code, args));
        this.code = code;
        this.message = resolveMessage(code, args);
    }

    // Constructor with error code and arguments
    public VitrayaException(VitrayaErrorCodes vitrayaErrorCodes, Object[] args) {
        this(resolveMessage(vitrayaErrorCodes.getCode(), args));
    }

    @Override
    public String getMessage() {
        return message;
    }

    // Message resolution logic
    private static String resolveMessage(String code, Object[] args) {
        if (messageSource == null) {
            throw new IllegalStateException("MessageSource is not initialized");
        }
        String messageKey = VitrayaErrorCodes.getMessageKey(code);
        return messageSource.getMessage(messageKey, args, Locale.getDefault());
    }

}
