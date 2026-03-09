CREATE TABLE public.ponto_calendario (
                                         id BIGSERIAL PRIMARY KEY,
                                         data DATE NOT NULL UNIQUE,
                                         tipo VARCHAR(20) NOT NULL,
                                         descricao VARCHAR(255),
                                         criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                                         CONSTRAINT chk_ponto_calendario_tipo
                                             CHECK (tipo IN ('DIA_UTIL', 'FERIADO', 'RECESSO'))
);

CREATE INDEX idx_ponto_calendario_data
    ON public.ponto_calendario(data);