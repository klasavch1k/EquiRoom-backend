-- Prevent duplicate active entry for same (registration, class, model)
CREATE UNIQUE INDEX IF NOT EXISTS idx_entry_unique_active
    ON entries (registration_id, class_id, model_id)
    WHERE active = true;
