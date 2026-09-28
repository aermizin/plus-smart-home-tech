--liquibase formatted sql
--changeset a.ermizin:2

ALTER TABLE inventories DROP COLUMN IF EXISTS available_quantity;