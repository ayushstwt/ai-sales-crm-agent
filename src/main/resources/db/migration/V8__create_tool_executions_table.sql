-- Tool Executions Migration (Step 5.3)
CREATE TABLE IF NOT EXISTS tool_executions (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_id BIGINT,
    conversation_id BIGINT,
    tool_name VARCHAR(100) NOT NULL,
    arguments TEXT,
    result TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',
    execution_time_ms BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_tool_executions_organization FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_tool_executions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_tool_executions_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_tool_executions_org_id ON tool_executions(organization_id);
CREATE INDEX IF NOT EXISTS idx_tool_executions_conv_id ON tool_executions(conversation_id);
CREATE INDEX IF NOT EXISTS idx_tool_executions_tool_name ON tool_executions(tool_name);
CREATE INDEX IF NOT EXISTS idx_tool_executions_created_on ON tool_executions(created_on);