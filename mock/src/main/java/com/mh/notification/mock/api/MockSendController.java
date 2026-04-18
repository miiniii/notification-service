package com.mh.notification.mock.api;

import com.mh.notification.mock.service.MockSendService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
@Slf4j
public class MockSendController {

    private final MockSendService mockSendService;

    public MockSendController(MockSendService mockSendService) {
        this.mockSendService = mockSendService;
    }

    @GetMapping("/health")
    public String health() {
        return "mock server ok";
    }

    @PostMapping("/send")
    public ResponseEntity<MockSendSuccessResponse> send(
            @RequestHeader(value = "X-Request-ID", required = false) String requestId,
            @Valid @RequestBody MockSendRequest request
    ) {
        log.info("[MOCK SERVER] requestId={}", requestId);
        return ResponseEntity.ok(mockSendService.handleSend(request));
    }
}
