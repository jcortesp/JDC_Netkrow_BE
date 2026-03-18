-- Add celular column to remissions table
ALTER TABLE remissions ADD COLUMN IF NOT EXISTS celular VARCHAR(20);
