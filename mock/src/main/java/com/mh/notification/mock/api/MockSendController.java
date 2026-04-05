package com.mh.notification.mock.api;

import com.mh.notification.mock.service.MockSendService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
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
    public ResponseEntity<MockSendSuccessResponse> send(@Valid @RequestBody MockSendRequest request) {
        return ResponseEntity.ok(mockSendService.handleSend(request));
    }
}
