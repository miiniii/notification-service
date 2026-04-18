package com.mh.notification.infrastructure.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.MockApiClient;
import com.mh.notification.infrastructure.client.mock.MockApiProperties;
import com.mh.notification.infrastructure.client.mock.SecondaryMockApiClient;
import com.mh.notification.infrastructure.client.mock.SecondaryMockApiProperties;
import com.mh.notification.infrastructure.client.mock.dto.MockApiSendRequest;
import org.junit.jupiter.api.*;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationGatewayFallbackIntegrationTest {

    private static WireMockServer primaryServer;
    private static WireMockServer secondaryServer;

    private NotificationSendGateway notificationSendGateway;

    @BeforeAll
    static void beforeAll() {
        primaryServer = new WireMockServer(18081);
        secondaryServer = new WireMockServer(18082);

        primaryServer.start();
        secondaryServer.start();
    }

    @AfterAll
    static void afterAll() {
        if (primaryServer != null) {
            primaryServer.stop();
        }
        if (secondaryServer != null) {
            secondaryServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        primaryServer.resetAll();
        secondaryServer.resetAll();

        MockApiProperties mockApiProperties = new MockApiProperties();
        mockApiProperties.setBaseUrl("http://localhost:18081");
        mockApiProperties.setSendPath("/mock/send");
        mockApiProperties.setConnectTimeoutMs(1000);
        mockApiProperties.setReadTimeoutMs(3000);

        SecondaryMockApiProperties secondaryMockApiProperties = new SecondaryMockApiProperties();
        secondaryMockApiProperties.setBaseUrl("http://localhost:18082");
        secondaryMockApiProperties.setSendPath("/mock/send");
        secondaryMockApiProperties.setConnectTimeoutMs(1000);
        secondaryMockApiProperties.setReadTimeoutMs(3000);

        RestClient primaryRestClient = RestClient.builder()
                .baseUrl(mockApiProperties.getBaseUrl())
                .requestFactory(simpleRequestFactory(
                        mockApiProperties.getConnectTimeoutMs(),
                        mockApiProperties.getReadTimeoutMs()
                ))
                .build();
        RestClient secondaryRestClient = RestClient.builder()
                .baseUrl(secondaryMockApiProperties.getBaseUrl())
                .requestFactory(simpleRequestFactory(
                        secondaryMockApiProperties.getConnectTimeoutMs(),
                        secondaryMockApiProperties.getReadTimeoutMs()
                ))
                .build();

        CircuitBreakerFactory<?, ?> circuitBreakerFactory = mock(CircuitBreakerFactory.class);
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);

        when(circuitBreakerFactory.create(any())).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            Supplier<?> toRun = invocation.getArgument(0);
            Function<Throwable, ?> fallback = invocation.getArgument(1);

            try {
                return toRun.get();
            } catch (Throwable t) {
                return fallback.apply(t);
            }
        });

        MockApiClient mockApiClient = new MockApiClient(
                mockApiProperties,
                circuitBreakerFactory,
                primaryRestClient
        );

        SecondaryMockApiClient secondaryMockApiClient = new SecondaryMockApiClient(
                secondaryMockApiProperties,
                circuitBreakerFactory,
                secondaryRestClient
        );

        notificationSendGateway = new NotificationSendGateway(
                mockApiClient,
                secondaryMockApiClient
        );
    }

    @Test
    void primaryTimeout_thenSecondarySuccess() {
        // given
        primaryServer.stubFor(post(urlEqualTo("/mock/send"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(4000)));

        secondaryServer.stubFor(post(urlEqualTo("/mock/send"))
                .willReturn(aResponse()
                        .withStatus(200)));

        MockApiSendRequest request = new MockApiSendRequest(
                "req-123456789",
                "SMS",
                "01012345678",
                "결제 완료\n결제가 정상적으로 완료되었습니다.",
                Map.of(
                        "notificationId", 1L,
                        "channel", "SMS",
                        "retryCount", 0,
                        "title", "결제 완료"
                )
        );

        // when & then
        assertThatCode(() -> notificationSendGateway.send(request, "req-123456789"))
                .doesNotThrowAnyException();

        primaryServer.verify(1, postRequestedFor(urlEqualTo("/mock/send")));
        secondaryServer.verify(1, postRequestedFor(urlEqualTo("/mock/send")));
    }

    @Test
    void primary429_thenNoFallback() {
        // given
        primaryServer.stubFor(post(urlEqualTo("/mock/send"))
                .willReturn(aResponse()
                        .withStatus(429)));

        secondaryServer.stubFor(post(urlEqualTo("/mock/send"))
                .willReturn(aResponse()
                        .withStatus(200)));

        MockApiSendRequest request = new MockApiSendRequest(
                "req-123456789",
                "SMS",
                "01012345678",
                "결제 완료\n결제가 정상적으로 완료되었습니다.",
                Map.of(
                        "notificationId", 1L,
                        "channel", "SMS",
                        "retryCount", 0,
                        "title", "결제 완료"
                )
        );

        // when
        NotificationSendException exception = org.junit.jupiter.api.Assertions.assertThrows(
                NotificationSendException.class,
                () -> notificationSendGateway.send(request, "req-123456789")
        );

        // then
        org.assertj.core.api.Assertions.assertThat(exception.getFailureType())
                .isEqualTo(FailureType.HTTP_FAIL);
        org.assertj.core.api.Assertions.assertThat(exception.getFailureStatusCode())
                .isEqualTo(429);

        primaryServer.verify(1, postRequestedFor(urlEqualTo("/mock/send")));
        secondaryServer.verify(0, postRequestedFor(urlEqualTo("/mock/send")));
    }

    private ClientHttpRequestFactory simpleRequestFactory(int connectTimeoutMs, int readTimeoutMs) {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return factory;
    }


}
