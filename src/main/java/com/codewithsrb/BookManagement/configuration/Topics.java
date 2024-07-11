package com.codewithsrb.BookManagement.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kafka.topics")
@Getter
@Setter
public class Topics {

    private String inputOutputTopic;
    private String errorTopic;
}
