package com.vitraya.adjudication.engine.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class GoogleChatNotificationService {

    @Value("${google.chat.notification.webhook.url:https://chat.googleapis.com/v1/spaces/AAAAs-dlF18/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=Q2WLPCvrH4eQm4IJDo8XRH75Co4q0FiHw6oilnu_2hg}")
    private String webhookUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleChatNotificationService.class);

    public void sendMessage(String message) {
        GoogleChatNotificationServiceRequestBody requestBody = new GoogleChatNotificationServiceRequestBody(message);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            ResponseEntity<RestApiResponse> responseEntity = requestFactory.getRestTemplate().exchange(webhookUrl,
//                    HttpMethod.POST, new HttpEntity<>(requestBody, headers), RestApiResponse.class);
        } catch (Exception e) {
            LOGGER.info("GoogleChatNotificationService: sendMessage -> Caught exception: " + e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GoogleChatNotificationServiceRequestBody {
        private String text;
    }
}
