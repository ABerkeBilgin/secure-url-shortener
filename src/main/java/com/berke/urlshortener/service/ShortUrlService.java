package com.berke.urlshortener.service;

import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.exception.ShortUrlNotFoundException;
import com.berke.urlshortener.repository.ShortUrlRepository;
import com.berke.urlshortener.strategy.ShorteningStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository repository;
    private final ShorteningStrategy shorteningStrategy;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public ShortUrl createShortUrl(String originalUrl) {
        log.info("Generating ShortUrl for: {}", originalUrl);

        // 1. [DB] Save initial
        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(originalUrl)
                .build();
        ShortUrl savedUrl = repository.save(shortUrl);

        // 2. [LOGIC] Generate code
        String shortCode = shorteningStrategy.encode(savedUrl.getId());
        
        // 3. [DB] Update with code
        savedUrl.setShortCode(shortCode);
        ShortUrl finalUrl = repository.save(savedUrl);

        // 4. [REDIS] Write-Through Cache
        String cacheKey = "shortUrl:" + shortCode;
        redisTemplate.opsForValue().set(cacheKey, finalUrl, CACHE_TTL);
        
        
        log.info("ShortUrl created successfully. ShortCode: {}, OriginalUrl: {}", shortCode, originalUrl);
        return finalUrl;
    }

    public ShortUrl getOriginalUrl(String shortCode) {
        String cacheKey = "shortUrl:" + shortCode;

        // 1. [REDIS] Cache Lookup
        ShortUrl cachedUrl = (ShortUrl) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedUrl != null) {
            log.info("Cache Hit for ShortCode: {}", shortCode);
            return cachedUrl;
        }

        // 2. [DB] Database Fallback
        log.warn("Cache Miss for ShortCode: {}. Fetching from database...", shortCode); 

        ShortUrl dbUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.error("ShortUrl not found for code: {}", shortCode);
                    return new ShortUrlNotFoundException("Code not found: " + shortCode);
                });

        // 3. [REDIS] Update Cache
        if (dbUrl.getExpiresAt() == null || dbUrl.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
             redisTemplate.opsForValue().set(cacheKey, dbUrl, CACHE_TTL);
             log.info("Cache updated for ShortCode: {}", shortCode);
        }

        return dbUrl;
    }
}