package com.berke.urlshortener.service;

import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.exception.ShortUrlNotFoundException;
import com.berke.urlshortener.repository.ShortUrlRepository;
import com.berke.urlshortener.strategy.ShorteningStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository repository;
    private final ShorteningStrategy shorteningStrategy;

    @Transactional
    public ShortUrl createShortUrl(String originalUrl) {
       
        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(originalUrl)
                .build();
        
        ShortUrl savedUrl = repository.save(shortUrl);
        
        String code = shorteningStrategy.encode(savedUrl.getId());

        savedUrl.setShortCode(code);
        return repository.save(savedUrl);
    }

    public ShortUrl getOriginalUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException("Code not found: " + shortCode));
    }
}