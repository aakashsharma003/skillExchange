--liquibase formatted sql
--changeset akash:2025-08-15-001

-- Create schema
CREATE SCHEMA IF NOT EXISTS skillexchange;

-- User table
CREATE TABLE IF NOT EXISTS skillexchange.users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    skills TEXT[],
    github_profile VARCHAR(200),
    bio VARCHAR(500),
    linkedin_profile VARCHAR(300),
    youtube_profile VARCHAR(300),
    instagram_profile VARCHAR(300),
    profile_picture_url VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ChatRoom table
CREATE TABLE IF NOT EXISTS skillexchange.chat_rooms (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    exchange_request_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES skillexchange.users(id),
    CONSTRAINT fk_receiver FOREIGN KEY (receiver_id) REFERENCES skillexchange.users(id)
);

-- ChatMessage table
CREATE TABLE IF NOT EXISTS skillexchange.chat_messages (
    id UUID PRIMARY KEY,
    chat_room_id UUID NOT NULL,
    sender_email VARCHAR(150) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_room FOREIGN KEY (chat_room_id) REFERENCES skillexchange.chat_rooms(id)
);

-- ExchangeRequest table
CREATE TABLE IF NOT EXISTS skillexchange.exchange_requests (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    requested_skill VARCHAR(100) NOT NULL,
    offered_skill VARCHAR(100),
    status VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_exchange_sender FOREIGN KEY (sender_id) REFERENCES skillexchange.users(id),
    CONSTRAINT fk_exchange_receiver FOREIGN KEY (receiver_id) REFERENCES skillexchange.users(id)
);

-- OTP table
CREATE TABLE IF NOT EXISTS skillexchange.otp_details (
    id UUID PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
-- Users
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON skillexchange.users (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_users_skills_gin ON skillexchange.users USING GIN (skills);

-- Chat rooms
CREATE INDEX IF NOT EXISTS idx_chat_rooms_sender ON skillexchange.chat_rooms(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_receiver ON skillexchange.chat_rooms(receiver_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_last_activity ON skillexchange.chat_rooms(last_activity_at);

-- Chat messages
CREATE INDEX IF NOT EXISTS idx_chat_messages_room ON skillexchange.chat_messages(chat_room_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON skillexchange.chat_messages(created_at);

-- Exchange requests
CREATE INDEX IF NOT EXISTS idx_exchange_requests_receiver ON skillexchange.exchange_requests(receiver_id);
CREATE INDEX IF NOT EXISTS idx_exchange_requests_sender ON skillexchange.exchange_requests(sender_id);
CREATE INDEX IF NOT EXISTS idx_exchange_requests_status ON skillexchange.exchange_requests(status);
CREATE INDEX IF NOT EXISTS idx_exchange_requests_created_at ON skillexchange.exchange_requests(created_at);
