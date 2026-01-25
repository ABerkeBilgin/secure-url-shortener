package com.berke.urlshortener.dto;

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
}