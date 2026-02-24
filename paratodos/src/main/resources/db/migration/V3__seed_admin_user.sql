INSERT INTO public.usuarios (
    email,
    senha_hash,
    ativo,
    role,
    autenticacao_2fa,
    tentativas_login
)
VALUES (
    'admin@local',
    '$2b$10$G9954sJQ/RwX22cQrL0KvOCS6m99H6dCInnlkSHxwK/dkPSzVBDfa',
    true,
    'ADMIN',
    false,
    0
)
ON CONFLICT (email) DO NOTHING;