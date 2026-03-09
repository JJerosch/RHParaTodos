CREATE TABLE public.ponto_ocorrencias (
                                          id BIGSERIAL PRIMARY KEY,
                                          funcionario_id BIGINT NOT NULL REFERENCES public.funcionarios(id) ON DELETE CASCADE,
                                          data_inicio DATE NOT NULL,
                                          data_fim DATE NOT NULL,
                                          tipo VARCHAR(30) NOT NULL,
                                          observacao TEXT,
                                          abona_dia BOOLEAN NOT NULL DEFAULT TRUE,
                                          bloqueia_marcacao BOOLEAN NOT NULL DEFAULT TRUE,
                                          criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),

                                          CONSTRAINT chk_ponto_ocorrencias_tipo
                                              CHECK (tipo IN ('FERIAS', 'ATESTADO', 'LICENCA', 'FALTA_JUSTIFICADA')),

                                          CONSTRAINT chk_ponto_ocorrencias_periodo
                                              CHECK (data_fim >= data_inicio)
);

CREATE INDEX idx_ponto_ocorrencias_funcionario_periodo
    ON public.ponto_ocorrencias (funcionario_id, data_inicio, data_fim);