package com.berke.urlshortener.scheduler;

import com.berke.urlshortener.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkCleanupScheduler {

    private final ShortUrlRepository repository;

    // Runs every day at 04:00 AM
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupExpiredLinks() {
        log.info("Cleanup Job Started: Scanning for expired links...");
        
        LocalDateTime now = LocalDateTime.now();
        
        repository.deleteByExpiresAtBefore(now);
        
        log.info("Cleanup Finished. Expired links removed from database.");
    }
}