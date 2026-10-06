--liquibase formatted sql
--changeset a.ermizin:1

-- создаем таблицу categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(500)
);

-- создаем таблицу products
CREATE TABLE IF NOT EXISTS products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price BIGINT NOT NULL,
    category_id BIGINT REFERENCES categories(id),
    image_url VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);

-- индекс на подстроку
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_products_name_trgm
    ON products USING GIN (lower(name) gin_trgm_ops);