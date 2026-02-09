// Script para o dashboard
// NOTA: Auth é carregado globalmente via auth.js

document.addEventListener("DOMContentLoaded", () => {
  // Verificar autenticação
  if (!Auth.requireAuth()) return;

  const user = Auth.getCurrentUser();

  // Atualizar informações do usuário na interface
  const userNameEl = document.getElementById("userName");
  const userFullNameEl = document.getElementById("userFullName");
  const userProfileEl = document.getElementById("userProfile");

  if (userNameEl) userNameEl.textContent = user.username;
  if (userFullNameEl) userFullNameEl.textContent = user.username;
  if (userProfileEl) userProfileEl.textContent = user.profileName;

  // Toggle do menu do usuário
  const userMenuButton = document.getElementById("userMenuButton");
  const userDropdown = document.getElementById("userDropdown");

  if (userMenuButton && userDropdown) {
    userMenuButton.addEventListener("click", (e) => {
      e.stopPropagation();
      userDropdown.classList.toggle("active");
    });

    // Fechar dropdown ao clicar fora
    document.addEventListener("click", () => {
      userDropdown.classList.remove("active");
    });
  }

  // Logout
  const logoutButton = document.getElementById("logoutButton");
  if (logoutButton) {
    logoutButton.addEventListener("click", (e) => {
      e.preventDefault();
      if (confirm("Deseja realmente sair do sistema?")) {
        Auth.logout();
      }
    });
  }

  // Toggle da sidebar em mobile
  const sidebarToggle = document.getElementById("sidebarToggle");
  const sidebar = document.getElementById("sidebar");

  if (sidebarToggle && sidebar) {
    sidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("active");
    });
  }

  // Fechar sidebar ao clicar em um link (mobile)
  const navItems = document.querySelectorAll(".nav-item");
  navItems.forEach((item) => {
    item.addEventListener("click", () => {
      if (window.innerWidth <= 768 && sidebar) {
        sidebar.classList.remove("active");
      }
    });
  });

  // Filtrar itens do menu baseado nas permissões do usuário
  filterMenuByPermissions(user.permissions);
});

// Filtrar menu baseado nas permissões
function filterMenuByPermissions(permissions) {
  const navItems = document.querySelectorAll(".nav-item[data-permission]");

  navItems.forEach((item) => {
    const requiredPermission = item.dataset.permission;

    if (!permissions.includes("all") && !permissions.includes(requiredPermission)) {
      item.style.display = "none";
    }
  });
}
