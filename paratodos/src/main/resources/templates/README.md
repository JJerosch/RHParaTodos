# RH Para Todos - Frontend

Sistema completo de Recursos Humanos desenvolvido com HTML, CSS e JavaScript puro.

## 📁 Estrutura do Projeto

```
front/
├── index.html              # Página de login
├── dashboard.html          # Dashboard principal
├── employees.html          # Gestão de funcionários
├── departments.html        # Gestão de departamentos
├── positions.html          # Gestão de cargos
├── recruitment.html        # Recrutamento e seleção
├── training.html           # Treinamentos
├── performance.html        # Avaliações de desempenho
├── payroll.html            # Folha de pagamento
├── benefits.html           # Benefícios
├── vacation.html           # Férias
├── timesheet.html          # Controle de ponto
├── reports.html            # Relatórios
├── settings.html           # Configurações
└── public/
    ├── styles/
    │   ├── global.css      # Variáveis e estilos globais
    │   ├── components.css  # Componentes reutilizáveis
    │   ├── dashboard.css   # Estilos do layout
    │   └── login.css       # Estilos da página de login
    └── scripts/
        ├── auth.js         # Autenticação e API client
        ├── components.js   # Componentes JS (Toast, Modal, etc)
        ├── employees.js    # Lógica da página de funcionários
        ├── dashboard.js    # Lógica do dashboard
        └── login.js        # Lógica do login
```

## 🚀 Como Executar

### Opção 1: Python
```bash
cd front
python -m http.server 5500
```

### Opção 2: Node.js
```bash
npx serve front -l 5500
```

### Opção 3: VS Code
Use a extensão "Live Server" e abra o `index.html`

Acesse: **http://localhost:5500**

## 🔐 Credenciais de Teste

| Email | Senha | Perfil |
|-------|-------|--------|
| admin@rhparatodos.com.br | admin123 | Administrador do Sistema |
| maria.costa@rhparatodos.com.br | admin123 | RH - Chefe |
| joao.silva@rhparatodos.com.br | admin123 | RH - Assistente |
| carlos.santos@rhparatodos.com.br | admin123 | DP - Chefe |
| ana.oliveira@rhparatodos.com.br | admin123 | DP - Assistente |

## 🎨 Paleta de Cores

```css
--primary-dark: #2c3e50     /* Azul escuro */
--primary-gray: #34495e     /* Cinza azulado */
--light-gray: #95a5a6       /* Cinza claro */
--success-green: #27ae60    /* Verde */
--warning-orange: #f39c12   /* Laranja */
--error-red: #e74c3c        /* Vermelho */
```

## 📱 Funcionalidades

### Gestão de Pessoas
- ✅ CRUD completo de funcionários
- ✅ Gestão de departamentos
- ✅ Gestão de cargos
- ✅ Filtros e busca avançada

### Recursos Humanos
- ✅ Recrutamento e seleção
- ✅ Treinamentos
- ✅ Avaliações de desempenho

### Departamento Pessoal
- ✅ Folha de pagamento
- ✅ Gestão de benefícios
- ✅ Controle de férias
- ✅ Registro de ponto

### Administração
- ✅ Relatórios gerenciais
- ✅ Configurações do sistema
- ✅ Gestão de usuários

## 🔗 Integração com Backend

O frontend está preparado para integrar com o backend Spring Boot via API REST.

Configuração em `public/scripts/auth.js`:
```javascript
const AUTH_CONFIG = {
  API_BASE_URL: "http://localhost:8080/api/v1"
}
```

Quando o backend não está disponível, o sistema funciona em modo simulado com dados mock.

## 📦 Componentes JS Disponíveis

### Toast (Notificações)
```javascript
Toast.success('Mensagem de sucesso');
Toast.error('Mensagem de erro');
Toast.warning('Mensagem de aviso');
Toast.info('Mensagem informativa');
```

### Modal
```javascript
Modal.open('modalId');
Modal.close('modalId');
```

### Confirmação
```javascript
Confirm.show('Tem certeza?', () => {
  // Ação confirmada
});
```

### Formatadores
```javascript
Format.currency(1000);      // R$ 1.000,00
Format.date('2026-01-13');  // 13/01/2026
Format.cpf('12345678900');  // 123.456.789-00
Format.phone('11999998888'); // (11) 99999-8888
```

### Máscaras de Input
```html
<input type="text" data-mask="cpf">
<input type="text" data-mask="phone">
<input type="text" data-mask="cep">
<input type="text" data-mask="currency">
```

## 📄 Licença

MIT License
