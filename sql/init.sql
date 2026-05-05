-- PostgreSQL stores ONLY authentication & user hierarchy
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    parent BIGINT REFERENCES users(id)
    );