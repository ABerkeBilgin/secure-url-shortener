package com.berke.urlshortener.repository;

import com.berke.urlshortener.entity.ShortUrl;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime date);
}