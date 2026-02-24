-- V5: Usuários adicionais sem 2FA
INSERT INTO public.usuarios (email, senha_hash, ativo, role, autenticacao_2fa, tipo_2fa, tentativas_login) VALUES
  ('rh.chefe2@local',      '$2b$10$YF6jzFx0Af5v010POOfXmeFwb6nFVD2roTzca5d7M92HLrcXdPRgi', true, 'RH_CHEFE',      false, 'NENHUMA', 0),
  ('rh.assistente2@local', '$2b$10$qsPCziNG/nKZ3.vcLTMXlOnjp2QyEA5vriKEYPa7KeBro9DMk2bfa', true, 'RH_ASSISTENTE', false, 'NENHUMA', 0),
  ('dp.chefe2@local',      '$2b$10$xt6I0CqvAk5CR7zW0xBzveW.XZ/z6rYx6jWm.XN2qdV1BcN5Avd3O', true, 'DP_CHEFE',      false, 'NENHUMA', 0),
  ('dp.assistente2@local', '$2b$10$uL9ZZcWtQGqcTvv58CRflOuQWRCkM254C8Xn.Ow58qeq3GbYjPcSe', true, 'DP_ASSISTENTE', false, 'NENHUMA', 0),
  ('funcionario2@local',   '$2b$10$ETJv18fmb8XoRpfirhH1z.kHRlYun4SjzBy6ZaOfEC7hasb1Ljz8y', true, 'EMPLOYEE',      false, 'NENHUMA', 0),
  ('funcionario3@local',   '$2b$10$Vv5JI6hd59OEqDjDhJYT8.myH8eyVqsj9bE4wBpi7yCU929gl0/oe', true, 'EMPLOYEE',      false, 'NENHUMA', 0)
ON CONFLICT (email) DO NOTHING;