
CREATE TABLE IF NOT EXISTS short_urls (
    id BIGSERIAL PRIMARY KEY,
    original_url TEXT NOT NULL,
    short_code VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    visit_count BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_short_code ON short_urls(short_code);