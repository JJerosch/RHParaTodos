// Timesheet Management Script
document.addEventListener("DOMContentLoaded", () => {
  console.log("Timesheet page loaded")
  initTimesheetPage()
})

function initTimesheetPage() {
  loadTimesheetData()
  setupEventListeners()
  updateCurrentTime()
  setInterval(updateCurrentTime, 60000) // Update every minute
}

function setupEventListeners() {
  // Search
  const searchInput = document.querySelector('.form-input[placeholder*="Buscar"]')
  if (searchInput) {
    searchInput.addEventListener("input", handleSearch)
  }

  // Date filter
  const dateInput = document.querySelector('input[type="date"]')
  if (dateInput) {
    dateInput.addEventListener("change", handleDateChange)
  }

  // Manual entry button
  const manualButton = document.querySelector(".btn-primary")
  if (manualButton) {
    manualButton.addEventListener("click", openManualEntryModal)
  }

  // Export button
  const exportButton = document.querySelector(".btn-secondary")
  if (exportButton) {
    exportButton.addEventListener("click", exportTimesheet)
  }
}

async function loadTimesheetData(date) {
  const selectedDate = date || document.querySelector('input[type="date"]')?.value

  try {
    const response = await window.ApiClient.get(`/api/timesheet?date=${selectedDate}`)
    if (response.success) {
      updateTimesheetStats(response.data.stats)
      updateTimesheetTable(response.data.entries)
    }
  } catch (error) {
    console.error("Error loading timesheet:", error)
  }
}

function updateTimesheetStats(stats) {
  const cards = document.querySelectorAll(".stat-card .stat-value")
  if (cards.length >= 4) {
    cards[0].textContent = stats.present || 0
    cards[1].textContent = stats.overtime || "0h"
    cards[2].textContent = stats.absences || 0
    cards[3].textContent = stats.timeBank || "+0h"
  }
}

function updateTimesheetTable(entries) {
  const tbody = document.querySelector(".table tbody")
  if (!tbody || !entries) return

  tbody.innerHTML = entries
    .map(
      (entry) => `
    <tr>
      <td>
        <div class="employee-cell">
          <div class="employee-avatar-sm">${entry.initials}</div>
          <strong>${entry.employeeName}</strong>
        </div>
      </td>
      <td>${entry.clockIn || "-"}</td>
      <td>${entry.lunchOut || "-"}</td>
      <td>${entry.lunchIn || "-"}</td>
      <td>${entry.clockOut || "-"}</td>
      <td><strong>${entry.totalHours || "-"}</strong></td>
      <td><span class="badge badge-${entry.statusClass}">${entry.statusText}</span></td>
      <td>
        <div class="table-actions">
          <button class="btn-icon" data-action="view" data-id="${entry.employeeId}" title="Ver Detalhes">👁️</button>
          ${
            entry.status !== "absent"
              ? `
            <button class="btn-icon" data-action="edit" data-id="${entry.employeeId}" title="Editar">✏️</button>
          `
              : `
            <button class="btn-icon" data-action="justify" data-id="${entry.employeeId}" title="Justificar">📄</button>
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

function handleDateChange(event) {
  const date = event.target.value
  loadTimesheetData(date)
}

function openManualEntryModal() {
  console.log("Open manual time entry modal")
  // Open modal for manual time entry
}

function exportTimesheet() {
  console.log("Exporting timesheet report")
  // Generate and download Excel/PDF report
}

function updateCurrentTime() {
  const now = new Date()
  const timeString = now.toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  })
  // Update any time display elements if needed
}
