-- Tabela de solicitações de ações sobre funcionários (edição, desativação, exclusão)
CREATE TABLE solicitacoes (
    id              BIGSERIAL PRIMARY KEY,
    tipo            VARCHAR(20)  NOT NULL,  -- EDICAO, DESATIVACAO, EXCLUSAO
    funcionario_id  BIGINT       NOT NULL REFERENCES funcionarios(id),
    solicitante_id  BIGINT       NOT NULL REFERENCES usuarios(id),
    dados_json      TEXT,                   -- JSON com dados propostos (para EDICAO)
    motivo          TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE', -- PENDENTE, APROVADA, REJEITADA
    aprovador_id    BIGINT       REFERENCES usuarios(id),
    data_solicitacao TIMESTAMP   NOT NULL DEFAULT NOW(),
    data_decisao    TIMESTAMP,
    observacao_aprovador TEXT
);

CREATE INDEX idx_solicitacoes_status ON solicitacoes(status);
CREATE INDEX idx_solicitacoes_funcionario ON solicitacoes(funcionario_id);
