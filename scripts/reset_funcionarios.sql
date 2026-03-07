-- ===========================================================================
-- SCRIPT: Reset completo de Funcionários e Usuários
-- Exclui todos os 29 funcionários e recria 12 (2 por role) com dados completos
-- Para cada role: 1 com 2FA ativado (CODIGO), 1 com login normal
--
-- ROLES: ADMIN, RH_CHEFE, RH_ASSISTENTE, DP_CHEFE, DP_ASSISTENTE, EMPLOYEE
--
-- Senha padrão para TODOS os logins: Admin@123
-- BCrypt hash: $2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG
-- ===========================================================================
SET client_encoding = 'UTF8';
SET search_path = public;

BEGIN;

-- ===========================================================================
-- 1. LIMPEZA - Remove dados que referenciam funcionários
-- ===========================================================================
DELETE FROM public.promocoes;
DELETE FROM public.treinamento_inscricoes;
DELETE FROM public.funcionarios_beneficios;
DELETE FROM public.dados_funcionarios_documentos;
DELETE FROM public.dados_emergencia;
DELETE FROM public.historico_cargos;
DELETE FROM public.salarios_historico;
DELETE FROM public.folha_pagamentos;
DELETE FROM public.ferias_solicitacoes;
DELETE FROM public.ferias_registros;
DELETE FROM public.ferias_periodos_aquisitivos;
DELETE FROM public.ferias_logs;
DELETE FROM public.afastamentos;
DELETE FROM public.rescisoes;
DELETE FROM public.ponto_marcacoes;
DELETE FROM public.ponto_justificativas;
DELETE FROM public.ponto_apuracao_diaria;
DELETE FROM public.ponto_banco_horas;

-- Remove funcionários (gestor_id é self-reference, precisa limpar antes)
UPDATE public.funcionarios SET gestor_id = NULL;
DELETE FROM public.funcionarios;

-- Remove dados que referenciam usuários
DELETE FROM public.entrevistas;
DELETE FROM public.candidaturas;
DELETE FROM public.candidatos;
DELETE FROM public.vagas;
DELETE FROM public.treinamento_inscricoes;
DELETE FROM public.treinamento_turmas;
DELETE FROM public.treinamentos;
DELETE FROM public.avaliacao_ciclos;
DELETE FROM public.usuarios_2fa_codes;
DELETE FROM public.usuario_roles;
DELETE FROM public.auth_refresh_tokens;
DELETE FROM public.auth_password_resets;
DELETE FROM public.logs_auditoria;
DELETE FROM public.ponto_logs;
DELETE FROM public.usuarios;

-- ===========================================================================
-- 2. USUÁRIOS - 12 usuários (2 por role)
-- Senha para TODOS: Senha@123
-- ===========================================================================
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login) VALUES
  -- ADMIN
  ('carlos.santos@grupoorp.com',    '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'ADMIN',          false, 'NENHUMA', 0),
  ('patricia.mendes@grupoorp.com',  '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'ADMIN',          true,  'CODIGO',  0),
  -- RH_CHEFE
  ('ana.oliveira@grupoorp.com',     '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'RH_CHEFE',       true,  'CODIGO',  0),
  ('roberto.costa@grupoorp.com',    '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'RH_CHEFE',       false, 'NENHUMA', 0),
  -- RH_ASSISTENTE
  ('diana.souza@grupoorp.com',      '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'RH_ASSISTENTE',  true,  'CODIGO',  0),
  ('lucas.pereira@grupoorp.com',    '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'RH_ASSISTENTE',  false, 'NENHUMA', 0),
  -- DP_CHEFE
  ('bruno.dias@grupoorp.com',       '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'DP_CHEFE',       true,  'CODIGO',  0),
  ('camila.lima@grupoorp.com',      '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'DP_CHEFE',       false, 'NENHUMA', 0),
  -- DP_ASSISTENTE
  ('eduardo.barbosa@grupoorp.com',  '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'DP_ASSISTENTE',  false, 'NENHUMA', 0),
  ('juliana.ribeiro@grupoorp.com',  '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'DP_ASSISTENTE',  true,  'CODIGO',  0),
  -- EMPLOYEE
  ('fernanda.gomes@grupoorp.com',   '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'EMPLOYEE',       true,  'CODIGO',  0),
  ('thiago.araujo@grupoorp.com',    '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'EMPLOYEE',       false, 'NENHUMA', 0);

-- Vincula usuario_roles
INSERT INTO public.usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM public.usuarios u
JOIN public.roles r ON r.nome = u.role;

-- ===========================================================================
-- 3. FUNCIONÁRIOS - 12 funcionários completos (todos os campos preenchidos)
-- ===========================================================================
DO $$
DECLARE
  -- jornada
  j_padrao bigint;
  -- departamentos
  dep_rh  bigint; dep_dp  bigint; dep_ti  bigint; dep_fin bigint;
  dep_com bigint; dep_mkt bigint;
  -- cargos
  c_coord_ti    bigint; c_dba         bigint;
  c_chefe_rh    bigint; c_analista_rh bigint;
  c_assist_rh   bigint; c_recrutador  bigint;
  c_chefe_dp    bigint; c_analista_folha bigint;
  c_assist_dp   bigint;
  c_analista_fin bigint; c_exec_vendas bigint;
  -- usuarios
  u_carlos bigint; u_patricia bigint;
  u_ana bigint; u_roberto bigint;
  u_diana bigint; u_lucas bigint;
  u_bruno bigint; u_camila bigint;
  u_eduardo bigint; u_juliana bigint;
  u_fernanda bigint; u_thiago bigint;
  -- funcionarios (para setar gestor depois)
  f_carlos bigint; f_ana bigint; f_bruno bigint;
BEGIN
  -- Jornada
  SELECT id INTO j_padrao FROM public.ponto_jornadas WHERE nome = 'Padrão 8h' LIMIT 1;

  -- Departamentos
  SELECT id INTO dep_rh  FROM public.departamentos WHERE nome = 'Recursos Humanos' LIMIT 1;
  SELECT id INTO dep_dp  FROM public.departamentos WHERE nome = 'Departamento Pessoal' LIMIT 1;
  SELECT id INTO dep_ti  FROM public.departamentos WHERE nome = 'Tecnologia' LIMIT 1;
  SELECT id INTO dep_fin FROM public.departamentos WHERE nome = 'Financeiro' LIMIT 1;
  SELECT id INTO dep_com FROM public.departamentos WHERE nome = 'Comercial' LIMIT 1;
  SELECT id INTO dep_mkt FROM public.departamentos WHERE nome = 'Marketing' LIMIT 1;

  -- Cargos
  SELECT id INTO c_coord_ti      FROM public.cargos WHERE titulo = 'Coordenador de TI'     AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_dba            FROM public.cargos WHERE titulo = 'DBA'                   AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_chefe_rh      FROM public.cargos WHERE titulo = 'Chefe de RH'           AND departamento_id = dep_rh  LIMIT 1;
  SELECT id INTO c_analista_rh   FROM public.cargos WHERE titulo = 'Analista de RH'        AND departamento_id = dep_rh  LIMIT 1;
  SELECT id INTO c_assist_rh     FROM public.cargos WHERE titulo = 'Assistente de RH'      AND departamento_id = dep_rh  LIMIT 1;
  SELECT id INTO c_recrutador    FROM public.cargos WHERE titulo = 'Recrutador'             AND departamento_id = dep_rh  LIMIT 1;
  SELECT id INTO c_chefe_dp      FROM public.cargos WHERE titulo = 'Chefe de DP'           AND departamento_id = dep_dp  LIMIT 1;
  SELECT id INTO c_analista_folha FROM public.cargos WHERE titulo = 'Analista de Folha'    AND departamento_id = dep_dp  LIMIT 1;
  SELECT id INTO c_assist_dp     FROM public.cargos WHERE titulo = 'Assistente de DP'      AND departamento_id = dep_dp  LIMIT 1;
  SELECT id INTO c_analista_fin  FROM public.cargos WHERE titulo = 'Analista Financeiro'   AND departamento_id = dep_fin LIMIT 1;
  SELECT id INTO c_exec_vendas   FROM public.cargos WHERE titulo = 'Executivo de Vendas'   AND departamento_id = dep_com LIMIT 1;

  -- Usuarios
  SELECT id INTO u_carlos   FROM public.usuarios WHERE email = 'carlos.santos@grupoorp.com';
  SELECT id INTO u_patricia FROM public.usuarios WHERE email = 'patricia.mendes@grupoorp.com';
  SELECT id INTO u_ana      FROM public.usuarios WHERE email = 'ana.oliveira@grupoorp.com';
  SELECT id INTO u_roberto  FROM public.usuarios WHERE email = 'roberto.costa@grupoorp.com';
  SELECT id INTO u_diana    FROM public.usuarios WHERE email = 'diana.souza@grupoorp.com';
  SELECT id INTO u_lucas    FROM public.usuarios WHERE email = 'lucas.pereira@grupoorp.com';
  SELECT id INTO u_bruno    FROM public.usuarios WHERE email = 'bruno.dias@grupoorp.com';
  SELECT id INTO u_camila   FROM public.usuarios WHERE email = 'camila.lima@grupoorp.com';
  SELECT id INTO u_eduardo  FROM public.usuarios WHERE email = 'eduardo.barbosa@grupoorp.com';
  SELECT id INTO u_juliana  FROM public.usuarios WHERE email = 'juliana.ribeiro@grupoorp.com';
  SELECT id INTO u_fernanda FROM public.usuarios WHERE email = 'fernanda.gomes@grupoorp.com';
  SELECT id INTO u_thiago   FROM public.usuarios WHERE email = 'thiago.araujo@grupoorp.com';

  -- =============================================
  -- ADMIN 1 - Carlos Eduardo Santos (sem 2FA)
  -- Coordenador de TI, Tecnologia
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'ADM001', 'Carlos Eduardo Santos', '123.456.789-01', '34.567.890-1', '1988-05-15', 'Masculino', 'Casado',
    'carlos.pessoal@gmail.com', 'carlos.santos@grupoorp.com', '(11) 3456-7890', '(11) 99876-5432',
    '01310-100', 'Avenida Paulista', '1000', 'Sala 1201', 'Bela Vista', 'São Paulo', 'SP',
    'Banco do Brasil', '1234-5', '67890-1', 'Corrente', 'carlos.santos@grupoorp.com',
    c_coord_ti, dep_ti, '2020-03-02', 'ATIVO', 'CLT', 15000.00,
    u_carlos, j_padrao, '2023-01-15', 1,
    'Maria Helena Santos', 'Esposa', '(11) 99876-1111'
  );

  -- =============================================
  -- ADMIN 2 - Patrícia Lima Mendes (2FA ativo)
  -- DBA, Tecnologia
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'ADM002', 'Patrícia Lima Mendes', '234.567.890-12', '45.678.901-2', '1992-11-28', 'Feminino', 'Solteira',
    'patricia.lima@outlook.com', 'patricia.mendes@grupoorp.com', '(11) 3456-7891', '(11) 98765-4321',
    '04543-011', 'Rua Funchal', '418', 'Andar 12', 'Vila Olímpia', 'São Paulo', 'SP',
    'Itaú', '0987-6', '54321-0', 'Corrente', '23456789012',
    c_dba, dep_ti, '2021-07-12', 'ATIVO', 'CLT', 12000.00,
    u_patricia, j_padrao, '2021-07-12', 0,
    'José Mendes', 'Pai', '(11) 97654-3210'
  );

  -- =============================================
  -- RH_CHEFE 1 - Ana Beatriz Oliveira (2FA ativo)
  -- Chefe de RH, Recursos Humanos
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'RHC001', 'Ana Beatriz Oliveira', '345.678.901-23', '56.789.012-3', '1985-08-20', 'Feminino', 'Divorciada',
    'anabeatriz.o@gmail.com', 'ana.oliveira@grupoorp.com', '(11) 3456-7892', '(11) 97654-3219',
    '01046-001', 'Rua Barão de Itapetininga', '255', 'Conj 84', 'República', 'São Paulo', 'SP',
    'Bradesco', '2345-6', '78901-2', 'Corrente', '(11) 97654-3219',
    c_chefe_rh, dep_rh, '2019-01-07', 'ATIVO', 'CLT', 11500.00,
    u_ana, j_padrao, '2022-06-01', 2,
    'Roberto Oliveira', 'Irmão', '(11) 96543-2109'
  );

  -- =============================================
  -- RH_CHEFE 2 - Roberto Almeida Costa (sem 2FA)
  -- Analista de RH, Recursos Humanos
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'RHC002', 'Roberto Almeida Costa', '456.789.012-34', '67.890.123-4', '1990-02-14', 'Masculino', 'Casado',
    'roberto.ac@hotmail.com', 'roberto.costa@grupoorp.com', '(11) 3456-7893', '(11) 96543-2108',
    '02012-000', 'Rua Voluntários da Pátria', '3800', 'Bloco B Ap 42', 'Santana', 'São Paulo', 'SP',
    'Santander', '3456-7', '89012-3', 'Corrente', 'roberto.ac@hotmail.com',
    c_analista_rh, dep_rh, '2022-04-18', 'ATIVO', 'CLT', 5800.00,
    u_roberto, j_padrao, '2022-04-18', 1,
    'Cláudia Costa', 'Esposa', '(11) 95432-1098'
  );

  -- =============================================
  -- RH_ASSISTENTE 1 - Diana Ferreira Souza (2FA ativo)
  -- Assistente de RH, Recursos Humanos
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'RHA001', 'Diana Ferreira Souza', '567.890.123-45', '78.901.234-5', '1998-06-30', 'Feminino', 'Solteira',
    'diana.fs@gmail.com', 'diana.souza@grupoorp.com', '(11) 3456-7894', '(11) 95432-1097',
    '03108-010', 'Rua do Gasômetro', '500', 'Ap 71', 'Brás', 'São Paulo', 'SP',
    'Nubank', '0001', '98765-4', 'Corrente', '56789012345',
    c_assist_rh, dep_rh, '2024-02-05', 'ATIVO', 'CLT', 3200.00,
    u_diana, j_padrao, '2024-02-05', 0,
    'Marcos Souza', 'Pai', '(11) 94321-0987'
  );

  -- =============================================
  -- RH_ASSISTENTE 2 - Lucas Martins Pereira (sem 2FA)
  -- Recrutador, Recursos Humanos
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'RHA002', 'Lucas Martins Pereira', '678.901.234-56', '89.012.345-6', '1996-12-10', 'Masculino', 'União Estável',
    'lucas.mp@yahoo.com', 'lucas.pereira@grupoorp.com', '(11) 3456-7895', '(11) 94321-0986',
    '05407-002', 'Rua dos Pinheiros', '1100', 'Sala 3', 'Pinheiros', 'São Paulo', 'SP',
    'Inter', '0001', '87654-3', 'Corrente', 'lucas.mp@yahoo.com',
    c_recrutador, dep_rh, '2023-08-14', 'ATIVO', 'CLT', 4200.00,
    u_lucas, j_padrao, '2023-08-14', 1,
    'Carla Pereira', 'Companheira', '(11) 93210-9876'
  );

  -- =============================================
  -- DP_CHEFE 1 - Bruno Henrique Dias (2FA ativo)
  -- Chefe de DP, Departamento Pessoal
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'DPC001', 'Bruno Henrique Dias', '789.012.345-67', '90.123.456-7', '1987-09-03', 'Masculino', 'Casado',
    'bruno.hd@gmail.com', 'bruno.dias@grupoorp.com', '(11) 3456-7896', '(11) 93210-9875',
    '04538-132', 'Rua Joaquim Floriano', '466', 'Conj 1501', 'Itaim Bibi', 'São Paulo', 'SP',
    'Banco do Brasil', '5678-9', '12345-6', 'Corrente', '(11) 93210-9875',
    c_chefe_dp, dep_dp, '2018-11-19', 'ATIVO', 'CLT', 11000.00,
    u_bruno, j_padrao, '2021-03-01', 3,
    'Fernanda Dias', 'Esposa', '(11) 92109-8765'
  );

  -- =============================================
  -- DP_CHEFE 2 - Camila Rodrigues Lima (sem 2FA)
  -- Analista de Folha, Departamento Pessoal
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'DPC002', 'Camila Rodrigues Lima', '890.123.456-78', '01.234.567-8', '1993-04-22', 'Feminino', 'Solteira',
    'camila.rl@outlook.com', 'camila.lima@grupoorp.com', '(11) 3456-7897', '(11) 92109-8764',
    '01311-200', 'Rua Augusta', '2025', 'Ap 156', 'Cerqueira César', 'São Paulo', 'SP',
    'Caixa', '0234-5', '67890-1', 'Poupança', '89012345678',
    c_analista_folha, dep_dp, '2022-06-01', 'ATIVO', 'CLT', 5200.00,
    u_camila, j_padrao, '2022-06-01', 0,
    'Renato Lima', 'Pai', '(11) 91098-7654'
  );

  -- =============================================
  -- DP_ASSISTENTE 1 - Eduardo Souza Barbosa (sem 2FA)
  -- Assistente de DP, Departamento Pessoal
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'DPA001', 'Eduardo Souza Barbosa', '901.234.567-89', '12.345.678-9', '1999-01-17', 'Masculino', 'Solteiro',
    'eduardo.sb@gmail.com', 'eduardo.barbosa@grupoorp.com', '(11) 3456-7898', '(11) 91098-7653',
    '09541-100', 'Rua Giovanni Battista Pirelli', '750', 'Bloco A Ap 23', 'Vila Homero Thon', 'Santo André', 'SP',
    'Bradesco', '6789-0', '23456-7', 'Corrente', 'eduardo.sb@gmail.com',
    c_assist_dp, dep_dp, '2024-09-02', 'ATIVO', 'CLT', 3500.00,
    u_eduardo, j_padrao, '2024-09-02', 0,
    'Sônia Barbosa', 'Mãe', '(11) 90987-6543'
  );

  -- =============================================
  -- DP_ASSISTENTE 2 - Juliana Castro Ribeiro (2FA ativo)
  -- Assistente de DP, Departamento Pessoal
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'DPA002', 'Juliana Castro Ribeiro', '012.345.678-90', '23.456.789-0', '1995-07-08', 'Feminino', 'Casada',
    'juliana.cr@hotmail.com', 'juliana.ribeiro@grupoorp.com', '(11) 3456-7899', '(11) 90987-6542',
    '06454-000', 'Alameda Araguaia', '2104', 'Torre 2 Sala 45', 'Alphaville', 'Barueri', 'SP',
    'Itaú', '1234-5', '34567-8', 'Corrente', '(11) 90987-6542',
    c_assist_dp, dep_dp, '2023-03-20', 'ATIVO', 'CLT', 3800.00,
    u_juliana, j_padrao, '2023-03-20', 1,
    'Ricardo Ribeiro', 'Marido', '(11) 89876-5432'
  );

  -- =============================================
  -- EMPLOYEE 1 - Fernanda Oliveira Gomes (2FA ativo)
  -- Analista Financeiro, Financeiro
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'EMP001', 'Fernanda Oliveira Gomes', '147.258.369-01', '34.567.890-X', '1997-03-25', 'Feminino', 'Solteira',
    'fernanda.og@gmail.com', 'fernanda.gomes@grupoorp.com', '(11) 3456-7800', '(11) 89876-5431',
    '04547-005', 'Rua Fidêncio Ramos', '302', 'Conj 121', 'Vila Olímpia', 'São Paulo', 'SP',
    'Nubank', '0001', '45678-9', 'Corrente', '14725836901',
    c_analista_fin, dep_fin, '2024-01-08', 'ATIVO', 'CLT', 6200.00,
    u_fernanda, j_padrao, '2024-01-08', 0,
    'Teresa Gomes', 'Mãe', '(11) 88765-4321'
  );

  -- =============================================
  -- EMPLOYEE 2 - Thiago Nascimento Araújo (sem 2FA)
  -- Executivo de Vendas, Comercial
  -- =============================================
  INSERT INTO public.funcionarios (
    matricula, nome_completo, cpf, rg, data_nascimento, genero, estado_civil,
    email_pessoal, email_corporativo, telefone, celular,
    cep, logradouro, numero, complemento, bairro, cidade, estado,
    banco, agencia, conta, tipo_conta, pix,
    cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual,
    usuario_id, jornada_id, cargo_desde, dependentes_qtd,
    emergencia_nome, emergencia_parentesco, emergencia_telefone
  ) VALUES (
    'EMP002', 'Thiago Nascimento Araújo', '258.369.147-02', '45.678.901-X', '1994-10-12', 'Masculino', 'Casado',
    'thiago.na@yahoo.com', 'thiago.araujo@grupoorp.com', '(11) 3456-7801', '(11) 88765-4320',
    '01452-001', 'Rua Oscar Freire', '725', 'Loja 3', 'Jardim Paulista', 'São Paulo', 'SP',
    'Santander', '4567-8', '56789-0', 'Corrente', 'thiago.na@yahoo.com',
    c_exec_vendas, dep_com, '2023-05-15', 'ATIVO', 'CLT', 5800.00,
    u_thiago, j_padrao, '2023-05-15', 2,
    'Amanda Araújo', 'Esposa', '(11) 87654-3219'
  );

  -- =============================================
  -- 4. GESTORES - Define hierarquia
  -- =============================================
  SELECT id INTO f_carlos FROM public.funcionarios WHERE matricula = 'ADM001';
  SELECT id INTO f_ana    FROM public.funcionarios WHERE matricula = 'RHC001';
  SELECT id INTO f_bruno  FROM public.funcionarios WHERE matricula = 'DPC001';

  -- Patrícia (ADM002) reporta para Carlos (ADM001)
  UPDATE public.funcionarios SET gestor_id = f_carlos WHERE matricula = 'ADM002';

  -- Roberto, Diana, Lucas (RH) reportam para Ana (RHC001)
  UPDATE public.funcionarios SET gestor_id = f_ana WHERE matricula IN ('RHC002', 'RHA001', 'RHA002');

  -- Camila, Eduardo, Juliana (DP) reportam para Bruno (DPC001)
  UPDATE public.funcionarios SET gestor_id = f_bruno WHERE matricula IN ('DPC002', 'DPA001', 'DPA002');

  -- Fernanda (Financeiro) e Thiago (Comercial) reportam para Carlos (Admin/TI)
  UPDATE public.funcionarios SET gestor_id = f_carlos WHERE matricula IN ('EMP001', 'EMP002');

END $$;

COMMIT;

-- ===========================================================================
-- RESUMO DOS LOGINS
-- ===========================================================================
-- | Email                            | Senha     | Role           | 2FA     |
-- |----------------------------------|-----------|----------------|---------|
-- | carlos.santos@grupoorp.com       | Admin@123 | ADMIN          | Não     |
-- | patricia.mendes@grupoorp.com     | Admin@123 | ADMIN          | Sim     |
-- | ana.oliveira@grupoorp.com        | Admin@123 | RH_CHEFE       | Sim     |
-- | roberto.costa@grupoorp.com       | Admin@123 | RH_CHEFE       | Não     |
-- | diana.souza@grupoorp.com         | Admin@123 | RH_ASSISTENTE  | Sim     |
-- | lucas.pereira@grupoorp.com       | Admin@123 | RH_ASSISTENTE  | Não     |
-- | bruno.dias@grupoorp.com          | Admin@123 | DP_CHEFE       | Sim     |
-- | camila.lima@grupoorp.com         | Admin@123 | DP_CHEFE       | Não     |
-- | eduardo.barbosa@grupoorp.com     | Admin@123 | DP_ASSISTENTE  | Não     |
-- | juliana.ribeiro@grupoorp.com     | Admin@123 | DP_ASSISTENTE  | Sim     |
-- | fernanda.gomes@grupoorp.com      | Admin@123 | EMPLOYEE       | Sim     |
-- | thiago.araujo@grupoorp.com       | Admin@123 | EMPLOYEE       | Não     |
-- ===========================================================================
