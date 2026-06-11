-- DDL: Schema do Sistema de Precificação de Notebooks

-- Habilita extensão para UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabela de parâmetros de custos de precificação
CREATE TABLE IF NOT EXISTS cost_parameters (
    id         BIGSERIAL PRIMARY KEY,
    parametro  VARCHAR(100) NOT NULL UNIQUE,
    valor      NUMERIC(20, 10) NOT NULL,
    descricao  VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tabela de notebooks
CREATE TABLE IF NOT EXISTS notebooks (
    id                 BIGSERIAL PRIMARY KEY,
    version            BIGINT NOT NULL DEFAULT 0,
    nome               VARCHAR(255) NOT NULL UNIQUE,
    custo_dev          NUMERIC(20, 10) NOT NULL,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Migração: adiciona coluna de lock otimista em bancos existentes
ALTER TABLE notebooks ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
