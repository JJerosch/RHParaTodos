// Reports Management Script
const ApiClient = {
  get: async (url) => {
    // Mock implementation for demonstration purposes
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, data: { url: "https://example.com/report.pdf" } })
      }, 1000)
    })
  },
}

document.addEventListener("DOMContentLoaded", () => {
  console.log("Reports page loaded")
  initReportsPage()
})

function initReportsPage() {
  setupEventListeners()
}

function setupEventListeners() {
  // Generate report buttons
  document.querySelectorAll(".report-item .btn").forEach((btn) => {
    btn.addEventListener("click", handleGenerateReport)
  })
}

async function handleGenerateReport(event) {
  const button = event.currentTarget
  const reportItem = button.closest(".report-item")
  const reportTitle = reportItem.querySelector("h3").textContent

  // Show loading state
  button.textContent = "Gerando..."
  button.disabled = true

  try {
    // Determine report type based on title
    const reportType = getReportType(reportTitle)

    // Call API to generate report
    const response = await ApiClient.get(`/api/reports/${reportType}/generate`)

    if (response.success) {
      // Download the report
      downloadReport(response.data.url, reportTitle)
      button.textContent = "Gerar PDF"
    } else {
      alert("Erro ao gerar relatório")
    }
  } catch (error) {
    console.error("Error generating report:", error)
    alert("Erro ao gerar relatório")
  } finally {
    button.disabled = false
  }
}

function getReportType(title) {
  const reportMap = {
    "Headcount por Departamento": "headcount",
    "Turnover Rate": "turnover",
    "Tempo Médio de Contratação": "hiring-time",
    "Análise de Avaliações": "performance-analysis",
    "Folha de Pagamento Mensal": "monthly-payroll",
    "Custos por Departamento": "department-costs",
    "Provisões e Encargos": "provisions",
    "Relatório Contábil": "accounting",
    "Controle de Ponto Mensal": "monthly-timesheet",
    "Banco de Horas": "time-bank",
    Absenteísmo: "absenteeism",
    "Horas Extras": "overtime",
  }

  return reportMap[title] || "custom"
}

function downloadReport(url, title) {
  const link = document.createElement("a")
  link.href = url
  link.download = `${sanitizeFilename(title)}_${getCurrentDate()}.pdf`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

function sanitizeFilename(filename) {
  return filename
    .toLowerCase()
    .replace(/[àáâãäå]/g, "a")
    .replace(/[èéêë]/g, "e")
    .replace(/[ìíîï]/g, "i")
    .replace(/[òóôõö]/g, "o")
    .replace(/[ùúûü]/g, "u")
    .replace(/[ç]/g, "c")
    .replace(/\s+/g, "-")
    .replace(/[^a-z0-9-]/g, "")
}

function getCurrentDate() {
  const now = new Date()
  return now.toISOString().split("T")[0]
}
