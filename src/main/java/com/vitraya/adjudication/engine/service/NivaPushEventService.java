package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.mysql.entity.NivaPushEvent;
import com.vitraya.adjudication.engine.mysql.repository.NivaPushEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class NivaPushEventService {

    private final NivaPushEventRepository nivaPushEventRepository;

    /**
     * Persist a new Niva push event in MySQL.
     */
    public NivaPushEvent recordEvent(String intimationNumber,
                                     String status,
                                     String message, Long timeTakenMs) {
        NivaPushEvent event = new NivaPushEvent();
        event.setIntimationNumber(intimationNumber);
        event.setStatus(status);
        event.setMessage(message);
        event.setTimeTakenMs(timeTakenMs);
        Date now = new Date();
        event.setDateCreated(now);
        event.setDateUpdated(now);
        NivaPushEvent saved = nivaPushEventRepository.save(event);
        log.debug("Recorded Niva push event for {} status {}", intimationNumber, status);
        return saved;
    }

    /**
     * Update message/status for an existing event.
     */
    public NivaPushEvent updateEvent(Long id,
                                     String status,
                                     String message,
                                     Long timeTakenMs) {
        return nivaPushEventRepository.findById(id)
                .map(existing -> {
                    existing.setStatus(status);
                    existing.setMessage(message);
                    existing.setTimeTakenMs(timeTakenMs);
                    existing.setDateUpdated(new Date());
                    return nivaPushEventRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Niva push event not found for id: " + id));
    }

    public NivaPushEvent getEventById(Long id) {
        return nivaPushEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Niva push event not found for id: " + id));
    }
}

