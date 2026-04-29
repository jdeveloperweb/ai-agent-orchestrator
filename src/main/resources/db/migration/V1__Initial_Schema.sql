CREATE TABLE agent_sessions (
    id BIGSERIAL PRIMARY KEY,
    objective VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    is_spec_only BOOLEAN,
    result_json TEXT
);

CREATE TABLE agent_logs (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT,
    type VARCHAR(50),
    message TEXT,
    created_at TIMESTAMP
);

CREATE INDEX idx_agent_logs_session_id ON agent_logs(session_id);
