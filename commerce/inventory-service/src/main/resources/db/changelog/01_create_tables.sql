--liquibase formatted sql
--changeset a.ermizin:7

-- создаем таблицу inventories
CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT UNIQUE NOT NULL,
    quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    version BIGINT
);

--rollback DROP TABLE inventories;
