package com.mh.notification.infrastructure.client.mock;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "secondary-mock-api")
@Getter
@Setter
public class SecondaryMockApiProperties {

    private String baseURL;
    private String sendPath;
    private int connectTimeoutMs;
    private int readTimeoutMs;
}
