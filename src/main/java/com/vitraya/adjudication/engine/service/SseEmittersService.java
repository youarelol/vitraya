package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.NotificationRequestDTO;
import com.vitraya.adjudication.engine.dto.enums.NotificationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmittersService {
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public void createEmitter(SseEmitter emitter, String userId) {
        /*userEmitters.put(userId, emitter);

        // Remove emitter when complete or timeout
        emitter.onCompletion(() -> userEmitters.remove(userId));
        emitter.onTimeout(() -> userEmitters.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("message").data(
                    new NotificationRequestDTO(userId, NotificationTypeEnum.WELCOME_NOTIFICATION, false, "Connected")));
        } catch (Exception e) {
            log.error("Error while creating emitter", e);
            emitter.completeWithError(e);
        }*/
    }

    public void sendNotification(NotificationRequestDTO notificationRequestDTO) {
        /*try {
            if (userEmitters != null) {
                if (notificationRequestDTO.isBroadcast()) {
                    userEmitters.forEach((user, emitter) -> sendToEmitter(emitter, notificationRequestDTO));
                } else {
                    SseEmitter emitter = userEmitters.get(notificationRequestDTO.getUsername());
                    if (emitter != null) {
                        sendToEmitter(emitter, notificationRequestDTO);
                    }
                }
            } else {
                log.error("No user emitters found, current size is {}", userEmitters.size());
            }
        } catch (Exception e) {
            log.error("Error while sending notification", e);
        }*/
    }

    private void sendToEmitter(SseEmitter emitter, NotificationRequestDTO notificationRequestDTO) {
        /*try {
            emitter.send(SseEmitter.event().name("message").data(notificationRequestDTO));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }*/
    }

    public NotificationRequestDTO getNotificationRequestDTO(String userId, String message, NotificationTypeEnum notificationType) {
        // return new NotificationRequestDTO(userId, notificationType, false, message);
        return null;
    }

    public SseEmitter getUserEmitter(String userid) {
        // return userEmitters.get(userid);
        return null;
    }

    public void createAndSendNotification(String userid, String message, NotificationTypeEnum notificationTypeEnum) {
        // ToDo: This is the temporary solution to handle the null userId
        /*if (userid == null) {
            for (String user : userEmitters.keySet()) {
                userid = user;
                break;
            }
        }

        NotificationRequestDTO notificationRequestDTO = getNotificationRequestDTO(
                userid, message, notificationTypeEnum);

        sendNotification(notificationRequestDTO);*/
    }
}
