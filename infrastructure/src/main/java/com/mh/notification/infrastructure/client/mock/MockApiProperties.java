package com.mh.notification.infrastructure.client.mock;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mock-api")
@Getter
@Setter
public class MockApiProperties {

    private String baseUrl;
    private String sendPath;
    private int connectTimeoutMs;
    private int readTimeoutMs;

}