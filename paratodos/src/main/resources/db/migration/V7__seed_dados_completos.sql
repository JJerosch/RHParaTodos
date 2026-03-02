-- V7 - Seed completo: mais departamentos, cargos e funcionários para demonstração
-- Garante que o dashboard, quadro de lotação, cargos e promoções tenham dados reais
SET client_encoding = 'UTF8';
SET search_path = public;

-- =============================================
-- 1. NOVOS DEPARTAMENTOS
-- =============================================
INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Comercial', 'Vendas, negociação e relacionamento com clientes', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Comercial');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Marketing', 'Comunicação, branding e estratégias de marketing', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Marketing');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Jurídico', 'Assessoria jurídica, contratos e compliance', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Jurídico');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Operações', 'Logística, infraestrutura e gestão operacional', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Operações');

INSERT INTO public.departamentos (nome, descricao, ativo)
SELECT 'Administrativo', 'Recepção, facilities e serviços gerais', true
WHERE NOT EXISTS (SELECT 1 FROM public.departamentos WHERE nome = 'Administrativo');

-- =============================================
-- 2. NOVOS CARGOS (por departamento)
-- =============================================
DO $$
DECLARE
  dep_rh bigint; dep_dp bigint; dep_ti bigint; dep_fin bigint;
  dep_com bigint; dep_mkt bigint; dep_jur bigint; dep_ops bigint; dep_adm bigint;
BEGIN
  SELECT id INTO dep_rh  FROM public.departamentos WHERE nome='Recursos Humanos' LIMIT 1;
  SELECT id INTO dep_dp  FROM public.departamentos WHERE nome='Departamento Pessoal' LIMIT 1;
  SELECT id INTO dep_ti  FROM public.departamentos WHERE nome='Tecnologia' LIMIT 1;
  SELECT id INTO dep_fin FROM public.departamentos WHERE nome='Financeiro' LIMIT 1;
  SELECT id INTO dep_com FROM public.departamentos WHERE nome='Comercial' LIMIT 1;
  SELECT id INTO dep_mkt FROM public.departamentos WHERE nome='Marketing' LIMIT 1;
  SELECT id INTO dep_jur FROM public.departamentos WHERE nome='Jurídico' LIMIT 1;
  SELECT id INTO dep_ops FROM public.departamentos WHERE nome='Operações' LIMIT 1;
  SELECT id INTO dep_adm FROM public.departamentos WHERE nome='Administrativo' LIMIT 1;

  -- RH - cargos adicionais
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Analista de RH' AND departamento_id=dep_rh) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Analista de RH', 'Análise e execução de processos de RH', 'PLENO', dep_rh, 4000, 7000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Recrutador' AND departamento_id=dep_rh) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Recrutador', 'Recrutamento e seleção de talentos', 'PLENO', dep_rh, 3500, 6000, true);
  END IF;

  -- DP - cargos adicionais
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Analista de Folha' AND departamento_id=dep_dp) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Analista de Folha', 'Processamento de folha de pagamento', 'PLENO', dep_dp, 3800, 6500, true);
  END IF;

  -- TI - cargos adicionais
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Coordenador de TI' AND departamento_id=dep_ti) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Coordenador de TI', 'Coordenação da equipe de tecnologia', 'GESTAO', dep_ti, 10000, 16000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Desenvolvedor Júnior' AND departamento_id=dep_ti) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Desenvolvedor Júnior', 'Desenvolvimento de sistemas - nível inicial', 'JUNIOR', dep_ti, 3000, 5000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Analista de Suporte' AND departamento_id=dep_ti) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Analista de Suporte', 'Suporte técnico e help desk', 'JUNIOR', dep_ti, 2500, 4500, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='DBA' AND departamento_id=dep_ti) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('DBA', 'Administração de banco de dados', 'SENIOR', dep_ti, 8000, 14000, true);
  END IF;

  -- Financeiro - cargos adicionais
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Gerente Financeiro' AND departamento_id=dep_fin) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Gerente Financeiro', 'Gestão financeira e orçamentária', 'GESTAO', dep_fin, 10000, 18000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Assistente Financeiro' AND departamento_id=dep_fin) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Assistente Financeiro', 'Apoio em rotinas financeiras e contas a pagar/receber', 'JUNIOR', dep_fin, 2200, 3800, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Contador' AND departamento_id=dep_fin) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Contador', 'Contabilidade geral e obrigações fiscais', 'SENIOR', dep_fin, 6000, 11000, true);
  END IF;

  -- Comercial
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Gerente Comercial' AND departamento_id=dep_com) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Gerente Comercial', 'Gestão da equipe de vendas e metas', 'GESTAO', dep_com, 9000, 15000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Executivo de Vendas' AND departamento_id=dep_com) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Executivo de Vendas', 'Prospecção e fechamento de negócios', 'PLENO', dep_com, 4000, 8000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Assistente Comercial' AND departamento_id=dep_com) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Assistente Comercial', 'Suporte administrativo ao time comercial', 'JUNIOR', dep_com, 2200, 3500, true);
  END IF;

  -- Marketing
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Coordenador de Marketing' AND departamento_id=dep_mkt) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Coordenador de Marketing', 'Coordenação de campanhas e estratégia', 'GESTAO', dep_mkt, 7000, 12000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Analista de Marketing' AND departamento_id=dep_mkt) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Analista de Marketing', 'Análise de campanhas e métricas', 'PLENO', dep_mkt, 3500, 6500, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Designer' AND departamento_id=dep_mkt) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Designer', 'Design gráfico e identidade visual', 'PLENO', dep_mkt, 3500, 6000, true);
  END IF;

  -- Jurídico
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Advogado Sênior' AND departamento_id=dep_jur) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Advogado Sênior', 'Assessoria jurídica estratégica e contencioso', 'SENIOR', dep_jur, 10000, 18000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Advogado Júnior' AND departamento_id=dep_jur) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Advogado Júnior', 'Apoio jurídico em contratos e consultoria', 'JUNIOR', dep_jur, 4500, 7500, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Paralegal' AND departamento_id=dep_jur) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Paralegal', 'Suporte documental e pesquisa jurídica', 'JUNIOR', dep_jur, 2500, 4500, true);
  END IF;

  -- Operações
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Gerente de Operações' AND departamento_id=dep_ops) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Gerente de Operações', 'Gestão logística e operacional', 'GESTAO', dep_ops, 9000, 15000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Analista de Logística' AND departamento_id=dep_ops) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Analista de Logística', 'Controle de estoque e distribuição', 'PLENO', dep_ops, 3500, 6000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Auxiliar de Operações' AND departamento_id=dep_ops) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Auxiliar de Operações', 'Apoio nas rotinas operacionais', 'JUNIOR', dep_ops, 1800, 2800, true);
  END IF;

  -- Administrativo
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Coordenador Administrativo' AND departamento_id=dep_adm) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Coordenador Administrativo', 'Coordenação de facilities e serviços gerais', 'GESTAO', dep_adm, 5000, 9000, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Recepcionista' AND departamento_id=dep_adm) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Recepcionista', 'Atendimento e recepção', 'JUNIOR', dep_adm, 1800, 2800, true);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.cargos WHERE titulo='Auxiliar Administrativo' AND departamento_id=dep_adm) THEN
    INSERT INTO public.cargos (titulo, descricao, nivel, departamento_id, salario_minimo, salario_maximo, ativo)
    VALUES ('Auxiliar Administrativo', 'Apoio em rotinas administrativas', 'JUNIOR', dep_adm, 1800, 2500, true);
  END IF;
END $$;

-- =============================================
-- 3. NOVOS FUNCIONÁRIOS
-- =============================================
DO $$
DECLARE
  j_padrao bigint;
  dep_rh bigint; dep_dp bigint; dep_ti bigint; dep_fin bigint;
  dep_com bigint; dep_mkt bigint; dep_jur bigint; dep_ops bigint; dep_adm bigint;
  -- cargos
  c_analista_rh bigint; c_recrutador bigint;
  c_analista_folha bigint;
  c_coord_ti bigint; c_dev_jr bigint; c_suporte bigint; c_dba bigint; c_dev bigint;
  c_ger_fin bigint; c_assist_fin bigint; c_contador bigint; c_analista_fin bigint;
  c_ger_com bigint; c_exec_vendas bigint; c_assist_com bigint;
  c_coord_mkt bigint; c_analista_mkt bigint; c_designer bigint;
  c_adv_sr bigint; c_adv_jr bigint; c_paralegal bigint;
  c_ger_ops bigint; c_analista_log bigint; c_aux_ops bigint;
  c_coord_adm bigint; c_recepcionista bigint; c_aux_adm bigint;
BEGIN
  SELECT id INTO j_padrao FROM public.ponto_jornadas WHERE nome='Padrão 8h' LIMIT 1;

  -- departamentos
  SELECT id INTO dep_rh  FROM public.departamentos WHERE nome='Recursos Humanos' LIMIT 1;
  SELECT id INTO dep_dp  FROM public.departamentos WHERE nome='Departamento Pessoal' LIMIT 1;
  SELECT id INTO dep_ti  FROM public.departamentos WHERE nome='Tecnologia' LIMIT 1;
  SELECT id INTO dep_fin FROM public.departamentos WHERE nome='Financeiro' LIMIT 1;
  SELECT id INTO dep_com FROM public.departamentos WHERE nome='Comercial' LIMIT 1;
  SELECT id INTO dep_mkt FROM public.departamentos WHERE nome='Marketing' LIMIT 1;
  SELECT id INTO dep_jur FROM public.departamentos WHERE nome='Jurídico' LIMIT 1;
  SELECT id INTO dep_ops FROM public.departamentos WHERE nome='Operações' LIMIT 1;
  SELECT id INTO dep_adm FROM public.departamentos WHERE nome='Administrativo' LIMIT 1;

  -- cargos
  SELECT id INTO c_analista_rh FROM public.cargos WHERE titulo='Analista de RH' AND departamento_id=dep_rh LIMIT 1;
  SELECT id INTO c_recrutador FROM public.cargos WHERE titulo='Recrutador' AND departamento_id=dep_rh LIMIT 1;
  SELECT id INTO c_analista_folha FROM public.cargos WHERE titulo='Analista de Folha' AND departamento_id=dep_dp LIMIT 1;
  SELECT id INTO c_coord_ti FROM public.cargos WHERE titulo='Coordenador de TI' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_dev FROM public.cargos WHERE titulo='Desenvolvedor' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_dev_jr FROM public.cargos WHERE titulo='Desenvolvedor Júnior' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_suporte FROM public.cargos WHERE titulo='Analista de Suporte' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_dba FROM public.cargos WHERE titulo='DBA' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_ger_fin FROM public.cargos WHERE titulo='Gerente Financeiro' AND departamento_id=dep_fin LIMIT 1;
  SELECT id INTO c_analista_fin FROM public.cargos WHERE titulo='Analista Financeiro' AND departamento_id=dep_fin LIMIT 1;
  SELECT id INTO c_assist_fin FROM public.cargos WHERE titulo='Assistente Financeiro' AND departamento_id=dep_fin LIMIT 1;
  SELECT id INTO c_contador FROM public.cargos WHERE titulo='Contador' AND departamento_id=dep_fin LIMIT 1;
  SELECT id INTO c_ger_com FROM public.cargos WHERE titulo='Gerente Comercial' AND departamento_id=dep_com LIMIT 1;
  SELECT id INTO c_exec_vendas FROM public.cargos WHERE titulo='Executivo de Vendas' AND departamento_id=dep_com LIMIT 1;
  SELECT id INTO c_assist_com FROM public.cargos WHERE titulo='Assistente Comercial' AND departamento_id=dep_com LIMIT 1;
  SELECT id INTO c_coord_mkt FROM public.cargos WHERE titulo='Coordenador de Marketing' AND departamento_id=dep_mkt LIMIT 1;
  SELECT id INTO c_analista_mkt FROM public.cargos WHERE titulo='Analista de Marketing' AND departamento_id=dep_mkt LIMIT 1;
  SELECT id INTO c_designer FROM public.cargos WHERE titulo='Designer' AND departamento_id=dep_mkt LIMIT 1;
  SELECT id INTO c_adv_sr FROM public.cargos WHERE titulo='Advogado Sênior' AND departamento_id=dep_jur LIMIT 1;
  SELECT id INTO c_adv_jr FROM public.cargos WHERE titulo='Advogado Júnior' AND departamento_id=dep_jur LIMIT 1;
  SELECT id INTO c_paralegal FROM public.cargos WHERE titulo='Paralegal' AND departamento_id=dep_jur LIMIT 1;
  SELECT id INTO c_ger_ops FROM public.cargos WHERE titulo='Gerente de Operações' AND departamento_id=dep_ops LIMIT 1;
  SELECT id INTO c_analista_log FROM public.cargos WHERE titulo='Analista de Logística' AND departamento_id=dep_ops LIMIT 1;
  SELECT id INTO c_aux_ops FROM public.cargos WHERE titulo='Auxiliar de Operações' AND departamento_id=dep_ops LIMIT 1;
  SELECT id INTO c_coord_adm FROM public.cargos WHERE titulo='Coordenador Administrativo' AND departamento_id=dep_adm LIMIT 1;
  SELECT id INTO c_recepcionista FROM public.cargos WHERE titulo='Recepcionista' AND departamento_id=dep_adm LIMIT 1;
  SELECT id INTO c_aux_adm FROM public.cargos WHERE titulo='Auxiliar Administrativo' AND departamento_id=dep_adm LIMIT 1;

  -- =============================================
  -- RH (já existem 2, adicionando mais 2)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-01') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1001', 'Mariana Costa Silva', '111.111.111-01', '1993-03-15', 'F', 'mariana.costa@empresa.com', '(11)99001-0001', 'São Paulo', 'SP', c_analista_rh, dep_rh, '2024-06-10', 'ATIVO', 'CLT', 5200.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-02') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1002', 'Rafael Santos Oliveira', '111.111.111-02', '1996-07-22', 'M', 'rafael.santos@empresa.com', '(11)99001-0002', 'São Paulo', 'SP', c_recrutador, dep_rh, '2025-01-15', 'ATIVO', 'CLT', 4500.00, j_padrao, 1);
  END IF;

  -- =============================================
  -- DP (já existem 2, adicionando mais 1)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-03') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1003', 'Camila Ribeiro Almeida', '111.111.111-03', '1994-11-08', 'F', 'camila.ribeiro@empresa.com', '(11)99001-0003', 'São Paulo', 'SP', c_analista_folha, dep_dp, '2024-03-01', 'ATIVO', 'CLT', 4800.00, j_padrao, 2);
  END IF;

  -- =============================================
  -- TI (já existe 1, adicionando mais 5)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-04') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1004', 'Lucas Pereira Souza', '111.111.111-04', '1991-03-05', 'M', 'lucas.pereira@empresa.com', '(11)99001-0004', 'São Paulo', 'SP', c_coord_ti, dep_ti, '2022-08-01', 'ATIVO', 'CLT', 13500.00, j_padrao, 1);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-05') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1005', 'Gabriela Lima Ferreira', '111.111.111-05', '1998-09-18', 'F', 'gabriela.lima@empresa.com', '(11)99001-0005', 'Campinas', 'SP', c_dev, dep_ti, '2024-01-10', 'ATIVO', 'CLT', 7500.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-06') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1006', 'Thiago Mendes Rocha', '111.111.111-06', '2000-03-28', 'M', 'thiago.mendes@empresa.com', '(11)99001-0006', 'São Paulo', 'SP', c_dev_jr, dep_ti, current_date - 15, 'ATIVO', 'CLT', 3800.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-07') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1007', 'Amanda Souza Castro', '111.111.111-07', '1997-06-12', 'F', 'amanda.souza@empresa.com', '(11)99001-0007', 'São Paulo', 'SP', c_suporte, dep_ti, '2025-06-01', 'ATIVO', 'CLT', 3200.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-08') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1008', 'Pedro Augusto Nunes', '111.111.111-08', '1989-12-01', 'M', 'pedro.nunes@empresa.com', '(11)99001-0008', 'São Paulo', 'SP', c_dba, dep_ti, '2023-05-15', 'ATIVO', 'CLT', 11000.00, j_padrao, 3);
  END IF;

  -- =============================================
  -- Financeiro (já existe 1, adicionando mais 3)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-09') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1009', 'Roberto Carlos Dias', '111.111.111-09', '1985-08-20', 'M', 'roberto.dias@empresa.com', '(11)99001-0009', 'São Paulo', 'SP', c_ger_fin, dep_fin, '2021-02-01', 'ATIVO', 'CLT', 14000.00, j_padrao, 2);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-10') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1010', 'Juliana Martins Gomes', '111.111.111-10', '1992-03-12', 'F', 'juliana.martins@empresa.com', '(11)99001-0010', 'São Paulo', 'SP', c_contador, dep_fin, '2023-09-01', 'ATIVO', 'CLT', 8500.00, j_padrao, 1);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-11') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1011', 'Beatriz Alves Nascimento', '111.111.111-11', '1999-01-25', 'F', 'beatriz.alves@empresa.com', '(11)99001-0011', 'Guarulhos', 'SP', c_assist_fin, dep_fin, current_date - 20, 'ATIVO', 'CLT', 2800.00, j_padrao, 0);
  END IF;

  -- =============================================
  -- Comercial (novo, 4 funcionários)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-12') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1012', 'Marcos Antônio Vieira', '111.111.111-12', '1987-04-18', 'M', 'marcos.vieira@empresa.com', '(11)99001-0012', 'São Paulo', 'SP', c_ger_com, dep_com, '2022-01-10', 'ATIVO', 'CLT', 12000.00, j_padrao, 2);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-13') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1013', 'Carolina Fernandes Lima', '111.111.111-13', '1995-03-07', 'F', 'carolina.fernandes@empresa.com', '(11)99001-0013', 'São Paulo', 'SP', c_exec_vendas, dep_com, '2024-04-01', 'ATIVO', 'CLT', 6000.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-14') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1014', 'Diego Ramos Barbosa', '111.111.111-14', '1993-10-30', 'M', 'diego.ramos@empresa.com', '(11)99001-0014', 'Osasco', 'SP', c_exec_vendas, dep_com, '2025-09-15', 'ATIVO', 'CLT', 5500.00, j_padrao, 1);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-15') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1015', 'Larissa Melo Santos', '111.111.111-15', '2001-05-14', 'F', 'larissa.melo@empresa.com', '(11)99001-0015', 'São Paulo', 'SP', c_assist_com, dep_com, current_date - 10, 'ATIVO', 'CLT', 2500.00, j_padrao, 0);
  END IF;

  -- =============================================
  -- Marketing (novo, 3 funcionários)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-16') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1016', 'Vanessa Lopes Cardoso', '111.111.111-16', '1990-12-19', 'F', 'vanessa.lopes@empresa.com', '(11)99001-0016', 'São Paulo', 'SP', c_coord_mkt, dep_mkt, '2023-03-01', 'ATIVO', 'CLT', 9500.00, j_padrao, 1);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-17') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1017', 'Felipe Azevedo Moura', '111.111.111-17', '1997-03-22', 'M', 'felipe.azevedo@empresa.com', '(11)99001-0017', 'São Paulo', 'SP', c_analista_mkt, dep_mkt, '2024-08-01', 'ATIVO', 'CLT', 4800.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-18') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1018', 'Isabela Correia Duarte', '111.111.111-18', '1999-08-05', 'F', 'isabela.correia@empresa.com', '(11)99001-0018', 'Barueri', 'SP', c_designer, dep_mkt, current_date - 25, 'ATIVO', 'CLT', 4200.00, j_padrao, 0);
  END IF;

  -- =============================================
  -- Jurídico (novo, 3 funcionários)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-19') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1019', 'Ricardo Teixeira Monteiro', '111.111.111-19', '1983-06-09', 'M', 'ricardo.teixeira@empresa.com', '(11)99001-0019', 'São Paulo', 'SP', c_adv_sr, dep_jur, '2020-11-01', 'ATIVO', 'CLT', 15000.00, j_padrao, 2);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-20') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1020', 'Natália Fonseca Pinto', '111.111.111-20', '1996-02-14', 'F', 'natalia.fonseca@empresa.com', '(11)99001-0020', 'São Paulo', 'SP', c_adv_jr, dep_jur, '2025-04-01', 'ATIVO', 'CLT', 5800.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-21') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1021', 'Gustavo Henrique Araújo', '111.111.111-21', '2000-03-30', 'M', 'gustavo.araujo@empresa.com', '(11)99001-0021', 'Santo André', 'SP', c_paralegal, dep_jur, '2025-10-01', 'ATIVO', 'CLT', 3200.00, j_padrao, 0);
  END IF;

  -- =============================================
  -- Operações (novo, 4 funcionários)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-22') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1022', 'Alexandre Costa Borges', '111.111.111-22', '1986-09-03', 'M', 'alexandre.costa@empresa.com', '(11)99001-0022', 'São Paulo', 'SP', c_ger_ops, dep_ops, '2021-07-01', 'ATIVO', 'CLT', 12500.00, j_padrao, 3);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-23') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1023', 'Patrícia Ramos Cunha', '111.111.111-23', '1994-03-11', 'F', 'patricia.ramos@empresa.com', '(11)99001-0023', 'Mauá', 'SP', c_analista_log, dep_ops, '2024-05-20', 'ATIVO', 'CLT', 4500.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-24') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1024', 'Vinícius Almeida Prado', '111.111.111-24', '1999-07-25', 'M', 'vinicius.almeida@empresa.com', '(11)99001-0024', 'São Paulo', 'SP', c_aux_ops, dep_ops, '2025-08-01', 'ATIVO', 'CLT', 2200.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-25') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1025', 'Renata Souza Freitas', '111.111.111-25', '2001-11-17', 'F', 'renata.freitas@empresa.com', '(11)99001-0025', 'Diadema', 'SP', c_aux_ops, dep_ops, current_date - 8, 'ATIVO', 'CLT', 2000.00, j_padrao, 0);
  END IF;

  -- =============================================
  -- Administrativo (novo, 3 funcionários)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-26') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1026', 'Sandra Maria Oliveira', '111.111.111-26', '1988-05-28', 'F', 'sandra.oliveira@empresa.com', '(11)99001-0026', 'São Paulo', 'SP', c_coord_adm, dep_adm, '2022-04-01', 'ATIVO', 'CLT', 7000.00, j_padrao, 2);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-27') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1027', 'Aline Barros Teixeira', '111.111.111-27', '2002-03-20', 'F', 'aline.barros@empresa.com', '(11)99001-0027', 'São Paulo', 'SP', c_recepcionista, dep_adm, '2025-07-01', 'ATIVO', 'CLT', 2200.00, j_padrao, 0);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-28') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1028', 'José Ricardo Moreira', '111.111.111-28', '1995-09-15', 'M', 'jose.moreira@empresa.com', '(11)99001-0028', 'Cotia', 'SP', c_aux_adm, dep_adm, current_date - 5, 'ATIVO', 'CLT', 2000.00, j_padrao, 0);
  END IF;

  -- =============================================
  -- 1 funcionário DESLIGADO (para stats)
  -- =============================================
  IF NOT EXISTS (SELECT 1 FROM public.funcionarios WHERE cpf='111.111.111-29') THEN
    INSERT INTO public.funcionarios (matricula, nome_completo, cpf, data_nascimento, genero, email_corporativo, telefone, cidade, estado, cargo_id, departamento_id, data_admissao, data_desligamento, status, tipo_contrato, salario_atual, jornada_id, dependentes_qtd)
    VALUES ('1029', 'Antônio Pereira da Silva', '111.111.111-29', '1990-04-10', 'M', 'antonio.pereira@empresa.com', '(11)99001-0029', 'São Paulo', 'SP', c_exec_vendas, dep_com, '2023-03-01', current_date - 5, 'DESLIGADO', 'CLT', 5000.00, j_padrao, 0);
  END IF;

END $$;

-- =============================================
-- 4. SOLICITAÇÕES DE PROMOÇÃO PENDENTES (para dashboard)
-- =============================================
DO $$
DECLARE
  f_gabriela bigint;
  f_diego bigint;
  u_rh_chefe bigint;
  c_dev bigint;
  c_dev_jr bigint;
  c_ger_com bigint;
  c_exec_vendas bigint;
  dep_ti bigint;
  dep_com bigint;
BEGIN
  SELECT id INTO f_gabriela FROM public.funcionarios WHERE cpf='111.111.111-05' LIMIT 1;
  SELECT id INTO f_diego FROM public.funcionarios WHERE cpf='111.111.111-14' LIMIT 1;
  SELECT id INTO u_rh_chefe FROM public.usuarios WHERE email='rh.chefe@local' LIMIT 1;
  SELECT id INTO dep_ti FROM public.departamentos WHERE nome='Tecnologia' LIMIT 1;
  SELECT id INTO dep_com FROM public.departamentos WHERE nome='Comercial' LIMIT 1;
  SELECT id INTO c_dev FROM public.cargos WHERE titulo='Desenvolvedor' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_dev_jr FROM public.cargos WHERE titulo='Desenvolvedor Júnior' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_ger_com FROM public.cargos WHERE titulo='Gerente Comercial' AND departamento_id=dep_com LIMIT 1;
  SELECT id INTO c_exec_vendas FROM public.cargos WHERE titulo='Executivo de Vendas' AND departamento_id=dep_com LIMIT 1;

  -- Promoção pendente: Gabriela (Dev Pleno -> Sênior? Reajuste)
  IF f_gabriela IS NOT NULL AND u_rh_chefe IS NOT NULL
     AND NOT EXISTS (SELECT 1 FROM public.promocoes WHERE funcionario_id=f_gabriela AND status='PENDENTE') THEN
    INSERT INTO public.promocoes (funcionario_id, cargo_atual_id, cargo_novo_id, departamento_atual_id, departamento_novo_id, salario_atual, salario_novo, motivo, tipo, solicitante_id, status)
    VALUES (f_gabriela, c_dev, c_dev, dep_ti, dep_ti, 7500.00, 9000.00, 'Excelente desempenho nos últimos 6 meses. Entregas acima da média.', 'REAJUSTE', u_rh_chefe, 'PENDENTE');
  END IF;

  -- Promoção pendente: Diego (Exec Vendas -> quer virar Gerente)
  IF f_diego IS NOT NULL AND u_rh_chefe IS NOT NULL
     AND NOT EXISTS (SELECT 1 FROM public.promocoes WHERE funcionario_id=f_diego AND status='PENDENTE') THEN
    INSERT INTO public.promocoes (funcionario_id, cargo_atual_id, cargo_novo_id, departamento_atual_id, departamento_novo_id, salario_atual, salario_novo, motivo, tipo, solicitante_id, status)
    VALUES (f_diego, c_exec_vendas, c_ger_com, dep_com, dep_com, 5500.00, 10000.00, 'Destaque em metas de vendas. Perfil de liderança reconhecido pela equipe.', 'PROMOCAO', u_rh_chefe, 'PENDENTE');
  END IF;
END $$;

-- =============================================
-- 5. VAGAS ABERTAS (para dashboard)
-- =============================================
DO $$
DECLARE
  dep_ti bigint; dep_com bigint; dep_mkt bigint;
  c_dev_jr bigint; c_exec_vendas bigint; c_analista_mkt bigint;
  u_rh_chefe bigint;
BEGIN
  SELECT id INTO dep_ti  FROM public.departamentos WHERE nome='Tecnologia' LIMIT 1;
  SELECT id INTO dep_com FROM public.departamentos WHERE nome='Comercial' LIMIT 1;
  SELECT id INTO dep_mkt FROM public.departamentos WHERE nome='Marketing' LIMIT 1;
  SELECT id INTO c_dev_jr FROM public.cargos WHERE titulo='Desenvolvedor Júnior' AND departamento_id=dep_ti LIMIT 1;
  SELECT id INTO c_exec_vendas FROM public.cargos WHERE titulo='Executivo de Vendas' AND departamento_id=dep_com LIMIT 1;
  SELECT id INTO c_analista_mkt FROM public.cargos WHERE titulo='Analista de Marketing' AND departamento_id=dep_mkt LIMIT 1;
  SELECT id INTO u_rh_chefe FROM public.usuarios WHERE email='rh.chefe@local' LIMIT 1;

  IF NOT EXISTS (SELECT 1 FROM public.vagas WHERE titulo='Desenvolvedor Júnior - Fullstack' AND status IN ('ABERTA','EM_ANDAMENTO')) THEN
    INSERT INTO public.vagas (titulo, descricao, departamento_id, cargo_id, quantidade, prioridade, salario_min, salario_max, tipo_contrato, local_trabalho, modelo_trabalho, requisitos, status, publicada_em, criado_por_id)
    VALUES ('Desenvolvedor Júnior - Fullstack', 'Vaga para desenvolvedor fullstack com conhecimento em Java e React', dep_ti, c_dev_jr, 2, 'ALTA', 3000, 5000, 'CLT', 'São Paulo - SP', 'HIBRIDO', 'Java, Spring Boot, React, SQL', 'ABERTA', now() - interval '5 days', u_rh_chefe);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.vagas WHERE titulo='Executivo de Vendas B2B' AND status IN ('ABERTA','EM_ANDAMENTO')) THEN
    INSERT INTO public.vagas (titulo, descricao, departamento_id, cargo_id, quantidade, prioridade, salario_min, salario_max, tipo_contrato, local_trabalho, modelo_trabalho, requisitos, status, publicada_em, criado_por_id)
    VALUES ('Executivo de Vendas B2B', 'Vaga para executivo de vendas com experiência em B2B e SaaS', dep_com, c_exec_vendas, 1, 'MEDIA', 4000, 8000, 'CLT', 'São Paulo - SP', 'PRESENCIAL', 'Vendas B2B, CRM, negociação', 'ABERTA', now() - interval '10 days', u_rh_chefe);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.vagas WHERE titulo='Analista de Marketing Digital' AND status IN ('ABERTA','EM_ANDAMENTO')) THEN
    INSERT INTO public.vagas (titulo, descricao, departamento_id, cargo_id, quantidade, prioridade, salario_min, salario_max, tipo_contrato, local_trabalho, modelo_trabalho, requisitos, status, publicada_em, criado_por_id)
    VALUES ('Analista de Marketing Digital', 'Vaga para analista focado em campanhas digitais e SEO', dep_mkt, c_analista_mkt, 1, 'MEDIA', 3500, 6500, 'CLT', 'São Paulo - SP', 'REMOTO', 'Marketing digital, Google Ads, SEO, Analytics', 'EM_ANDAMENTO', now() - interval '20 days', u_rh_chefe);
  END IF;
END $$;
