package com.salatin.orderservice.config;

import com.salatin.orderservice.model.LogMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ReceiverOptions<String, LogMessage> kafkaReceiverOptions(
            @Value(value = "${kafka.topic.log-message}") String topic,
            KafkaProperties kafkaProperties) {

        ReceiverOptions<String, LogMessage> basicReceiverOptions =
                ReceiverOptions.create(kafkaProperties.buildConsumerProperties());
        return basicReceiverOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, LogMessage> reactiveKafkaConsumerTemplate(
            ReceiverOptions<String, LogMessage> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }
}
