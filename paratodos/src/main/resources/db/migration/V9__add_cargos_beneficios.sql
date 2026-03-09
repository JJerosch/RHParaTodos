-- ============================================================
-- V9 — Tabela de relacionamento Cargos ↔ Benefícios
-- ============================================================

CREATE TABLE cargos_beneficios (
    cargo_id          BIGINT NOT NULL REFERENCES cargos(id) ON DELETE CASCADE,
    tipo_beneficio_id BIGINT NOT NULL REFERENCES tipos_beneficios(id) ON DELETE CASCADE,
    PRIMARY KEY (cargo_id, tipo_beneficio_id)
);

CREATE INDEX idx_cargos_beneficios_cargo ON cargos_beneficios(cargo_id);
CREATE INDEX idx_cargos_beneficios_beneficio ON cargos_beneficios(tipo_beneficio_id);
