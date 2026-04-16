package com.vitraya.adjudication.engine.dto;

import com.vitraya.adjudication.engine.dto.enums.NotificationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDTO {
    private String username;
    private NotificationTypeEnum notificationType;
    private boolean broadcast;
    private Object message;
}
