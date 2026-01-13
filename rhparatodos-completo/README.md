# RH Para Todos - Sistema de Gestão de RH

Sistema completo de gestão de Recursos Humanos e Departamento Pessoal com frontend em HTML/CSS/JS e backend em Spring Boot.

## 🚀 Como Executar

### Pré-requisitos

- **Java 21** ou superior
- **PostgreSQL 15** ou superior
- **Maven 3.9+** (ou use o wrapper `./mvnw`)
- **Live Server** (extensão do VS Code) ou qualquer servidor HTTP para o frontend

### 1. Configurar o Banco de Dados

```bash
# Conectar ao PostgreSQL
psql -U postgres

# Criar o banco de dados
CREATE DATABASE rhparatodos;

# Sair do psql
\q
```

### 2. Executar o Backend (Spring Boot)

```bash
# Entrar na pasta do backend
cd backend

# Executar com Maven
./mvnw spring-boot:run

# Ou, se tiver Maven instalado globalmente
mvn spring-boot:run
```

O backend estará disponível em: `http://localhost:8080`

### 3. Executar o Frontend

**Opção A: Com Live Server (VS Code)**
1. Abra a pasta `front` no VS Code
2. Clique com botão direito no `index.html`
3. Selecione "Open with Live Server"

**Opção B: Com Python**
```bash
cd front
python -m http.server 5500
```

**Opção C: Com Node.js**
```bash
cd front
npx serve -p 5500
```

O frontend estará disponível em: `http://localhost:5500` ou `http://127.0.0.1:5500`

---

## 👤 Usuários de Teste

Após a primeira execução, os seguintes usuários estarão disponíveis:

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `admin` | `admin123` | Administrador do Sistema |
| `maria.costa` | `admin123` | Chefe de RH |
| `joao.silva` | `admin123` | Assistente de RH |
| `carlos.santos` | `admin123` | Chefe do DP |
| `ana.oliveira` | `admin123` | Assistente do DP |

---

## 🏗️ Estrutura do Projeto

```
rhparatodos-completo/
├── frontend/                    # Interface do usuário
│   ├── index.html              # Página de login
│   ├── dashboard.html          # Dashboard principal
│   ├── employees.html          # Gestão de funcionários
│   └── public/
│       ├── scripts/
│       │   ├── auth.js         # Autenticação e API client
│       │   ├── login.js        # Lógica do login
│       │   └── dashboard.js    # Lógica do dashboard
│       └── styles/
│           └── *.css           # Estilos
│
└── backend/                     # API Spring Boot
    ├── pom.xml                 # Dependências Maven
    └── src/main/
        ├── java/sistema/rhparatodos/
        │   ├── config/         # Configurações (Security, CORS)
        │   ├── controller/     # Endpoints REST
        │   ├── dto/            # Data Transfer Objects
        │   ├── entity/         # Entidades JPA
        │   ├── repository/     # Repositórios
        │   ├── security/       # JWT e Filtros
        │   └── service/        # Lógica de negócio
        └── resources/
            ├── application.yml # Configurações
            └── db/migration/   # Migrações Flyway
```

---

## 🔌 Endpoints da API

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth/login` | Realizar login |
| POST | `/api/v1/auth/logout` | Realizar logout |
| GET | `/api/v1/auth/validate` | Validar token |
| GET | `/api/v1/auth/health` | Health check |

### Exemplo de Login

**Request:**
```json
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "profile": "admin"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1...",
  "refreshToken": "eyJhbGciOiJIUzI1...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@empresa.com",
    "profile": "admin",
    "profileName": "Administrador do Sistema",
    "permissions": ["all"],
    "loginTime": "2026-01-12T10:30:00"
  }
}
```

---

## ⚙️ Configurações

### Alterar porta do backend

Edite `backend/src/main/resources/application.yml`:
```yaml
server:
  port: 8080  # Altere aqui
```

### Alterar URL do backend no frontend

Edite `front/public/scripts/auth.js`:
```javascript
const AUTH_CONFIG = {
  API_BASE_URL: "http://localhost:8080/api/v1",  // Altere aqui
  ...
};
```

### Configurar CORS para produção

Edite `backend/src/main/java/sistema/rhparatodos/config/SecurityConfig.java`:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://seu-dominio.com"  // Adicione seu domínio
));
```

---

## 🐛 Problemas Comuns

### Erro 404 ao acessar dashboard

**Causa:** O frontend está tentando acessar uma rota que não existe.

**Solução:** Certifique-se de que está acessando `dashboard.html` e não `/admin/dashboard`.

### Erro de CORS

**Causa:** O backend não está permitindo requisições do frontend.

**Solução:** 
1. Verifique se o backend está rodando
2. Verifique se a origem do frontend está na lista de origens permitidas

### Erro "require is not defined"

**Causa:** O arquivo JavaScript está usando sintaxe Node.js (require) em um navegador.

**Solução:** Use os arquivos corrigidos deste pacote que usam variáveis globais ao invés de require.

### Erro de autenticação

**Causa:** Token inválido ou expirado.

**Solução:** Faça logout e login novamente.

---

## 📝 Próximos Passos

1. **Implementar outros endpoints** (funcionários, folha de pagamento, etc.)
2. **Adicionar testes** automatizados
3. **Configurar Docker** para facilitar deploy
4. **Implementar auditoria** de ações
5. **Adicionar 2FA** (autenticação em dois fatores)

---

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais e comerciais.
