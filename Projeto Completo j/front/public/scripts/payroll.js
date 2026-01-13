// Payroll Management Script
document.addEventListener("DOMContentLoaded", () => {
  console.log("Payroll page loaded")

  // Initialize page
  initPayrollPage()
})

function initPayrollPage() {
  // Load payroll data
  loadPayrollData()

  // Setup event listeners
  setupEventListeners()
}

function setupEventListeners() {
  // Search functionality
  const searchInput = document.querySelector('.form-input[placeholder*="Buscar"]')
  if (searchInput) {
    searchInput.addEventListener("input", handleSearch)
  }

  // Filter by department
  const departmentFilter = document.querySelector(".form-select")
  if (departmentFilter) {
    departmentFilter.addEventListener("change", handleFilterChange)
  }

  // Period navigation
  const periodButtons = document.querySelectorAll(".period-actions .btn")
  periodButtons.forEach((btn) => {
    btn.addEventListener("click", handlePeriodChange)
  })

  // Action buttons
  document.querySelectorAll(".btn-icon").forEach((btn) => {
    btn.addEventListener("click", handleActionClick)
  })
}

const ApiClient = {
  get: async (url) => {
    // Mock implementation for demonstration purposes
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: true,
          data: {
            summary: {
              totalGross: 100000,
              totalDeductions: 20000,
              totalNet: 80000,
              pending: 5,
            },
            employees: [
              {
                id: "E001",
                name: "John Doe",
                initials: "JD",
                position: "Software Engineer",
                baseSalary: 50000,
                additions: 10000,
                deductions: 5000,
                netSalary: 55000,
                status: "calculated",
                statusText: "Calculado",
              },
              {
                id: "E002",
                name: "Jane Smith",
                initials: "JS",
                position: "Project Manager",
                baseSalary: 60000,
                additions: 15000,
                deductions: 7000,
                netSalary: 68000,
                status: "pending",
                statusText: "Pendente",
              },
            ],
          },
        })
      }, 1000)
    })
  },
}

async function loadPayrollData(department) {
  try {
    const response = await ApiClient.get(`/api/payroll/current${department ? `?department=${department}` : ""}`)
    if (response.success) {
      displayPayrollData(response.data)
    }
  } catch (error) {
    console.error("Error loading payroll:", error)
  }
}

function displayPayrollData(data) {
  // Update summary cards
  updateSummaryCards(data.summary)

  // Update table
  updatePayrollTable(data.employees)
}

function updateSummaryCards(summary) {
  // Update values in stat cards
  const cards = document.querySelectorAll(".stat-card")
  if (cards.length >= 4) {
    cards[0].querySelector(".stat-value").textContent = formatCurrency(summary.totalGross)
    cards[1].querySelector(".stat-value").textContent = formatCurrency(summary.totalDeductions)
    cards[2].querySelector(".stat-value").textContent = formatCurrency(summary.totalNet)
    cards[3].querySelector(".stat-value").textContent = summary.pending
  }
}

function updatePayrollTable(employees) {
  const tbody = document.querySelector(".table tbody")
  if (!tbody || !employees) return

  tbody.innerHTML = employees
    .map(
      (emp) => `
    <tr ${emp.status === "pending" ? 'class="warning-row"' : ""}>
      <td>
        <div class="employee-cell">
          <div class="employee-avatar-sm">${emp.initials}</div>
          <div>
            <strong>${emp.name}</strong>
            <small>${emp.id} - ${emp.position}</small>
          </div>
        </div>
      </td>
      <td>${formatCurrency(emp.baseSalary)}</td>
      <td class="positive">+ ${formatCurrency(emp.additions)}</td>
      <td class="negative">- ${formatCurrency(emp.deductions)}</td>
      <td><strong>${formatCurrency(emp.netSalary)}</strong></td>
      <td><span class="badge badge-${emp.status === "calculated" ? "success" : "warning"}">${emp.statusText}</span></td>
      <td>
        <div class="table-actions">
          <button class="btn-icon" data-action="view" data-id="${emp.id}" title="Ver Detalhes">👁️</button>
          <button class="btn-icon" data-action="edit" data-id="${emp.id}" title="Editar">✏️</button>
          <button class="btn-icon" data-action="payslip" data-id="${emp.id}" title="Holerite">📄</button>
        </div>
      </td>
    </tr>
  `,
    )
    .join("")
}

function handleSearch(event) {
  const searchTerm = event.target.value.toLowerCase()
  const rows = document.querySelectorAll(".table tbody tr")

  rows.forEach((row) => {
    const text = row.textContent.toLowerCase()
    row.style.display = text.includes(searchTerm) ? "" : "none"
  })
}

function handleFilterChange(event) {
  const department = event.target.value
  // Reload data with filter
  loadPayrollData(department)
}

function handlePeriodChange(event) {
  const action = event.target.textContent
  console.log("Period change:", action)
  // Load different period
}

function handleActionClick(event) {
  const button = event.currentTarget
  const action = button.dataset.action
  const id = button.dataset.id

  switch (action) {
    case "view":
      viewPayrollDetails(id)
      break
    case "edit":
      editPayroll(id)
      break
    case "payslip":
      generatePayslip(id)
      break
  }
}

function viewPayrollDetails(id) {
  console.log("View payroll details:", id)
  // Open modal with details
}

function editPayroll(id) {
  console.log("Edit payroll:", id)
  // Open edit modal
}

function generatePayslip(id) {
  console.log("Generate payslip:", id)
  // Generate and download PDF
}

function formatCurrency(value) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value || 0)
}
