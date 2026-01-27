package com.berke.urlshortener.controller;

import com.berke.urlshortener.dto.AnalyticsResponse;
import com.berke.urlshortener.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsResponse> getUrlStatistics(@PathVariable String shortCode) {
        AnalyticsResponse stats = analyticsService.getAnalytics(shortCode);
        return ResponseEntity.ok(stats);
    }
}