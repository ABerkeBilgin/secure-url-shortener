package com.berke.urlshortener.repository;

import com.berke.urlshortener.dto.StatProjection;
import com.berke.urlshortener.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findByShortUrlId(Long shortUrlId);

    @Query("SELECT c.browser as key, COUNT(c) as count FROM ClickEvent c WHERE c.shortUrl.id = :shortUrlId GROUP BY c.browser")
    List<StatProjection> countBrowsersByShortUrlId(Long shortUrlId);

    @Query("SELECT c.os as key, COUNT(c) as count FROM ClickEvent c WHERE c.shortUrl.id = :shortUrlId GROUP BY c.os")
    List<StatProjection> countOsByShortUrlId(Long shortUrlId);

    @Query("SELECT c.clickedAt FROM ClickEvent c WHERE c.shortUrl.id = :shortUrlId ORDER BY c.clickedAt DESC LIMIT 5")
    List<LocalDateTime> findLast5Clicks(Long shortUrlId);
}