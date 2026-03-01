-- V5 - Campos de contato de emergencia no perfil do funcionario

ALTER TABLE public.funcionarios
    ADD COLUMN emergencia_nome       VARCHAR(255),
    ADD COLUMN emergencia_parentesco VARCHAR(100),
    ADD COLUMN emergencia_telefone   VARCHAR(20);
