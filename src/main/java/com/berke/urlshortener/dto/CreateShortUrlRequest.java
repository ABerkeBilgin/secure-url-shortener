package com.berke.urlshortener.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortUrlRequest {
    @NotBlank(message = "URL cannot be empty")
    @Pattern(regexp = "^(http|https)://.*", message = "URL must start with http:// or https://")
    private String originalUrl;

    @Future(message = "Expiration date must be in the future") 
    private LocalDateTime expirationDate;
}