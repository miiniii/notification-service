package com.mh.notification.infrastructure.config;

import com.mh.notification.infrastructure.client.mock.MockApiProperties;
import com.mh.notification.infrastructure.client.mock.SecondaryMockApiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("mockApiRestClient")
    public RestClient mockApiRestClient(MockApiProperties mockApiProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(mockApiProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(mockApiProperties.getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(mockApiProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    @Qualifier("secondaryMockApiRestClient")
    public RestClient secondaryMockApiRestClient(SecondaryMockApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

}
