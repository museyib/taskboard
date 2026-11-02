CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    name VARCHAR(255)
);

CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(1000) NOT NULL UNIQUE,
    expiration_date TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE login_fail (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    timestamp BIGINT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    locked_until TIMESTAMP WITH TIME ZONE
);

CREATE TABLE label (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP,
    deleted boolean
);

CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20),
    created_by VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    dead_line TIMESTAMP,
    closed_at TIMESTAMP,
    deleted boolean
);

CREATE TABLE task_label (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT REFERENCES task(id),
    label_id BIGINT REFERENCES label(id)
);

CREATE TABLE task_notification (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(100),
    task_id BIGINT REFERENCES task(id),
    created_at TIMESTAMP,
    message TEXT
)