package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.dto.ClaimRunDTO;
import com.vitraya.adjudication.engine.utils.AppConstants;
import com.vitraya.adjudication.engine.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class KafkaListenerService {

    private final ConcurrentHashMap<String, Long> processedMessages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> retryCountMap = new ConcurrentHashMap<>();
    private final ClaimService claimService;
    private final VNeuronService vNeuronService;

    @Value("${app.kafka.process.timeout}")
    private long processTimeout;

    @Value("${app.kafka.retry.limit}")
    private int retryLimit;

    public KafkaListenerService(ClaimService claimService, VNeuronService vNeuronService) {
        this.claimService = claimService;
        this.vNeuronService = vNeuronService;
    }

    private boolean isDuplicate(String message) {
        long currentTime = System.currentTimeMillis();
        return processedMessages.compute(message, (key, lastProcessedTime) -> {
            if (lastProcessedTime == null || (currentTime - lastProcessedTime) > processTimeout) {
                return currentTime;
            }
            return lastProcessedTime;
        }) != currentTime;
    }

    private boolean shouldRetry(String messageKey, Acknowledgment acknowledgment) {
        int count = retryCountMap.getOrDefault(messageKey, 0);
        if (count < retryLimit) {
            retryCountMap.put(messageKey, count + 1);
            log.warn("Retry attempt {} for message {}", count + 1, messageKey);
            return true;
        } else {
            retryCountMap.remove(messageKey);
            log.error("Max retry reached. Acknowledging message to avoid reprocessing: {}", messageKey);
            acknowledgment.acknowledge();
            return false;
        }
    }

    @KafkaListener(topics = AppConstants.CLAIM_TOPIC_NAME,
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "${app.kafka.listener.count}")
    public void processClaimMessage(ConsumerRecord<String, String> record, String message, Acknowledgment acknowledgment) {
        log.info("Thread: threadNeme - {}, topic - {}, partition- {}, offset - {}, value - {}",
                Thread.currentThread().getName(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.value());

        if (isDuplicate(message)) {
            log.error("Duplicate claim message. Skipping: {}", message);
            acknowledgment.acknowledge();
            return;
        }

        try {
            log.info("Processing claim message: {}", message);
            claimService.processClaim(message);
            acknowledgment.acknowledge();
            retryCountMap.remove(message);
        } catch (Exception e) {
            log.error("Error processing claim message: {}", message, e);
//            shouldRetry(message, acknowledgment);
            acknowledgment.acknowledge();
        }
    }


    @KafkaListener(topics = AppConstants.EMAIL_FLOW_CLAIM_TOPIC_NAME,
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "${app.kafka.listener.count}")
    public void processEmailClaimMessage(ConsumerRecord<String, String> record, String message, Acknowledgment acknowledgment) {
        log.info("Thread: threadNeme - {}, topic - {}, partition- {}, offset - {}, value - {}",
                Thread.currentThread().getName(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.value());

        if (isDuplicate(message)) {
            log.error("Duplicate claim message. Skipping: {}", message);
            acknowledgment.acknowledge();
            return;
        }

        try {
            log.info("Processing claim message: {}", message);
            claimService.sendClaimFromEmailFlow(message);
            acknowledgment.acknowledge();
            retryCountMap.remove(message);

        } catch (Exception e) {
            log.error("Error processing claim message: {}", message, e);
            acknowledgment.acknowledge();

        }
    }

    @KafkaListener(topics = AppConstants.NON_ADJUDICATION_CLAIM_TOPIC_NAME,
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "${app.kafka.listener.count}")
    public void processNonAdjudicationClaimMessage(ConsumerRecord<String, String> record, String message, Acknowledgment acknowledgment) {
        log.info("Thread: threadNeme - {}, topic - {}, partition- {}, offset - {}, value - {}",
                Thread.currentThread().getName(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.value());

        if (isDuplicate(message)) {
            log.error("Duplicate claim message. Skipping: {}", message);
            acknowledgment.acknowledge();
            return;
        }

        try {
            log.info("Processing claim message: {}", message);
            claimService.processNonAdjudicationClaim(message);
            acknowledgment.acknowledge();
            retryCountMap.remove(message);

        } catch (Exception e) {
            log.error("Error processing claim message: {}", message, e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = AppConstants.VNEURON_TOPIC_NAME,
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "${app.kafka.listener.count}")
    public void processVNeuronMessage(ConsumerRecord<String, String> record, String message, Acknowledgment acknowledgment) {
        log.info("Thread: threadNeme - {}, topic - {}, partition- {}, offset - {}, value - {}",
                Thread.currentThread().getName(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.value());

        if (isDuplicate(message)) {
            log.warn("Duplicate vNeuron message. Skipping: {}", message);
            acknowledgment.acknowledge();
            return;
        }

        try {
            log.info("Processing vNeuron message: {}", message);
            ClaimRunDTO claimRunDTO = GsonUtils.fromJson(message, ClaimRunDTO.class);
            vNeuronService.processVNeuronRequest(claimRunDTO);
            claimService.checkClaimAutoSubmissionCondition(claimRunDTO.getClaimId());
            acknowledgment.acknowledge();
            retryCountMap.remove(message);
        } catch (Exception e) {
            log.error("Error processing vNeuron message: {}", message, e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = AppConstants.TARIFF_PML_TOPIC_NAME,
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "${app.kafka.listener.count}")
    public void processClaimTariffPMLMessage(ConsumerRecord<String, String> record, String message, Acknowledgment acknowledgment) {
        log.info("Thread: threadNeme - {}, topic - {}, partition- {}, offset - {}, value - {}",
                Thread.currentThread().getName(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.value());

        if (isDuplicate(message)) {
            log.warn("Duplicate PML message. Skipping: {}", message);
            acknowledgment.acknowledge();
            return;
        }

        try {
            log.info("Processing Tariff PML message: {}", message);
            claimService.processClaimTariffPML(message);
            acknowledgment.acknowledge();
            retryCountMap.remove(message);
        } catch (Exception e) {
            log.error("Error processing Tariff PML message: {}", message, e);
            acknowledgment.acknowledge();
        }
    }
}
