-- Inserir perfis padrão (se não existirem)
INSERT INTO perfis (nome, descricao, criado_em)
SELECT 'ADMIN', 'Administrador do Sistema - Acesso total', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'ADMIN');

INSERT INTO perfis (nome, descricao, criado_em)
SELECT 'RH_CHEFE', 'Chefe de Recursos Humanos', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'RH_CHEFE');

INSERT INTO perfis (nome, descricao, criado_em)
SELECT 'RH_ASSISTENTE', 'Assistente de Recursos Humanos', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'RH_ASSISTENTE');

INSERT INTO perfis (nome, descricao, criado_em)
SELECT 'DP_CHEFE', 'Chefe de Departamento Pessoal', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'DP_CHEFE');

INSERT INTO perfis (nome, descricao, criado_em)
SELECT 'DP_ASSISTENTE', 'Assistente de Departamento Pessoal', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM perfis WHERE nome = 'DP_ASSISTENTE');
