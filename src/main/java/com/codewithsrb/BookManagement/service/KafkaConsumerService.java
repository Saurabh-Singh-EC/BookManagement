package com.codewithsrb.BookManagement.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A simple kafka consumer service which is listening to the topic and logging the consumed records.
 */
@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(List<ConsumerRecord<SpecificRecord, SpecificRecord>> records, Acknowledgment acknowledgment) {
        records.forEach(consumerRecord -> log.info(String.format("************ Read the data from offset: %s. Payload: %s *********", consumerRecord.offset(), consumerRecord.value())));
        acknowledgment.acknowledge();
    }
}