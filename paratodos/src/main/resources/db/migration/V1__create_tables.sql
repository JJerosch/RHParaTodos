--
-- PostgreSQL database dump
--

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: base_fgts_folha(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.base_fgts_folha(p_folha_id bigint) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_base numeric;
BEGIN
  SELECT COALESCE(SUM(fl.valor), 0)
    INTO v_base
  FROM public.folha_lancamentos fl
  LEFT JOIN public.folha_eventos_catalogo c_id ON c_id.id = fl.evento_id
  LEFT JOIN public.folha_eventos_catalogo c_cd ON c_cd.codigo = fl.codigo
  WHERE fl.folha_id = p_folha_id
    AND COALESCE(c_id.natureza, c_cd.natureza) = 'PROVENTO'
    AND COALESCE(c_id.incide_fgts, c_cd.incide_fgts, false) = true;

  RETURN COALESCE(v_base, 0);
END;
$$;


--
-- Name: base_inss_folha(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.base_inss_folha(p_folha_id bigint) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_base numeric;
BEGIN
  SELECT COALESCE(SUM(fl.valor), 0)
  INTO v_base
  FROM public.folha_lancamentos fl
  LEFT JOIN public.folha_eventos_catalogo c_id ON c_id.id = fl.evento_id
  LEFT JOIN public.folha_eventos_catalogo c_cd ON c_cd.codigo = fl.codigo
  WHERE fl.folha_id = p_folha_id
    AND COALESCE(c_id.natureza, c_cd.natureza) = 'PROVENTO'
    AND COALESCE(c_id.incide_inss, c_cd.incide_inss, false) = true;

  RETURN COALESCE(v_base, 0);
END;
$$;


--
-- Name: base_irrf_folha(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.base_irrf_folha(p_folha_id bigint) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_base numeric;
BEGIN
  SELECT COALESCE(SUM(fl.valor), 0)
  INTO v_base
  FROM public.folha_lancamentos fl
  LEFT JOIN public.folha_eventos_catalogo c_id ON c_id.id = fl.evento_id
  LEFT JOIN public.folha_eventos_catalogo c_cd ON c_cd.codigo = fl.codigo
  WHERE fl.folha_id = p_folha_id
    AND COALESCE(c_id.natureza, c_cd.natureza) = 'PROVENTO'
    AND COALESCE(c_id.incide_irrf, c_cd.incide_irrf, false) = true;

  RETURN COALESCE(v_base, 0);
END;
$$;


--
-- Name: calcular_inss_progressivo(numeric, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.calcular_inss_progressivo(p_base numeric, p_ano integer) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
  r record;
  v_total numeric := 0;
  v_parcela numeric;
  v_lim_sup numeric;
BEGIN
  IF p_base IS NULL OR p_base <= 0 THEN
    RETURN 0;
  END IF;

  FOR r IN
    SELECT faixa_inicio, faixa_fim, aliquota
    FROM public.inss_faixas
    WHERE ano = p_ano
    ORDER BY faixa_inicio
  LOOP
    EXIT WHEN p_base <= r.faixa_inicio;

    v_lim_sup := COALESCE(r.faixa_fim, p_base);
    v_parcela := LEAST(p_base, v_lim_sup) - r.faixa_inicio;

    IF v_parcela > 0 THEN
      v_total := v_total + (v_parcela * r.aliquota);
    END IF;

    EXIT WHEN r.faixa_fim IS NULL OR p_base <= r.faixa_fim;
  END LOOP;

  RETURN ROUND(v_total, 2);
END;
$$;


--
-- Name: calcular_irrf_progressivo(numeric, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.calcular_irrf_progressivo(p_base numeric, p_data date) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
  r RECORD;
  v_irrf numeric := 0;
BEGIN
  SELECT *
  INTO r
  FROM public.irrf_tabela
  WHERE p_base >= faixa_min
    AND (faixa_max IS NULL OR p_base <= faixa_max)
    AND vigente_desde <= p_data
  ORDER BY vigente_desde DESC
  LIMIT 1;

  IF FOUND THEN
    v_irrf := (p_base * r.aliquota) - r.parcela_deduzir;
  END IF;

  RETURN GREATEST(ROUND(v_irrf, 2), 0);
END $$;


--
-- Name: fechar_folha(bigint, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fechar_folha(p_folha_id bigint, p_usuario_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_status text;
BEGIN
  SELECT status INTO v_status
  FROM public.folha_pagamentos
  WHERE id = p_folha_id;

  IF v_status IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  IF v_status = 'FECHADA' THEN
    RETURN;
  END IF;

  -- garante tudo atualizado antes de fechar
  PERFORM public.recalcular_folha(p_folha_id);

  UPDATE public.folha_pagamentos
  SET status = 'FECHADA',
      fechado_em = now(),
      fechado_por = p_usuario_id
  WHERE id = p_folha_id;
END;
$$;


--
-- Name: fechar_folha_msg(bigint, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fechar_folha_msg(p_folha_id bigint, p_usuario_id bigint) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_status text;
BEGIN
  SELECT status INTO v_status
  FROM public.folha_pagamentos
  WHERE id = p_folha_id;

  IF v_status IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  IF v_status = 'FECHADA' THEN
    RETURN 'JA_FECHADA';
  END IF;

  PERFORM public.recalcular_folha(p_folha_id);

  UPDATE public.folha_pagamentos
  SET status = 'FECHADA',
      fechado_em = now(),
      fechado_por = p_usuario_id
  WHERE id = p_folha_id;

  RETURN 'FECHADA_OK';
END;
$$;


--
-- Name: fechar_folha_strict(bigint, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fechar_folha_strict(p_folha_id bigint, p_usuario_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_status text;
BEGIN
  SELECT status INTO v_status
  FROM public.folha_pagamentos
  WHERE id = p_folha_id
  FOR UPDATE;

  IF v_status IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  IF v_status = 'FECHADA' THEN
    RAISE EXCEPTION 'Folha % jÃ¡ estÃ¡ FECHADA.', p_folha_id;
  END IF;

  PERFORM public.recalcular_folha(p_folha_id);

  UPDATE public.folha_pagamentos
  SET status    = 'FECHADA',
      fechado_em = now(),
      fechado_por = p_usuario_id
  WHERE id = p_folha_id;
END;
$$;


--
-- Name: fn_auditoria_lgpd(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_auditoria_lgpd() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO auditoria_lgpd (
        tabela_afetada,
        registro_id,
        operacao,
        usuario_id,
        perfil_usuario,
        dados_antes,
        dados_depois,
        ip_origem,
        criado_em
    )
    VALUES (
        TG_TABLE_NAME,
        COALESCE(NEW.id, OLD.id),
        TG_OP,
        current_setting('app.usuario_id', true)::bigint,
        current_setting('app.perfil_usuario', true),
        CASE WHEN TG_OP IN ('UPDATE','DELETE') THEN to_jsonb(OLD) END,
        CASE WHEN TG_OP IN ('INSERT','UPDATE') THEN to_jsonb(NEW) END,
        inet_client_addr(),
        now()
    );

    RETURN NEW;
END;
$$;


--
-- Name: gerar_eventos_ponto_folha(bigint, bigint, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.gerar_eventos_ponto_folha(p_folha_id bigint, p_funcionario_id bigint, p_competencia date) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_horas_extras numeric := 0;
    v_horas_faltas numeric := 0;
    v_valor_hora numeric;
    v_evt_hextra bigint;
    v_evt_falta bigint;
BEGIN
    SELECT salario_atual / 220
    INTO v_valor_hora
    FROM funcionarios
    WHERE id = p_funcionario_id;

    SELECT id INTO v_evt_hextra FROM public.folha_eventos_catalogo WHERE codigo='HEXTRA';
    SELECT id INTO v_evt_falta FROM public.folha_eventos_catalogo WHERE codigo='FALTA';

    SELECT
        COALESCE(SUM(horas_extras), 0),
        COALESCE(SUM(horas_faltantes), 0)
    INTO
        v_horas_extras,
        v_horas_faltas
    FROM ponto_apuracao_diaria
    WHERE funcionario_id = p_funcionario_id
      AND date_trunc('month', data) = date_trunc('month', p_competencia);

    IF v_horas_extras > 0 THEN
        INSERT INTO folha_lancamentos (
            folha_id, tipo, codigo, descricao, referencia, valor, origem, evento_id
        )
        VALUES (
            p_folha_id,
            'PROVENTO',
            'HEXTRA',
            'Horas Extras',
            v_horas_extras,
            ROUND(v_horas_extras * v_valor_hora * 1.5, 2),
            'PONTO',
            v_evt_hextra
        );
    END IF;

    IF v_horas_faltas > 0 THEN
        INSERT INTO folha_lancamentos (
            folha_id, tipo, codigo, descricao, referencia, valor, origem, evento_id
        )
        VALUES (
            p_folha_id,
            'DESCONTO',
            'FALTA',
            'Desconto por Faltas',
            v_horas_faltas,
            ROUND(v_horas_faltas * v_valor_hora, 2),
            'PONTO',
            v_evt_falta
        );
    END IF;
END;
$$;


--
-- Name: gerar_fgts(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.gerar_fgts(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_base numeric;
  v_valor numeric;
  v_evento_id bigint;
BEGIN
  v_base := public.base_fgts_folha(p_folha_id);
  v_valor := ROUND(v_base * 0.08, 2);

  SELECT id INTO v_evento_id
  FROM public.folha_eventos_catalogo
  WHERE codigo = 'FGTS';

  DELETE FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id
    AND codigo = 'FGTS';

  IF v_valor > 0 THEN
    INSERT INTO public.folha_lancamentos
      (folha_id, tipo, codigo, descricao, referencia, valor, origem, evento_id)
    VALUES
      (p_folha_id, 'INFORMATIVO', 'FGTS', 'FGTS (8%) - Encargo Empresa', v_base, v_valor, 'CALCULO_AUTOMATICO', v_evento_id);
  END IF;
END;
$$;


--
-- Name: gerar_lancamento_inss(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.gerar_lancamento_inss(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_competencia date;
  v_ano int;
  v_ano_usado int;
  v_base numeric;
  v_valor numeric;
  v_evento_id bigint;
BEGIN
  SELECT competencia INTO v_competencia
  FROM public.folha_pagamentos
  WHERE id = p_folha_id;

  IF v_competencia IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  v_ano := EXTRACT(YEAR FROM v_competencia)::int;
  v_base := public.base_inss_folha(p_folha_id);

  -- se nÃ£o tiver faixa para o ano da competÃªncia, usa o maior ano existente (ex.: 2026)
  IF NOT EXISTS (SELECT 1 FROM public.inss_faixas WHERE ano = v_ano) THEN
    SELECT MAX(ano) INTO v_ano_usado FROM public.inss_faixas;

    IF v_ano_usado IS NULL THEN
      RAISE EXCEPTION 'Tabela INSS (inss_faixas) estÃ¡ vazia.';
    END IF;
  ELSE
    v_ano_usado := v_ano;
  END IF;

  v_valor := public.calcular_inss_progressivo(v_base, v_ano_usado);

  SELECT id INTO v_evento_id
  FROM public.folha_eventos_catalogo
  WHERE codigo = 'INSS';

  DELETE FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id
    AND codigo = 'INSS';

  IF v_valor > 0 THEN
    INSERT INTO public.folha_lancamentos
      (folha_id, tipo, codigo, descricao, referencia, valor, origem, evento_id)
    VALUES
      (p_folha_id, 'DESCONTO', 'INSS', 'Desconto INSS', v_base, v_valor, 'CALCULO_AUTOMATICO', v_evento_id);
  END IF;
END;
$$;


--
-- Name: gerar_lancamento_irrf(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.gerar_lancamento_irrf(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_competencia date;
  v_ano_comp int;

  v_ref_vigencia date;      -- data usada pra tabela IRRF (ex.: 2026-01-01)
  v_ano_param int;          -- ano usado no irrf_parametros

  v_base_bruta numeric;
  v_inss numeric := 0;

  v_dependentes int := 0;
  v_valor_dep numeric := 0;
  v_deducao_dep numeric := 0;

  v_base_liquida numeric := 0;
  v_valor_irrf numeric := 0;

  v_evento_id bigint;
BEGIN
  SELECT fp.competencia, COALESCE(f.dependentes_qtd,0)
    INTO v_competencia, v_dependentes
  FROM public.folha_pagamentos fp
  JOIN public.funcionarios f ON f.id = fp.funcionario_id
  WHERE fp.id = p_folha_id;

  IF v_competencia IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  v_ano_comp := EXTRACT(YEAR FROM v_competencia)::int;

  -- 1) escolher ano de parÃ¢metros (fallback -> MAX)
  IF EXISTS (SELECT 1 FROM public.irrf_parametros WHERE ano = v_ano_comp) THEN
    v_ano_param := v_ano_comp;
  ELSE
    SELECT MAX(ano) INTO v_ano_param FROM public.irrf_parametros;
  END IF;

  IF v_ano_param IS NULL THEN
    RAISE EXCEPTION 'Tabela IRRF (irrf_parametros) estÃ¡ vazia.';
  END IF;

  -- 2) escolher vigÃªncia da tabela (fallback -> MAX)
  SELECT MAX(vigente_desde)
    INTO v_ref_vigencia
  FROM public.irrf_tabela
  WHERE vigente_desde IS NOT NULL
    AND vigente_desde <= v_competencia;

  IF v_ref_vigencia IS NULL THEN
    SELECT MAX(vigente_desde)
      INTO v_ref_vigencia
    FROM public.irrf_tabela
    WHERE vigente_desde IS NOT NULL;
  END IF;

  IF v_ref_vigencia IS NULL THEN
    RAISE EXCEPTION 'Tabela IRRF (irrf_tabela) sem vigente_desde (vazia ou NULL).';
  END IF;

  -- bases
  v_base_bruta := public.base_irrf_folha(p_folha_id);

  SELECT COALESCE(valor,0) INTO v_inss
  FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id AND codigo = 'INSS'
  ORDER BY id DESC
  LIMIT 1;

  SELECT COALESCE(valor_dependente,0) INTO v_valor_dep
  FROM public.irrf_parametros
  WHERE ano = v_ano_param;

  v_deducao_dep := COALESCE(v_dependentes,0) * COALESCE(v_valor_dep,0);
  v_base_liquida := GREATEST(v_base_bruta - v_inss - v_deducao_dep, 0);

  -- chama usando a vigÃªncia escolhida (ex.: 2026-01-01)
  v_valor_irrf := public.calcular_irrf_progressivo(v_base_liquida, v_ref_vigencia);

  SELECT id INTO v_evento_id
  FROM public.folha_eventos_catalogo
  WHERE codigo = 'IRRF';

  DELETE FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id
    AND codigo = 'IRRF';

  IF v_valor_irrf > 0 THEN
    INSERT INTO public.folha_lancamentos
      (folha_id, tipo, codigo, descricao, referencia, valor, origem, evento_id)
    VALUES
      (p_folha_id, 'DESCONTO', 'IRRF', 'IRRF', v_base_liquida, ROUND(v_valor_irrf,2), 'CALCULO_AUTOMATICO', v_evento_id);
  END IF;
END;
$$;


--
-- Name: gerar_lancamentos_beneficios(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.gerar_lancamentos_beneficios(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_funcionario_id bigint;
    v_competencia date;
BEGIN
    SELECT funcionario_id, competencia
    INTO v_funcionario_id, v_competencia
    FROM folha_pagamentos
    WHERE id = p_folha_id;

    DELETE FROM folha_lancamentos
    WHERE folha_id = p_folha_id
      AND origem = 'BENEFICIO';

    INSERT INTO folha_lancamentos (
        folha_id, tipo, codigo, descricao, referencia, valor, origem, evento_id
    )
    SELECT
        p_folha_id,
        tb.natureza,
        'BEN_' || tb.id,
        tb.nome,
        NULL,
        COALESCE(fb.valor, tb.valor_padrao),
        'BENEFICIO',
        c.id
    FROM funcionarios_beneficios fb
    JOIN tipos_beneficios tb ON tb.id = fb.tipo_beneficio_id
    LEFT JOIN folha_eventos_catalogo c ON c.codigo = ('BEN_' || tb.id)
    WHERE fb.funcionario_id = v_funcionario_id
      AND fb.ativo = true
      AND fb.data_inicio <= (date_trunc('month', v_competencia) + interval '1 month - 1 day')
      AND (fb.data_fim IS NULL OR fb.data_fim >= date_trunc('month', v_competencia));
END;
$$;


--
-- Name: gerar_lancamentos_ferias(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.gerar_lancamentos_ferias() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_folha_id bigint;
  v_competencia date;

  v_salario numeric(10,2);
  v_valor_dia numeric(10,2);
  v_total_ferias numeric(10,2);
  v_um_terco numeric(10,2);
BEGIN
  IF TG_OP <> 'INSERT' THEN
    RETURN NEW;
  END IF;

  v_competencia := date_trunc('month', NEW.data_inicio)::date;

  SELECT id INTO v_folha_id
  FROM public.folha_pagamentos
  WHERE funcionario_id = NEW.funcionario_id
    AND date_trunc('month', competencia)::date = v_competencia
    AND status = 'ABERTA'
  LIMIT 1;

  IF v_folha_id IS NULL THEN
    RAISE EXCEPTION 'NÃ£o existe folha ABERTA para o funcionÃ¡rio % na competÃªncia %',
      NEW.funcionario_id, v_competencia;
  END IF;

  SELECT salario_atual INTO v_salario
  FROM public.funcionarios
  WHERE id = NEW.funcionario_id;

  v_valor_dia := ROUND(v_salario / 30.0, 2);
  v_total_ferias := ROUND(v_valor_dia * NEW.dias_gozados, 2);
  v_um_terco := ROUND(v_total_ferias / 3.0, 2);

  INSERT INTO public.folha_lancamentos (folha_id, tipo, codigo, descricao, valor, origem)
  VALUES (v_folha_id, 'PROVENTO', 'FERIAS', 'FÃ©rias gozadas', v_total_ferias, 'FERIAS');

  INSERT INTO public.folha_lancamentos (folha_id, tipo, codigo, descricao, valor, origem)
  VALUES (v_folha_id, 'PROVENTO', 'FERIAS_1_3', '1/3 Constitucional de FÃ©rias', v_um_terco, 'FERIAS');

  -- caminho B: NÃƒO recalcula impostos automaticamente aqui
  RETURN NEW;
END;
$$;


--
-- Name: processar_beneficios_e_recalcular(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.processar_beneficios_e_recalcular(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- 1. Gera lanÃ§amentos de benefÃ­cios
    PERFORM gerar_lancamentos_beneficios(p_folha_id);

    -- 2. Recalcula totais da folha
    PERFORM recalcular_totais_folha(p_folha_id);
END;
$$;


--
-- Name: processar_folha(bigint, bigint, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.processar_folha(p_folha_id bigint, p_funcionario_id bigint, p_usuario_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_competencia date;
  v_evt_salario bigint;
BEGIN
  DELETE FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id
    AND origem IN ('CONTRATO','BENEFICIO','PONTO','FERIAS','SISTEMA','CALCULO_AUTOMATICO');

  SELECT id INTO v_evt_salario
  FROM public.folha_eventos_catalogo
  WHERE codigo = 'SALARIO';

  INSERT INTO public.folha_lancamentos (folha_id, tipo, codigo, descricao, valor, origem, evento_id)
  SELECT p_folha_id, 'PROVENTO', 'SALARIO', 'SalÃ¡rio Base', salario_atual, 'CONTRATO', v_evt_salario
  FROM public.funcionarios
  WHERE id = p_funcionario_id;

  PERFORM public.gerar_lancamentos_beneficios(p_folha_id);

  SELECT competencia INTO v_competencia
  FROM public.folha_pagamentos
  WHERE id = p_folha_id;

  PERFORM public.gerar_eventos_ponto_folha(p_folha_id, p_funcionario_id, v_competencia);

  PERFORM public.recalcular_folha(p_folha_id);
END;
$$;


--
-- Name: reabrir_folha(bigint, bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.reabrir_folha(p_folha_id bigint, p_usuario_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_status text;
BEGIN
  SELECT status INTO v_status
  FROM public.folha_pagamentos
  WHERE id = p_folha_id;

  IF v_status IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  IF v_status <> 'FECHADA' THEN
    RETURN;
  END IF;

  UPDATE public.folha_pagamentos
  SET status = 'ABERTA',
      fechado_em = NULL,
      fechado_por = NULL
  WHERE id = p_folha_id;
END;
$$;


--
-- Name: recalcular_folha(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.recalcular_folha(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_status text;
BEGIN
  -- trava a linha da folha (evita concorrÃªncia simples)
  SELECT status INTO v_status
  FROM public.folha_pagamentos
  WHERE id = p_folha_id
  FOR UPDATE;

  IF v_status IS NULL THEN
    RAISE EXCEPTION 'Folha % nÃ£o encontrada.', p_folha_id;
  END IF;

  IF v_status = 'FECHADA' THEN
    RAISE EXCEPTION 'Folha % estÃ¡ FECHADA. Recalcular nÃ£o Ã© permitido.', p_folha_id;
  END IF;

  -- Ordem correta: IRRF depende do INSS
  PERFORM public.gerar_lancamento_inss(p_folha_id);
  PERFORM public.gerar_lancamento_irrf(p_folha_id);
  PERFORM public.gerar_fgts(p_folha_id);

  -- Atualiza totais (sem depender de trigger)
  UPDATE public.folha_pagamentos fp
  SET
    total_proventos = COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='PROVENTO'), 0),
    total_descontos = COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='DESCONTO'), 0),
    valor_liquido   = COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='PROVENTO'), 0)
                    - COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='DESCONTO'), 0)
  WHERE fp.id = p_folha_id;

END;
$$;


--
-- Name: recalcular_totais_folha(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.recalcular_totais_folha() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.folha_pagamentos fp
    SET
      total_proventos = COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='PROVENTO'), 0),
      total_descontos = COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='DESCONTO'), 0),
      valor_liquido   = COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='PROVENTO'), 0)
                      - COALESCE((SELECT SUM(valor) FROM public.folha_lancamentos WHERE folha_id = fp.id AND tipo='DESCONTO'), 0)
    WHERE fp.id = NEW.folha_id;

    RETURN NEW;
END;
$$;


--
-- Name: recalcular_totais_folha(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.recalcular_totais_folha(p_folha_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_proventos numeric;
  v_descontos numeric;
BEGIN
  SELECT COALESCE(SUM(valor),0) INTO v_proventos
  FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id AND tipo='PROVENTO';

  SELECT COALESCE(SUM(valor),0) INTO v_descontos
  FROM public.folha_lancamentos
  WHERE folha_id = p_folha_id AND tipo='DESCONTO';

  UPDATE public.folha_pagamentos
  SET total_proventos = v_proventos,
      total_descontos = v_descontos,
      valor_liquido   = v_proventos - v_descontos
  WHERE id = p_folha_id;
END;
$$;


--
-- Name: trg_bloquear_lancamento_folha_fechada(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_bloquear_lancamento_folha_fechada() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_status text;
BEGIN
  SELECT status INTO v_status
  FROM public.folha_pagamentos
  WHERE id = COALESCE(NEW.folha_id, OLD.folha_id);

  IF v_status = 'FECHADA' THEN
    RAISE EXCEPTION 'Folha % estÃ¡ FECHADA. NÃ£o Ã© permitido alterar lanÃ§amentos.', COALESCE(NEW.folha_id, OLD.folha_id);
  END IF;

  RETURN COALESCE(NEW, OLD);
END;
$$;


--
-- Name: trg_calcular_irrf(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_calcular_irrf() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM gerar_lancamento_irrf(NEW.id);
    RETURN NEW;
END;
$$;


--
-- Name: trg_folha_beneficios(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_folha_beneficios() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.status = 'ABERTA' THEN
        PERFORM gerar_lancamentos_beneficios(NEW.id);
    END IF;
    RETURN NEW;
END;
$$;


--
-- Name: trg_folha_calcular_inss(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_folha_calcular_inss() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM gerar_lancamento_inss(NEW.id);
    RETURN NEW;
END;
$$;


--
-- Name: trg_gerar_irrf(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_gerar_irrf() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM gerar_lancamento_irrf(NEW.folha_id);
    RETURN NEW;
END;
$$;


--
-- Name: trg_processar_folha_insert(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_processar_folha_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_usuario_id bigint;
BEGIN
  v_usuario_id := NULLIF(current_setting('app.usuario_id', true), '')::bigint;
  PERFORM public.processar_folha(NEW.id, NEW.funcionario_id, COALESCE(v_usuario_id, 0));
  RETURN NEW;
END;
$$;


--
-- Name: trg_recalcular_folha_lancamentos(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_recalcular_folha_lancamentos() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM recalcular_totais_folha(NEW.folha_id);
    RETURN NEW;
END;
$$;


--
-- Name: trg_set_evento_id_por_codigo(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_set_evento_id_por_codigo() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NEW.codigo IS NULL THEN
    RETURN NEW;
  END IF;

  IF TG_OP = 'INSERT'
     OR NEW.evento_id IS NULL
     OR (TG_OP = 'UPDATE' AND NEW.codigo IS DISTINCT FROM OLD.codigo)
  THEN
    SELECT id INTO NEW.evento_id
    FROM public.folha_eventos_catalogo
    WHERE codigo = NEW.codigo;
  END IF;

  RETURN NEW;
END $$;


--
-- Name: trg_sync_evento_beneficio(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.trg_sync_evento_beneficio() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  INSERT INTO public.folha_eventos_catalogo
  (codigo, descricao, natureza, tipo, incide_ferias, incide_decimo, incide_inss, incide_fgts, incide_irrf, ativo)
  VALUES
  (
    'BEN_' || NEW.id,
    NEW.nome,
    NEW.natureza,
    'SALARIAL',
    COALESCE(NEW.incide_ferias,false),
    COALESCE(NEW.incide_decimo,false),
    CASE WHEN NEW.natureza='PROVENTO' THEN true ELSE false END,
    CASE WHEN NEW.natureza='PROVENTO' THEN true ELSE false END,
    CASE WHEN NEW.natureza='PROVENTO' THEN true ELSE false END,
    COALESCE(NEW.ativo,true)
  )
  ON CONFLICT (codigo) DO UPDATE
  SET descricao      = EXCLUDED.descricao,
      natureza       = EXCLUDED.natureza,
      tipo           = EXCLUDED.tipo,
      incide_ferias  = EXCLUDED.incide_ferias,
      incide_decimo  = EXCLUDED.incide_decimo,
      incide_inss    = EXCLUDED.incide_inss,
      incide_fgts    = EXCLUDED.incide_fgts,
      incide_irrf    = EXCLUDED.incide_irrf,
      ativo          = EXCLUDED.ativo;

  RETURN NEW;
END $$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: afastamentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.afastamentos (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    tipo character varying(50) NOT NULL,
    data_inicio date NOT NULL,
    data_fim date,
    remunerado boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: afastamentos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.afastamentos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: afastamentos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.afastamentos_id_seq OWNED BY public.afastamentos.id;


--
-- Name: auditoria_lgpd; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auditoria_lgpd (
    id bigint NOT NULL,
    tabela_afetada character varying(50) NOT NULL,
    registro_id bigint NOT NULL,
    operacao character varying(10) NOT NULL,
    usuario_id bigint,
    perfil_usuario character varying(30),
    dados_antes jsonb,
    dados_depois jsonb,
    motivo text,
    ip_origem inet,
    user_agent text,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: auditoria_lgpd_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.auditoria_lgpd_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: auditoria_lgpd_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.auditoria_lgpd_id_seq OWNED BY public.auditoria_lgpd.id;


--
-- Name: auth_password_resets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auth_password_resets (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    token_hash text NOT NULL,
    criado_em timestamp without time zone DEFAULT now(),
    expira_em timestamp without time zone NOT NULL,
    usado_em timestamp without time zone,
    ip_origem inet,
    user_agent text
);


--
-- Name: auth_password_resets_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.auth_password_resets_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: auth_password_resets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.auth_password_resets_id_seq OWNED BY public.auth_password_resets.id;


--
-- Name: auth_refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auth_refresh_tokens (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    token_hash text NOT NULL,
    criado_em timestamp without time zone DEFAULT now(),
    expira_em timestamp without time zone NOT NULL,
    revogado_em timestamp without time zone,
    ip_origem inet,
    user_agent text
);


--
-- Name: auth_refresh_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.auth_refresh_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: auth_refresh_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.auth_refresh_tokens_id_seq OWNED BY public.auth_refresh_tokens.id;


--
-- Name: cargos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cargos (
    id bigint NOT NULL,
    titulo character varying(100) NOT NULL,
    descricao text,
    nivel character varying(50),
    departamento_id bigint,
    salario_minimo numeric(10,2),
    salario_maximo numeric(10,2),
    ativo boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: cargos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cargos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cargos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cargos_id_seq OWNED BY public.cargos.id;


--
-- Name: dados_emergencia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dados_emergencia (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    nome character varying(255) NOT NULL,
    parentesco character varying(50) NOT NULL,
    numero_emergencia character varying(20) NOT NULL,
    telefone_alternativo character varying(20),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    tipo_sanguineo character varying(10),
    cpf character varying(14),
    alergias text,
    condicoes_medicas text,
    endereco character varying(255)
);


--
-- Name: contatos_emergencia_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contatos_emergencia_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: contatos_emergencia_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.contatos_emergencia_id_seq OWNED BY public.dados_emergencia.id;


--
-- Name: dados_funcionarios_documentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dados_funcionarios_documentos (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    numero character varying(100),
    arquivo_url character varying(500),
    data_emissao date,
    data_validade date,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    tipo_documento_id bigint NOT NULL
);


--
-- Name: departamentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.departamentos (
    id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    departamento_pai_id bigint,
    ativo boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: departamentos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.departamentos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: departamentos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.departamentos_id_seq OWNED BY public.departamentos.id;


--
-- Name: documentos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.documentos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: documentos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.documentos_id_seq OWNED BY public.dados_funcionarios_documentos.id;


--
-- Name: folha_eventos_catalogo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.folha_eventos_catalogo (
    id bigint NOT NULL,
    codigo character varying(20) NOT NULL,
    descricao character varying(255) NOT NULL,
    natureza character varying(20) NOT NULL,
    tipo character varying(20) NOT NULL,
    incide_ferias boolean DEFAULT false,
    incide_decimo boolean DEFAULT false,
    incide_inss boolean DEFAULT false,
    incide_fgts boolean DEFAULT false,
    incide_irrf boolean DEFAULT false,
    ativo boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_folha_eventos_catalogo_natureza CHECK (((natureza)::text = ANY (ARRAY[('PROVENTO'::character varying)::text, ('DESCONTO'::character varying)::text, ('INFORMATIVO'::character varying)::text]))),
    CONSTRAINT chk_folha_eventos_catalogo_tipo CHECK (((tipo)::text = ANY (ARRAY[('SALARIAL'::character varying)::text, ('IMPOSTO'::character varying)::text, ('ENCARGO'::character varying)::text]))),
    CONSTRAINT chk_natureza CHECK (((natureza)::text = ANY (ARRAY[('PROVENTO'::character varying)::text, ('DESCONTO'::character varying)::text, ('INFORMATIVO'::character varying)::text]))),
    CONSTRAINT chk_tipo CHECK (((tipo)::text = ANY (ARRAY[('SALARIAL'::character varying)::text, ('NAO_SALARIAL'::character varying)::text, ('ENCARGO'::character varying)::text, ('IMPOSTO'::character varying)::text, ('BENEFICIO'::character varying)::text])))
);


--
-- Name: eventos_folha_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.eventos_folha_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: eventos_folha_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.eventos_folha_id_seq OWNED BY public.folha_eventos_catalogo.id;


--
-- Name: feriados; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.feriados (
    id bigint NOT NULL,
    data date NOT NULL,
    descricao character varying(100),
    nacional boolean DEFAULT false,
    estadual boolean DEFAULT false,
    municipal boolean DEFAULT false,
    estado character(2),
    cidade character varying(100)
);


--
-- Name: feriados_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.feriados_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: feriados_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.feriados_id_seq OWNED BY public.feriados.id;


--
-- Name: ferias_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ferias_logs (
    id bigint NOT NULL,
    funcionario_id bigint,
    usuario_id bigint,
    acao character varying(100),
    detalhe text,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ferias_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ferias_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ferias_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ferias_logs_id_seq OWNED BY public.ferias_logs.id;


--
-- Name: ferias_periodos_aquisitivos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ferias_periodos_aquisitivos (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    dias_direito integer DEFAULT 30,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ferias_periodos_aquisitivos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ferias_periodos_aquisitivos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ferias_periodos_aquisitivos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ferias_periodos_aquisitivos_id_seq OWNED BY public.ferias_periodos_aquisitivos.id;


--
-- Name: ferias_registros; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ferias_registros (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    solicitacao_id bigint NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    dias_gozados integer NOT NULL,
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ferias_registros_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ferias_registros_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ferias_registros_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ferias_registros_id_seq OWNED BY public.ferias_registros.id;


--
-- Name: ferias_solicitacoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ferias_solicitacoes (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    periodo_aquisitivo_id bigint NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    dias_solicitados integer NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying,
    solicitado_em timestamp without time zone DEFAULT now(),
    aprovado_por bigint,
    aprovado_em timestamp without time zone
);


--
-- Name: ferias_solicitacoes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ferias_solicitacoes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ferias_solicitacoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ferias_solicitacoes_id_seq OWNED BY public.ferias_solicitacoes.id;


--
-- Name: folha_lancamentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.folha_lancamentos (
    id bigint NOT NULL,
    folha_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    codigo character varying(20) NOT NULL,
    descricao character varying(255),
    referencia numeric(10,2),
    valor numeric(10,2) NOT NULL,
    origem character varying(30),
    criado_em timestamp without time zone DEFAULT now(),
    evento_id bigint NOT NULL,
    CONSTRAINT chk_folha_lancamentos_tipo CHECK (((tipo)::text = ANY (ARRAY[('PROVENTO'::character varying)::text, ('DESCONTO'::character varying)::text, ('INFORMATIVO'::character varying)::text])))
);


--
-- Name: folha_eventos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.folha_eventos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: folha_eventos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.folha_eventos_id_seq OWNED BY public.folha_lancamentos.id;


--
-- Name: folha_pagamentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.folha_pagamentos (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    competencia date NOT NULL,
    status character varying(20) DEFAULT 'ABERTA'::character varying,
    criado_em timestamp without time zone DEFAULT now(),
    fechado_em timestamp without time zone,
    fechado_por bigint,
    total_proventos numeric(12,2) DEFAULT 0,
    total_descontos numeric(12,2) DEFAULT 0,
    valor_liquido numeric(12,2) DEFAULT 0
);


--
-- Name: folha_pagamentos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.folha_pagamentos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: folha_pagamentos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.folha_pagamentos_id_seq OWNED BY public.folha_pagamentos.id;


--
-- Name: funcionarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.funcionarios (
    id bigint NOT NULL,
    matricula character varying(20) NOT NULL,
    nome_completo character varying(255) NOT NULL,
    cpf character varying(14) NOT NULL,
    rg character varying(20),
    data_nascimento date NOT NULL,
    genero character varying(20),
    estado_civil character varying(20),
    email_pessoal character varying(255),
    email_corporativo character varying(255),
    telefone character varying(20),
    celular character varying(20),
    cep character varying(9),
    logradouro character varying(255),
    numero character varying(10),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    estado character varying(2),
    banco character varying(100),
    agencia character varying(10),
    conta character varying(20),
    tipo_conta character varying(20),
    pix character varying(255),
    cargo_id bigint,
    departamento_id bigint,
    gestor_id bigint,
    data_admissao date NOT NULL,
    data_desligamento date,
    status character varying(20) DEFAULT 'ATIVO'::character varying,
    tipo_contrato character varying(30),
    salario_atual numeric(10,2),
    usuario_id bigint,
    foto_url character varying(500),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    jornada_id bigint,
    cargo_desde date,
    dependentes_qtd integer DEFAULT 0 NOT NULL
);


--
-- Name: funcionarios_beneficios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.funcionarios_beneficios (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    tipo_beneficio_id bigint NOT NULL,
    valor numeric(10,2),
    data_inicio date NOT NULL,
    data_fim date,
    ativo boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: funcionarios_beneficios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.funcionarios_beneficios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: funcionarios_beneficios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.funcionarios_beneficios_id_seq OWNED BY public.funcionarios_beneficios.id;


--
-- Name: funcionarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.funcionarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: funcionarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.funcionarios_id_seq OWNED BY public.funcionarios.id;


--
-- Name: historico_cargos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.historico_cargos (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    cargo_anterior_id bigint,
    cargo_novo_id bigint NOT NULL,
    departamento_anterior_id bigint,
    departamento_novo_id bigint,
    tipo_movimentacao character varying(50),
    motivo text,
    data_vigencia date NOT NULL,
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: historico_cargos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.historico_cargos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: historico_cargos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.historico_cargos_id_seq OWNED BY public.historico_cargos.id;


--
-- Name: inss_faixas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inss_faixas (
    id bigint NOT NULL,
    ano integer NOT NULL,
    faixa_inicio numeric(10,2) NOT NULL,
    faixa_fim numeric(10,2),
    aliquota numeric(5,4) NOT NULL,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: inss_faixas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inss_faixas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: inss_faixas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.inss_faixas_id_seq OWNED BY public.inss_faixas.id;


--
-- Name: irrf_parametros; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.irrf_parametros (
    ano integer NOT NULL,
    valor_dependente numeric(10,2) NOT NULL
);


--
-- Name: irrf_tabela; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.irrf_tabela (
    id bigint NOT NULL,
    faixa_min numeric(10,2),
    faixa_max numeric(10,2),
    aliquota numeric(5,4),
    parcela_deduzir numeric(10,2),
    vigente_desde date
);


--
-- Name: irrf_tabela_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.irrf_tabela_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: irrf_tabela_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.irrf_tabela_id_seq OWNED BY public.irrf_tabela.id;


--
-- Name: logs_auditoria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.logs_auditoria (
    id bigint NOT NULL,
    usuario_id bigint,
    acao character varying(50) NOT NULL,
    tabela character varying(100),
    registro_id bigint,
    dados_anteriores jsonb,
    dados_novos jsonb,
    ip_address character varying(45),
    user_agent text,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: logs_auditoria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.logs_auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: logs_auditoria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.logs_auditoria_id_seq OWNED BY public.logs_auditoria.id;


--
-- Name: permissoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permissoes (
    id bigint NOT NULL,
    chave character varying(80) NOT NULL,
    descricao text
);


--
-- Name: permissoes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.permissoes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: permissoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.permissoes_id_seq OWNED BY public.permissoes.id;


--
-- Name: ponto_apuracao_diaria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_apuracao_diaria (
    id bigint NOT NULL,
    funcionario_id bigint,
    data date NOT NULL,
    horas_trabalhadas numeric(5,2),
    horas_extras numeric(5,2),
    horas_faltantes numeric(5,2),
    fechado boolean DEFAULT false,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ponto_apuracao_diaria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_apuracao_diaria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_apuracao_diaria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_apuracao_diaria_id_seq OWNED BY public.ponto_apuracao_diaria.id;


--
-- Name: ponto_banco_horas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_banco_horas (
    id bigint NOT NULL,
    funcionario_id bigint,
    data date NOT NULL,
    tipo character varying(10) NOT NULL,
    horas numeric(5,2) NOT NULL,
    origem character varying(50),
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ponto_banco_horas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_banco_horas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_banco_horas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_banco_horas_id_seq OWNED BY public.ponto_banco_horas.id;


--
-- Name: ponto_fechamentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_fechamentos (
    id bigint NOT NULL,
    competencia date NOT NULL,
    funcionario_id bigint,
    fechado_por bigint,
    fechado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ponto_fechamentos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_fechamentos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_fechamentos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_fechamentos_id_seq OWNED BY public.ponto_fechamentos.id;


--
-- Name: ponto_jornadas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_jornadas (
    id bigint NOT NULL,
    nome character varying(50) NOT NULL,
    carga_horaria_diaria numeric(4,2) NOT NULL,
    intervalo_minutos integer NOT NULL,
    horario_entrada time without time zone,
    horario_saida time without time zone,
    flexivel boolean DEFAULT false,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ponto_jornadas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_jornadas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_jornadas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_jornadas_id_seq OWNED BY public.ponto_jornadas.id;


--
-- Name: ponto_justificativas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_justificativas (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data date NOT NULL,
    motivo character varying(50) NOT NULL,
    descricao text,
    documento_url character varying(500),
    status character varying(20) DEFAULT 'PENDENTE'::character varying,
    solicitado_em timestamp without time zone DEFAULT now(),
    aprovado_por bigint,
    aprovado_em timestamp without time zone
);


--
-- Name: ponto_justificativas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_justificativas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_justificativas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_justificativas_id_seq OWNED BY public.ponto_justificativas.id;


--
-- Name: ponto_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_logs (
    id bigint NOT NULL,
    usuario_id bigint,
    acao character varying(100),
    detalhe text,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ponto_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_logs_id_seq OWNED BY public.ponto_logs.id;


--
-- Name: ponto_marcacoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ponto_marcacoes (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    data_hora timestamp without time zone NOT NULL,
    origem character varying(20) NOT NULL,
    ip character varying(45),
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: ponto_marcacoes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ponto_marcacoes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ponto_marcacoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ponto_marcacoes_id_seq OWNED BY public.ponto_marcacoes.id;


--
-- Name: regras_inss; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.regras_inss AS
 SELECT id,
    faixa_inicio,
    faixa_fim,
    (round((aliquota * (100)::numeric), 2))::numeric(5,2) AS percentual
   FROM public.inss_faixas;


--
-- Name: regras_inss_atual; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.regras_inss_atual AS
 SELECT id,
    faixa_inicio,
    faixa_fim,
    (round((aliquota * (100)::numeric), 2))::numeric(5,2) AS percentual
   FROM public.inss_faixas
  WHERE (ano = (EXTRACT(year FROM CURRENT_DATE))::integer);


--
-- Name: regras_irrf; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.regras_irrf AS
 SELECT id,
    faixa_min AS faixa_inicio,
    faixa_max AS faixa_fim,
    (round((aliquota * (100)::numeric), 2))::numeric(5,2) AS percentual,
    parcela_deduzir AS deducao
   FROM public.irrf_tabela;


--
-- Name: regras_irrf_atual; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.regras_irrf_atual AS
 WITH vd AS (
         SELECT max(irrf_tabela_1.vigente_desde) AS vigente
           FROM public.irrf_tabela irrf_tabela_1
          WHERE (irrf_tabela_1.vigente_desde <= CURRENT_DATE)
        )
 SELECT id,
    faixa_min AS faixa_inicio,
    faixa_max AS faixa_fim,
    (round((aliquota * (100)::numeric), 2))::numeric(5,2) AS percentual,
    parcela_deduzir AS deducao
   FROM public.irrf_tabela
  WHERE (vigente_desde = ( SELECT vd.vigente
           FROM vd));


--
-- Name: rescisoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rescisoes (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    data_rescisao date NOT NULL,
    tipo character varying(50),
    motivo text,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: rescisoes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.rescisoes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rescisoes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.rescisoes_id_seq OWNED BY public.rescisoes.id;


--
-- Name: role_permissoes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role_permissoes (
    role_id bigint NOT NULL,
    permissao_id bigint NOT NULL
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    nome character varying(50) NOT NULL,
    descricao text
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: salarios_historico; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.salarios_historico (
    id bigint NOT NULL,
    funcionario_id bigint,
    salario numeric(10,2) NOT NULL,
    valido_desde date NOT NULL,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: salarios_historico_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.salarios_historico_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: salarios_historico_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.salarios_historico_id_seq OWNED BY public.salarios_historico.id;


--
-- Name: tipos_beneficios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipos_beneficios (
    id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    possui_desconto_folha boolean DEFAULT true,
    valor_padrao numeric(10,2),
    ativo boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    natureza character varying(20) NOT NULL,
    incide_ferias boolean DEFAULT false,
    incide_decimo boolean DEFAULT false,
    CONSTRAINT chk_incidencia_beneficios CHECK ((((natureza)::text = 'PROVENTO'::text) OR ((incide_ferias = false) AND (incide_decimo = false)))),
    CONSTRAINT chk_tipos_beneficios_natureza CHECK (((natureza)::text = ANY (ARRAY[('PROVENTO'::character varying)::text, ('DESCONTO'::character varying)::text, ('INFORMATIVO'::character varying)::text])))
);


--
-- Name: tipos_beneficios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tipos_beneficios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipos_beneficios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tipos_beneficios_id_seq OWNED BY public.tipos_beneficios.id;


--
-- Name: tipos_documentos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tipos_documentos (
    id bigint NOT NULL,
    nome character varying(50) NOT NULL,
    descricao text,
    obrigatorio boolean DEFAULT false,
    ativo boolean DEFAULT true,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: tipos_documentos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tipos_documentos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipos_documentos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tipos_documentos_id_seq OWNED BY public.tipos_documentos.id;


--
-- Name: usuario_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario_roles (
    usuario_id bigint NOT NULL,
    role_id bigint NOT NULL,
    criado_em timestamp without time zone DEFAULT now()
);


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    ativo boolean DEFAULT true,
    ultimo_login timestamp without time zone,
    token_recuperacao character varying(255),
    token_expiracao timestamp without time zone,
    autenticacao_2fa boolean DEFAULT false,
    segredo_2fa character varying(255),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    tipo_2fa character varying(20),
    tentativas_login integer DEFAULT 0,
    bloqueado_ate timestamp without time zone,
    role character varying(30) DEFAULT 'EMPLOYEE'::character varying NOT NULL,
    email_verificado_em timestamp without time zone,
    CONSTRAINT chk_usuarios_role CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'EMPLOYEE'::character varying, 'RH_CHEFE'::character varying, 'RH_ASSISTENTE'::character varying, 'DP_CHEFE'::character varying, 'DP_ASSISTENTE'::character varying])::text[]))),
    CONSTRAINT ck_usuarios_role CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'EMPLOYEE'::character varying, 'RH_CHEFE'::character varying, 'RH_ASSISTENTE'::character varying, 'DP_CHEFE'::character varying, 'DP_ASSISTENTE'::character varying])::text[])))
);


--
-- Name: usuarios_2fa_codes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios_2fa_codes (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    codigo character varying(10) NOT NULL,
    expira_em timestamp without time zone NOT NULL,
    usado boolean DEFAULT false,
    criado_em timestamp without time zone DEFAULT now(),
    codigo_hash text
);


--
-- Name: usuarios_2fa_codes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_2fa_codes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_2fa_codes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_2fa_codes_id_seq OWNED BY public.usuarios_2fa_codes.id;


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: afastamentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.afastamentos ALTER COLUMN id SET DEFAULT nextval('public.afastamentos_id_seq'::regclass);


--
-- Name: auditoria_lgpd id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auditoria_lgpd ALTER COLUMN id SET DEFAULT nextval('public.auditoria_lgpd_id_seq'::regclass);


--
-- Name: auth_password_resets id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_password_resets ALTER COLUMN id SET DEFAULT nextval('public.auth_password_resets_id_seq'::regclass);


--
-- Name: auth_refresh_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_refresh_tokens ALTER COLUMN id SET DEFAULT nextval('public.auth_refresh_tokens_id_seq'::regclass);


--
-- Name: cargos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cargos ALTER COLUMN id SET DEFAULT nextval('public.cargos_id_seq'::regclass);


--
-- Name: dados_emergencia id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_emergencia ALTER COLUMN id SET DEFAULT nextval('public.contatos_emergencia_id_seq'::regclass);


--
-- Name: dados_funcionarios_documentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_funcionarios_documentos ALTER COLUMN id SET DEFAULT nextval('public.documentos_id_seq'::regclass);


--
-- Name: departamentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departamentos ALTER COLUMN id SET DEFAULT nextval('public.departamentos_id_seq'::regclass);


--
-- Name: feriados id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feriados ALTER COLUMN id SET DEFAULT nextval('public.feriados_id_seq'::regclass);


--
-- Name: ferias_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_logs ALTER COLUMN id SET DEFAULT nextval('public.ferias_logs_id_seq'::regclass);


--
-- Name: ferias_periodos_aquisitivos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_periodos_aquisitivos ALTER COLUMN id SET DEFAULT nextval('public.ferias_periodos_aquisitivos_id_seq'::regclass);


--
-- Name: ferias_registros id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_registros ALTER COLUMN id SET DEFAULT nextval('public.ferias_registros_id_seq'::regclass);


--
-- Name: ferias_solicitacoes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_solicitacoes ALTER COLUMN id SET DEFAULT nextval('public.ferias_solicitacoes_id_seq'::regclass);


--
-- Name: folha_eventos_catalogo id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_eventos_catalogo ALTER COLUMN id SET DEFAULT nextval('public.eventos_folha_id_seq'::regclass);


--
-- Name: folha_lancamentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_lancamentos ALTER COLUMN id SET DEFAULT nextval('public.folha_eventos_id_seq'::regclass);


--
-- Name: folha_pagamentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_pagamentos ALTER COLUMN id SET DEFAULT nextval('public.folha_pagamentos_id_seq'::regclass);


--
-- Name: funcionarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios ALTER COLUMN id SET DEFAULT nextval('public.funcionarios_id_seq'::regclass);


--
-- Name: funcionarios_beneficios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios_beneficios ALTER COLUMN id SET DEFAULT nextval('public.funcionarios_beneficios_id_seq'::regclass);


--
-- Name: historico_cargos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos ALTER COLUMN id SET DEFAULT nextval('public.historico_cargos_id_seq'::regclass);


--
-- Name: inss_faixas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inss_faixas ALTER COLUMN id SET DEFAULT nextval('public.inss_faixas_id_seq'::regclass);


--
-- Name: irrf_tabela id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.irrf_tabela ALTER COLUMN id SET DEFAULT nextval('public.irrf_tabela_id_seq'::regclass);


--
-- Name: logs_auditoria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.logs_auditoria ALTER COLUMN id SET DEFAULT nextval('public.logs_auditoria_id_seq'::regclass);


--
-- Name: permissoes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissoes ALTER COLUMN id SET DEFAULT nextval('public.permissoes_id_seq'::regclass);


--
-- Name: ponto_apuracao_diaria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_apuracao_diaria ALTER COLUMN id SET DEFAULT nextval('public.ponto_apuracao_diaria_id_seq'::regclass);


--
-- Name: ponto_banco_horas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_banco_horas ALTER COLUMN id SET DEFAULT nextval('public.ponto_banco_horas_id_seq'::regclass);


--
-- Name: ponto_fechamentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_fechamentos ALTER COLUMN id SET DEFAULT nextval('public.ponto_fechamentos_id_seq'::regclass);


--
-- Name: ponto_jornadas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_jornadas ALTER COLUMN id SET DEFAULT nextval('public.ponto_jornadas_id_seq'::regclass);


--
-- Name: ponto_justificativas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_justificativas ALTER COLUMN id SET DEFAULT nextval('public.ponto_justificativas_id_seq'::regclass);


--
-- Name: ponto_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_logs ALTER COLUMN id SET DEFAULT nextval('public.ponto_logs_id_seq'::regclass);


--
-- Name: ponto_marcacoes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_marcacoes ALTER COLUMN id SET DEFAULT nextval('public.ponto_marcacoes_id_seq'::regclass);


--
-- Name: rescisoes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rescisoes ALTER COLUMN id SET DEFAULT nextval('public.rescisoes_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: salarios_historico id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salarios_historico ALTER COLUMN id SET DEFAULT nextval('public.salarios_historico_id_seq'::regclass);


--
-- Name: tipos_beneficios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_beneficios ALTER COLUMN id SET DEFAULT nextval('public.tipos_beneficios_id_seq'::regclass);


--
-- Name: tipos_documentos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_documentos ALTER COLUMN id SET DEFAULT nextval('public.tipos_documentos_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Name: usuarios_2fa_codes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios_2fa_codes ALTER COLUMN id SET DEFAULT nextval('public.usuarios_2fa_codes_id_seq'::regclass);


--
-- Name: afastamentos afastamentos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.afastamentos
    ADD CONSTRAINT afastamentos_pkey PRIMARY KEY (id);


--
-- Name: auditoria_lgpd auditoria_lgpd_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auditoria_lgpd
    ADD CONSTRAINT auditoria_lgpd_pkey PRIMARY KEY (id);


--
-- Name: auth_password_resets auth_password_resets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_password_resets
    ADD CONSTRAINT auth_password_resets_pkey PRIMARY KEY (id);


--
-- Name: auth_refresh_tokens auth_refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_refresh_tokens
    ADD CONSTRAINT auth_refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: cargos cargos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cargos
    ADD CONSTRAINT cargos_pkey PRIMARY KEY (id);


--
-- Name: dados_emergencia dados_emergencia_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_emergencia
    ADD CONSTRAINT dados_emergencia_pkey PRIMARY KEY (id);


--
-- Name: dados_funcionarios_documentos dados_funcionarios_documentos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_funcionarios_documentos
    ADD CONSTRAINT dados_funcionarios_documentos_pkey PRIMARY KEY (id);


--
-- Name: departamentos departamentos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departamentos
    ADD CONSTRAINT departamentos_pkey PRIMARY KEY (id);


--
-- Name: folha_eventos_catalogo eventos_folha_codigo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_eventos_catalogo
    ADD CONSTRAINT eventos_folha_codigo_key UNIQUE (codigo);


--
-- Name: folha_eventos_catalogo eventos_folha_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_eventos_catalogo
    ADD CONSTRAINT eventos_folha_pkey PRIMARY KEY (id);


--
-- Name: feriados feriados_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feriados
    ADD CONSTRAINT feriados_pkey PRIMARY KEY (id);


--
-- Name: ferias_logs ferias_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_logs
    ADD CONSTRAINT ferias_logs_pkey PRIMARY KEY (id);


--
-- Name: ferias_periodos_aquisitivos ferias_periodos_aquisitivos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_periodos_aquisitivos
    ADD CONSTRAINT ferias_periodos_aquisitivos_pkey PRIMARY KEY (id);


--
-- Name: ferias_registros ferias_registros_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_registros
    ADD CONSTRAINT ferias_registros_pkey PRIMARY KEY (id);


--
-- Name: ferias_solicitacoes ferias_solicitacoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_solicitacoes
    ADD CONSTRAINT ferias_solicitacoes_pkey PRIMARY KEY (id);


--
-- Name: folha_lancamentos folha_eventos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_lancamentos
    ADD CONSTRAINT folha_eventos_pkey PRIMARY KEY (id);


--
-- Name: folha_pagamentos folha_pagamentos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_pagamentos
    ADD CONSTRAINT folha_pagamentos_pkey PRIMARY KEY (id);


--
-- Name: funcionarios_beneficios funcionarios_beneficios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios_beneficios
    ADD CONSTRAINT funcionarios_beneficios_pkey PRIMARY KEY (id);


--
-- Name: funcionarios funcionarios_cpf_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_cpf_key UNIQUE (cpf);


--
-- Name: funcionarios funcionarios_email_corporativo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_email_corporativo_key UNIQUE (email_corporativo);


--
-- Name: funcionarios funcionarios_matricula_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_matricula_key UNIQUE (matricula);


--
-- Name: funcionarios funcionarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_pkey PRIMARY KEY (id);


--
-- Name: historico_cargos historico_cargos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_pkey PRIMARY KEY (id);


--
-- Name: inss_faixas inss_faixas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inss_faixas
    ADD CONSTRAINT inss_faixas_pkey PRIMARY KEY (id);


--
-- Name: irrf_parametros irrf_parametros_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.irrf_parametros
    ADD CONSTRAINT irrf_parametros_pkey PRIMARY KEY (ano);


--
-- Name: irrf_tabela irrf_tabela_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.irrf_tabela
    ADD CONSTRAINT irrf_tabela_pkey PRIMARY KEY (id);


--
-- Name: logs_auditoria logs_auditoria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.logs_auditoria
    ADD CONSTRAINT logs_auditoria_pkey PRIMARY KEY (id);


--
-- Name: permissoes permissoes_chave_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissoes
    ADD CONSTRAINT permissoes_chave_key UNIQUE (chave);


--
-- Name: permissoes permissoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permissoes
    ADD CONSTRAINT permissoes_pkey PRIMARY KEY (id);


--
-- Name: ponto_apuracao_diaria ponto_apuracao_diaria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_apuracao_diaria
    ADD CONSTRAINT ponto_apuracao_diaria_pkey PRIMARY KEY (id);


--
-- Name: ponto_banco_horas ponto_banco_horas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_banco_horas
    ADD CONSTRAINT ponto_banco_horas_pkey PRIMARY KEY (id);


--
-- Name: ponto_fechamentos ponto_fechamentos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_fechamentos
    ADD CONSTRAINT ponto_fechamentos_pkey PRIMARY KEY (id);


--
-- Name: ponto_jornadas ponto_jornadas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_jornadas
    ADD CONSTRAINT ponto_jornadas_pkey PRIMARY KEY (id);


--
-- Name: ponto_justificativas ponto_justificativas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_justificativas
    ADD CONSTRAINT ponto_justificativas_pkey PRIMARY KEY (id);


--
-- Name: ponto_logs ponto_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_logs
    ADD CONSTRAINT ponto_logs_pkey PRIMARY KEY (id);


--
-- Name: ponto_marcacoes ponto_marcacoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_marcacoes
    ADD CONSTRAINT ponto_marcacoes_pkey PRIMARY KEY (id);


--
-- Name: rescisoes rescisoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rescisoes
    ADD CONSTRAINT rescisoes_pkey PRIMARY KEY (id);


--
-- Name: role_permissoes role_permissoes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissoes
    ADD CONSTRAINT role_permissoes_pkey PRIMARY KEY (role_id, permissao_id);


--
-- Name: roles roles_nome_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_nome_key UNIQUE (nome);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: salarios_historico salarios_historico_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salarios_historico
    ADD CONSTRAINT salarios_historico_pkey PRIMARY KEY (id);


--
-- Name: tipos_beneficios tipos_beneficios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_beneficios
    ADD CONSTRAINT tipos_beneficios_pkey PRIMARY KEY (id);


--
-- Name: tipos_documentos tipos_documentos_nome_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_documentos
    ADD CONSTRAINT tipos_documentos_nome_key UNIQUE (nome);


--
-- Name: tipos_documentos tipos_documentos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tipos_documentos
    ADD CONSTRAINT tipos_documentos_pkey PRIMARY KEY (id);


--
-- Name: folha_pagamentos uq_folha_funcionario_competencia; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_pagamentos
    ADD CONSTRAINT uq_folha_funcionario_competencia UNIQUE (funcionario_id, competencia);


--
-- Name: usuario_roles usuario_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario_roles
    ADD CONSTRAINT usuario_roles_pkey PRIMARY KEY (usuario_id, role_id);


--
-- Name: usuarios_2fa_codes usuarios_2fa_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios_2fa_codes
    ADD CONSTRAINT usuarios_2fa_codes_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: idx_auth_password_resets_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auth_password_resets_usuario ON public.auth_password_resets USING btree (usuario_id);


--
-- Name: idx_auth_refresh_tokens_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auth_refresh_tokens_usuario ON public.auth_refresh_tokens USING btree (usuario_id);


--
-- Name: idx_folha_lancamentos_codigo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_folha_lancamentos_codigo ON public.folha_lancamentos USING btree (codigo);


--
-- Name: idx_folha_lancamentos_evento; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_folha_lancamentos_evento ON public.folha_lancamentos USING btree (evento_id);


--
-- Name: idx_folha_lancamentos_folha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_folha_lancamentos_folha ON public.folha_lancamentos USING btree (folha_id);


--
-- Name: idx_folha_lancamentos_folha_codigo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_folha_lancamentos_folha_codigo ON public.folha_lancamentos USING btree (folha_id, codigo);


--
-- Name: idx_folha_lancamentos_folha_tipo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_folha_lancamentos_folha_tipo ON public.folha_lancamentos USING btree (folha_id, tipo);


--
-- Name: idx_func_beneficios_ativos; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_func_beneficios_ativos ON public.funcionarios_beneficios USING btree (funcionario_id) WHERE (ativo = true);


--
-- Name: idx_funcionarios_cargo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_funcionarios_cargo ON public.funcionarios USING btree (cargo_id);


--
-- Name: idx_funcionarios_departamento; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_funcionarios_departamento ON public.funcionarios USING btree (departamento_id);


--
-- Name: idx_inss_faixas_ano; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inss_faixas_ano ON public.inss_faixas USING btree (ano);


--
-- Name: idx_inss_faixas_ano_inicio; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_inss_faixas_ano_inicio ON public.inss_faixas USING btree (ano, faixa_inicio);


--
-- Name: idx_irrf_tabela_faixa; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_irrf_tabela_faixa ON public.irrf_tabela USING btree (faixa_min);


--
-- Name: idx_irrf_tabela_vigencia; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_irrf_tabela_vigencia ON public.irrf_tabela USING btree (vigente_desde);


--
-- Name: idx_logs_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_logs_usuario ON public.logs_auditoria USING btree (usuario_id);


--
-- Name: idx_usuarios_email_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_usuarios_email_ativo ON public.usuarios USING btree (email, ativo);


--
-- Name: idx_usuarios_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_usuarios_role ON public.usuarios USING btree (role);


--
-- Name: uq_auth_password_resets_token_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_auth_password_resets_token_hash ON public.auth_password_resets USING btree (token_hash);


--
-- Name: uq_auth_refresh_tokens_token_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_auth_refresh_tokens_token_hash ON public.auth_refresh_tokens USING btree (token_hash);


--
-- Name: uq_folha_lancamentos_folha_codigo_origem_auto; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_folha_lancamentos_folha_codigo_origem_auto ON public.folha_lancamentos USING btree (folha_id, codigo) WHERE ((origem)::text = 'CALCULO_AUTOMATICO'::text);


--
-- Name: uq_folha_singletons; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_folha_singletons ON public.folha_lancamentos USING btree (folha_id, codigo) WHERE ((codigo)::text = ANY (ARRAY[('SALARIO'::character varying)::text, ('INSS'::character varying)::text, ('IRRF'::character varying)::text, ('FGTS'::character varying)::text]));


--
-- Name: uq_func_beneficio_ativo; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_func_beneficio_ativo ON public.funcionarios_beneficios USING btree (funcionario_id, tipo_beneficio_id) WHERE (ativo = true);


--
-- Name: uq_inss_faixas_ano_inicio; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_inss_faixas_ano_inicio ON public.inss_faixas USING btree (ano, faixa_inicio);


--
-- Name: uq_usuarios_email_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_usuarios_email_lower ON public.usuarios USING btree (lower((email)::text));


--
-- Name: folha_lancamentos tg_bloquear_lancamento_folha_fechada; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER tg_bloquear_lancamento_folha_fechada BEFORE INSERT OR DELETE OR UPDATE ON public.folha_lancamentos FOR EACH ROW EXECUTE FUNCTION public.trg_bloquear_lancamento_folha_fechada();


--
-- Name: folha_pagamentos tg_folha_insert; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER tg_folha_insert AFTER INSERT ON public.folha_pagamentos FOR EACH ROW EXECUTE FUNCTION public.trg_processar_folha_insert();


--
-- Name: folha_lancamentos tg_set_evento_id_por_codigo; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER tg_set_evento_id_por_codigo BEFORE INSERT OR UPDATE OF codigo, evento_id ON public.folha_lancamentos FOR EACH ROW EXECUTE FUNCTION public.trg_set_evento_id_por_codigo();


--
-- Name: tipos_beneficios tg_tipos_beneficios_sync_evento; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER tg_tipos_beneficios_sync_evento AFTER INSERT OR UPDATE OF nome, natureza, incide_ferias, incide_decimo, ativo ON public.tipos_beneficios FOR EACH ROW EXECUTE FUNCTION public.trg_sync_evento_beneficio();


--
-- Name: ferias_periodos_aquisitivos trg_audit_ferias_periodos_aquisitivos; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_audit_ferias_periodos_aquisitivos AFTER INSERT OR DELETE OR UPDATE ON public.ferias_periodos_aquisitivos FOR EACH ROW EXECUTE FUNCTION public.fn_auditoria_lgpd();


--
-- Name: ferias_registros trg_audit_ferias_registros; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_audit_ferias_registros AFTER INSERT OR DELETE OR UPDATE ON public.ferias_registros FOR EACH ROW EXECUTE FUNCTION public.fn_auditoria_lgpd();


--
-- Name: ferias_solicitacoes trg_audit_ferias_solicitacoes; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_audit_ferias_solicitacoes AFTER INSERT OR DELETE OR UPDATE ON public.ferias_solicitacoes FOR EACH ROW EXECUTE FUNCTION public.fn_auditoria_lgpd();


--
-- Name: folha_lancamentos trg_audit_folha_lancamentos; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_audit_folha_lancamentos AFTER INSERT OR DELETE OR UPDATE ON public.folha_lancamentos FOR EACH ROW EXECUTE FUNCTION public.fn_auditoria_lgpd();


--
-- Name: funcionarios trg_audit_funcionarios; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_audit_funcionarios AFTER INSERT OR DELETE OR UPDATE ON public.funcionarios FOR EACH ROW EXECUTE FUNCTION public.fn_auditoria_lgpd();


--
-- Name: funcionarios_beneficios trg_audit_funcionarios_beneficios; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_audit_funcionarios_beneficios AFTER INSERT OR DELETE OR UPDATE ON public.funcionarios_beneficios FOR EACH ROW EXECUTE FUNCTION public.fn_auditoria_lgpd();


--
-- Name: ferias_registros trg_gerar_lancamentos_ferias; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gerar_lancamentos_ferias AFTER INSERT ON public.ferias_registros FOR EACH ROW EXECUTE FUNCTION public.gerar_lancamentos_ferias();


--
-- Name: auth_password_resets auth_password_resets_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_password_resets
    ADD CONSTRAINT auth_password_resets_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: auth_refresh_tokens auth_refresh_tokens_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auth_refresh_tokens
    ADD CONSTRAINT auth_refresh_tokens_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: cargos cargos_departamento_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cargos
    ADD CONSTRAINT cargos_departamento_id_fkey FOREIGN KEY (departamento_id) REFERENCES public.departamentos(id);


--
-- Name: dados_emergencia dados_emergencia_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_emergencia
    ADD CONSTRAINT dados_emergencia_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id) ON DELETE CASCADE;


--
-- Name: dados_funcionarios_documentos dados_funcionarios_documentos_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_funcionarios_documentos
    ADD CONSTRAINT dados_funcionarios_documentos_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id) ON DELETE CASCADE;


--
-- Name: dados_funcionarios_documentos dados_funcionarios_documentos_tipo_documento_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dados_funcionarios_documentos
    ADD CONSTRAINT dados_funcionarios_documentos_tipo_documento_id_fkey FOREIGN KEY (tipo_documento_id) REFERENCES public.tipos_documentos(id);


--
-- Name: departamentos departamentos_departamento_pai_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.departamentos
    ADD CONSTRAINT departamentos_departamento_pai_id_fkey FOREIGN KEY (departamento_pai_id) REFERENCES public.departamentos(id);


--
-- Name: ferias_logs ferias_logs_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_logs
    ADD CONSTRAINT ferias_logs_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ferias_logs ferias_logs_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_logs
    ADD CONSTRAINT ferias_logs_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: ferias_periodos_aquisitivos ferias_periodos_aquisitivos_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_periodos_aquisitivos
    ADD CONSTRAINT ferias_periodos_aquisitivos_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ferias_registros ferias_registros_criado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_registros
    ADD CONSTRAINT ferias_registros_criado_por_fkey FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);


--
-- Name: ferias_registros ferias_registros_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_registros
    ADD CONSTRAINT ferias_registros_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ferias_registros ferias_registros_solicitacao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_registros
    ADD CONSTRAINT ferias_registros_solicitacao_id_fkey FOREIGN KEY (solicitacao_id) REFERENCES public.ferias_solicitacoes(id);


--
-- Name: ferias_solicitacoes ferias_solicitacoes_aprovado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_solicitacoes
    ADD CONSTRAINT ferias_solicitacoes_aprovado_por_fkey FOREIGN KEY (aprovado_por) REFERENCES public.usuarios(id);


--
-- Name: ferias_solicitacoes ferias_solicitacoes_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_solicitacoes
    ADD CONSTRAINT ferias_solicitacoes_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ferias_solicitacoes ferias_solicitacoes_periodo_aquisitivo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ferias_solicitacoes
    ADD CONSTRAINT ferias_solicitacoes_periodo_aquisitivo_id_fkey FOREIGN KEY (periodo_aquisitivo_id) REFERENCES public.ferias_periodos_aquisitivos(id);


--
-- Name: folha_lancamentos fk_folha_evento_catalogo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_lancamentos
    ADD CONSTRAINT fk_folha_evento_catalogo FOREIGN KEY (evento_id) REFERENCES public.folha_eventos_catalogo(id);


--
-- Name: folha_pagamentos fk_folha_funcionario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_pagamentos
    ADD CONSTRAINT fk_folha_funcionario FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: usuarios_2fa_codes fk_usuarios_2fa_codes; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios_2fa_codes
    ADD CONSTRAINT fk_usuarios_2fa_codes FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: folha_lancamentos folha_eventos_folha_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_lancamentos
    ADD CONSTRAINT folha_eventos_folha_id_fkey FOREIGN KEY (folha_id) REFERENCES public.folha_pagamentos(id);


--
-- Name: folha_pagamentos folha_pagamentos_fechado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_pagamentos
    ADD CONSTRAINT folha_pagamentos_fechado_por_fkey FOREIGN KEY (fechado_por) REFERENCES public.usuarios(id);


--
-- Name: folha_pagamentos folha_pagamentos_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.folha_pagamentos
    ADD CONSTRAINT folha_pagamentos_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: funcionarios_beneficios funcionarios_beneficios_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios_beneficios
    ADD CONSTRAINT funcionarios_beneficios_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: funcionarios_beneficios funcionarios_beneficios_tipo_beneficio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios_beneficios
    ADD CONSTRAINT funcionarios_beneficios_tipo_beneficio_id_fkey FOREIGN KEY (tipo_beneficio_id) REFERENCES public.tipos_beneficios(id);


--
-- Name: funcionarios funcionarios_cargo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_cargo_id_fkey FOREIGN KEY (cargo_id) REFERENCES public.cargos(id);


--
-- Name: funcionarios funcionarios_departamento_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_departamento_id_fkey FOREIGN KEY (departamento_id) REFERENCES public.departamentos(id);


--
-- Name: funcionarios funcionarios_gestor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_gestor_id_fkey FOREIGN KEY (gestor_id) REFERENCES public.funcionarios(id);


--
-- Name: funcionarios funcionarios_jornada_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_jornada_id_fkey FOREIGN KEY (jornada_id) REFERENCES public.ponto_jornadas(id);


--
-- Name: funcionarios funcionarios_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funcionarios
    ADD CONSTRAINT funcionarios_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: historico_cargos historico_cargos_cargo_anterior_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_cargo_anterior_id_fkey FOREIGN KEY (cargo_anterior_id) REFERENCES public.cargos(id);


--
-- Name: historico_cargos historico_cargos_cargo_novo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_cargo_novo_id_fkey FOREIGN KEY (cargo_novo_id) REFERENCES public.cargos(id);


--
-- Name: historico_cargos historico_cargos_criado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_criado_por_fkey FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);


--
-- Name: historico_cargos historico_cargos_departamento_anterior_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_departamento_anterior_id_fkey FOREIGN KEY (departamento_anterior_id) REFERENCES public.departamentos(id);


--
-- Name: historico_cargos historico_cargos_departamento_novo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_departamento_novo_id_fkey FOREIGN KEY (departamento_novo_id) REFERENCES public.departamentos(id);


--
-- Name: historico_cargos historico_cargos_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historico_cargos
    ADD CONSTRAINT historico_cargos_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: logs_auditoria logs_auditoria_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.logs_auditoria
    ADD CONSTRAINT logs_auditoria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: ponto_apuracao_diaria ponto_apuracao_diaria_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_apuracao_diaria
    ADD CONSTRAINT ponto_apuracao_diaria_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ponto_banco_horas ponto_banco_horas_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_banco_horas
    ADD CONSTRAINT ponto_banco_horas_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ponto_fechamentos ponto_fechamentos_fechado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_fechamentos
    ADD CONSTRAINT ponto_fechamentos_fechado_por_fkey FOREIGN KEY (fechado_por) REFERENCES public.usuarios(id);


--
-- Name: ponto_fechamentos ponto_fechamentos_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_fechamentos
    ADD CONSTRAINT ponto_fechamentos_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ponto_justificativas ponto_justificativas_aprovado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_justificativas
    ADD CONSTRAINT ponto_justificativas_aprovado_por_fkey FOREIGN KEY (aprovado_por) REFERENCES public.usuarios(id);


--
-- Name: ponto_justificativas ponto_justificativas_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_justificativas
    ADD CONSTRAINT ponto_justificativas_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: ponto_logs ponto_logs_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_logs
    ADD CONSTRAINT ponto_logs_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: ponto_marcacoes ponto_marcacoes_criado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_marcacoes
    ADD CONSTRAINT ponto_marcacoes_criado_por_fkey FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);


--
-- Name: ponto_marcacoes ponto_marcacoes_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ponto_marcacoes
    ADD CONSTRAINT ponto_marcacoes_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: role_permissoes role_permissoes_permissao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissoes
    ADD CONSTRAINT role_permissoes_permissao_id_fkey FOREIGN KEY (permissao_id) REFERENCES public.permissoes(id) ON DELETE CASCADE;


--
-- Name: role_permissoes role_permissoes_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role_permissoes
    ADD CONSTRAINT role_permissoes_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: salarios_historico salarios_historico_funcionario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salarios_historico
    ADD CONSTRAINT salarios_historico_funcionario_id_fkey FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);


--
-- Name: usuario_roles usuario_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario_roles
    ADD CONSTRAINT usuario_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: usuario_roles usuario_roles_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario_roles
    ADD CONSTRAINT usuario_roles_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--



