// Employees Management Script
// Mock data - em produção, virá do Spring Boot via API REST

let employees = [
  {
    id: 1,
    employeeId: "EMP001",
    fullName: "João Pedro Silva",
    birthDate: "1990-05-15",
    cpf: "123.456.789-00",
    rg: "12.345.678-9",
    gender: "M",
    maritalStatus: "married",
    position: "developer-sr",
    department: "tech",
    email: "joao.silva@empresa.com",
    phone: "(11) 98765-4321",
    salary: 12000,
    hireDate: "2020-03-10",
    status: "active",
    manager: "Maria Silva",
  },
  {
    id: 2,
    employeeId: "EMP002",
    fullName: "Maria Santos Costa",
    birthDate: "1988-08-22",
    cpf: "987.654.321-00",
    rg: "98.765.432-1",
    gender: "F",
    maritalStatus: "single",
    position: "manager",
    department: "hr",
    email: "maria.costa@empresa.com",
    phone: "(11) 98765-1234",
    salary: 15000,
    hireDate: "2018-01-15",
    status: "active",
    manager: null,
  },
  {
    id: 3,
    employeeId: "EMP003",
    fullName: "Pedro Henrique Oliveira",
    birthDate: "1995-03-10",
    cpf: "456.789.123-00",
    rg: "45.678.912-3",
    gender: "M",
    maritalStatus: "single",
    position: "developer-pl",
    department: "tech",
    email: "pedro.oliveira@empresa.com",
    phone: "(11) 98765-5678",
    salary: 8000,
    hireDate: "2021-06-20",
    status: "vacation",
    manager: "Maria Silva",
  },
  {
    id: 4,
    employeeId: "EMP004",
    fullName: "Ana Carolina Lima",
    birthDate: "1992-11-30",
    cpf: "321.654.987-00",
    rg: "32.165.498-7",
    gender: "F",
    maritalStatus: "married",
    position: "analyst",
    department: "finance",
    email: "ana.lima@empresa.com",
    phone: "(11) 98765-9012",
    salary: 6500,
    hireDate: "2019-09-05",
    status: "active",
    manager: "João Santos",
  },
  {
    id: 5,
    employeeId: "EMP005",
    fullName: "Carlos Eduardo Ferreira",
    birthDate: "1985-07-18",
    cpf: "789.123.456-00",
    rg: "78.912.345-6",
    gender: "M",
    maritalStatus: "divorced",
    position: "coordinator",
    department: "sales",
    email: "carlos.ferreira@empresa.com",
    phone: "(11) 98765-3456",
    salary: 10000,
    hireDate: "2017-04-12",
    status: "active",
    manager: "Maria Silva",
  },
]

const positionNames = {
  "developer-jr": "Desenvolvedor Júnior",
  "developer-pl": "Desenvolvedor Pleno",
  "developer-sr": "Desenvolvedor Sênior",
  analyst: "Analista",
  coordinator: "Coordenador",
  manager: "Gerente",
  director: "Diretor",
}

const departmentNames = {
  tech: "Tecnologia",
  hr: "Recursos Humanos",
  finance: "Financeiro",
  sales: "Vendas",
  marketing: "Marketing",
}

const statusNames = {
  active: "Ativo",
  vacation: "Férias",
  away: "Afastado",
  terminated: "Desligado",
}

const statusClasses = {
  active: "badge-success",
  vacation: "badge-info",
  away: "badge-warning",
  terminated: "badge-danger",
}

let currentEmployee = null
let filteredEmployees = [...employees]

document.addEventListener("DOMContentLoaded", () => {
  // Initialize
  renderEmployeesTable()
  setupEventListeners()
})

function setupEventListeners() {
  // Search
  document.getElementById("searchInput").addEventListener("input", filterEmployees)

  // Filters
  document.getElementById("statusFilter").addEventListener("change", filterEmployees)
  document.getElementById("departmentFilter").addEventListener("change", filterEmployees)

  // Buttons
  document.getElementById("addEmployeeBtn").addEventListener("click", openAddEmployeeModal)
  document.getElementById("exportBtn").addEventListener("click", exportEmployees)

  // Modal controls
  document.getElementById("closeModal").addEventListener("click", closeEmployeeModal)
  document.getElementById("closeViewModal").addEventListener("click", closeViewModal)
  document.getElementById("cancelBtn").addEventListener("click", closeEmployeeModal)

  // Form tabs
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const tabName = e.target.dataset.tab
      switchTab(tabName)
    })
  })

  // Form submission
  document.getElementById("employeeForm").addEventListener("submit", saveEmployee)

  // Close modal on overlay click
  document.getElementById("employeeModal").addEventListener("click", (e) => {
    if (e.target.id === "employeeModal") {
      closeEmployeeModal()
    }
  })

  document.getElementById("viewEmployeeModal").addEventListener("click", (e) => {
    if (e.target.id === "viewEmployeeModal") {
      closeViewModal()
    }
  })
}

function renderEmployeesTable() {
  const tbody = document.getElementById("employeesTableBody")
  tbody.innerHTML = ""

  if (filteredEmployees.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: var(--spacing-8); color: var(--gray-500);">
          Nenhum funcionário encontrado
        </td>
      </tr>
    `
    return
  }

  filteredEmployees.forEach((employee) => {
    const tr = document.createElement("tr")
    tr.innerHTML = `
      <td>${employee.fullName}</td>
      <td>${positionNames[employee.position] || employee.position}</td>
      <td>${departmentNames[employee.department] || employee.department}</td>
      <td>${employee.email}</td>
      <td>${employee.phone}</td>
      <td>
        <span class="badge ${statusClasses[employee.status]}">
          ${statusNames[employee.status]}
        </span>
      </td>
      <td>
        <div class="table-actions">
          <button class="btn-icon" onclick="viewEmployee(${employee.id})" title="Visualizar">👁️</button>
          <button class="btn-icon" onclick="editEmployee(${employee.id})" title="Editar">✏️</button>
          <button class="btn-icon danger" onclick="deleteEmployee(${employee.id})" title="Excluir">🗑️</button>
        </div>
      </td>
    `
    tbody.appendChild(tr)
  })
}

function filterEmployees() {
  const searchTerm = document.getElementById("searchInput").value.toLowerCase()
  const statusFilter = document.getElementById("statusFilter").value
  const departmentFilter = document.getElementById("departmentFilter").value

  filteredEmployees = employees.filter((employee) => {
    const matchesSearch =
      employee.fullName.toLowerCase().includes(searchTerm) ||
      employee.email.toLowerCase().includes(searchTerm) ||
      positionNames[employee.position].toLowerCase().includes(searchTerm) ||
      departmentNames[employee.department].toLowerCase().includes(searchTerm)

    const matchesStatus = !statusFilter || employee.status === statusFilter
    const matchesDepartment = !departmentFilter || employee.department === departmentFilter

    return matchesSearch && matchesStatus && matchesDepartment
  })

  renderEmployeesTable()
}

function openAddEmployeeModal() {
  currentEmployee = null
  document.getElementById("modalTitle").textContent = "Adicionar Funcionário"
  document.getElementById("employeeForm").reset()

  // Generate employee ID
  const nextId = Math.max(...employees.map((e) => Number.parseInt(e.employeeId.replace("EMP", "")))) + 1
  document.querySelector('[name="employeeId"]').value = `EMP${String(nextId).padStart(3, "0")}`

  document.getElementById("employeeModal").classList.add("active")
}

function closeEmployeeModal() {
  document.getElementById("employeeModal").classList.remove("active")
  currentEmployee = null
  switchTab("personal")
}

function closeViewModal() {
  document.getElementById("viewEmployeeModal").classList.remove("active")
}

function switchTab(tabName) {
  // Update buttons
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.tab === tabName)
  })

  // Update content
  document.querySelectorAll(".tab-content").forEach((content) => {
    content.classList.toggle("active", content.dataset.tab === tabName)
  })
}

function saveEmployee(e) {
  e.preventDefault()

  const formData = new FormData(e.target)
  const employeeData = Object.fromEntries(formData.entries())

  if (currentEmployee) {
    // Update existing
    const index = employees.findIndex((e) => e.id === currentEmployee.id)
    employees[index] = { ...employees[index], ...employeeData }
    alert("Funcionário atualizado com sucesso!")
  } else {
    // Add new
    const newEmployee = {
      id: Date.now(),
      ...employeeData,
    }
    employees.push(newEmployee)
    alert("Funcionário cadastrado com sucesso!")
  }

  // Em produção: fazer POST/PUT para Spring Boot
  // await ApiClient.post('/employees', employeeData)

  closeEmployeeModal()
  filterEmployees()
}

function viewEmployee(id) {
  const employee = employees.find((e) => e.id === id)
  if (!employee) return

  const detailsContainer = document.getElementById("employeeDetails")

  const initials = employee.fullName
    .split(" ")
    .map((n) => n[0])
    .join("")
    .substring(0, 2)

  detailsContainer.innerHTML = `
    <div class="employee-header">
      <div class="employee-avatar-large">${initials}</div>
      <div class="employee-header-info">
        <h2>${employee.fullName}</h2>
        <p>${positionNames[employee.position]} - ${departmentNames[employee.department]}</p>
        <span class="badge ${statusClasses[employee.status]}">${statusNames[employee.status]}</span>
      </div>
    </div>

    <div class="employee-detail-section">
      <h3>Dados Pessoais</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">CPF</span>
          <span class="detail-value">${employee.cpf || "-"}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">RG</span>
          <span class="detail-value">${employee.rg || "-"}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Data de Nascimento</span>
          <span class="detail-value">${formatDate(employee.birthDate) || "-"}</span>
        </div>
      </div>
    </div>

    <div class="employee-detail-section">
      <h3>Dados Profissionais</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">Matrícula</span>
          <span class="detail-value">${employee.employeeId}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Data de Admissão</span>
          <span class="detail-value">${formatDate(employee.hireDate)}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Salário</span>
          <span class="detail-value">R$ ${Number.parseFloat(employee.salary || 0).toLocaleString("pt-BR", {
            minimumFractionDigits: 2,
          })}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Gestor Direto</span>
          <span class="detail-value">${employee.manager || "-"}</span>
        </div>
      </div>
    </div>

    <div class="employee-detail-section">
      <h3>Contato</h3>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">E-mail</span>
          <span class="detail-value">${employee.email}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Telefone</span>
          <span class="detail-value">${employee.phone}</span>
        </div>
      </div>
    </div>

    <div class="modal-footer">
      <button class="btn btn-primary" onclick="editEmployee(${employee.id}); closeViewModal();">Editar Funcionário</button>
    </div>
  `

  document.getElementById("viewEmployeeModal").classList.add("active")
}

function editEmployee(id) {
  const employee = employees.find((e) => e.id === id)
  if (!employee) return

  currentEmployee = employee

  document.getElementById("modalTitle").textContent = "Editar Funcionário"

  // Populate form
  Object.keys(employee).forEach((key) => {
    const input = document.querySelector(`[name="${key}"]`)
    if (input) {
      input.value = employee[key] || ""
    }
  })

  document.getElementById("employeeModal").classList.add("active")
}

function deleteEmployee(id) {
  const employee = employees.find((e) => e.id === id)
  if (!employee) return

  if (confirm(`Deseja realmente excluir o funcionário ${employee.fullName}?`)) {
    employees = employees.filter((e) => e.id !== id)

    // Em produção: fazer DELETE para Spring Boot
    // await ApiClient.delete(`/employees/${id}`)

    alert("Funcionário excluído com sucesso!")
    filterEmployees()
  }
}

function exportEmployees() {
  // Simular exportação para CSV
  let csv = "Matrícula,Nome,Cargo,Departamento,Email,Telefone,Status\n"

  filteredEmployees.forEach((emp) => {
    csv += `${emp.employeeId},"${emp.fullName}","${positionNames[emp.position]}","${
      departmentNames[emp.department]
    }",${emp.email},${emp.phone},"${statusNames[emp.status]}"\n`
  })

  const blob = new Blob([csv], { type: "text/csv" })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement("a")
  a.href = url
  a.download = `funcionarios_${new Date().toISOString().split("T")[0]}.csv`
  a.click()

  alert("Dados exportados com sucesso!")
}

function formatDate(dateString) {
  if (!dateString) return "-"
  const date = new Date(dateString + "T00:00:00")
  return date.toLocaleDateString("pt-BR")
}
