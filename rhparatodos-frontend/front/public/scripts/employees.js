// ==========================================
// PÁGINA DE FUNCIONÁRIOS
// ==========================================

// Estado da página
let employees = [];
let filteredEmployees = [];
let currentPage = 1;
let perPage = 10;
let currentEmployeeId = null;

// Dados mock para desenvolvimento
const mockEmployees = [
  {
    id: 1,
    matricula: "001234",
    nome_completo: "João Silva Santos",
    cpf: "123.456.789-00",
    email_corporativo: "joao.santos@empresa.com.br",
    telefone: "(11) 98765-4321",
    departamento: "Tecnologia",
    departamento_id: 1,
    cargo: "Desenvolvedor Senior",
    cargo_id: 2,
    data_admissao: "2020-03-15",
    status: "ATIVO",
    salario_atual: 12500.00,
    tipo_contrato: "CLT"
  },
  {
    id: 2,
    matricula: "001235",
    nome_completo: "Maria Oliveira Costa",
    cpf: "987.654.321-00",
    email_corporativo: "maria.costa@empresa.com.br",
    telefone: "(11) 91234-5678",
    departamento: "Recursos Humanos",
    departamento_id: 2,
    cargo: "Coordenadora de RH",
    cargo_id: 3,
    data_admissao: "2019-08-01",
    status: "ATIVO",
    salario_atual: 9800.00,
    tipo_contrato: "CLT"
  },
  {
    id: 3,
    matricula: "001236",
    nome_completo: "Pedro Henrique Almeida",
    cpf: "456.789.123-00",
    email_corporativo: "pedro.almeida@empresa.com.br",
    telefone: "(21) 99876-5432",
    departamento: "Financeiro",
    departamento_id: 3,
    cargo: "Analista Financeiro",
    cargo_id: 1,
    data_admissao: "2021-02-10",
    status: "FERIAS",
    salario_atual: 6500.00,
    tipo_contrato: "CLT"
  },
  {
    id: 4,
    matricula: "001237",
    nome_completo: "Ana Paula Ferreira",
    cpf: "321.654.987-00",
    email_corporativo: "ana.ferreira@empresa.com.br",
    telefone: "(11) 97654-3210",
    departamento: "Comercial",
    departamento_id: 4,
    cargo: "Gerente Comercial",
    cargo_id: 4,
    data_admissao: "2018-11-20",
    status: "ATIVO",
    salario_atual: 15000.00,
    tipo_contrato: "CLT"
  },
  {
    id: 5,
    matricula: "001238",
    nome_completo: "Carlos Eduardo Lima",
    cpf: "789.123.456-00",
    email_corporativo: "carlos.lima@empresa.com.br",
    telefone: "(31) 98765-1234",
    departamento: "Operações",
    departamento_id: 5,
    cargo: "Analista de Operações",
    cargo_id: 1,
    data_admissao: "2022-06-05",
    status: "ATIVO",
    salario_atual: 5200.00,
    tipo_contrato: "CLT"
  },
  {
    id: 6,
    matricula: "001239",
    nome_completo: "Juliana Santos Ribeiro",
    cpf: "654.987.321-00",
    email_corporativo: "juliana.ribeiro@empresa.com.br",
    telefone: "(11) 96543-2109",
    departamento: "Tecnologia",
    departamento_id: 1,
    cargo: "Desenvolvedora Pleno",
    cargo_id: 2,
    data_admissao: "2021-09-15",
    status: "ATIVO",
    salario_atual: 8500.00,
    tipo_contrato: "CLT"
  },
  {
    id: 7,
    matricula: "001240",
    nome_completo: "Roberto Martins Souza",
    cpf: "147.258.369-00",
    email_corporativo: "roberto.souza@empresa.com.br",
    telefone: "(21) 95432-1098",
    departamento: "Financeiro",
    departamento_id: 3,
    cargo: "Diretor Financeiro",
    cargo_id: 5,
    data_admissao: "2017-03-01",
    status: "ATIVO",
    salario_atual: 25000.00,
    tipo_contrato: "CLT"
  },
  {
    id: 8,
    matricula: "001241",
    nome_completo: "Fernanda Gomes Pereira",
    cpf: "369.147.258-00",
    email_corporativo: "fernanda.pereira@empresa.com.br",
    telefone: "(11) 94321-0987",
    departamento: "Recursos Humanos",
    departamento_id: 2,
    cargo: "Assistente de RH",
    cargo_id: 1,
    data_admissao: "2023-01-10",
    status: "ATIVO",
    salario_atual: 3800.00,
    tipo_contrato: "CLT"
  },
  {
    id: 9,
    matricula: "001242",
    nome_completo: "Marcos Antônio Silva",
    cpf: "258.369.147-00",
    email_corporativo: "marcos.silva@empresa.com.br",
    telefone: "(41) 93210-9876",
    departamento: "Comercial",
    departamento_id: 4,
    cargo: "Vendedor",
    cargo_id: 1,
    data_admissao: "2022-04-18",
    status: "AFASTADO",
    salario_atual: 4500.00,
    tipo_contrato: "CLT"
  },
  {
    id: 10,
    matricula: "001243",
    nome_completo: "Patricia Reis Mendes",
    cpf: "951.753.486-00",
    email_corporativo: "patricia.mendes@empresa.com.br",
    telefone: "(11) 92109-8765",
    departamento: "Tecnologia",
    departamento_id: 1,
    cargo: "Tech Lead",
    cargo_id: 3,
    data_admissao: "2019-05-22",
    status: "ATIVO",
    salario_atual: 18000.00,
    tipo_contrato: "CLT"
  },
  {
    id: 11,
    matricula: "001244",
    nome_completo: "Lucas Oliveira Nunes",
    cpf: "753.159.486-00",
    email_corporativo: "lucas.nunes@empresa.com.br",
    telefone: "(51) 91098-7654",
    departamento: "Operações",
    departamento_id: 5,
    cargo: "Coordenador de Operações",
    cargo_id: 3,
    data_admissao: "2020-07-14",
    status: "ATIVO",
    salario_atual: 9200.00,
    tipo_contrato: "CLT"
  },
  {
    id: 12,
    matricula: "001245",
    nome_completo: "Camila Rodrigues Dias",
    cpf: "159.753.486-00",
    email_corporativo: "camila.dias@empresa.com.br",
    telefone: "(11) 90987-6543",
    departamento: "Financeiro",
    departamento_id: 3,
    cargo: "Analista Contábil",
    cargo_id: 1,
    data_admissao: "2021-11-08",
    status: "ATIVO",
    salario_atual: 5800.00,
    tipo_contrato: "CLT"
  }
];

// Inicialização
document.addEventListener("DOMContentLoaded", () => {
  // Verificar autenticação
  if (!Auth.requireAuth()) return;
  
  // Inicializar interface
  initUserMenu();
  initSidebar();
  loadEmployees();
  initTabs();
  Mask.initAll();
});

// Inicializar menu do usuário
function initUserMenu() {
  const user = Auth.getCurrentUser();
  if (user) {
    document.getElementById("userName").textContent = user.email?.split("@")[0] || user.username;
    document.getElementById("userFullName").textContent = user.email || user.username;
    document.getElementById("userProfile").textContent = user.profileName || user.profile;
    document.getElementById("userAvatar").textContent = (user.email || user.username)[0].toUpperCase();
  }

  // Toggle dropdown
  document.getElementById("userMenuButton").addEventListener("click", () => {
    document.getElementById("userDropdown").classList.toggle("active");
  });

  // Logout
  document.getElementById("logoutButton").addEventListener("click", (e) => {
    e.preventDefault();
    Auth.logout();
  });

  // Close dropdown on click outside
  document.addEventListener("click", (e) => {
    if (!e.target.closest(".user-menu")) {
      document.getElementById("userDropdown").classList.remove("active");
    }
  });
}

// Inicializar sidebar
function initSidebar() {
  const sidebar = document.getElementById("sidebar");
  const toggleBtn = document.getElementById("sidebarToggle");
  const mobileBtn = document.getElementById("mobileMenuBtn");

  toggleBtn?.addEventListener("click", () => {
    sidebar.classList.toggle("collapsed");
  });

  mobileBtn?.addEventListener("click", () => {
    sidebar.classList.toggle("mobile-open");
  });
}

// Inicializar tabs
function initTabs() {
  document.querySelectorAll('.tabs').forEach(tabContainer => {
    const buttons = tabContainer.querySelectorAll('.tab-btn');
    const contentId = tabContainer.dataset.tabContent;
    
    buttons.forEach(btn => {
      btn.addEventListener('click', () => {
        buttons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        
        const target = btn.dataset.tab;
        if (contentId) {
          const container = document.getElementById(contentId);
          container.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
          });
          const targetContent = container.querySelector(`[data-tab-content="${target}"]`);
          if (targetContent) targetContent.classList.add('active');
        }
      });
    });
  });
}

// Carregar funcionários
async function loadEmployees() {
  try {
    // Tentar carregar do backend
    const response = await ApiClient.get('/employees');
    if (response.success && response.data) {
      employees = response.data;
    } else {
      // Usar dados mock
      employees = mockEmployees;
    }
  } catch (error) {
    console.log("Usando dados mock");
    employees = mockEmployees;
  }
  
  filteredEmployees = [...employees];
  updateStats();
  renderTable();
}

// Atualizar estatísticas
function updateStats() {
  const total = employees.length;
  const ativos = employees.filter(e => e.status === "ATIVO").length;
  const ferias = employees.filter(e => e.status === "FERIAS").length;
  const desligados = employees.filter(e => e.status === "DESLIGADO").length;
  
  document.getElementById("statTotal").textContent = total;
  document.getElementById("statAtivos").textContent = ativos;
  document.getElementById("statFerias").textContent = ferias;
  document.getElementById("statDesligados").textContent = desligados;
}

// Filtrar funcionários
function filterEmployees() {
  const search = document.getElementById("searchInput").value.toLowerCase();
  const department = document.getElementById("filterDepartment").value;
  const status = document.getElementById("filterStatus").value;
  
  filteredEmployees = employees.filter(emp => {
    const matchSearch = !search || 
      emp.nome_completo.toLowerCase().includes(search) ||
      emp.matricula.toLowerCase().includes(search) ||
      emp.cpf.includes(search);
    
    const matchDept = !department || emp.departamento === department;
    const matchStatus = !status || emp.status === status;
    
    return matchSearch && matchDept && matchStatus;
  });
  
  currentPage = 1;
  renderTable();
}

// Renderizar tabela
function renderTable() {
  const tbody = document.getElementById("employeesTableBody");
  const start = (currentPage - 1) * perPage;
  const end = start + perPage;
  const pageData = filteredEmployees.slice(start, end);
  
  if (pageData.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7">
          <div class="empty-state">
            <div class="empty-state-icon">👥</div>
            <h3 class="empty-state-title">Nenhum funcionário encontrado</h3>
            <p class="empty-state-text">Tente ajustar os filtros ou adicione um novo funcionário.</p>
          </div>
        </td>
      </tr>
    `;
  } else {
    tbody.innerHTML = pageData.map(emp => `
      <tr>
        <td>
          <div class="cell-avatar">
            <div class="avatar">${getInitials(emp.nome_completo)}</div>
            <div class="cell-info">
              <span class="cell-primary">${emp.nome_completo}</span>
              <span class="cell-secondary">${emp.email_corporativo || ''}</span>
            </div>
          </div>
        </td>
        <td>${emp.matricula}</td>
        <td class="hide-mobile">${emp.departamento}</td>
        <td class="hide-mobile">${emp.cargo}</td>
        <td class="hide-mobile">${formatDate(emp.data_admissao)}</td>
        <td><span class="badge ${getStatusClass(emp.status)}">${getStatusLabel(emp.status)}</span></td>
        <td>
          <div class="cell-actions">
            <button class="btn-icon view" onclick="viewEmployee(${emp.id})" title="Visualizar">👁️</button>
            <button class="btn-icon edit" onclick="editEmployee(${emp.id})" title="Editar">✏️</button>
            <button class="btn-icon delete" onclick="deleteEmployee(${emp.id})" title="Excluir">🗑️</button>
          </div>
        </td>
      </tr>
    `).join('');
  }
  
  renderPagination();
  updateTableInfo();
}

// Renderizar paginação
function renderPagination() {
  const totalPages = Math.ceil(filteredEmployees.length / perPage);
  const pagination = document.getElementById("pagination");
  
  let html = '';
  
  html += `<button class="pagination-btn" onclick="goToPage(${currentPage - 1})" ${currentPage === 1 ? 'disabled' : ''}>←</button>`;
  
  for (let i = 1; i <= totalPages; i++) {
    if (i === 1 || i === totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
      html += `<button class="pagination-btn ${i === currentPage ? 'active' : ''}" onclick="goToPage(${i})">${i}</button>`;
    } else if (i === currentPage - 2 || i === currentPage + 2) {
      html += `<span style="padding: 0 8px;">...</span>`;
    }
  }
  
  html += `<button class="pagination-btn" onclick="goToPage(${currentPage + 1})" ${currentPage === totalPages ? 'disabled' : ''}>→</button>`;
  
  pagination.innerHTML = html;
}

// Navegar para página
function goToPage(page) {
  const totalPages = Math.ceil(filteredEmployees.length / perPage);
  if (page >= 1 && page <= totalPages) {
    currentPage = page;
    renderTable();
  }
}

// Mudar quantidade por página
function changePerPage() {
  perPage = parseInt(document.getElementById("perPage").value);
  currentPage = 1;
  renderTable();
}

// Atualizar info da tabela
function updateTableInfo() {
  const start = (currentPage - 1) * perPage + 1;
  const end = Math.min(currentPage * perPage, filteredEmployees.length);
  const total = filteredEmployees.length;
  
  document.getElementById("tableInfo").textContent = 
    total === 0 ? 'Nenhum resultado' : `Mostrando ${start}-${end} de ${total} resultados`;
}

// Ordenar tabela
function sortTable(columnIndex) {
  const headers = document.querySelectorAll('#employeesTable th');
  const header = headers[columnIndex];
  const isAsc = header.classList.contains('asc');
  
  headers.forEach(h => h.classList.remove('asc', 'desc'));
  header.classList.add(isAsc ? 'desc' : 'asc');
  
  const sortKeys = ['nome_completo', 'matricula', 'departamento', 'cargo', 'data_admissao', 'status'];
  const key = sortKeys[columnIndex];
  
  filteredEmployees.sort((a, b) => {
    let aVal = a[key] || '';
    let bVal = b[key] || '';
    
    if (key === 'data_admissao') {
      aVal = new Date(aVal);
      bVal = new Date(bVal);
    }
    
    if (aVal < bVal) return isAsc ? 1 : -1;
    if (aVal > bVal) return isAsc ? -1 : 1;
    return 0;
  });
  
  renderTable();
}

// Abrir modal de funcionário
function openEmployeeModal(id = null) {
  currentEmployeeId = id;
  const form = document.getElementById("employeeForm");
  form.reset();
  
  // Reset tabs to first
  document.querySelectorAll('.tab-btn').forEach((btn, i) => {
    btn.classList.toggle('active', i === 0);
  });
  document.querySelectorAll('.tab-content').forEach((content, i) => {
    content.classList.toggle('active', i === 0);
  });
  
  if (id) {
    document.getElementById("modalTitle").textContent = "Editar Funcionário";
    const emp = employees.find(e => e.id === id);
    if (emp) {
      Form.populate('employeeForm', emp);
    }
  } else {
    document.getElementById("modalTitle").textContent = "Novo Funcionário";
    // Gerar matrícula automática
    const lastMatricula = Math.max(...employees.map(e => parseInt(e.matricula) || 0));
    document.querySelector('[name="matricula"]').value = String(lastMatricula + 1).padStart(6, '0');
  }
  
  Modal.open('employeeModal');
  Mask.initAll();
}

// Editar funcionário
function editEmployee(id) {
  openEmployeeModal(id);
}

// Visualizar funcionário
function viewEmployee(id) {
  const emp = employees.find(e => e.id === id);
  if (!emp) return;
  
  currentEmployeeId = id;
  
  const content = document.getElementById("viewEmployeeContent");
  content.innerHTML = `
    <div style="display: flex; align-items: center; gap: var(--spacing-6); margin-bottom: var(--spacing-6); padding-bottom: var(--spacing-6); border-bottom: 1px solid var(--gray-200);">
      <div class="avatar avatar-lg">${getInitials(emp.nome_completo)}</div>
      <div>
        <h2 style="margin: 0; color: var(--gray-800);">${emp.nome_completo}</h2>
        <p style="margin: var(--spacing-1) 0 0; color: var(--gray-500);">${emp.cargo} • ${emp.departamento}</p>
        <span class="badge ${getStatusClass(emp.status)}" style="margin-top: var(--spacing-2);">${getStatusLabel(emp.status)}</span>
      </div>
    </div>
    
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: var(--spacing-6);">
      <div>
        <h4 style="color: var(--gray-600); margin-bottom: var(--spacing-3); font-size: var(--font-size-sm); text-transform: uppercase;">Dados Pessoais</h4>
        <p><strong>Matrícula:</strong> ${emp.matricula}</p>
        <p><strong>CPF:</strong> ${emp.cpf}</p>
        <p><strong>Email:</strong> ${emp.email_corporativo || '-'}</p>
        <p><strong>Telefone:</strong> ${emp.telefone || '-'}</p>
      </div>
      
      <div>
        <h4 style="color: var(--gray-600); margin-bottom: var(--spacing-3); font-size: var(--font-size-sm); text-transform: uppercase;">Dados Profissionais</h4>
        <p><strong>Admissão:</strong> ${formatDate(emp.data_admissao)}</p>
        <p><strong>Tipo Contrato:</strong> ${emp.tipo_contrato}</p>
        <p><strong>Salário:</strong> ${formatCurrency(emp.salario_atual)}</p>
      </div>
    </div>
  `;
  
  Modal.open('viewEmployeeModal');
}

// Editar a partir da visualização
function editFromView() {
  Modal.close('viewEmployeeModal');
  editEmployee(currentEmployeeId);
}

// Salvar funcionário
async function saveEmployee() {
  const form = document.getElementById("employeeForm");
  
  // Validação básica
  if (!form.checkValidity()) {
    form.reportValidity();
    return;
  }
  
  const data = Form.serialize('employeeForm');
  
  // Remove máscara do CPF
  data.cpf = data.cpf.replace(/\D/g, '');
  
  // Converter salário
  if (data.salario_atual) {
    data.salario_atual = parseFloat(data.salario_atual.replace(/[^\d,]/g, '').replace(',', '.')) || 0;
  }
  
  try {
    if (currentEmployeeId) {
      // Atualizar
      const response = await ApiClient.put(`/employees/${currentEmployeeId}`, data);
      if (response.success) {
        Toast.success('Funcionário atualizado com sucesso!');
      } else {
        // Mock update
        const index = employees.findIndex(e => e.id === currentEmployeeId);
        if (index > -1) {
          employees[index] = { ...employees[index], ...data };
        }
        Toast.success('Funcionário atualizado com sucesso!');
      }
    } else {
      // Criar
      const response = await ApiClient.post('/employees', data);
      if (response.success) {
        Toast.success('Funcionário cadastrado com sucesso!');
      } else {
        // Mock create
        const newId = Math.max(...employees.map(e => e.id)) + 1;
        employees.push({
          id: newId,
          ...data,
          departamento: getDepartmentName(data.departamento_id),
          cargo: getCargoName(data.cargo_id)
        });
        Toast.success('Funcionário cadastrado com sucesso!');
      }
    }
    
    Modal.close('employeeModal');
    filterEmployees();
    updateStats();
  } catch (error) {
    Toast.error('Erro ao salvar funcionário');
  }
}

// Excluir funcionário
function deleteEmployee(id) {
  const emp = employees.find(e => e.id === id);
  if (!emp) return;
  
  Confirm.show(
    `Tem certeza que deseja excluir o funcionário <strong>${emp.nome_completo}</strong>?<br><small>Esta ação não pode ser desfeita.</small>`,
    async () => {
      try {
        const response = await ApiClient.delete(`/employees/${id}`);
        if (response.success) {
          Toast.success('Funcionário excluído com sucesso!');
        } else {
          // Mock delete
          employees = employees.filter(e => e.id !== id);
          Toast.success('Funcionário excluído com sucesso!');
        }
        filterEmployees();
        updateStats();
      } catch (error) {
        Toast.error('Erro ao excluir funcionário');
      }
    }
  );
}

// Exportar funcionários
function exportEmployees() {
  const headers = ['Matrícula', 'Nome', 'CPF', 'Departamento', 'Cargo', 'Admissão', 'Status', 'Salário'];
  const rows = filteredEmployees.map(emp => [
    emp.matricula,
    emp.nome_completo,
    emp.cpf,
    emp.departamento,
    emp.cargo,
    formatDate(emp.data_admissao),
    getStatusLabel(emp.status),
    formatCurrency(emp.salario_atual)
  ]);
  
  let csv = headers.join(';') + '\n';
  csv += rows.map(row => row.join(';')).join('\n');
  
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `funcionarios_${new Date().toISOString().split('T')[0]}.csv`;
  link.click();
  
  Toast.success('Exportação realizada com sucesso!');
}

// Buscar CEP
async function searchCEP(cep) {
  cep = cep.replace(/\D/g, '');
  if (cep.length !== 8) return;
  
  try {
    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
    const data = await response.json();
    
    if (!data.erro) {
      document.getElementById('logradouro').value = data.logradouro || '';
      document.getElementById('bairro').value = data.bairro || '';
      document.getElementById('cidade').value = data.localidade || '';
      document.getElementById('estado').value = data.uf || '';
    }
  } catch (error) {
    console.error('Erro ao buscar CEP:', error);
  }
}

// Helpers
function getInitials(name) {
  return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
}

function formatDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString + 'T00:00:00');
  return date.toLocaleDateString('pt-BR');
}

function formatCurrency(value) {
  if (!value) return '-';
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
}

function getStatusClass(status) {
  const classes = {
    'ATIVO': 'badge-success',
    'FERIAS': 'badge-warning',
    'AFASTADO': 'badge-info',
    'DESLIGADO': 'badge-danger'
  };
  return classes[status] || 'badge-info';
}

function getStatusLabel(status) {
  const labels = {
    'ATIVO': 'Ativo',
    'FERIAS': 'Em Férias',
    'AFASTADO': 'Afastado',
    'DESLIGADO': 'Desligado'
  };
  return labels[status] || status;
}

function getDepartmentName(id) {
  const depts = { 1: 'Tecnologia', 2: 'Recursos Humanos', 3: 'Financeiro', 4: 'Comercial', 5: 'Operações' };
  return depts[id] || '';
}

function getCargoName(id) {
  const cargos = { 1: 'Analista', 2: 'Desenvolvedor', 3: 'Coordenador', 4: 'Gerente', 5: 'Diretor' };
  return cargos[id] || '';
}
