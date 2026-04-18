package com.mh.notification.infrastructure.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.MockApiClient;
import com.mh.notification.infrastructure.client.mock.MockApiProperties;
import com.mh.notification.infrastructure.client.mock.dto.MockApiSendRequest;
import org.junit.jupiter.api.*;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MockApiClientTimeoutIntegrationTest {

    private static WireMockServer wireMockServer;
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private CircuitBreaker circuitBreaker;

    private MockApiClient mockApiClient;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(18081);
        wireMockServer.start();
        configureFor("localhost", 18081);
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        MockApiProperties properties = new MockApiProperties();
        properties.setBaseUrl("http://localhost:18081");
        properties.setSendPath("/mock/send");
        properties.setConnectTimeoutMs(1000);
        properties.setReadTimeoutMs(3000);

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(simpleRequestFactory(
                        properties.getConnectTimeoutMs(),
                        properties.getReadTimeoutMs()
                ))
                .build();

        circuitBreakerFactory = mock(CircuitBreakerFactory.class);
        circuitBreaker = mock(CircuitBreaker.class);

        when(circuitBreakerFactory.create("mockApi")).thenReturn(circuitBreaker);

        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            java.util.function.Supplier<?> toRun = invocation.getArgument(0);
            java.util.function.Function<Throwable, ?> fallback = invocation.getArgument(1);

            try {
                return toRun.get();
            } catch (Throwable t) {
                return fallback.apply(t);
            }
        });

        mockApiClient = new MockApiClient(properties, circuitBreakerFactory, restClient);

        wireMockServer.resetAll();

        mockApiClient = new MockApiClient(properties, circuitBreakerFactory, restClient);

        wireMockServer.resetAll();
    }

    @Test
    void readTimeout_shouldFailAround3Seconds() {
        // given
        stubFor(post(urlEqualTo("/mock/send"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(4000)));

        MockApiSendRequest request = new MockApiSendRequest(
                "req-123456789",
                "SMS",
                "01012345678",
                "결제 완료",
                Map.of("notificationId", 1L)
        );

        long start = System.currentTimeMillis();

        // when
        NotificationSendException exception = assertThrows(
                NotificationSendException.class,
                () -> mockApiClient.send(request, "req-123456789")
        );

        System.out.println("failureType = " + exception.getFailureType());
        System.out.println("message = " + exception.getMessage());
        System.out.println("cause = " + exception.getCause());

        long elapsed = System.currentTimeMillis() - start;

        // then
        assertThat(exception.getFailureType()).isEqualTo(FailureType.TIMEOUT);
        assertThat(elapsed).isGreaterThanOrEqualTo(2500);
        assertThat(elapsed).isLessThan(5000);
    }

    private ClientHttpRequestFactory simpleRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }



}
