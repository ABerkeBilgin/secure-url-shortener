package com.berke.urlshortener.entity; // DÜZELTİLDİ: model -> entity

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "click_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    private String browser;
    private String os;

    @Column(name = "device_type")
    private String deviceType;
}