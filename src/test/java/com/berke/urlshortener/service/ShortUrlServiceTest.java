package com.berke.urlshortener.service;

import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.exception.ShortUrlNotFoundException;
import com.berke.urlshortener.repository.ShortUrlRepository;
import com.berke.urlshortener.strategy.ShorteningStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private ShorteningStrategy shorteningStrategy;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ShortUrlService service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ReflectionTestUtils.setField(service, "defaultExpirationDays", 365);
        ReflectionTestUtils.setField(service, "maxExpirationDays", 730);
    }

    @Test
    @DisplayName("Create ShortUrl - Başarılı Senaryo")
    void createShortUrl_Success() {
        String originalUrl = "https://www.google.com";
        ShortUrl initialUrl = ShortUrl.builder().id(1L).originalUrl(originalUrl).build();
        ShortUrl finalUrl = ShortUrl.builder().id(1L).originalUrl(originalUrl).shortCode("abc").build();

        when(repository.save(any(ShortUrl.class))).thenReturn(initialUrl).thenReturn(finalUrl);
        when(shorteningStrategy.encode(1L)).thenReturn("abc");

        ShortUrl result = service.createShortUrl(originalUrl, null);

        assertNotNull(result);
        assertEquals("abc", result.getShortCode());
        
        verify(repository, times(2)).save(any(ShortUrl.class));
        verify(valueOperations).set(eq("shortUrl:abc"), any(ShortUrl.class), any(Duration.class));
    }

    @Test
    @DisplayName("Get Original Url - Cache Hit (Redis'ten Oku)")
    void getOriginalUrl_CacheHit() {
        String shortCode = "abc";
        ShortUrl cachedUrl = ShortUrl.builder().shortCode(shortCode).originalUrl("https://cache.com").build();
        
        when(valueOperations.get("shortUrl:" + shortCode)).thenReturn(cachedUrl);

        ShortUrl result = service.getOriginalUrl(shortCode);

        assertEquals("https://cache.com", result.getOriginalUrl());
        verify(repository, never()).findByShortCode(anyString());
    }

    @Test
    @DisplayName("Get Original Url - Cache Miss (Veritabanından Oku)")
    void getOriginalUrl_CacheMiss() {
        String shortCode = "xyz";
        ShortUrl dbUrl = ShortUrl.builder().shortCode(shortCode).originalUrl("https://db.com").build();

        when(valueOperations.get("shortUrl:" + shortCode)).thenReturn(null);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(dbUrl));

        ShortUrl result = service.getOriginalUrl(shortCode);

        assertEquals("https://db.com", result.getOriginalUrl());
        verify(valueOperations).set(eq("shortUrl:" + shortCode), eq(dbUrl), any(Duration.class));
    }

    @Test
    @DisplayName("Get Original Url - Bulunamadı Hatası (404)")
    void getOriginalUrl_NotFound() {
        String shortCode = "404";
        when(valueOperations.get("shortUrl:" + shortCode)).thenReturn(null);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(ShortUrlNotFoundException.class, () -> service.getOriginalUrl(shortCode));
    }
}