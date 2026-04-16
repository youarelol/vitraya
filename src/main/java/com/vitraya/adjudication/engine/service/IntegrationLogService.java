package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.mongodb.entity.IntegrationLogDocument;
import com.vitraya.adjudication.engine.mongodb.repository.IntegrationLogRepository;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationLogService {

    private final IntegrationLogRepository integrationLogRepository;

    /**
     * Generic helper to persist integration logs for any external/internal interaction.
     */
//    public IntegrationLogDocument logInteraction(String pushId,
//                                                 String identifier,
//                                                 String entity,
//                                                 String url,
//                                                 Object request,
//                                                 Object response,
//                                                 Long timeTakenMillis,
//                                                 String notes) {
//        return logInteraction(pushId, identifier, entity, url, request, response, timeTakenMillis, notes);
//    }

    public IntegrationLogDocument logInteraction(long pushId,
                                                 String identifier,
                                                 String entity,
                                                 String url,
                                                 Object request,
                                                 Object response,
                                                 Long timeTakenMillis,
                                                 String notes) {
        IntegrationLogDocument document = IntegrationLogDocument.builder()
                .pushId(pushId)
                .identifier(identifier)
                .entity(entity)
                .url(url)
                .request(GsonUtils.toJson(request))
                .response(GsonUtils.toJson(response))
                .timeTakenMs(timeTakenMillis)
                .notes(notes)
                .build();
        document.markCreated();
        IntegrationLogDocument saved = integrationLogRepository.save(document);
        log.debug("Saved integration log for {} - {}", entity, identifier);
        return saved;
    }

    /**
     * Update an existing log when additional info is available.
     */
    public IntegrationLogDocument updateResponse(String logId,
                                                 Object response,
                                                 Long timeTakenMillis,
                                                 String notes) {
        return integrationLogRepository.findById(logId)
                .map(existing -> {
                    existing.setResponse(GsonUtils.toJson(response));
                    existing.setTimeTakenMs(timeTakenMillis);
                    existing.setNotes(notes);
                    existing.markUpdated();
                    return integrationLogRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Integration log not found for id: " + logId));
    }
}

