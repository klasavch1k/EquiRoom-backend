-- 1) optimistic locking columns
ALTER TABLE IF EXISTS shows
    ADD COLUMN IF NOT EXISTS version BIGINT;

ALTER TABLE IF EXISTS registrations
    ADD COLUMN IF NOT EXISTS version BIGINT;

ALTER TABLE IF EXISTS entries
    ADD COLUMN IF NOT EXISTS version BIGINT;

-- backfill null versions for existing rows
UPDATE shows SET version = 0 WHERE version IS NULL;
UPDATE registrations SET version = 0 WHERE version IS NULL;
UPDATE entries SET version = 0 WHERE version IS NULL;

-- make version not null where possible
DO $$
BEGIN
    BEGIN
        ALTER TABLE shows ALTER COLUMN version SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;

    BEGIN
        ALTER TABLE registrations ALTER COLUMN version SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;

    BEGIN
        ALTER TABLE entries ALTER COLUMN version SET NOT NULL;
    EXCEPTION WHEN OTHERS THEN
        NULL;
    END;
END $$;

-- 2) unique constraint: one registration per user per show
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_registration_user_show'
    ) THEN
        ALTER TABLE registrations
            ADD CONSTRAINT uk_registration_user_show UNIQUE (user_id, show_id);
    END IF;
END $$;

-- 3) users.email unique
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_users_email'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uk_users_email UNIQUE (email);
    END IF;
END $$;
