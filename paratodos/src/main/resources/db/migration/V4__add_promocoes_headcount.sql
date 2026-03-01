-- V4 - Promoções, Headcount por departamento, ajustes recrutamento

-- =============================================
-- 1. PROMOÇÕES (Solicitações de mudança de cargo/departamento/salário)
-- =============================================

CREATE TABLE public.promocoes (
    id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    cargo_atual_id bigint,
    cargo_novo_id bigint,
    departamento_atual_id bigint,
    departamento_novo_id bigint,
    salario_atual numeric(10,2),
    salario_novo numeric(10,2),
    motivo text NOT NULL,
    tipo character varying(30) DEFAULT 'PROMOCAO',
    solicitante_id bigint NOT NULL,
    aprovador_id bigint,
    status character varying(20) DEFAULT 'PENDENTE',
    data_solicitacao timestamp without time zone DEFAULT now(),
    data_decisao timestamp without time zone,
    observacao_aprovador text,
    CONSTRAINT chk_promocoes_status CHECK ((status)::text IN ('PENDENTE','APROVADA','REJEITADA')),
    CONSTRAINT chk_promocoes_tipo CHECK ((tipo)::text IN ('PROMOCAO','TRANSFERENCIA','REAJUSTE','CONTRATACAO'))
);

CREATE SEQUENCE public.promocoes_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.promocoes_id_seq OWNED BY public.promocoes.id;
ALTER TABLE ONLY public.promocoes ALTER COLUMN id SET DEFAULT nextval('public.promocoes_id_seq'::regclass);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT promocoes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_funcionario FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_cargo_atual FOREIGN KEY (cargo_atual_id) REFERENCES public.cargos(id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_cargo_novo FOREIGN KEY (cargo_novo_id) REFERENCES public.cargos(id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_dept_atual FOREIGN KEY (departamento_atual_id) REFERENCES public.departamentos(id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_dept_novo FOREIGN KEY (departamento_novo_id) REFERENCES public.departamentos(id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_solicitante FOREIGN KEY (solicitante_id) REFERENCES public.usuarios(id);
ALTER TABLE ONLY public.promocoes ADD CONSTRAINT fk_promocoes_aprovador FOREIGN KEY (aprovador_id) REFERENCES public.usuarios(id);

CREATE INDEX idx_promocoes_status ON public.promocoes(status);
CREATE INDEX idx_promocoes_funcionario ON public.promocoes(funcionario_id);
CREATE INDEX idx_promocoes_solicitante ON public.promocoes(solicitante_id);

-- =============================================
-- 2. HEADCOUNT - Limite de vagas por departamento
-- =============================================

ALTER TABLE public.departamentos ADD COLUMN headcount_limite integer DEFAULT 0;

-- =============================================
-- 3. AJUSTES CANDIDATOS - Campos extras para recrutamento simplificado
-- =============================================

ALTER TABLE public.candidatos ADD COLUMN cpf character varying(14);
ALTER TABLE public.candidatos ADD COLUMN data_nascimento date;
ALTER TABLE public.candidatos ADD COLUMN cidade character varying(100);
ALTER TABLE public.candidatos ADD COLUMN estado character varying(2);
ALTER TABLE public.candidatos ADD COLUMN pretensao_salarial numeric(10,2);
ALTER TABLE public.candidatos ADD COLUMN observacoes text;
