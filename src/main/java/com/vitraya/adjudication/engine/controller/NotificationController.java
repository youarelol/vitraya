package com.vitraya.adjudication.engine.controller;

import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.service.SseEmittersService;
import com.vitraya.adjudication.engine.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final SseEmittersService emittersService;
    private final UserService userService;

    public NotificationController(SseEmittersService emittersService, UserService userService) {
        this.emittersService = emittersService;
        this.userService = userService;
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(value = "token", required = false) String token) {
        Users user = userService.findUserFromToken(token);
        SseEmitter emitter = emittersService.getUserEmitter(user.getUserid());

        if (emitter != null) {
            return emitter;
        } else {
            emitter = new SseEmitter(0L); // 0L for no timeout
            // Add the emitter to a list of subscribers or handle it in another way
            emittersService.createEmitter(emitter, user.getUserid());
        }

        return emitter;
    }

    // Push event to all or specific user
    /*@PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        emittersService.sendNotification(request);
        return ResponseEntity.ok("Notification sent");
    }*/
}
