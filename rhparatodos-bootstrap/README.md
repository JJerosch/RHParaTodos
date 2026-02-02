# RH Para Todos - Frontend Bootstrap + Thymeleaf

Sistema completo de Gestão de Recursos Humanos com Bootstrap 5 e Thymeleaf, pronto para integração com Spring Boot.

## 📊 Estatísticas do Projeto

- **24 templates HTML** totalizando ~9.000 linhas
- **1.213 linhas de CSS** customizado
- **677 linhas de JavaScript** utilitário
- **100% responsivo** para desktop, tablet e mobile

## 📁 Estrutura Completa

```
rhparatodos-bootstrap/
├── templates/
│   ├── fragments/              # Componentes reutilizáveis
│   │   ├── head.html           # Meta tags, CSS imports
│   │   ├── sidebar.html        # Menu lateral com permissões
│   │   ├── header.html         # Topbar, breadcrumb, alertas
│   │   └── scripts.html        # JS imports, modais, paginação
│   │
│   ├── auth/
│   │   └── login.html          # Tela de login com branding
│   │
│   ├── employees/              # Gestão de Funcionários
│   │   ├── list.html           # Lista com filtros e bulk actions
│   │   ├── form.html           # Cadastro/edição (5 abas)
│   │   └── view.html           # Visualização detalhada
│   │
│   ├── departments/
│   │   └── list.html           # Grid de departamentos
│   │
│   ├── positions/              # Cargos e Salários
│   │   ├── index.html          # Gestão de cargos
│   │   └── list.html           # Lista com faixas salariais
│   │
│   ├── recruitment/
│   │   └── index.html          # Vagas, candidatos, entrevistas
│   │
│   ├── onboarding/
│   │   └── index.html          # Integração, checklist, follow-ups
│   │
│   ├── training/
│   │   └── index.html          # Catálogo, trilhas, inscrições
│   │
│   ├── evaluation/
│   │   └── index.html          # Ciclos, PDIs, 9-box
│   │
│   ├── payroll/
│   │   └── index.html          # Folha, holerites, encargos
│   │
│   ├── benefits/
│   │   └── index.html          # Benefícios, adesões, fornecedores
│   │
│   ├── vacation/
│   │   └── index.html          # Solicitações, aprovações, calendário
│   │
│   ├── timesheet/
│   │   └── index.html          # Ponto, justificativas, banco de horas
│   │
│   ├── endomarketing/
│   │   └── index.html          # Brindes, clima, eventos
│   │
│   ├── turnover/
│   │   └── index.html          # Análise de rotatividade
│   │
│   ├── reports/
│   │   └── index.html          # Relatórios e KPIs
│   │
│   ├── settings/
│   │   └── index.html          # Usuários, perfis, LGPD, auditoria
│   │
│   └── dashboard.html          # Dashboard executivo
│
└── static/
    ├── css/custom.css          # 1.213 linhas de CSS customizado
    └── js/app.js               # 677 linhas de utilitários JS
```

## 🎨 Paleta de Cores

```css
--rh-primary: #2c3e50      /* Azul escuro - cor principal */
--rh-secondary: #34495e    /* Cinza azulado */
--rh-success: #27ae60      /* Verde */
--rh-warning: #f39c12      /* Laranja */
--rh-danger: #e74c3c       /* Vermelho */
--rh-info: #3498db         /* Azul claro */
```

## 🔧 Tecnologias Utilizadas

- **Bootstrap 5.3.2** - Framework CSS
- **Bootstrap Icons 1.11.3** - Ícones
- **Chart.js 4.4.1** - Gráficos
- **Google Fonts (Inter)** - Tipografia
- **Thymeleaf** - Template engine Spring Boot

## 📦 CDN Links (já incluídos em head.html)

```html
<!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

<!-- Bootstrap Icons -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
```

## 🔐 Perfis de Acesso

| Perfil | Código | Permissões |
|--------|--------|------------|
| Administrador | `ADMIN` | Acesso total ao sistema |
| Chefe de RH | `RH_CHEFE` | Gestão de pessoas, recrutamento, avaliações |
| Assistente de RH | `RH_ASSISTENTE` | Cadastros, triagem, onboarding |
| Chefe de DP | `DP_CHEFE` | Folha, férias, benefícios, rescisões |
| Assistente de DP | `DP_ASSISTENTE` | Lançamentos, holerites, conferências |

## 🧩 Fragmentos Thymeleaf

### head.html
```html
<head th:replace="~{fragments/head :: head('Título da Página')}"></head>
```

### sidebar.html
```html
<div th:replace="~{fragments/sidebar :: sidebar}"></div>
```
- Usa `th:classappend` para item ativo
- Usa `sec:authorize` para permissões
- Variável esperada: `${menu}` (ex: 'funcionarios', 'dashboard')

### header.html
```html
<div th:replace="~{fragments/header :: header(${breadcrumbs})}"></div>
<div th:replace="~{fragments/header :: alerts}"></div>
```
- Breadcrumbs: `List<Map<String, String>>` com `label` e `url`
- Alerts: `${mensagemSucesso}`, `${mensagemErro}`, `${mensagemAviso}`, `${mensagemInfo}`

### scripts.html
```html
<div th:replace="~{fragments/scripts :: scripts}"></div>
<div th:replace="~{fragments/scripts :: pagination(${page}, '/url-base')}"></div>
<div th:replace="~{fragments/scripts :: modal-delete}"></div>
<div th:replace="~{fragments/scripts :: empty-state('bi-icon', 'Título', 'Mensagem')}"></div>
```

## 📋 Variáveis Thymeleaf Esperadas

### Dashboard
```java
model.addAttribute("stats", dashboardStats);           // Estatísticas gerais
model.addAttribute("ultimasContratacoes", lista);      // Últimos funcionários
model.addAttribute("proximosAniversarios", lista);     // Aniversariantes
model.addAttribute("feriasPendentes", lista);          // Férias para aprovar
model.addAttribute("vagasAbertas", lista);             // Vagas em aberto
model.addAttribute("dadosDepartamentos", mapGrafico);  // Dados para gráficos
```

### Lista de Funcionários
```java
model.addAttribute("funcionarios", pageResult);        // Page<Funcionario>
model.addAttribute("departamentos", listaDepts);       // Para filtro
model.addAttribute("cargos", listaCargos);             // Para filtro
model.addAttribute("stats", estatisticas);             // Cards de estatísticas
```

### Formulário de Funcionário
```java
model.addAttribute("funcionario", funcionario);        // Entidade (null se novo)
model.addAttribute("departamentos", lista);
model.addAttribute("cargos", lista);
model.addAttribute("gestores", lista);
model.addAttribute("estados", listaUFs);
model.addAttribute("proximaMatricula", "000157");      // Próxima matrícula
```

### Folha de Pagamento
```java
model.addAttribute("folha", listaItens);               // Itens da folha
model.addAttribute("resumo", resumoFolha);             // Totais
model.addAttribute("encargos", encargosPatronais);
model.addAttribute("competencias", listaCompetencias);
model.addAttribute("competenciaAtual", "2026-01");
```

## 🛠️ Funções JavaScript Disponíveis (app.js)

### Máscaras
```javascript
// Automática via data-mask
<input type="text" data-mask="cpf">
<input type="text" data-mask="cnpj">
<input type="text" data-mask="phone">
<input type="text" data-mask="cep">
<input type="text" data-mask="currency">
<input type="text" data-mask="date">

// Manual
Mask.cpf('12345678900')      // 123.456.789-00
Mask.currency('150000')      // R$ 1.500,00
```

### Formatadores
```javascript
Format.currency(1500)         // R$ 1.500,00
Format.date('2026-01-15')     // 15/01/2026
Format.initials('João Silva') // JS
Format.cpf('12345678900')     // 123.456.789-00
```

### Toasts
```javascript
Toast.success('Salvo com sucesso!')
Toast.error('Erro ao salvar')
Toast.warning('Atenção!')
Toast.info('Informação')
```

### Confirmação
```javascript
Confirm.show({
    title: 'Confirmar',
    message: 'Tem certeza?',
    confirmText: 'Sim',
    confirmClass: 'btn-danger',
    onConfirm: () => { /* ação */ }
});

Confirm.delete('Nome do item', () => { /* excluir */ });
```

### Validação
```javascript
Validator.cpf('123.456.789-00')   // true/false
Validator.cnpj('...')
Validator.email('...')
Validator.phone('...')
Validator.required('valor')
```

### Utilitários
```javascript
Utils.debounce(fn, 300)           // Debounce
Utils.serializeForm('formId')     // Serializa formulário
Utils.populateForm('formId', obj) // Preenche formulário
Utils.copyToClipboard('texto')    // Copia para clipboard
Utils.scrollTo('elementId')       // Scroll suave

ViaCEP.fetch('01310100')          // Busca CEP
ViaCEP.autofill('cepInput', { ... }) // Auto-preenche endereço

Loading.show()                    // Mostra loading
Loading.hide()                    // Esconde loading

Sidebar.toggle()                  // Toggle sidebar
```

## 🔌 Integração com Spring Boot

### SecurityConfig
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/funcionarios/**").hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE")
                .requestMatchers("/folha-pagamento/**").hasAnyRole("ADMIN", "DP_CHEFE", "DP_ASSISTENTE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
            );
        return http.build();
    }
}
```

### Controller Exemplo
```java
@Controller
@RequestMapping("/funcionarios")
public class FuncionarioController {

    @GetMapping
    public String listar(Model model, Pageable pageable,
                         @RequestParam(required = false) String busca) {
        model.addAttribute("funcionarios", service.buscar(busca, pageable));
        model.addAttribute("departamentos", deptService.listarAtivos());
        model.addAttribute("menu", "funcionarios");
        model.addAttribute("breadcrumbs", List.of(
            Map.of("label", "Funcionários", "url", "/funcionarios")
        ));
        return "employees/list";
    }
    
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("funcionario", new Funcionario());
        model.addAttribute("proximaMatricula", service.gerarProximaMatricula());
        // ... outros atributos
        return "employees/form";
    }
    
    @PostMapping("/novo")
    public String salvar(@Valid Funcionario funcionario, 
                         RedirectAttributes redirect) {
        service.salvar(funcionario);
        redirect.addFlashAttribute("mensagemSucesso", "Funcionário cadastrado!");
        return "redirect:/funcionarios";
    }
}
```

## 📱 Responsividade

O layout é totalmente responsivo:
- **Desktop (≥992px)**: Sidebar fixa, tabelas completas
- **Tablet (768-991px)**: Sidebar overlay, colunas adaptadas
- **Mobile (<768px)**: Layout simplificado, colunas `hide-mobile` ocultadas

Classes auxiliares:
```html
<td class="hide-mobile">Oculto em mobile</td>
```

## 🚀 Como Usar

1. Copie a pasta `templates` para `src/main/resources/templates`
2. Copie a pasta `static` para `src/main/resources/static`
3. Configure Spring Security com Thymeleaf Extras
4. Implemente os controllers necessários
5. Pronto!

## ✅ Módulos Implementados

| Módulo | Funcionalidades |
|--------|-----------------|
| **Funcionários** | CRUD completo, 5 abas (pessoal, contato, profissional, bancário, dependentes) |
| **Departamentos** | Grid com cards, hierarquia, organograma |
| **Cargos** | Níveis, faixas salariais, CBO, política de remuneração |
| **Recrutamento** | Vagas, candidatos, etapas, entrevistas |
| **Onboarding** | Checklist, buddy, follow-ups 30/60/90 dias |
| **Treinamentos** | Catálogo, trilhas, inscrições, certificados |
| **Avaliações** | Ciclos 180°/360°, PDI, competências, 9-box |
| **Folha** | Cálculos, holerites, encargos, provisões |
| **Benefícios** | Cadastro, adesões, fornecedores, custos |
| **Férias** | Solicitações, aprovações, vencidas, calendário |
| **Ponto** | Marcações, justificativas, banco de horas |
| **Endomarketing** | Brindes, clima, eventos, gamificação |
| **Turnover** | Análise, custos, entrevistas de desligamento |
| **Relatórios** | KPIs, gráficos, exportação PDF/Excel |
| **Configurações** | Usuários, perfis, auditoria, LGPD |

## 📝 Credenciais de Teste

| Email | Senha | Perfil |
|-------|-------|--------|
| admin@rhparatodos.com.br | admin123 | Administrador |
| maria.costa@rhparatodos.com.br | admin123 | RH Chefe |
| carlos.santos@rhparatodos.com.br | admin123 | DP Chefe |

---

© 2026 RH Para Todos. Desenvolvido para o projeto acadêmico.
