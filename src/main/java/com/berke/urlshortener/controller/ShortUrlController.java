package com.berke.urlshortener.controller;

import com.berke.urlshortener.dto.CreateShortUrlRequest;
import com.berke.urlshortener.dto.ShortUrlResponse;
import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.service.ShortUrlService;

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
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        ShortUrl shortUrl = service.getOriginalUrl(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(shortUrl.getOriginalUrl()));
        
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}