package com.berke.urlshortener.service;

import com.berke.urlshortener.dto.AnalyticsResponse;
import com.berke.urlshortener.entity.ClickEvent;
import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.exception.ShortUrlNotFoundException;
import com.berke.urlshortener.repository.ClickEventRepository;
import com.berke.urlshortener.repository.ShortUrlRepository;
import com.berke.urlshortener.util.UserAgentUtil;

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
        
        log.info("Analytics saved: Code={}, OS={}, Browser={}", shortCode, os, browser);
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Short URL not found: " + shortCode));

        
        List<ClickEvent> clicks = clickEventRepository.findByShortUrlId(shortUrl.getId());

        Map<String, Long> browserStats = clicks.stream()
                .collect(Collectors.groupingBy(ClickEvent::getBrowser, Collectors.counting()));

        Map<String, Long> osStats = clicks.stream()
                .collect(Collectors.groupingBy(ClickEvent::getOs, Collectors.counting()));

        List<String> last5Clicks = clicks.stream()
                .sorted((a, b) -> b.getClickedAt().compareTo(a.getClickedAt())) 
                .limit(5)
                .map(click -> click.getClickedAt().toString())
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .totalClicks(clicks.size())
                .browsers(browserStats)
                .operatingSystems(osStats)
                .lastClicks(last5Clicks)
                .build();
    }
}