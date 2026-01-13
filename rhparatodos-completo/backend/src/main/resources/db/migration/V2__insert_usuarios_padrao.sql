-- Inserir usuários padrão de teste (se não existirem)
-- Senha: admin123 (BCrypt hash)

-- Admin
INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'admin@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'ADMIN'),
       true, 
       CURRENT_TIMESTAMP, 
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@rhparatodos.com.br');

-- Chefe de RH
INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'maria.costa@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'RH_CHEFE'),
       true, 
       CURRENT_TIMESTAMP, 
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'maria.costa@rhparatodos.com.br');

-- Assistente de RH
INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'joao.silva@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'RH_ASSISTENTE'),
       true, 
       CURRENT_TIMESTAMP, 
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'joao.silva@rhparatodos.com.br');

-- Chefe de DP
INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'carlos.santos@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'DP_CHEFE'),
       true, 
       CURRENT_TIMESTAMP, 
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'carlos.santos@rhparatodos.com.br');

-- Assistente de DP
INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'ana.oliveira@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'DP_ASSISTENTE'),
       true, 
       CURRENT_TIMESTAMP, 
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'ana.oliveira@rhparatodos.com.br');
