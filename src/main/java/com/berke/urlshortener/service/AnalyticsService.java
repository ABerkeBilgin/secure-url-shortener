package com.berke.urlshortener.service;

import com.berke.urlshortener.dto.AnalyticsResponse;
import com.berke.urlshortener.dto.StatProjection;
import com.berke.urlshortener.entity.ClickEvent;
import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.exception.ShortUrlNotFoundException;
import com.berke.urlshortener.repository.ClickEventRepository;
import com.berke.urlshortener.repository.ShortUrlRepository;
import com.berke.urlshortener.util.UserAgentUtil;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final UserAgentUtil userAgentUtil;
    private final MeterRegistry meterRegistry;

    @Async
    public void logClick(String shortCode, String ipAddress, String userAgentHeader) {
        
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found: " + shortCode));

        String browser = userAgentUtil.getBrowser(userAgentHeader);
        String os = userAgentUtil.getOs(userAgentHeader);
        String device = userAgentUtil.getDevice(userAgentHeader);

        ClickEvent clickEvent = ClickEvent.builder()
                .shortUrl(shortUrl)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .browser(browser)
                .os(os)
                .deviceType(device)
                .build();

        clickEventRepository.save(clickEvent);

        shortUrlRepository.incrementVisitCount(shortUrl.getId());

        meterRegistry.counter("url_shortener_clicks", 
            "browser", browser, 
            "os", os, 
            "device", device
        ).increment();
        
        log.info("Analytics saved & Counter incremented: Code={}", shortCode);
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found: " + shortCode));

        Long shortUrlId = shortUrl.getId();

        Map<String, Long> browserStats = clickEventRepository.countBrowsersByShortUrlId(shortUrlId)
                .stream()
                .collect(Collectors.toMap(StatProjection::getKey, StatProjection::getCount));

        Map<String, Long> osStats = clickEventRepository.countOsByShortUrlId(shortUrlId)
                .stream()
                .collect(Collectors.toMap(StatProjection::getKey, StatProjection::getCount));

        List<String> lastClicks = clickEventRepository.findLast5Clicks(shortUrlId)
                .stream()
                .map(LocalDateTime::toString)
                .collect(Collectors.toList());

        long totalClicks = shortUrl.getVisitCount();

        return AnalyticsResponse.builder()
                .totalClicks(totalClicks)
                .browsers(browserStats)
                .operatingSystems(osStats)
                .lastClicks(lastClicks)
                .build();
    }
}