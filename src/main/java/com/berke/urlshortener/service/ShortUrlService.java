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
import java.time.LocalDateTime;

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
                .expiresAt(LocalDateTime.now().plusDays(30)) 
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
             if (cachedUrl.getExpiresAt() != null && cachedUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
                redisTemplate.delete(cacheKey);
                throw new ShortUrlNotFoundException("Link expired (Cache)");
            }
            return cachedUrl;
        }

        // 2. [DB] Database Fallback
        log.warn("Cache Miss for ShortCode: {}. Fetching from database...", shortCode); 

        ShortUrl dbUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.error("ShortUrl not found for code: {}", shortCode);
                    return new ShortUrlNotFoundException("Code not found: " + shortCode);
                });

        if (dbUrl.getExpiresAt() != null && dbUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Link found but expired: {}", shortCode);
            throw new ShortUrlNotFoundException("Link expired");
        }

        // 3. [REDIS] Update Cache
        redisTemplate.opsForValue().set(cacheKey, dbUrl, CACHE_TTL);
        log.info("Cache updated for ShortCode: {}", shortCode);

        return dbUrl;
    }
}