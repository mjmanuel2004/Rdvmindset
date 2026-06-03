CREATE TABLE agents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- VOCAL, CHATBOT
    vapi_assistant_id VARCHAR(255),
    botpress_bot_id VARCHAR(255),
    phone_number VARCHAR(20),
    system_prompt TEXT,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE agent_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agent_id UUID UNIQUE REFERENCES agents(id) ON DELETE CASCADE,
    tone VARCHAR(50) DEFAULT 'PROFESSIONAL',
    faq TEXT,
    pricing TEXT,
    appointment_duration_minutes INT DEFAULT 30,
    model_industry VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
