--liquibase formatted sql
--changeset a.ermizin:2

ALTER TABLE products ALTER COLUMN price TYPE NUMERIC(10, 2);
--rollback ALTER TABLE products ALTER COLUMN price TYPE BIGINT;