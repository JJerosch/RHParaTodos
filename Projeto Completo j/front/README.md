# Sistema de RH e Departamento Pessoal

Sistema completo de gestão de Recursos Humanos e Departamento Pessoal, desenvolvido com HTML, CSS e JavaScript, totalmente compatível com Spring Boot.

## Características

### Design
- **Paleta de Cores**: Branco como cor principal e azul (#0066CC) como secundária
- **Interface Profissional**: Design corporativo, limpo e minimalista
- **Responsivo**: Compatível com desktop, tablet e mobile

### Tecnologias
- **Frontend**: HTML5, CSS3 (Custom Properties), JavaScript (ES6+)
- **Backend**: Preparado para integração com Spring Boot via API REST
- **Autenticação**: Sistema de login com JWT tokens

### Módulos Implementados

#### 1. Autenticação e Segurança
- Login com suporte a 5 perfis de acesso
- Autenticação 2FA
- Logs de auditoria
- Conformidade com LGPD

#### 2. Gestão de Pessoas
- Cadastro completo de funcionários
- Organograma corporativo
- Dados pessoais, profissionais e documentos
- Contatos de emergência

#### 3. Recursos Humanos
- **Recrutamento e Seleção**: Gestão de vagas e candidatos
- **Avaliação de Desempenho**: Ciclos de avaliação, autoavaliação, 360°
- **Treinamentos**: Catálogo de cursos, inscrições, certificados

#### 4. Departamento Pessoal
- **Folha de Pagamento**: Cálculo completo, descontos, holerites
- **Gestão de Férias**: Controle de períodos, solicitações, aprovações
- **Controle de Ponto**: Marcações, banco de horas, justificativas

#### 5. Administração
- **Relatórios**: Headcount, turnover, custos, análises financeiras
- **Configurações**: Usuários, permissões, integrações
- **Dashboard**: Visão executiva com KPIs

## Perfis de Acesso

1. **Administrador do Sistema (RH e DP)**: Acesso total ao sistema
2. **Chefe de Recursos Humanos**: Gestão estratégica de RH
3. **Assistente de Recursos Humanos**: Execução operacional de RH
4. **Chefe do Departamento de Pagamentos**: Gestão da folha e finanças
5. **Assistente do Departamento de Pagamentos**: Suporte operacional ao DP

## Integração com Spring Boot

O sistema está preparado para integração completa com backend Spring Boot:

### Endpoints API REST
```
/api/v1/auth/login          - Autenticação
/api/v1/employees           - Gestão de funcionários
/api/v1/recruitment         - Recrutamento
/api/v1/performance         - Avaliações
/api/v1/training            - Treinamentos
/api/v1/payroll             - Folha de pagamento
/api/v1/vacation            - Férias
/api/v1/timesheet           - Ponto
/api/v1/reports             - Relatórios
```

### Autenticação
- Bearer Token (JWT)
- Refresh Token
- Validação de sessão

### Segurança
- Criptografia de senhas
- HTTPS obrigatório
- Rate limiting
- Proteção contra CSRF

## Como Usar

### Desenvolvimento Local
1. Abra o arquivo `index.html` em um navegador
2. Use as credenciais de teste (disponíveis com `?test=1` na URL)
3. Explore os diferentes módulos do sistema

### Integração com Spring Boot
1. Configure o `API_BASE_URL` em `public/scripts/auth.js`
2. Implemente os controllers REST no backend Spring Boot
3. Configure CORS para permitir requisições do frontend
4. Implemente autenticação JWT no Spring Security

## Estrutura de Arquivos

```
/
├── index.html                      # Página de login
├── dashboard.html                  # Dashboard principal
├── employees.html                  # Gestão de funcionários
├── organizational-chart.html       # Organograma
├── recruitment.html                # Recrutamento
├── performance.html                # Avaliações
├── training.html                   # Treinamentos
├── payroll.html                    # Folha de pagamento
├── vacation.html                   # Férias
├── timesheet.html                  # Ponto
├── reports.html                    # Relatórios
├── settings.html                   # Configurações
└── public/
    ├── styles/
    │   ├── global.css              # Estilos globais e variáveis
    │   ├── login.css               # Estilos do login
    │   ├── dashboard.css           # Estilos do layout principal
    │   ├── employees.css           # Estilos de funcionários
    │   ├── orgchart.css            # Estilos do organograma
    │   ├── recruitment.css         # Estilos de recrutamento
    │   ├── performance.css         # Estilos de avaliação
    │   ├── training.css            # Estilos de treinamentos
    │   ├── payroll.css             # Estilos da folha
    │   ├── reports.css             # Estilos de relatórios
    │   └── settings.css            # Estilos de configurações
    └── scripts/
        ├── auth.js                 # Autenticação e API client
        ├── login.js                # Lógica do login
        ├── dashboard.js            # Lógica do dashboard
        ├── employees.js            # Lógica de funcionários
        ├── recruitment.js          # Lógica de recrutamento
        └── settings.js             # Lógica de configurações
```

## Próximos Passos

Para colocar em produção:
1. Implementar backend Spring Boot com todos os endpoints
2. Configurar banco de dados (PostgreSQL/MySQL)
3. Implementar segurança com Spring Security
4. Configurar servidor de aplicação (Tomcat/Undertow)
5. Deploy em cloud provider (AWS/Azure/GCP)

## Licença

Sistema desenvolvido para fins educacionais e comerciais.
