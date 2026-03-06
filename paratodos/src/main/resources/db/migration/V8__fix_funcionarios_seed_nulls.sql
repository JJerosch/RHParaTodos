-- V8 - Corrige funcionários do seed V7 que ficaram com cargo_id e departamento_id nulos
-- Os lookups de departamento/cargo no bloco de funcionários da V7 retornaram NULL
-- para os departamentos pré-existentes (RH, DP, TI, Financeiro).
SET client_encoding = 'UTF8';
SET search_path = public;

DO $$
DECLARE
  dep_rh  bigint;
  dep_dp  bigint;
  dep_ti  bigint;
  dep_fin bigint;

  c_analista_rh    bigint;
  c_recrutador     bigint;
  c_analista_folha bigint;
  c_coord_ti       bigint;
  c_dev            bigint;
  c_dev_jr         bigint;
  c_suporte        bigint;
  c_dba            bigint;
  c_ger_fin        bigint;
  c_contador       bigint;
  c_assist_fin     bigint;
BEGIN
  -- Busca departamentos
  SELECT id INTO dep_rh  FROM public.departamentos WHERE nome = 'Recursos Humanos' LIMIT 1;
  SELECT id INTO dep_dp  FROM public.departamentos WHERE nome = 'Departamento Pessoal' LIMIT 1;
  SELECT id INTO dep_ti  FROM public.departamentos WHERE nome = 'Tecnologia' LIMIT 1;
  SELECT id INTO dep_fin FROM public.departamentos WHERE nome = 'Financeiro' LIMIT 1;

  -- Busca cargos
  SELECT id INTO c_analista_rh    FROM public.cargos WHERE titulo = 'Analista de RH'       AND departamento_id = dep_rh  LIMIT 1;
  SELECT id INTO c_recrutador     FROM public.cargos WHERE titulo = 'Recrutador'            AND departamento_id = dep_rh  LIMIT 1;
  SELECT id INTO c_analista_folha FROM public.cargos WHERE titulo = 'Analista de Folha'     AND departamento_id = dep_dp  LIMIT 1;
  SELECT id INTO c_coord_ti       FROM public.cargos WHERE titulo = 'Coordenador de TI'     AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_dev            FROM public.cargos WHERE titulo = 'Desenvolvedor'         AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_dev_jr         FROM public.cargos WHERE titulo = 'Desenvolvedor Júnior'  AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_suporte        FROM public.cargos WHERE titulo = 'Analista de Suporte'   AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_dba            FROM public.cargos WHERE titulo = 'DBA'                   AND departamento_id = dep_ti  LIMIT 1;
  SELECT id INTO c_ger_fin        FROM public.cargos WHERE titulo = 'Gerente Financeiro'    AND departamento_id = dep_fin LIMIT 1;
  SELECT id INTO c_contador       FROM public.cargos WHERE titulo = 'Contador'              AND departamento_id = dep_fin LIMIT 1;
  SELECT id INTO c_assist_fin     FROM public.cargos WHERE titulo = 'Assistente Financeiro' AND departamento_id = dep_fin LIMIT 1;

  -- Se o cargo 'Desenvolvedor' não existe (c_dev IS NULL), usa 'Desenvolvedor Júnior'
  IF c_dev IS NULL THEN
    c_dev := c_dev_jr;
  END IF;

  -- =============================================
  -- Corrige funcionários com cargo/departamento NULL
  -- Usa CPF como chave estável (não depende de ID)
  -- =============================================

  -- RH
  UPDATE public.funcionarios SET cargo_id = c_analista_rh, departamento_id = dep_rh
  WHERE cpf = '111.111.111-01' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_recrutador, departamento_id = dep_rh
  WHERE cpf = '111.111.111-02' AND (cargo_id IS NULL OR departamento_id IS NULL);

  -- DP
  UPDATE public.funcionarios SET cargo_id = c_analista_folha, departamento_id = dep_dp
  WHERE cpf = '111.111.111-03' AND (cargo_id IS NULL OR departamento_id IS NULL);

  -- TI
  UPDATE public.funcionarios SET cargo_id = c_coord_ti, departamento_id = dep_ti
  WHERE cpf = '111.111.111-04' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_dev, departamento_id = dep_ti
  WHERE cpf = '111.111.111-05' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_dev_jr, departamento_id = dep_ti
  WHERE cpf = '111.111.111-06' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_suporte, departamento_id = dep_ti
  WHERE cpf = '111.111.111-07' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_dba, departamento_id = dep_ti
  WHERE cpf = '111.111.111-08' AND (cargo_id IS NULL OR departamento_id IS NULL);

  -- Financeiro
  UPDATE public.funcionarios SET cargo_id = c_ger_fin, departamento_id = dep_fin
  WHERE cpf = '111.111.111-09' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_contador, departamento_id = dep_fin
  WHERE cpf = '111.111.111-10' AND (cargo_id IS NULL OR departamento_id IS NULL);

  UPDATE public.funcionarios SET cargo_id = c_assist_fin, departamento_id = dep_fin
  WHERE cpf = '111.111.111-11' AND (cargo_id IS NULL OR departamento_id IS NULL);

  -- =============================================
  -- Corrige promoções que referenciam c_dev NULL
  -- =============================================
  IF c_dev IS NOT NULL THEN
    UPDATE public.promocoes SET cargo_atual_id = c_dev, cargo_novo_id = c_dev
    WHERE funcionario_id = (SELECT id FROM public.funcionarios WHERE cpf = '111.111.111-05' LIMIT 1)
      AND cargo_atual_id IS NULL;
  END IF;

END $$;
