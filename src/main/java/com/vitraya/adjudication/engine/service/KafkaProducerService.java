package com.vitraya.adjudication.engine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, long key, String message) {
        try {
            log.info("Sending message to topic: {}, message: {}", topic, message);
            Random random = new Random();
            int randomNumber = random.nextInt(12);
            kafkaTemplate.send(topic, randomNumber, String.valueOf(key), message);
        } catch (Exception e) {
            log.error("Error while sending message to topic: {}, message: {}", topic, message, e);
        }
    }
}
