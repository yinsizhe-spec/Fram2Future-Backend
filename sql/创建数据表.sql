CREATE TABLE farm (
    id BIGINT PRIMARY KEY,
    farm_name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    owner_name VARCHAR(100),
    status VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE esg_score (
    id BIGINT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    period VARCHAR(20),
    environmental_score DECIMAL(5,2),
    social_score DECIMAL(5,2),
    governance_score DECIMAL(5,2),
    total_score DECIMAL(5,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE token_record (
    id BIGINT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    token_amount DECIMAL(18,2),
    reason VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE transaction_record (
    id BIGINT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    transaction_type VARCHAR(50),
    amount DECIMAL(18,2),
    description VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);