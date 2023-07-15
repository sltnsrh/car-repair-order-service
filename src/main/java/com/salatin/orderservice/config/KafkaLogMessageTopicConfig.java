package com.salatin.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaLogMessageTopicConfig {
    @Value("${kafka.topic.order-logs}")
    private String orderLogsTopicName;

    @Bean
    public NewTopic logMessageTopic() {
        return TopicBuilder.name(orderLogsTopicName).build();
    }
}
