package com.salatin.orderservice.config;

import com.salatin.orderservice.model.dto.LogMessage;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;
    @Value("${kafka.groupid.consumer.messages}")
    private String groupMessageConsumerId;

    @Bean
    public ReactiveKafkaConsumerTemplate<String, LogMessage> reactiveKafkaConsumerTemplate(
            ReceiverOptions<String, LogMessage> kafkaReceiverOptions) {
        return new ReactiveKafkaConsumerTemplate<>(kafkaReceiverOptions);
    }

    @Bean
    public ReceiverOptions<String, LogMessage> kafkaReceiverOptions(
            @Value(value = "${kafka.topic.order-logs}") String topic,
            KafkaProperties kafkaProperties) {

        Map<String, Object> props = kafkaProperties.buildConsumerProperties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,groupMessageConsumerId);
        props.put(JsonDeserializer.TRUSTED_PACKAGES,"com.salatin.*");
        props.put(JsonDeserializer.TYPE_MAPPINGS, "message:com.salatin.orderservice.model.dto.LogMessage");

        ReceiverOptions<String, LogMessage> basicReceiverOptions = ReceiverOptions.create(props);

        return basicReceiverOptions.subscription(Collections.singletonList(topic));
    }
}
