-- İstatistik Tablosu
CREATE TABLE IF NOT EXISTS click_events (
    id BIGSERIAL PRIMARY KEY,
    short_url_id BIGINT NOT NULL,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    browser VARCHAR(50),    
    os VARCHAR(50),         
    device_type VARCHAR(50),

   
    CONSTRAINT fk_short_url FOREIGN KEY (short_url_id) REFERENCES short_urls(id)
);


CREATE INDEX IF NOT EXISTS idx_click_events_short_url_id ON click_events(short_url_id);