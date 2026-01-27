package com.berke.urlshortener.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse {
    private long totalClicks;
    private Map<String, Long> browsers;         
    private Map<String, Long> operatingSystems; 
    private List<String> lastClicks;            
}