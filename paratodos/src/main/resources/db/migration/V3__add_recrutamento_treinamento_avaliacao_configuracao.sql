-- V3 - Tabelas para módulos pendentes:
--   Recrutamento, Treinamentos, Avaliações de Desempenho, Configurações da Empresa

-- =============================================
-- 1. RECRUTAMENTO
-- =============================================

-- Vagas abertas
CREATE TABLE public.vagas (
    id bigint NOT NULL,
    titulo character varying(150) NOT NULL,
    descricao text,
    departamento_id bigint,
    cargo_id bigint,
    quantidade integer DEFAULT 1 NOT NULL,
    prioridade character varying(20) DEFAULT 'MEDIA',
    salario_min numeric(10,2),
    salario_max numeric(10,2),
    tipo_contrato character varying(30),
    local_trabalho character varying(100),
    modelo_trabalho character varying(20) DEFAULT 'PRESENCIAL',
    requisitos text,
    status character varying(20) DEFAULT 'ABERTA',
    publicada_em timestamp without time zone,
    encerrada_em timestamp without time zone,
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now(),
    atualizado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_vagas_status CHECK ((status)::text IN ('RASCUNHO','ABERTA','EM_ANDAMENTO','PAUSADA','ENCERRADA','CANCELADA')),
    CONSTRAINT chk_vagas_prioridade CHECK ((prioridade)::text IN ('BAIXA','MEDIA','ALTA','URGENTE')),
    CONSTRAINT chk_vagas_modelo CHECK ((modelo_trabalho)::text IN ('PRESENCIAL','HIBRIDO','REMOTO'))
);

CREATE SEQUENCE public.vagas_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.vagas_id_seq OWNED BY public.vagas.id;
ALTER TABLE ONLY public.vagas ALTER COLUMN id SET DEFAULT nextval('public.vagas_id_seq'::regclass);
ALTER TABLE ONLY public.vagas ADD CONSTRAINT vagas_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.vagas ADD CONSTRAINT fk_vagas_departamento FOREIGN KEY (departamento_id) REFERENCES public.departamentos(id);
ALTER TABLE ONLY public.vagas ADD CONSTRAINT fk_vagas_cargo FOREIGN KEY (cargo_id) REFERENCES public.cargos(id);
ALTER TABLE ONLY public.vagas ADD CONSTRAINT fk_vagas_criado_por FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);

CREATE INDEX idx_vagas_status ON public.vagas(status);
CREATE INDEX idx_vagas_departamento ON public.vagas(departamento_id);

-- Candidatos
CREATE TABLE public.candidatos (
    id bigint NOT NULL,
    nome_completo character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    telefone character varying(20),
    linkedin_url character varying(500),
    curriculo_url character varying(500),
    criado_em timestamp without time zone DEFAULT now()
);

CREATE SEQUENCE public.candidatos_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.candidatos_id_seq OWNED BY public.candidatos.id;
ALTER TABLE ONLY public.candidatos ALTER COLUMN id SET DEFAULT nextval('public.candidatos_id_seq'::regclass);
ALTER TABLE ONLY public.candidatos ADD CONSTRAINT candidatos_pkey PRIMARY KEY (id);

CREATE UNIQUE INDEX idx_candidatos_email ON public.candidatos(email);

-- Candidaturas (vínculo candidato <-> vaga + etapa do pipeline)
CREATE TABLE public.candidaturas (
    id bigint NOT NULL,
    vaga_id bigint NOT NULL,
    candidato_id bigint NOT NULL,
    etapa character varying(30) DEFAULT 'TRIAGEM',
    nota_geral numeric(3,1),
    observacoes text,
    motivo_rejeicao text,
    criado_em timestamp without time zone DEFAULT now(),
    atualizado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_candidaturas_etapa CHECK ((etapa)::text IN ('TRIAGEM','ENTREVISTA_RH','TESTE_TECNICO','ENTREVISTA_GESTOR','PROPOSTA','CONTRATADO','REJEITADO','DESISTIU'))
);

CREATE SEQUENCE public.candidaturas_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.candidaturas_id_seq OWNED BY public.candidaturas.id;
ALTER TABLE ONLY public.candidaturas ALTER COLUMN id SET DEFAULT nextval('public.candidaturas_id_seq'::regclass);
ALTER TABLE ONLY public.candidaturas ADD CONSTRAINT candidaturas_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.candidaturas ADD CONSTRAINT fk_candidaturas_vaga FOREIGN KEY (vaga_id) REFERENCES public.vagas(id) ON DELETE CASCADE;
ALTER TABLE ONLY public.candidaturas ADD CONSTRAINT fk_candidaturas_candidato FOREIGN KEY (candidato_id) REFERENCES public.candidatos(id);

CREATE UNIQUE INDEX idx_candidaturas_vaga_candidato ON public.candidaturas(vaga_id, candidato_id);
CREATE INDEX idx_candidaturas_etapa ON public.candidaturas(etapa);

-- Entrevistas
CREATE TABLE public.entrevistas (
    id bigint NOT NULL,
    candidatura_id bigint NOT NULL,
    data_hora timestamp without time zone NOT NULL,
    tipo character varying(30) DEFAULT 'PRESENCIAL',
    local_ou_link character varying(500),
    entrevistador_id bigint,
    nota numeric(3,1),
    parecer text,
    status character varying(20) DEFAULT 'AGENDADA',
    criado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_entrevistas_tipo CHECK ((tipo)::text IN ('PRESENCIAL','VIDEO','TELEFONE')),
    CONSTRAINT chk_entrevistas_status CHECK ((status)::text IN ('AGENDADA','REALIZADA','CANCELADA','NO_SHOW'))
);

CREATE SEQUENCE public.entrevistas_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.entrevistas_id_seq OWNED BY public.entrevistas.id;
ALTER TABLE ONLY public.entrevistas ALTER COLUMN id SET DEFAULT nextval('public.entrevistas_id_seq'::regclass);
ALTER TABLE ONLY public.entrevistas ADD CONSTRAINT entrevistas_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.entrevistas ADD CONSTRAINT fk_entrevistas_candidatura FOREIGN KEY (candidatura_id) REFERENCES public.candidaturas(id) ON DELETE CASCADE;
ALTER TABLE ONLY public.entrevistas ADD CONSTRAINT fk_entrevistas_entrevistador FOREIGN KEY (entrevistador_id) REFERENCES public.funcionarios(id);

CREATE INDEX idx_entrevistas_data ON public.entrevistas(data_hora);
CREATE INDEX idx_entrevistas_status ON public.entrevistas(status);

-- =============================================
-- 2. TREINAMENTOS
-- =============================================

-- Catálogo de treinamentos
CREATE TABLE public.treinamentos (
    id bigint NOT NULL,
    titulo character varying(200) NOT NULL,
    descricao text,
    categoria character varying(50),
    carga_horaria integer,
    modalidade character varying(20) DEFAULT 'PRESENCIAL',
    instrutor character varying(255),
    max_participantes integer,
    obrigatorio boolean DEFAULT false,
    departamento_id bigint,
    ativo boolean DEFAULT true,
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now(),
    atualizado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_treinamentos_modalidade CHECK ((modalidade)::text IN ('PRESENCIAL','ONLINE','HIBRIDO'))
);

CREATE SEQUENCE public.treinamentos_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.treinamentos_id_seq OWNED BY public.treinamentos.id;
ALTER TABLE ONLY public.treinamentos ALTER COLUMN id SET DEFAULT nextval('public.treinamentos_id_seq'::regclass);
ALTER TABLE ONLY public.treinamentos ADD CONSTRAINT treinamentos_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.treinamentos ADD CONSTRAINT fk_treinamentos_departamento FOREIGN KEY (departamento_id) REFERENCES public.departamentos(id);
ALTER TABLE ONLY public.treinamentos ADD CONSTRAINT fk_treinamentos_criado_por FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);

CREATE INDEX idx_treinamentos_categoria ON public.treinamentos(categoria);
CREATE INDEX idx_treinamentos_ativo ON public.treinamentos(ativo);

-- Turmas (instância de um treinamento com data)
CREATE TABLE public.treinamento_turmas (
    id bigint NOT NULL,
    treinamento_id bigint NOT NULL,
    data_inicio date NOT NULL,
    data_fim date,
    horario character varying(50),
    local_ou_link character varying(500),
    status character varying(20) DEFAULT 'PROGRAMADA',
    criado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_turmas_status CHECK ((status)::text IN ('PROGRAMADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA'))
);

CREATE SEQUENCE public.treinamento_turmas_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.treinamento_turmas_id_seq OWNED BY public.treinamento_turmas.id;
ALTER TABLE ONLY public.treinamento_turmas ALTER COLUMN id SET DEFAULT nextval('public.treinamento_turmas_id_seq'::regclass);
ALTER TABLE ONLY public.treinamento_turmas ADD CONSTRAINT treinamento_turmas_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.treinamento_turmas ADD CONSTRAINT fk_turmas_treinamento FOREIGN KEY (treinamento_id) REFERENCES public.treinamentos(id) ON DELETE CASCADE;

-- Inscrições de funcionários em turmas
CREATE TABLE public.treinamento_inscricoes (
    id bigint NOT NULL,
    turma_id bigint NOT NULL,
    funcionario_id bigint NOT NULL,
    status character varying(20) DEFAULT 'INSCRITO',
    nota numeric(4,1),
    presenca_percentual numeric(5,2),
    certificado_url character varying(500),
    inscrito_em timestamp without time zone DEFAULT now(),
    concluido_em timestamp without time zone,
    CONSTRAINT chk_inscricoes_status CHECK ((status)::text IN ('INSCRITO','EM_ANDAMENTO','CONCLUIDO','REPROVADO','CANCELADO','DESISTIU'))
);

CREATE SEQUENCE public.treinamento_inscricoes_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.treinamento_inscricoes_id_seq OWNED BY public.treinamento_inscricoes.id;
ALTER TABLE ONLY public.treinamento_inscricoes ALTER COLUMN id SET DEFAULT nextval('public.treinamento_inscricoes_id_seq'::regclass);
ALTER TABLE ONLY public.treinamento_inscricoes ADD CONSTRAINT treinamento_inscricoes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.treinamento_inscricoes ADD CONSTRAINT fk_inscricoes_turma FOREIGN KEY (turma_id) REFERENCES public.treinamento_turmas(id) ON DELETE CASCADE;
ALTER TABLE ONLY public.treinamento_inscricoes ADD CONSTRAINT fk_inscricoes_funcionario FOREIGN KEY (funcionario_id) REFERENCES public.funcionarios(id);

CREATE UNIQUE INDEX idx_inscricoes_turma_funcionario ON public.treinamento_inscricoes(turma_id, funcionario_id);

-- =============================================
-- 3. AVALIAÇÕES DE DESEMPENHO
-- =============================================

-- Ciclos de avaliação (ex: "Avaliação Q1 2026", "Anual 2025")
CREATE TABLE public.avaliacao_ciclos (
    id bigint NOT NULL,
    titulo character varying(150) NOT NULL,
    descricao text,
    tipo character varying(30) DEFAULT 'SEMESTRAL',
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    status character varying(20) DEFAULT 'RASCUNHO',
    criado_por bigint,
    criado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_ciclos_tipo CHECK ((tipo)::text IN ('TRIMESTRAL','SEMESTRAL','ANUAL','AVULSA')),
    CONSTRAINT chk_ciclos_status CHECK ((status)::text IN ('RASCUNHO','ABERTO','EM_ANDAMENTO','ENCERRADO'))
);

CREATE SEQUENCE public.avaliacao_ciclos_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.avaliacao_ciclos_id_seq OWNED BY public.avaliacao_ciclos.id;
ALTER TABLE ONLY public.avaliacao_ciclos ALTER COLUMN id SET DEFAULT nextval('public.avaliacao_ciclos_id_seq'::regclass);
ALTER TABLE ONLY public.avaliacao_ciclos ADD CONSTRAINT avaliacao_ciclos_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.avaliacao_ciclos ADD CONSTRAINT fk_ciclos_criado_por FOREIGN KEY (criado_por) REFERENCES public.usuarios(id);

-- Avaliações individuais (avaliador -> avaliado)
CREATE TABLE public.avaliacoes (
    id bigint NOT NULL,
    ciclo_id bigint NOT NULL,
    avaliado_id bigint NOT NULL,
    avaliador_id bigint NOT NULL,
    tipo character varying(30) DEFAULT 'GESTOR',
    nota_geral numeric(3,1),
    pontos_fortes text,
    pontos_melhoria text,
    metas text,
    comentarios text,
    status character varying(20) DEFAULT 'PENDENTE',
    respondido_em timestamp without time zone,
    criado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_avaliacoes_tipo CHECK ((tipo)::text IN ('AUTO','GESTOR','PAR','SUBORDINADO')),
    CONSTRAINT chk_avaliacoes_status CHECK ((status)::text IN ('PENDENTE','EM_ANDAMENTO','CONCLUIDA'))
);

CREATE SEQUENCE public.avaliacoes_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.avaliacoes_id_seq OWNED BY public.avaliacoes.id;
ALTER TABLE ONLY public.avaliacoes ALTER COLUMN id SET DEFAULT nextval('public.avaliacoes_id_seq'::regclass);
ALTER TABLE ONLY public.avaliacoes ADD CONSTRAINT avaliacoes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.avaliacoes ADD CONSTRAINT fk_avaliacoes_ciclo FOREIGN KEY (ciclo_id) REFERENCES public.avaliacao_ciclos(id) ON DELETE CASCADE;
ALTER TABLE ONLY public.avaliacoes ADD CONSTRAINT fk_avaliacoes_avaliado FOREIGN KEY (avaliado_id) REFERENCES public.funcionarios(id);
ALTER TABLE ONLY public.avaliacoes ADD CONSTRAINT fk_avaliacoes_avaliador FOREIGN KEY (avaliador_id) REFERENCES public.funcionarios(id);

CREATE INDEX idx_avaliacoes_ciclo ON public.avaliacoes(ciclo_id);
CREATE INDEX idx_avaliacoes_avaliado ON public.avaliacoes(avaliado_id);
CREATE INDEX idx_avaliacoes_status ON public.avaliacoes(status);

-- Critérios de avaliação (notas por competência)
CREATE TABLE public.avaliacao_criterios (
    id bigint NOT NULL,
    avaliacao_id bigint NOT NULL,
    competencia character varying(100) NOT NULL,
    nota numeric(3,1) NOT NULL,
    peso numeric(3,2) DEFAULT 1.00,
    comentario text
);

CREATE SEQUENCE public.avaliacao_criterios_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.avaliacao_criterios_id_seq OWNED BY public.avaliacao_criterios.id;
ALTER TABLE ONLY public.avaliacao_criterios ALTER COLUMN id SET DEFAULT nextval('public.avaliacao_criterios_id_seq'::regclass);
ALTER TABLE ONLY public.avaliacao_criterios ADD CONSTRAINT avaliacao_criterios_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.avaliacao_criterios ADD CONSTRAINT fk_criterios_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES public.avaliacoes(id) ON DELETE CASCADE;

-- =============================================
-- 4. CONFIGURAÇÕES DA EMPRESA
-- =============================================

CREATE TABLE public.configuracoes_empresa (
    id bigint NOT NULL,
    chave character varying(100) NOT NULL,
    valor text,
    tipo character varying(20) DEFAULT 'STRING',
    grupo character varying(50) DEFAULT 'GERAL',
    descricao character varying(255),
    atualizado_por bigint,
    atualizado_em timestamp without time zone DEFAULT now(),
    CONSTRAINT chk_config_tipo CHECK ((tipo)::text IN ('STRING','INTEGER','DECIMAL','BOOLEAN','JSON'))
);

CREATE SEQUENCE public.configuracoes_empresa_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE public.configuracoes_empresa_id_seq OWNED BY public.configuracoes_empresa.id;
ALTER TABLE ONLY public.configuracoes_empresa ALTER COLUMN id SET DEFAULT nextval('public.configuracoes_empresa_id_seq'::regclass);
ALTER TABLE ONLY public.configuracoes_empresa ADD CONSTRAINT configuracoes_empresa_pkey PRIMARY KEY (id);
ALTER TABLE ONLY public.configuracoes_empresa ADD CONSTRAINT fk_config_atualizado_por FOREIGN KEY (atualizado_por) REFERENCES public.usuarios(id);

CREATE UNIQUE INDEX idx_config_chave ON public.configuracoes_empresa(chave);

-- Seed de configurações padrão
INSERT INTO public.configuracoes_empresa (chave, valor, tipo, grupo, descricao) VALUES
    ('empresa.razao_social', 'RH Para Todos Ltda', 'STRING', 'EMPRESA', 'Razão social da empresa'),
    ('empresa.nome_fantasia', 'RH Para Todos', 'STRING', 'EMPRESA', 'Nome fantasia'),
    ('empresa.cnpj', '12.345.678/0001-90', 'STRING', 'EMPRESA', 'CNPJ'),
    ('empresa.inscricao_estadual', '', 'STRING', 'EMPRESA', 'Inscrição estadual'),
    ('empresa.endereco', 'Av. Paulista, 1000 - Bela Vista, São Paulo - SP', 'STRING', 'EMPRESA', 'Endereço completo'),
    ('empresa.email', 'contato@rhparatodos.com.br', 'STRING', 'EMPRESA', 'Email principal'),
    ('empresa.telefone', '(11) 3456-7890', 'STRING', 'EMPRESA', 'Telefone principal'),
    ('folha.dia_pagamento', '5', 'INTEGER', 'FOLHA', 'Dia de pagamento mensal'),
    ('folha.dia_adiantamento', '20', 'INTEGER', 'FOLHA', 'Dia do adiantamento'),
    ('folha.percentual_adiantamento', '40', 'DECIMAL', 'FOLHA', 'Percentual do adiantamento (%)'),
    ('ponto.tolerancia_minutos', '10', 'INTEGER', 'PONTO', 'Tolerância em minutos para atraso'),
    ('notificacao.email_ativo', 'true', 'BOOLEAN', 'NOTIFICACAO', 'Enviar notificações por email'),
    ('notificacao.aniversarios', 'true', 'BOOLEAN', 'NOTIFICACAO', 'Notificar aniversários'),
    ('seguranca.2fa_obrigatorio', 'false', 'BOOLEAN', 'SEGURANCA', 'Exigir 2FA para todos os usuários'),
    ('seguranca.senha_min_caracteres', '6', 'INTEGER', 'SEGURANCA', 'Mínimo de caracteres na senha');
