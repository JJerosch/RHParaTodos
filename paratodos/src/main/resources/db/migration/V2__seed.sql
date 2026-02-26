-- V2__seed_adjusted.sql
-- Seed essencial (DEV/DEMO): roles, permissões, jornadas, departamentos, cargos, tipos, usuários, funcionários, parâmetros
-- Sistema RH Para Todos
--
-- Observação: este arquivo depende do V1__baseline_adjusted.sql já aplicado.
SET client_encoding = 'UTF8';
SET search_path = public;


-- SEED ESSENCIAL (DEV/DEMO) - ajuste conforme necessidade de produção
-- =====================================================================

-- Roles (para futuro RBAC) + Permissões básicas
INSERT INTO public.roles (nome, descricao) VALUES
  ('ADMIN', 'Administrador do sistema'),
  ('RH_CHEFE', 'Chefe do RH'),
  ('RH_ASSISTENTE', 'Assistente do RH'),
  ('DP_CHEFE', 'Chefe do DP'),
  ('DP_ASSISTENTE', 'Assistente do DP'),
  ('EMPLOYEE', 'Funcionário')
ON CONFLICT (nome) DO NOTHING;

INSERT INTO public.permissoes (chave, descricao) VALUES
  ('DASHBOARD_VIEW', 'Visualizar dashboard'),
  ('EMPLOYEE_READ', 'Consultar funcionários'),
  ('EMPLOYEE_WRITE', 'Cadastrar/editar funcionários'),
  ('PAYROLL_PROCESS', 'Processar folha'),
  ('VACATION_APPROVE', 'Aprovar férias'),
  ('TIMESHEET_APPROVE', 'Aprovar ponto/justificativas')
ON CONFLICT (chave) DO NOTHING;

-- Mapeamento simples role -> permissões (mínimo viável)
INSERT INTO public.role_permissoes (role_id, permissao_id)
SELECT r.id, p.id
FROM public.roles r
JOIN public.permissoes p ON p.chave IN ('DASHBOARD_VIEW','EMPLOYEE_READ')
WHERE r.nome IN ('EMPLOYEE')
ON CONFLICT DO NOTHING;

INSERT INTO public.role_permissoes (role_id, permissao_id)
SELECT r.id, p.id
FROM public.roles r
JOIN public.permissoes p ON p.chave IN ('DASHBOARD_VIEW','EMPLOYEE_READ','EMPLOYEE_WRITE','VACATION_APPROVE','TIMESHEET_APPROVE')
WHERE r.nome IN ('RH_CHEFE','RH_ASSISTENTE')
ON CONFLICT DO NOTHING;

INSERT INTO public.role_permissoes (role_id, permissao_id)
SELECT r.id, p.id
FROM public.roles r
JOIN public.permissoes p ON p.chave IN ('DASHBOARD_VIEW','EMPLOYEE_READ','PAYROLL_PROCESS')
WHERE r.nome IN ('DP_CHEFE','DP_ASSISTENTE')
ON CONFLICT DO NOTHING;

INSERT INTO public.role_permissoes (role_id, permissao_id)
SELECT r.id, p.id
FROM public.roles r
JOIN public.permissoes p ON p.chave IN ('DASHBOARD_VIEW','EMPLOYEE_READ','EMPLOYEE_WRITE','PAYROLL_PROCESS','VACATION_APPROVE','TIMESHEET_APPROVE')
WHERE r.nome = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Jornadas (timesheet)
INSERT INTO public.ponto_jornadas (nome, carga_horaria_diaria, intervalo_minutos, horario_entrada, horario_saida, flexivel)
SELECT 'Padrão 8h', 8.00, 60, '08:00', '17:00', false
WHERE NOT EXISTS (SELECT 1 FROM public.ponto_jornadas WHERE nome = 'Padrão 8h');

INSERT INTO public.ponto_jornadas (nome, carga_horaria_diaria, intervalo_minutos, horario_entrada, horario_saida, flexivel)
SELECT '6h', 6.00, 15, '09:00', '15:15', false
WHERE NOT EXISTS (SELECT 1 FROM public.ponto_jornadas WHERE nome = '6h');

INSERT INTO public.ponto_jornadas (nome, carga_horaria_diaria, intervalo_minutos, flexivel)
SELECT 'Flexível', 8.00, 60, true
WHERE NOT EXISTS (SELECT 1 FROM public.ponto_jornadas WHERE nome = 'Flexível');

-- Departamentos
INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Recursos Humanos', 'Gestão de pessoas e processos de RH', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Recursos Humanos');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Departamento Pessoal', 'Folha, benefícios e rotinas trabalhistas', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Departamento Pessoal');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Tecnologia', 'TI/Desenvolvimento e suporte', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Tecnologia');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Financeiro', 'Contas, orçamento e financeiro', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Financeiro');

-- Cargos (dependem de departamentos)
DO $$
DECLARE
  dep_rh bigint;
  dep_dp bigint;
  dep_ti bigint;
  dep_fin bigint;
BEGIN
  SELECT id INTO dep_rh FROM public.departamentos WHERE nome='Recursos Humanos' ORDER BY id LIMIT 1;
  SELECT id INTO dep_dp FROM public.departamentos WHERE nome='Departamento Pessoal' ORDER BY id LIMIT 1;
  SELECT id INTO dep_ti FROM public.departamentos WHERE nome='Tecnologia' ORDER BY id LIMIT 1;
  SELECT id INTO dep_fin FROM public.departamentos WHERE nome='Financeiro' ORDER BY id LIMIT 1;

  -- RH
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Chefe de RH' AND departamento_id=dep_rh) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Chefe de RH','Responsável pela área de RH','SENIOR',dep_rh, 6000, 12000, true);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Assistente de RH' AND departamento_id=dep_rh) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Assistente de RH','Apoio operacional do RH','JUNIOR',dep_rh, 2500, 4500, true);
  END IF;

  -- DP
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Chefe de DP' AND departamento_id=dep_dp) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Chefe de DP','Responsável por folha e rotinas','SENIOR',dep_dp, 6500, 13000, true);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Assistente de DP' AND departamento_id=dep_dp) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Assistente de DP','Apoio operacional do DP','PLENO',dep_dp, 3000, 5000, true);
  END IF;

  -- TI
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Desenvolvedor' AND departamento_id=dep_ti) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Desenvolvedor','Desenvolvimento do sistema','PLENO',dep_ti, 5000, 10000, true);
  END IF;

  -- Financeiro
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Analista Financeiro' AND departamento_id=dep_fin) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Analista Financeiro','Análises e rotinas financeiras','PLENO',dep_fin, 4500, 8500, true);
  END IF;
END $$;

-- Tipos de documentos (pra alertas de vencimento no dashboard)
INSERT INTO public.tipos_documentos (nome, descricao, obrigatorio, ativo) VALUES
  ('ASO', 'Atestado de Saúde Ocupacional', true, true),
  ('CTPS', 'Carteira de Trabalho', true, true),
  ('RG', 'Documento de identidade', true, true),
  ('CPF', 'Cadastro de Pessoa Física', true, true),
  ('Comprovante de Residência', 'Comprovante de endereço', false, true)
ON CONFLICT (nome) DO UPDATE SET
  descricao = EXCLUDED.descricao,
  obrigatorio = EXCLUDED.obrigatorio,
  ativo = EXCLUDED.ativo;

-- Tipos de benefícios (vai sincronizar eventos via trigger tg_tipos_beneficios_sync_evento)
INSERT INTO public.tipos_beneficios (nome, descricao, possui_desconto_folha, valor_padrao, ativo, natureza, incide_ferias, incide_decimo)
SELECT 'Vale Transporte', 'VT', true, 220.00, true, 'DESCONTO', false, false
WHERE NOT EXISTS (SELECT 1 FROM public.tipos_beneficios WHERE nome='Vale Transporte');

INSERT INTO public.tipos_beneficios (nome, descricao, possui_desconto_folha, valor_padrao, ativo, natureza, incide_ferias, incide_decimo)
SELECT 'Vale Refeição', 'VR', false, 600.00, true, 'PROVENTO', true, true
WHERE NOT EXISTS (SELECT 1 FROM public.tipos_beneficios WHERE nome='Vale Refeição');

INSERT INTO public.tipos_beneficios (nome, descricao, possui_desconto_folha, valor_padrao, ativo, natureza, incide_ferias, incide_decimo)
SELECT 'Plano de Saúde', 'Benefício (desconto parcial)', true, 350.00, true, 'DESCONTO', false, false
WHERE NOT EXISTS (SELECT 1 FROM public.tipos_beneficios WHERE nome='Plano de Saúde');

-- Catálogo de eventos de folha (conteúdo do V2)
-- Seeds mínimos do catálogo de eventos (idempotente)
-- natureza: PROVENTO | DESCONTO | INFORMATIVO
-- tipo:     SALARIAL | IMPOSTO  | ENCARGO

INSERT INTO public.folha_eventos_catalogo
  (codigo, descricao, natureza, tipo,
   incide_ferias, incide_decimo, incide_inss, incide_fgts, incide_irrf, ativo)
VALUES
  ('SALARIO',    'Salário',               'PROVENTO',    'SALARIAL', true,  true,  true,  true,  true,  true),
  ('HEXTRA',     'Hora Extra',            'PROVENTO',    'SALARIAL', true,  true,  true,  true,  true,  true),
  ('FERIAS',     'Férias',                'PROVENTO',    'SALARIAL', false, false, true,  true,  true,  true),
  ('FERIAS_1_3', 'Férias 1/3 (Constit.)', 'PROVENTO',    'SALARIAL', false, false, true,  true,  true,  true),

  ('FALTA',      'Falta / Desconto',      'DESCONTO',    'SALARIAL', true,  true,  true,  true,  true,  true),

  ('INSS',       'INSS (Desconto)',       'DESCONTO',    'IMPOSTO',  false, false, false, false, false, true),
  ('IRRF',       'IRRF (Desconto)',       'DESCONTO',    'IMPOSTO',  false, false, false, false, false, true),

  ('FGTS',       'FGTS (Encargo/Info)',   'INFORMATIVO', 'ENCARGO',  false, false, false, false, false, true)

ON CONFLICT (codigo) DO UPDATE SET
  descricao     = EXCLUDED.descricao,
  natureza      = EXCLUDED.natureza,
  tipo          = EXCLUDED.tipo,
  incide_ferias = EXCLUDED.incide_ferias,
  incide_decimo = EXCLUDED.incide_decimo,
  incide_inss   = EXCLUDED.incide_inss,
  incide_fgts   = EXCLUDED.incide_fgts,
  incide_irrf   = EXCLUDED.incide_irrf,
  ativo         = EXCLUDED.ativo;

-- Usuários (conteúdo do V4 + V5). OBS: V3 fica redundante, então incorporamos tudo aqui.
-- V4: Seed all test users (6 profiles)
-- Passwords are BCrypt $2b$10$ hashed
-- 2FA via email (tipo_2fa = 'CODIGO') para quem tem autenticacao_2fa = true

-- admin@local / Admin@123 (sem 2FA)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('admin@local', '$2b$10$pyQa1YGxNkufoyPx2.DnP.jPuiyl0OEdIc8ePClQhoppqFb5IKVWG', true, 'ADMIN', false, 'NENHUMA', 0)
ON CONFLICT (email) DO NOTHING;

-- rh.chefe@local / RhChefe@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('rh.chefe@local', '$2b$10$34c8uUXWdx1qXy.KA4mhnO.2qKXxxlQye/AHmIhzzYzyOQ/6sprBW', true, 'RH_CHEFE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;

-- rh.assistente@local / RhAssist@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('rh.assistente@local', '$2b$10$wwS4K1TdiAAMihu1kLlgTOI4uL4DiY4lGv4oUp3dCfiFOQ2yMv7Jm', true, 'RH_ASSISTENTE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;

-- dp.chefe@local / DpChefe@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('dp.chefe@local', '$2b$10$MP9F5MDAd1FnP.ZDGKoLieeoGnN7gU77wiIreaPZB.A49T/554I7i', true, 'DP_CHEFE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;

-- dp.assistente@local / DpAssist@123 (sem 2FA)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('dp.assistente@local', '$2b$10$ZU8suZx4JUuh5P.hoA5abuuD9BtQbIeZU6lmZtsTxBJhNqmd4rsLe', true, 'DP_ASSISTENTE', false, 'NENHUMA', 0)
ON CONFLICT (email) DO NOTHING;

-- funcionario@local / Func@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('funcionario@local', '$2b$10$W6dWk8VPMfLFIkz0kduWyeyKyd5GeviKbukVtcD44grvpNbeq9HmO', true, 'EMPLOYEE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;


-- V5: Usuários adicionais sem 2FA
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login) VALUES
  ('rh.chefe2@local',      '$2b$10$YF6jzFx0Af5v010POOfXmeFwb6nFVD2roTzca5d7M92HLrcXdPRgi', true, 'RH_CHEFE',      false, 'NENHUMA', 0),
  ('rh.assistente2@local', '$2b$10$qsPCziNG/nKZ3.vcLTMXlOnjp2QyEA5vriKEYPa7KeBro9DMk2bfa', true, 'RH_ASSISTENTE', false, 'NENHUMA', 0),
  ('dp.chefe2@local',      '$2b$10$xt6I0CqvAk5CR7zW0xBzveW.XZ/z6rYx6jWm.XN2qdV1BcN5Avd3O', true, 'DP_CHEFE',      false, 'NENHUMA', 0),
  ('dp.assistente2@local', '$2b$10$uL9ZZcWtQGqcTvv58CRflOuQWRCkM254C8Xn.Ow58qeq3GbYjPcSe', true, 'DP_ASSISTENTE', false, 'NENHUMA', 0),
  ('funcionario2@local',   '$2b$10$ETJv18fmb8XoRpfirhH1z.kHRlYun4SjzBy6ZaOfEC7hasb1Ljz8y', true, 'EMPLOYEE',      false, 'NENHUMA', 0),
  ('funcionario3@local',   '$2b$10$Vv5JI6hd59OEqDjDhJYT8.myH8eyVqsj9bE4wBpi7yCU929gl0/oe', true, 'EMPLOYEE',      false, 'NENHUMA', 0)
ON CONFLICT (email) DO NOTHING;

-- Vincula usuarios -> roles (tabela usuario_roles) para quem você quiser usar RBAC depois
INSERT INTO public.usuario_roles (usuario_id, role_id)
SELECT u.id, r.id
FROM public.usuarios u
JOIN public.roles r ON r.nome = u.role
ON CONFLICT DO NOTHING;

-- Funcionários (mínimo para popular dashboard).
-- (CPFs e matrículas fictícias apenas para DEV/DEMO)
DO $$
DECLARE
  j_padrao bigint;
  dep_rh bigint; dep_dp bigint; dep_ti bigint; dep_fin bigint;
  cargo_rh_chefe bigint; cargo_rh_ass bigint; cargo_dp_chefe bigint; cargo_dp_ass bigint; cargo_dev bigint; cargo_fin bigint;

  u_admin bigint; u_rh_chefe bigint; u_rh_ass bigint; u_dp_chefe bigint; u_dp_ass bigint; u_emp bigint;
  f_rh_chefe bigint; f_dp_chefe bigint;
BEGIN
  SELECT id INTO j_padrao FROM public.ponto_jornadas WHERE nome='Padrão 8h' ORDER BY id LIMIT 1;

  SELECT id INTO dep_rh  FROM public.departamentos WHERE nome='Recursos Humanos' ORDER BY id LIMIT 1;
  SELECT id INTO dep_dp  FROM public.departamentos WHERE nome='Departamento Pessoal' ORDER BY id LIMIT 1;
  SELECT id INTO dep_ti  FROM public.departamentos WHERE nome='Tecnologia' ORDER BY id LIMIT 1;
  SELECT id INTO dep_fin FROM public.departamentos WHERE nome='Financeiro' ORDER BY id LIMIT 1;

  SELECT id INTO cargo_rh_chefe FROM public.cargos WHERE titulo='Chefe de RH' AND departamento_id=dep_rh ORDER BY id LIMIT 1;
  SELECT id INTO cargo_rh_ass   FROM public.cargos WHERE titulo='Assistente de RH' AND departamento_id=dep_rh ORDER BY id LIMIT 1;
  SELECT id INTO cargo_dp_chefe FROM public.cargos WHERE titulo='Chefe de DP' AND departamento_id=dep_dp ORDER BY id LIMIT 1;
  SELECT id INTO cargo_dp_ass   FROM public.cargos WHERE titulo='Assistente de DP' AND departamento_id=dep_dp ORDER BY id LIMIT 1;
  SELECT id INTO cargo_dev      FROM public.cargos WHERE titulo='Desenvolvedor' AND departamento_id=dep_ti ORDER BY id LIMIT 1;
  SELECT id INTO cargo_fin      FROM public.cargos WHERE titulo='Analista Financeiro' AND departamento_id=dep_fin ORDER BY id LIMIT 1;

  SELECT id INTO u_admin     FROM public.usuarios WHERE email='admin@local' ORDER BY id LIMIT 1;
  SELECT id INTO u_rh_chefe  FROM public.usuarios WHERE email='rh.chefe@local' ORDER BY id LIMIT 1;
  SELECT id INTO u_rh_ass    FROM public.usuarios WHERE email='rh.assistente@local' ORDER BY id LIMIT 1;
  SELECT id INTO u_dp_chefe  FROM public.usuarios WHERE email='dp.chefe@local' ORDER BY id LIMIT 1;
  SELECT id INTO u_dp_ass    FROM public.usuarios WHERE email='dp.assistente@local' ORDER BY id LIMIT 1;
  SELECT id INTO u_emp       FROM public.usuarios WHERE email='funcionario@local' ORDER BY id LIMIT 1;

  -- RH Chefe (gestor)
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='000.000.000-01') THEN
    INSERT INTO public.funcionarios
      (matricula, nome_completo, cpf, data_nascimento, email_corporativo, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, usuario_id, jornada_id, dependentes_qtd)
    VALUES
      ('0001', 'Ana RH Chefe', '000.000.000-01', '1990-03-10', 'rh.chefe@local', cargo_rh_chefe, dep_rh, current_date - 200, 'ATIVO', 'CLT', 9500.00, u_rh_chefe, j_padrao, 1);
  END IF;

  -- DP Chefe (gestor)
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='000.000.000-02') THEN
    INSERT INTO public.funcionarios
      (matricula, nome_completo, cpf, data_nascimento, email_corporativo, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, usuario_id, jornada_id, dependentes_qtd)
    VALUES
      ('0002', 'Bruno DP Chefe', '000.000.000-02', '1988-07-22', 'dp.chefe@local', cargo_dp_chefe, dep_dp, current_date - 180, 'ATIVO', 'CLT', 9800.00, u_dp_chefe, j_padrao, 0);
  END IF;

  -- Admin (TI)
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='000.000.000-03') THEN
    INSERT INTO public.funcionarios
      (matricula, nome_completo, cpf, data_nascimento, email_corporativo, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, usuario_id, jornada_id, dependentes_qtd)
    VALUES
      ('0003', 'Carlos Admin', '000.000.000-03', '1995-01-05', 'admin@local', cargo_dev, dep_ti, current_date - 120, 'ATIVO', 'CLT', 11000.00, u_admin, j_padrao, 0);
  END IF;

  -- RH Assistente (reporta para RH chefe)
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='000.000.000-04') THEN
    INSERT INTO public.funcionarios
      (matricula, nome_completo, cpf, data_nascimento, email_corporativo, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, usuario_id, jornada_id, dependentes_qtd)
    VALUES
      ('0004', 'Diana RH Assistente', '000.000.000-04', '2000-11-15', 'rh.assistente@local', cargo_rh_ass, dep_rh, current_date - 40, 'ATIVO', 'CLT', 3800.00, u_rh_ass, j_padrao, 0);
  END IF;

  -- DP Assistente (reporta para DP chefe)
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='000.000.000-05') THEN
    INSERT INTO public.funcionarios
      (matricula, nome_completo, cpf, data_nascimento, email_corporativo, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, usuario_id, jornada_id, dependentes_qtd)
    VALUES
      ('0005', 'Edu DP Assistente', '000.000.000-05', '1999-09-09', 'dp.assistente@local', cargo_dp_ass, dep_dp, current_date - 20, 'ATIVO', 'CLT', 4200.00, u_dp_ass, j_padrao, 0);
  END IF;

  -- Funcionário (Financeiro)
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='000.000.000-06') THEN
    INSERT INTO public.funcionarios
      (matricula, nome_completo, cpf, data_nascimento, email_corporativo, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, usuario_id, jornada_id, dependentes_qtd)
    VALUES
      ('0006', 'Fernanda Funcionária', '000.000.000-06', '1997-05-30', 'funcionario@local', cargo_fin, dep_fin, current_date - 10, 'ATIVO', 'CLT', 5200.00, u_emp, j_padrao, 2);
  END IF;

  -- Seta gestores (update é idempotente)
  SELECT id INTO f_rh_chefe FROM public.funcionarios WHERE cpf='000.000.000-01' ORDER BY id LIMIT 1;
  SELECT id INTO f_dp_chefe FROM public.funcionarios WHERE cpf='000.000.000-02' ORDER BY id LIMIT 1;

  UPDATE public.funcionarios
     SET gestor_id = f_rh_chefe
   WHERE cpf='000.000.000-04' AND (gestor_id IS DISTINCT FROM f_rh_chefe);

  UPDATE public.funcionarios
     SET gestor_id = f_dp_chefe
   WHERE cpf='000.000.000-05' AND (gestor_id IS DISTINCT FROM f_dp_chefe);
END $$;

-- Benefícios para 2 pessoas (pra dashboard/folha testar)
DO $$
DECLARE
  f1 bigint; f2 bigint;
  b_vr bigint; b_vt bigint;
BEGIN
  SELECT id INTO f1 FROM public.funcionarios WHERE cpf='000.000.000-04' ORDER BY id LIMIT 1; -- RH Assistente
  SELECT id INTO f2 FROM public.funcionarios WHERE cpf='000.000.000-06' ORDER BY id LIMIT 1; -- Funcionária

  SELECT id INTO b_vr FROM public.tipos_beneficios WHERE nome='Vale Refeição' ORDER BY id LIMIT 1;
  SELECT id INTO b_vt FROM public.tipos_beneficios WHERE nome='Vale Transporte' ORDER BY id LIMIT 1;

  IF f1 IS NOT NULL AND b_vr IS NOT NULL AND NOT EXISTS (
      SELECT 1 FROM public.funcionarios_beneficios WHERE funcionario_id=f1 AND tipo_beneficio_id=b_vr AND ativo=true
  ) THEN
    INSERT INTO public.funcionarios_beneficios (funcionario_id, tipo_beneficio_id, valor, data_inicio, ativo)
    VALUES (f1, b_vr, 600.00, current_date - 30, true);
  END IF;

  IF f2 IS NOT NULL AND b_vt IS NOT NULL AND NOT EXISTS (
      SELECT 1 FROM public.funcionarios_beneficios WHERE funcionario_id=f2 AND tipo_beneficio_id=b_vt AND ativo=true
  ) THEN
    INSERT INTO public.funcionarios_beneficios (funcionario_id, tipo_beneficio_id, valor, data_inicio, ativo)
    VALUES (f2, b_vt, 220.00, current_date - 15, true);
  END IF;
END $$;

-- Férias pendentes (pra aparecer no dashboard)
DO $$
DECLARE
  f bigint;
  p bigint;
BEGIN
  SELECT id INTO f FROM public.funcionarios WHERE cpf='000.000.000-06' ORDER BY id LIMIT 1;

  IF f IS NULL THEN
    RETURN;
  END IF;

  -- cria período aquisitivo se não existir
  IF NOT EXISTS (
    SELECT 1 FROM public.ferias_periodos_aquisitivos
     WHERE funcionario_id = f AND data_inicio = (current_date - interval '1 year')::date
  ) THEN
    INSERT INTO public.ferias_periodos_aquisitivos (funcionario_id, data_inicio, data_fim, dias_direito)
    VALUES (f, (current_date - interval '1 year')::date, (current_date - interval '1 day')::date, 30);
  END IF;

  SELECT id INTO p
  FROM public.ferias_periodos_aquisitivos
  WHERE funcionario_id = f
  ORDER BY data_inicio DESC
  LIMIT 1;

  -- Solicitação pendente
  IF p IS NOT NULL AND NOT EXISTS (
    SELECT 1 FROM public.ferias_solicitacoes WHERE funcionario_id=f AND periodo_aquisitivo_id=p AND status='PENDENTE'
  ) THEN
    INSERT INTO public.ferias_solicitacoes (funcionario_id, periodo_aquisitivo_id, data_inicio, data_fim, dias_solicitados, status)
    VALUES (f, p, (current_date + interval '20 day')::date, (current_date + interval '29 day')::date, 10, 'PENDENTE');
  END IF;
END $$;

-- Documento vencendo (pra aparecer no dashboard)
DO $$
DECLARE
  f bigint;
  td bigint;
BEGIN
  SELECT id INTO f FROM public.funcionarios WHERE cpf='000.000.000-06' ORDER BY id LIMIT 1;
  SELECT id INTO td FROM public.tipos_documentos WHERE nome='ASO' ORDER BY id LIMIT 1;

  IF f IS NULL OR td IS NULL THEN
    RETURN;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM public.dados_funcionarios_documentos
     WHERE funcionario_id=f AND tipo_documento_id=td AND data_validade = (current_date + interval '10 day')::date
  ) THEN
    INSERT INTO public.dados_funcionarios_documentos (funcionario_id, tipo_documento_id, numero, data_emissao, data_validade)
    VALUES (f, td, 'ASO-2026-0001', (current_date - interval '355 day')::date, (current_date + interval '10 day')::date);
  END IF;
END $$;

-- INSS/IRRF (valores de exemplo para DEV - ajuste conforme ano/legislação)
-- Se já existirem faixas/linha do ano, não insere.
INSERT INTO public.inss_faixas (ano, faixa_inicio, faixa_fim, aliquota)
SELECT 2025, 0.00, 1412.00, 0.075 WHERE NOT EXISTS (SELECT 1 FROM public.inss_faixas WHERE ano=2025);

INSERT INTO public.irrf_parametros (ano, valor_dependente)
SELECT 2025, 189.59 WHERE NOT EXISTS (SELECT 1 FROM public.irrf_parametros WHERE ano=2025);

INSERT INTO public.irrf_tabela (faixa_min, faixa_max, aliquota, parcela_deduzir, vigente_desde)
SELECT 0.00, 2259.20, 0.0000, 0.00, '2025-01-01'
WHERE NOT EXISTS (SELECT 1 FROM public.irrf_tabela WHERE vigente_desde='2025-01-01');

