-- V4: Seed all test users (6 profiles)
-- Passwords are BCrypt $2b$10$ hashed
-- 2FA via email (tipo_2fa = 'CODIGO') para quem tem autenticacao_2fa = true

-- admin@local / Admin@123 (sem 2FA)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('admin@local', '$2b$10$G9954sJQ/RwX22cQrL0KvOCS6m99H6dCInnlkSHxwK/dkPSzVBDfa', true, 'ADMIN', false, 'NENHUMA', 0)
ON CONFLICT (email) DO NOTHING;

-- rh.chefe@local / RhChefe@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('rh.chefe@local', '$2b$10$34c8uUXWdx1qXy.KA4mhnO.2qKXxxlQye/AHmIhzzYzyOQ/6sprBW', true, 'RH_CHEFE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;

-- rh.assistente@local / RhAssist@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('rh.assistente@local', '$2b$10$wwS4K1TdiAAMihu1kLlgTOI4uL4DiY4lGv4oUp3dCfiFOQ2yMv7Jm', true, 'RH_ASSISTENTE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;

-- dp.chefe@local / DpChefe@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('dp.chefe@local', '$2b$10$MP9F5MDAd1FnP.ZDGKoLieeoGnN7gU77wiIreaPZB.A49T/554I7i', true, 'DP_CHEFE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;

-- dp.assistente@local / DpAssist@123 (sem 2FA)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('dp.assistente@local', '$2b$10$ZU8suZx4JUuh5P.hoA5abuuD9BtQbIeZU6lmZtsTxBJhNqmd4rsLe', true, 'DP_ASSISTENTE', false, 'NENHUMA', 0)
ON CONFLICT (email) DO NOTHING;

-- funcionario@local / Func@123 (2FA por email)
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login)
VALUES ('funcionario@local', '$2b$10$W6dWk8VPMfLFIkz0kduWyeyKyd5GeviKbukVtcD44grvpNbeq9HmO', true, 'EMPLOYEE', true, 'CODIGO', 0)
ON CONFLICT (email) DO NOTHING;
