// Script para a página de login
document.addEventListener("DOMContentLoaded", () => {
  // Declare Auth and USER_PROFILES variables here
  const Auth = {
    isAuthenticated: () => {
      // Implement your authentication check logic here
      return false // Placeholder return value
    },
    getCurrentUser: () => {
      // Implement your get current user logic here
      return { profile: "admin" } // Placeholder return value
    },
    login: async (username, password, profile) => {
      // Implement your login logic here
      return { success: true } // Placeholder return value
    },
  }

  const USER_PROFILES = {
    admin: { dashboard: "/admin/dashboard" },
    user: { dashboard: "/user/dashboard" },
    // Add other profiles as needed
  }

  // Verificar se já está autenticado
  if (Auth.isAuthenticated()) {
    const user = Auth.getCurrentUser()
    window.location.href = USER_PROFILES[user.profile].dashboard
    return
  }

  const loginForm = document.getElementById("loginForm")
  const alertContainer = document.getElementById("alertContainer")

  // Mostrar mensagem de alerta
  function showAlert(message, type = "error") {
    const alertClass = type === "success" ? "alert-success" : "alert-error"
    alertContainer.innerHTML = `
      <div class="alert ${alertClass}">
        ${message}
      </div>
    `

    setTimeout(() => {
      alertContainer.innerHTML = ""
    }, 5000)
  }

  // Processar login
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault()

    const username = document.getElementById("username").value.trim()
    const password = document.getElementById("password").value
    const profile = document.getElementById("profile").value

    if (!username || !password || !profile) {
      showAlert("Por favor, preencha todos os campos")
      return
    }

    // Desabilitar botão durante o login
    const submitButton = loginForm.querySelector('button[type="submit"]')
    submitButton.disabled = true
    submitButton.textContent = "Entrando..."

    try {
      const result = await Auth.login(username, password, profile)

      if (result.success) {
        showAlert("Login realizado com sucesso!", "success")

        // Redirecionar para o dashboard
        setTimeout(() => {
          window.location.href = USER_PROFILES[profile].dashboard
        }, 1000)
      } else {
        showAlert(result.message || "Erro ao realizar login")
        submitButton.disabled = false
        submitButton.textContent = "Entrar no Sistema"
      }
    } catch (error) {
      showAlert("Erro ao conectar com o servidor")
      submitButton.disabled = false
      submitButton.textContent = "Entrar no Sistema"
    }
  })

  // Easter egg: preencher formulário para testes (remover em produção)
  if (window.location.search.includes("test=1")) {
    document.getElementById("username").value = "admin"
    document.getElementById("password").value = "admin123"
    document.getElementById("profile").value = "admin"
  }
})
