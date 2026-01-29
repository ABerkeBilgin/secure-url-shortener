ALTER TABLE short_urls ADD COLUMN user_id VARCHAR(255);

UPDATE short_urls SET user_id = 'legacy_system' WHERE user_id IS NULL;

ALTER TABLE short_urls ALTER COLUMN user_id SET NOT NULL;