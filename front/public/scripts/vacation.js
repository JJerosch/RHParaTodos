// Vacation Management Script
const ApiClient = {
  get: async (url) => {
    const response = await fetch(url)
    return await response.json()
  },
  post: async (url, data) => {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
    return await response.json()
  },
}

document.addEventListener("DOMContentLoaded", () => {
  console.log("Vacation page loaded")
  initVacationPage()
})

function initVacationPage() {
  loadVacationData()
  setupEventListeners()
}

function setupEventListeners() {
  // Search
  const searchInput = document.querySelector('.form-input[placeholder*="Buscar"]')
  if (searchInput) {
    searchInput.addEventListener("input", handleSearch)
  }

  // Status filter
  const statusFilter = document.querySelector(".form-select")
  if (statusFilter) {
    statusFilter.addEventListener("change", handleStatusChange)
  }

  // Action buttons
  document.querySelectorAll(".btn-success, .btn-danger").forEach((btn) => {
    btn.addEventListener("click", handleVacationAction)
  })

  // New request button
  const newButton = document.querySelector(".btn-primary")
  if (newButton) {
    newButton.addEventListener("click", openNewVacationModal)
  }
}

async function loadVacationData(status) {
  try {
    const response = await ApiClient.get(status ? `/api/vacation/requests?status=${status}` : "/api/vacation/requests")
    if (response.success) {
      updateVacationStats(response.data.stats)
      updateVacationTable(response.data.requests)
    }
  } catch (error) {
    console.error("Error loading vacation data:", error)
  }
}

function updateVacationStats(stats) {
  const cards = document.querySelectorAll(".stat-card .stat-value")
  if (cards.length >= 4) {
    cards[0].textContent = stats.scheduled || 0
    cards[1].textContent = stats.expiring || 0
    cards[2].textContent = stats.pending || 0
    cards[3].textContent = stats.onVacation || 0
  }
}

function updateVacationTable(requests) {
  const tbody = document.querySelector(".table tbody")
  if (!tbody || !requests) return

  tbody.innerHTML = requests
    .map(
      (req) => `
    <tr>
      <td>
        <div class="employee-cell">
          <div class="employee-avatar-sm">${req.initials}</div>
          <strong>${req.employeeName}</strong>
        </div>
      </td>
      <td>${req.acquisitivePeriod}</td>
      <td>${req.availableDays} dias</td>
      <td>${formatDate(req.startDate)}</td>
      <td>${formatDate(req.endDate)}</td>
      <td>${req.requestedDays} dias</td>
      <td><span class="badge badge-${req.statusClass}">${req.statusText}</span></td>
      <td>
        <div class="table-actions">
          ${
            req.status === "pending"
              ? `
            <button class="btn btn-success btn-sm" data-action="approve" data-id="${req.id}">Aprovar</button>
            <button class="btn btn-danger btn-sm" data-action="reject" data-id="${req.id}">Rejeitar</button>
          `
              : `
            <button class="btn-icon" data-action="view" data-id="${req.id}" title="Ver Detalhes">👁️</button>
            <button class="btn-icon" data-action="edit" data-id="${req.id}" title="Editar">✏️</button>
          `
          }
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

function handleStatusChange(event) {
  const status = event.target.value
  loadVacationData(status)
}

async function handleVacationAction(event) {
  const button = event.currentTarget
  const action = button.dataset.action
  const id = button.dataset.id

  if (action === "approve") {
    await approveVacation(id)
  } else if (action === "reject") {
    await rejectVacation(id)
  }
}

async function approveVacation(id) {
  try {
    const response = await ApiClient.post(`/api/vacation/${id}/approve`, {})
    if (response.success) {
      alert("Férias aprovadas com sucesso!")
      loadVacationData()
    }
  } catch (error) {
    console.error("Error approving vacation:", error)
    alert("Erro ao aprovar férias")
  }
}

async function rejectVacation(id) {
  const reason = prompt("Motivo da rejeição:")
  if (!reason) return

  try {
    const response = await ApiClient.post(`/api/vacation/${id}/reject`, { reason })
    if (response.success) {
      alert("Solicitação rejeitada")
      loadVacationData()
    }
  } catch (error) {
    console.error("Error rejecting vacation:", error)
    alert("Erro ao rejeitar solicitação")
  }
}

function openNewVacationModal() {
  console.log("Open new vacation request modal")
  // Open modal to create new vacation request
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString("pt-BR")
}
