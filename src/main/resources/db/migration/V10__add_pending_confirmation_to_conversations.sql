-- Add pending confirmation fields to conversations table
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS pending_action_status VARCHAR(50) DEFAULT 'NONE';
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS pending_action_type VARCHAR(100);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS pending_action_payload TEXT;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS pending_action_description TEXT;
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS pending_action_created_on TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_conversations_pending_status ON conversations(pending_action_status);
