package com.berke.urlshortener.controller;

import com.berke.urlshortener.dto.CreateShortUrlRequest;
import com.berke.urlshortener.dto.ShortUrlResponse;
import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.service.AnalyticsService;
import com.berke.urlshortener.service.ShortUrlService;
import com.berke.urlshortener.util.ClientIpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService service;
    private final AnalyticsService analyticsService;

    @PostMapping("/api/v1/urls")
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrl createdUrl = service.createShortUrl(request.getOriginalUrl(), request.getExpirationDate());
        
        ShortUrlResponse response = ShortUrlResponse.builder()
                .shortCode(createdUrl.getShortCode())
                .originalUrl(createdUrl.getOriginalUrl())
                .createdDate(createdUrl.getCreatedAt())
                .expirationData(createdUrl.getExpiresAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            HttpServletRequest request
    ) {
        ShortUrl shortUrl = service.getOriginalUrl(code);

        String ipAddress = ClientIpUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        analyticsService.logClick(code, ipAddress, userAgent);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(shortUrl.getOriginalUrl()));
        
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}