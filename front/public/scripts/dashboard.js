// Import the Auth module
const Auth = require("./auth") // Assuming Auth is exported from a module named auth.js

// Script para o dashboard
document.addEventListener("DOMContentLoaded", () => {
  // Verificar autenticação
  if (!Auth.requireAuth()) return

  const user = Auth.getCurrentUser()

  // Atualizar informações do usuário na interface
  document.getElementById("userName").textContent = user.username
  document.getElementById("userFullName").textContent = user.username
  document.getElementById("userProfile").textContent = user.profileName

  // Toggle do menu do usuário
  const userMenuButton = document.getElementById("userMenuButton")
  const userDropdown = document.getElementById("userDropdown")

  userMenuButton.addEventListener("click", (e) => {
    e.stopPropagation()
    userDropdown.classList.toggle("active")
  })

  // Fechar dropdown ao clicar fora
  document.addEventListener("click", () => {
    userDropdown.classList.remove("active")
  })

  // Logout
  document.getElementById("logoutButton").addEventListener("click", (e) => {
    e.preventDefault()
    if (confirm("Deseja realmente sair do sistema?")) {
      Auth.logout()
    }
  })

  // Toggle da sidebar em mobile
  const sidebarToggle = document.getElementById("sidebarToggle")
  const sidebar = document.getElementById("sidebar")

  if (sidebarToggle) {
    sidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("active")
    })
  }

  // Fechar sidebar ao clicar em um link (mobile)
  const navItems = document.querySelectorAll(".nav-item")
  navItems.forEach((item) => {
    item.addEventListener("click", () => {
      if (window.innerWidth <= 768) {
        sidebar.classList.remove("active")
      }
    })
  })

  // Filtrar itens do menu baseado nas permissões do usuário
  filterMenuByPermissions(user.permissions)
})

// Filtrar menu baseado nas permissões
function filterMenuByPermissions(permissions) {
  const navItems = document.querySelectorAll(".nav-item[data-permission]")

  navItems.forEach((item) => {
    const requiredPermission = item.dataset.permission

    if (!permissions.includes("all") && !permissions.includes(requiredPermission)) {
      item.style.display = "none"
    }
  })
}
