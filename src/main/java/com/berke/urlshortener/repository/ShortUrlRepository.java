package com.berke.urlshortener.repository;

import com.berke.urlshortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime date);

    @Modifying
    @Transactional
    @Query("UPDATE ShortUrl s SET s.visitCount = s.visitCount + 1 WHERE s.id = :id")
    void incrementVisitCount(Long id);
}