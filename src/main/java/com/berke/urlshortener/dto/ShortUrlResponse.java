package com.berke.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {
    
    private String shortCode;
    private String originalUrl;
    private LocalDateTime createdDate;
    private LocalDateTime expirationData;
}