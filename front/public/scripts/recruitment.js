// Recruitment Management
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners()
})

function setupEventListeners() {
  document.getElementById("addVacancyBtn").addEventListener("click", openVacancyModal)
  document.getElementById("closeModal").addEventListener("click", closeVacancyModal)
  document.getElementById("vacancyForm").addEventListener("submit", saveVacancy)

  document.getElementById("vacancyModal").addEventListener("click", (e) => {
    if (e.target.id === "vacancyModal") {
      closeVacancyModal()
    }
  })
}

function showTab(tabName) {
  const buttons = document.querySelectorAll(".page-toolbar .btn")
  buttons.forEach((btn) => btn.classList.remove("active"))

  event.target.classList.add("active")

  document.getElementById("vacanciesTab").classList.toggle("active", tabName === "vacancies")
  document.getElementById("candidatesTab").classList.toggle("active", tabName === "candidates")
}

function openVacancyModal() {
  document.getElementById("vacancyModal").classList.add("active")
}

function closeVacancyModal() {
  document.getElementById("vacancyModal").classList.remove("active")
  document.getElementById("vacancyForm").reset()
}

function saveVacancy(e) {
  e.preventDefault()
  // Em produção: POST para Spring Boot
  alert("Vaga publicada com sucesso!")
  closeVacancyModal()
}

function viewVacancy(id) {
  alert(`Visualizando vaga #${id}`)
}

function manageCandidates(id) {
  alert(`Gerenciando candidatos da vaga #${id}`)
  // Poderia redirecionar para a tab de candidatos filtrada
}
