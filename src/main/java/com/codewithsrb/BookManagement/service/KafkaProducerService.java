package com.codewithsrb.BookManagement.service;

import com.codewithsrb.BookManagement.configuration.Topics;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.CompletableFuture;

/**
 * A simple kafka producer service which sends the data to kafka topic
 * and logs the info before and after sending the data.
 */
@Service
@Slf4j
public class KafkaProducerService {

    private final Topics topics;

    private final KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

    public KafkaProducerService(Topics topics, KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate) {
        this.topics = topics;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(SpecificRecord key, SpecificRecord specificRecord) {

        log.info("********  Producing message to kafka topic : {}  *******", topics.getInputOutputTopic());
        kafkaTemplate.send(topics.getInputOutputTopic(), key, specificRecord);
        log.info("********  Produced message to kafka topic : {}  *********", topics.getInputOutputTopic());

    }
}
