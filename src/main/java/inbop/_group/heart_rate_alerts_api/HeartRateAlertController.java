package inbop._group.heart_rate_alerts_api;

// package com.example.heart.alert.controller;

import inbop._group.heart_rate_alerts_api.Dto.ActiveAlertResponse;
import inbop._group.heart_rate_alerts_api.Dto.CreateHeartRateAlertRequest;
import inbop._group.heart_rate_alerts_api.Dto.HeartRateAlertResponse;
import inbop._group.heart_rate_alerts_api.Dto.WeeklyAlertCountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HeartRateAlertController {

    private final HeartRateAlertService alertService;

    // 1. 심박수 경고 기록 생성
    @PostMapping("/heart-rate-alerts")
    public ResponseEntity<HeartRateAlertResponse> createAlert(
            @Valid @RequestBody CreateHeartRateAlertRequest request
    ) {
        HeartRateAlertResponse response = alertService.createAlert(request);
        return ResponseEntity
                .created(URI.create("/api/heart-rate-alerts/" + response.getId()))
                .body(response);
    }

    // 2. 사용자별 일주일 경고 횟수 조회
    @GetMapping("/users/{userId}/heart-rate-alerts/weekly")
    public ResponseEntity<List<WeeklyAlertCountResponse>> getWeeklyCounts(
            @PathVariable Long userId
    ) {
        List<WeeklyAlertCountResponse> result = alertService.getWeeklyCounts(userId);
        return ResponseEntity.ok(result);
    }

    // 3. 모든 사용자의 현재 경고 상태 조회 (보호자용)
    @GetMapping("/guardian/heart-rate-alerts/active")
    public ResponseEntity<List<ActiveAlertResponse>> getActiveAlerts() {
        List<ActiveAlertResponse> result = alertService.getActiveAlerts();
        return ResponseEntity.ok(result);
    }

    // (선택) 간단한 예외 매핑 – 전역 @ControllerAdvice 있으면 생략 가능
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(
                new ErrorResponse("Not Found", e.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse("Bad Request", e.getMessage())
        );
    }

    record ErrorResponse(String error, String message) {}
}
