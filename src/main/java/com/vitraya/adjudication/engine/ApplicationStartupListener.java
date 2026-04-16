package com.vitraya.adjudication.engine;

import com.vitraya.adjudication.engine.helper.VitrayaException;
import jakarta.annotation.PostConstruct;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener {
    private final MessageSource messageSource;

    public ApplicationStartupListener(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @PostConstruct
    public void initialize() {
        VitrayaException.setMessageSource(messageSource);
    }
}
