package com.berke.urlshortener.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse {
    private long totalClicks;
    private Map<String, Long> browsers;         // Örn: Chrome -> 10
    private Map<String, Long> operatingSystems; // Örn: Windows -> 5
    private List<String> lastClicks;            // Son 5 tıklama zamanı
}