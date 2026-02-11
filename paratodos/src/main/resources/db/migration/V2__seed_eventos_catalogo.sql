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