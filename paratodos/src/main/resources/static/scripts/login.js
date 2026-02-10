// Script para a página de login
// NOTA: Auth e USER_PROFILES são carregados globalmente via auth.js

document.addEventListener("DOMContentLoaded", () => {
  // Verificar se já está autenticado
  if (Auth.isAuthenticated()) {
    const user = Auth.getCurrentUser();
    const profile = USER_PROFILES[user.profile];
    if (profile) {
      window.location.href = profile.dashboard;
    } else {
      window.location.href = "dashboard.html";
    }
    return;
  }

  const loginForm = document.getElementById("loginForm");
  const alertContainer = document.getElementById("alertContainer");

  // Mostrar mensagem de alerta
  function showAlert(message, type = "error") {
    const alertClass = type === "success" ? "alert-success" : "alert-error";
    alertContainer.innerHTML = `
      <div class="alert ${alertClass}">
        ${message}
      </div>
    `;

    setTimeout(() => {
      alertContainer.innerHTML = "";
    }, 5000);
  }

  // Processar login
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const profile = document.getElementById("profile").value;

    if (!email || !password || !profile) {
      showAlert("Por favor, preencha todos os campos");
      return;
    }

    // Verificar se o perfil existe
    if (!USER_PROFILES[profile]) {
      showAlert("Perfil de acesso inválido");
      return;
    }

    // Desabilitar botão durante o login
    const submitButton = loginForm.querySelector('button[type="submit"]');
    const originalText = submitButton.textContent;
    submitButton.disabled = true;
    submitButton.textContent = "Entrando...";

    try {
      const result = await Auth.login(email, password, profile);

      if (result.success) {
        showAlert("Login realizado com sucesso!", "success");

        // Redirecionar para o dashboard
        setTimeout(() => {
          window.location.href = USER_PROFILES[profile].dashboard;
        }, 500);
      } else {
        showAlert(result.message || "Erro ao realizar login");
        submitButton.disabled = false;
        submitButton.textContent = originalText;
      }
    } catch (error) {
      console.error("Erro no login:", error);
      showAlert("Erro ao conectar com o servidor");
      submitButton.disabled = false;
      submitButton.textContent = originalText;
    }
  });

  // Easter egg: preencher formulário para testes (remover em produção)
  if (window.location.search.includes("test=1")) {
    document.getElementById("email").value = "admin@rhparatodos.com.br";
    document.getElementById("password").value = "admin123";
    document.getElementById("profile").value = "admin";
  }
});
