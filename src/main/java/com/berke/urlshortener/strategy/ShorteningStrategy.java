package com.berke.urlshortener.strategy;

public interface ShorteningStrategy {
    String encode(Long id);
    Long decode(String shortCode);

}
