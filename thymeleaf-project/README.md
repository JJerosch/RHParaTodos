# Sistema RH - Frontend Thymeleaf

## 📁 Estrutura do Projeto

```
src/main/resources/
├── templates/
│   ├── layout/
│   │   └── base.html              # Layout base com header, sidebar, footer
│   ├── fragments/
│   │   ├── sidebar.html           # Menu lateral de navegação
│   │   ├── topbar.html            # Barra superior com busca e notificações
│   │   ├── components.html        # Componentes reutilizáveis
│   │   └── modals.html            # Modais (confirmação, exclusão, etc.)
│   └── pages/
│       ├── login.html             # Página de login
│       ├── dashboard.html         # Dashboard principal
│       └── funcionarios/
│           ├── lista.html         # Lista de funcionários
│           └── form.html          # Formulário de funcionário
├── static/
│   ├── css/
│   │   ├── variables.css          # Variáveis CSS (cores, espaçamentos)
│   │   ├── base.css               # Reset e estilos base
│   │   ├── layout.css             # Grid e estrutura
│   │   ├── sidebar.css            # Estilos do menu lateral
│   │   ├── topbar.css             # Estilos da barra superior
│   │   ├── components.css         # Botões, badges, alerts
│   │   ├── tables.css             # Tabelas de dados
│   │   ├── forms.css              # Formulários
│   │   ├── modals.css             # Modais e toasts
│   │   └── cards.css              # Cards e painéis
│   ├── js/
│   │   ├── utils.js               # Funções utilitárias
│   │   ├── sidebar.js             # Controle do menu lateral
│   │   ├── topbar.js              # Dropdowns e busca
│   │   ├── modals.js              # Gerenciamento de modais
│   │   ├── toast.js               # Notificações toast
│   │   ├── tables.js              # Ordenação e filtros
│   │   └── forms.js               # Validação e máscaras
│   └── images/
│       └── icons/                 # ⚠️ ADICIONAR ÍCONES AQUI
└── application.properties
```

---

## 🎨 LISTA DE ÍCONES NECESSÁRIOS

Todos os ícones devem ser colocados em: `/src/main/resources/static/images/icons/`

### Navegação Principal
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `logo.png` | Logo do sistema | Sidebar, Login |
| `dashboard.png` | Ícone do dashboard | Sidebar |
| `employees.png` | Ícone de funcionários | Sidebar |
| `departments.png` | Ícone de departamentos | Sidebar |
| `positions.png` | Ícone de cargos | Sidebar |
| `payroll.png` | Ícone de folha de pagamento | Sidebar |
| `time-clock.png` | Ícone de ponto | Sidebar |
| `vacation.png` | Ícone de férias | Sidebar |
| `reports.png` | Ícone de relatórios | Sidebar |
| `settings.png` | Ícone de configurações | Sidebar |

### Ações
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `plus.png` | Adicionar novo | Botões de criação |
| `edit.png` | Editar item | Tabelas, cards |
| `trash.png` | Excluir item | Tabelas, modais |
| `eye.png` | Visualizar | Tabelas, senha |
| `eye-off.png` | Ocultar | Toggle de senha |
| `save.png` | Salvar | Formulários |
| `download.png` | Download/Exportar | Relatórios |
| `upload.png` | Upload | Formulários |
| `search.png` | Buscar | Topbar, tabelas |
| `filter.png` | Filtrar | Tabelas |
| `refresh.png` | Atualizar | Botões |
| `print.png` | Imprimir | Relatórios |
| `copy.png` | Copiar | Ações |

### Setas e Navegação
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `chevron-down.png` | Seta para baixo | Dropdowns, submenus |
| `chevron-up.png` | Seta para cima | Ordenação |
| `chevron-right.png` | Seta para direita | Breadcrumb, submenus |
| `chevron-left.png` | Seta para esquerda | Paginação |
| `arrow-right.png` | Seta direita | Links |
| `arrow-left.png` | Seta esquerda | Voltar |
| `arrow-up.png` | Seta cima | Tendência positiva |
| `arrow-down.png` | Seta baixo | Tendência negativa |
| `x.png` | Fechar (X) | Modais, alertas |
| `menu.png` | Menu hamburguer | Mobile |

### Status e Feedback
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `check.png` | Check simples | Checkboxes |
| `check-circle.png` | Sucesso | Alerts, toasts |
| `x-circle.png` | Erro | Alerts, toasts |
| `alert-circle.png` | Alerta | Alerts, validação |
| `alert-triangle.png` | Aviso | Modais de confirmação |
| `info.png` | Informação | Alerts, tooltips |
| `loader.png` | Carregando | Loading states |

### Usuário e Pessoas
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `user.png` | Usuário genérico | Perfil, listas |
| `users.png` | Múltiplos usuários | Equipes |
| `user-plus.png` | Adicionar usuário | Cadastro |
| `user-check.png` | Usuário verificado | Status |
| `user-x.png` | Usuário removido | Status |
| `default-avatar.png` | Avatar padrão | Quando sem foto |

### Comunicação
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `mail.png` | E-mail | Formulários, contato |
| `phone.png` | Telefone | Formulários, contato |
| `bell.png` | Notificação | Topbar |
| `message-circle.png` | Mensagem | Topbar |
| `send.png` | Enviar | Formulários |

### Data e Tempo
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `calendar.png` | Calendário | Datas, férias |
| `clock.png` | Relógio | Ponto, horários |
| `history.png` | Histórico | Logs, atividades |

### Localização e Organização
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `building.png` | Empresa/Prédio | Departamentos |
| `briefcase.png` | Trabalho/Cargo | Cargos |
| `map-pin.png` | Localização | Endereço |
| `home.png` | Casa/Início | Dashboard |
| `folder.png` | Pasta | Documentos |
| `file.png` | Arquivo | Documentos |
| `file-text.png` | Documento | Relatórios |

### Financeiro
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `dollar-sign.png` | Dinheiro | Salário, pagamentos |
| `credit-card.png` | Cartão | Dados bancários |
| `trending-up.png` | Tendência alta | Gráficos |
| `trending-down.png` | Tendência baixa | Gráficos |
| `bar-chart.png` | Gráfico barras | Dashboard |
| `pie-chart.png` | Gráfico pizza | Dashboard |

### Sistema
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `lock.png` | Cadeado/Senha | Login, segurança |
| `unlock.png` | Desbloqueado | Status |
| `key.png` | Chave | Permissões |
| `shield.png` | Escudo | Segurança |
| `log-out.png` | Sair | Sidebar, menu |
| `login.png` | Entrar | Login |
| `external-link.png` | Link externo | Links |
| `more-horizontal.png` | Mais opções (...) | Menus |
| `more-vertical.png` | Mais opções (⋮) | Menus |
| `sun.png` | Tema claro | Toggle tema |
| `moon.png` | Tema escuro | Toggle tema |
| `help-circle.png` | Ajuda | Suporte |
| `image.png` | Imagem | Upload de fotos |
| `camera.png` | Câmera | Foto de perfil |

### Específicos do RH
| Arquivo | Descrição | Usado em |
|---------|-----------|----------|
| `id-card.png` | Crachá/ID | CPF, documentos |
| `award.png` | Prêmio | Reconhecimento |
| `gift.png` | Presente | Aniversários |
| `cake.png` | Bolo | Aniversários |
| `graduation-cap.png` | Formação | Educação |
| `heart.png` | Benefícios | Plano de saúde |
| `activity.png` | Atividade | Logs |

---

## 📐 Especificações dos Ícones

### Tamanhos Recomendados
- **Padrão**: 24x24 pixels
- **Mínimo**: 16x16 pixels (ícones pequenos)
- **Máximo**: 48x48 pixels (ícones grandes)

### Formato
- **Preferido**: PNG com transparência
- **Alternativo**: SVG (converter para PNG)
- **Cor**: Monocromático (cinza escuro #374151)

### Classes CSS para Tamanhos
```css
.icon      { width: 20px; height: 20px; }  /* Padrão */
.icon-xs   { width: 12px; height: 12px; }  /* Extra pequeno */
.icon-sm   { width: 16px; height: 16px; }  /* Pequeno */
.icon-lg   { width: 24px; height: 24px; }  /* Grande */
.icon-xl   { width: 32px; height: 32px; }  /* Extra grande */
.icon-2xl  { width: 48px; height: 48px; }  /* 2x grande */
```

---

## 🔗 Onde Obter Ícones

### Opções Gratuitas
1. **Lucide Icons** (Recomendado)
   - https://lucide.dev/icons
   - Exportar como PNG 24x24
   - Estilo consistente e moderno

2. **Heroicons**
   - https://heroicons.com/
   - Usar versão "outline"

3. **Feather Icons**
   - https://feathericons.com/
   - Minimalista e limpo

4. **Tabler Icons**
   - https://tabler-icons.io/
   - Grande variedade

### Opções Pagas
1. **Noun Project** - https://thenounproject.com/
2. **Flaticon** - https://www.flaticon.com/
3. **Iconfinder** - https://www.iconfinder.com/

---

## 🚀 Como Usar

### 1. Baixar os Ícones
Escolha uma fonte de ícones e baixe todos os arquivos listados acima.

### 2. Colocar na Pasta Correta
```
src/main/resources/static/images/icons/
├── logo.png
├── dashboard.png
├── employees.png
├── ... (todos os outros)
```

### 3. Uso no Thymeleaf
```html
<!-- Ícone simples -->
<img th:src="@{/images/icons/dashboard.png}" alt="" class="icon">

<!-- Ícone com tamanho específico -->
<img th:src="@{/images/icons/users.png}" alt="" class="icon icon-lg">

<!-- Ícone em botão -->
<button class="btn btn--primary">
    <img th:src="@{/images/icons/plus.png}" alt="" class="icon btn-icon">
    <span>Novo Funcionário</span>
</button>
```

---

## ⚙️ Configuração Spring Boot

### application.properties
```properties
# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Static Resources
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.cache.period=0

# Encoding
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
```

### Dependências Maven (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

---

## 📝 Checklist de Implementação

- [ ] Baixar todos os ícones listados
- [ ] Colocar ícones em `/static/images/icons/`
- [ ] Configurar Spring Boot (application.properties)
- [ ] Criar controllers para cada página
- [ ] Configurar Spring Security
- [ ] Testar todas as páginas
- [ ] Ajustar tema dark mode (opcional)

---

## 🎨 Personalização de Cores

Edite o arquivo `variables.css` para alterar as cores do sistema:

```css
:root {
    /* Cor Principal */
    --color-primary-500: #3b82f6;  /* Azul padrão */
    --color-primary-600: #2563eb;
    --color-primary-700: #1d4ed8;
    
    /* Altere para sua cor preferida */
    /* Exemplo: Verde */
    /* --color-primary-500: #22c55e; */
    /* --color-primary-600: #16a34a; */
    /* --color-primary-700: #15803d; */
}
```
