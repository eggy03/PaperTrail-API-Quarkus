ALTER TABLE papertrailbot.message_log_content_table
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;