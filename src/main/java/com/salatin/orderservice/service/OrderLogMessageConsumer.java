package com.salatin.orderservice.service;

import com.salatin.orderservice.model.dto.LogMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Log4j2
public class OrderLogMessageConsumer {
    private final ReactiveKafkaConsumerTemplate<String, LogMessage> consumerTemplate;
    private final OrderService orderService;

    public Flux<LogMessage> consumeOrderLogUpdates() {
        return consumerTemplate.receiveAutoAck()
                .doOnNext(record -> {
                    var orderId = record.key();
                    var logMessage = record.value();
                    var offset = record.offset();

                    log.info("Received key={}, value={} from topic={}, offset={}",
                            orderId, logMessage, record.topic(), offset);

                    orderService.addLogToOrder(orderId, logMessage);
                })
                .map(ConsumerRecord::value);
    }

    @PostConstruct
    public void init() {
        log.info("In init(), running consumeOrderLogUpdates()");
        this.consumeOrderLogUpdates().subscribe();
    }
}
