CREATE TABLE public.vagas_beneficios (
    vaga_id            BIGINT NOT NULL,
    tipo_beneficio_id  BIGINT NOT NULL,
    CONSTRAINT pk_vagas_beneficios PRIMARY KEY (vaga_id, tipo_beneficio_id),
    CONSTRAINT fk_vb_vaga FOREIGN KEY (vaga_id) REFERENCES public.vagas(id) ON DELETE CASCADE,
    CONSTRAINT fk_vb_tipo_beneficio FOREIGN KEY (tipo_beneficio_id) REFERENCES public.tipos_beneficios(id) ON DELETE CASCADE
);
