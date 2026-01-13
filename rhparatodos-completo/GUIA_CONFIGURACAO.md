# RH Para Todos - Guia de Configuração

## 📊 Estrutura do Banco de Dados

O sistema foi adaptado para funcionar com seu banco de dados existente. 

### Tabelas Utilizadas:
- **usuarios** - Usuários do sistema (login por email)
- **perfis** - Perfis de acesso (ADMIN, RH_CHEFE, etc.)
- **funcionarios** - Cadastro de funcionários
- **departamentos** - Departamentos da empresa
- **cargos** - Cargos disponíveis
- **dependentes** - Dependentes dos funcionários
- **contatos_emergencia** - Contatos de emergência
- **documentos** - Documentos dos funcionários
- **tipos_beneficios** - Tipos de benefícios
- **funcionarios_beneficios** - Benefícios por funcionário
- **historico_cargos** - Histórico de mudanças de cargo
- **historico_salarios** - Histórico de alterações salariais
- **logs_auditoria** - Logs de auditoria do sistema

---

## 🚀 Como Executar

### 1. Configurar o Banco de Dados

Seu banco já está criado. Agora precisamos inserir os perfis e usuários de teste.

Execute no PostgreSQL:

```sql
-- Inserir perfis
INSERT INTO perfis (nome, descricao, criado_em) VALUES
('ADMIN', 'Administrador do Sistema - Acesso total', NOW()),
('RH_CHEFE', 'Chefe de Recursos Humanos', NOW()),
('RH_ASSISTENTE', 'Assistente de Recursos Humanos', NOW()),
('DP_CHEFE', 'Chefe de Departamento Pessoal', NOW()),
('DP_ASSISTENTE', 'Assistente de Departamento Pessoal', NOW())
ON CONFLICT (nome) DO NOTHING;

-- Inserir usuários de teste (senha: admin123)
INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'admin@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'ADMIN'),
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@rhparatodos.com.br');

INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'maria.costa@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'RH_CHEFE'),
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'maria.costa@rhparatodos.com.br');

INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'joao.silva@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'RH_ASSISTENTE'),
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'joao.silva@rhparatodos.com.br');

INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'carlos.santos@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'DP_CHEFE'),
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'carlos.santos@rhparatodos.com.br');

INSERT INTO usuarios (email, senha_hash, perfil_id, ativo, criado_em, atualizado_em)
SELECT 'ana.oliveira@rhparatodos.com.br', 
       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
       (SELECT id FROM perfis WHERE nome = 'DP_ASSISTENTE'),
       true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'ana.oliveira@rhparatodos.com.br');
```

### 2. Configurar o Backend

Edite `backend/src/main/resources/application.yml` se necessário:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rhparatodos
    username: postgres      # seu usuário do PostgreSQL
    password: postgres      # sua senha do PostgreSQL
```

### 3. Iniciar o Backend

```bash
cd backend
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

O backend iniciará na porta 8080.

### 4. Iniciar o Frontend

```bash
cd front
# Opção 1: Python
python -m http.server 5500

# Opção 2: VS Code Live Server (porta 5500)
```

### 5. Acessar o Sistema

- URL: http://localhost:5500
- Email: admin@rhparatodos.com.br
- Senha: admin123
- Perfil: Administrador do Sistema

---

## 👥 Usuários de Teste

| Email | Senha | Perfil |
|-------|-------|--------|
| admin@rhparatodos.com.br | admin123 | Administrador |
| maria.costa@rhparatodos.com.br | admin123 | Chefe de RH |
| joao.silva@rhparatodos.com.br | admin123 | Assistente de RH |
| carlos.santos@rhparatodos.com.br | admin123 | Chefe do DP |
| ana.oliveira@rhparatodos.com.br | admin123 | Assistente do DP |

---

## 🔑 Perfis e Permissões

### ADMIN (Administrador do Sistema)
- Acesso total a todas as funcionalidades
- Gestão de usuários e permissões
- Logs de auditoria

### RH_CHEFE (Chefe de RH)
- Gestão de funcionários
- Recrutamento e seleção
- Treinamentos
- Relatórios de RH

### RH_ASSISTENTE (Assistente de RH)
- Cadastro de funcionários
- Suporte ao recrutamento
- Onboarding

### DP_CHEFE (Chefe de Departamento Pessoal)
- Folha de pagamento
- Gestão de benefícios
- Relatórios financeiros

### DP_ASSISTENTE (Assistente de DP)
- Lançamentos de folha
- Cadastro de benefícios

---

## 🔧 Estrutura do Projeto

```
rhparatodos-completo/
├── backend/                    # Spring Boot (Java 21)
│   ├── src/main/java/sistema/rhparatodos/
│   │   ├── config/            # Configurações (Security, CORS)
│   │   ├── controller/        # REST Controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # Entidades JPA (Usuario, Perfil)
│   │   ├── repository/        # Repositórios JPA
│   │   ├── security/          # JWT Service, Filter
│   │   └── service/           # Serviços (AuthService)
│   ├── src/main/resources/
│   │   ├── application.yml    # Configurações
│   │   └── db/migration/      # Flyway migrations
│   └── pom.xml
│
└── front/                      # Frontend (HTML/CSS/JS)
    ├── index.html             # Página de login
    ├── dashboard.html         # Dashboard principal
    └── public/
        ├── scripts/
        │   ├── auth.js        # Autenticação
        │   ├── login.js       # Lógica do login
        │   └── dashboard.js   # Lógica do dashboard
        └── styles/
            ├── global.css     # Estilos globais
            ├── login.css      # Estilos do login
            └── dashboard.css  # Estilos do dashboard
```

---

## 📡 API Endpoints

### Autenticação
```
POST /api/v1/auth/login     # Login (email, password, profile)
POST /api/v1/auth/logout    # Logout
POST /api/v1/auth/refresh   # Renovar token (a implementar)
GET  /api/v1/auth/validate  # Validar token
GET  /api/v1/auth/health    # Health check
```

### Exemplo de Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@rhparatodos.com.br",
    "password": "admin123",
    "profile": "admin"
  }'
```

---

## ⚠️ Importante

1. **Flyway**: O sistema usa Flyway para migrações. Se você já tem dados, ele não vai sobrescrever.

2. **CORS**: Configurado para aceitar requisições de localhost:5500 e localhost:3000.

3. **JWT**: Token expira em 24 horas.

4. **Modo Offline**: Se o backend não estiver disponível, o frontend funciona em modo simulado.

---

## 🐛 Problemas Comuns

### "Flyway: Validate failed"
O Flyway está tentando validar o schema. Se você já tem o banco criado, você pode:
1. Desabilitar Flyway no application.yml: `spring.flyway.enabled: false`
2. Ou executar manualmente os scripts SQL de insert

### "CORS error"
Verifique se o frontend está rodando na porta 5500.

### "Connection refused"
Verifique se o PostgreSQL está rodando e se as credenciais no application.yml estão corretas.
