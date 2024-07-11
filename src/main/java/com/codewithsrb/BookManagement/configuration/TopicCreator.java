package com.codewithsrb.BookManagement.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for creating the topics.
 */
@Configuration
public class TopicCreator {

    private final Topics topics;

    public TopicCreator(Topics topics) {
        this.topics = topics;
    }

    @Bean
    NewTopic createInputOutputTopic() {
        return new NewTopic(topics.getInputOutputTopic(), 1, (short)1);
    }

    @Bean
    NewTopic createErrorTopic() {
        return new NewTopic(topics.getErrorTopic(), 1, (short)1);
    }
}
