package com.salatin.orderservice.service;

import com.salatin.orderservice.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderLogMessageProducer implements KafkaProducer<LogMessage> {
    private final ReactiveKafkaProducerTemplate<String, LogMessage> kafkaProducer;

    @Value("${kafka.topic.order-logs}")
    private String logMessageTopic;

    @Override
    public void send(String objectId, LogMessage logMessage) {

        kafkaProducer.send(logMessageTopic, objectId, logMessage)
                .doOnSuccess(senderResult -> log.info("Sent {} offset : {}",
                        logMessage, senderResult.recordMetadata().offset()))
                .subscribe();
    }
}
