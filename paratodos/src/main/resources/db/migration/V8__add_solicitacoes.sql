-- ============================================================
-- V8 — Tabela central de solicitações
-- ============================================================

CREATE TABLE solicitacoes (
    id              BIGSERIAL       PRIMARY KEY,

    -- Tipo da solicitação (enum no Java)
    tipo            VARCHAR(50)     NOT NULL,

    -- Status do fluxo
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',

    -- Referência genérica à entidade afetada
    referencia_tipo VARCHAR(50),        -- ex: FUNCIONARIO, CARGO, DEPARTAMENTO, VAGA
    referencia_id   BIGINT,             -- id da entidade afetada

    -- Quem solicitou e quem decidiu
    solicitante_id  BIGINT          NOT NULL REFERENCES usuarios(id),
    aprovador_id    BIGINT          REFERENCES usuarios(id),

    -- Justificativa e observações
    motivo          TEXT            NOT NULL,
    observacao      TEXT,

    -- Snapshot antes/depois em JSON (flexível para qualquer tipo)
    dados_antes     JSONB,
    dados_depois    JSONB,

    -- Timestamps
    criado_em       TIMESTAMP       NOT NULL DEFAULT NOW(),
    atualizado_em   TIMESTAMP       NOT NULL DEFAULT NOW(),
    decidido_em     TIMESTAMP
);

-- Índices para consultas frequentes
CREATE INDEX idx_solicitacoes_status      ON solicitacoes(status);
CREATE INDEX idx_solicitacoes_tipo        ON solicitacoes(tipo);
CREATE INDEX idx_solicitacoes_solicitante ON solicitacoes(solicitante_id);
CREATE INDEX idx_solicitacoes_referencia  ON solicitacoes(referencia_tipo, referencia_id);
