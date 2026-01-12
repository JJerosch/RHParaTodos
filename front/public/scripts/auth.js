// Simulação de autenticação - Compatível com Spring Boot
// Em produção, este arquivo fará requisições para endpoints REST do Spring Boot

const AUTH_CONFIG = {
  // Configurações para integração com Spring Boot
  API_BASE_URL: "/api/v1",
  ENDPOINTS: {
    LOGIN: "/auth/login",
    LOGOUT: "/auth/logout",
    REFRESH: "/auth/refresh",
    VALIDATE: "/auth/validate",
  },
}

// Perfis de usuário e suas permissões
const USER_PROFILES = {
  admin: {
    name: "Administrador do Sistema",
    permissions: ["all"],
    dashboard: "dashboard.html",
  },
  "rh-chefe": {
    name: "Chefe de RH",
    permissions: ["employees", "recruitment", "performance", "training", "reports"],
    dashboard: "dashboard.html",
  },
  "rh-assistente": {
    name: "Assistente de RH",
    permissions: ["employees-read", "recruitment-support", "onboarding"],
    dashboard: "dashboard.html",
  },
  "dp-chefe": {
    name: "Chefe de DP",
    permissions: ["payroll", "benefits", "vacation", "reports-financial"],
    dashboard: "dashboard.html",
  },
  "dp-assistente": {
    name: "Assistente de DP",
    permissions: ["payroll-entry", "benefits-read"],
    dashboard: "dashboard.html",
  },
}

// Funções de autenticação
const Auth = {
  // Login do usuário
  async login(username, password, profile) {
    try {
      // Em produção, fazer requisição POST para Spring Boot
      // const response = await fetch(`${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.LOGIN}`, {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json' },
      //   body: JSON.stringify({ username, password, profile })
      // });

      // Simulação de login para desenvolvimento
      return new Promise((resolve) => {
        setTimeout(() => {
          if (username && password && profile) {
            const userData = {
              id: Date.now(),
              username: username,
              profile: profile,
              profileName: USER_PROFILES[profile].name,
              permissions: USER_PROFILES[profile].permissions,
              token: "jwt_token_" + Math.random().toString(36).substr(2, 9),
              refreshToken: "refresh_token_" + Math.random().toString(36).substr(2, 9),
              loginTime: new Date().toISOString(),
            }

            // Salvar dados do usuário no localStorage
            localStorage.setItem("user", JSON.stringify(userData))
            localStorage.setItem("token", userData.token)

            resolve({ success: true, data: userData })
          } else {
            resolve({ success: false, message: "Credenciais inválidas" })
          }
        }, 500)
      })
    } catch (error) {
      return { success: false, message: "Erro ao conectar com o servidor" }
    }
  },

  // Logout do usuário
  logout() {
    // Em produção, fazer requisição para Spring Boot
    // fetch(`${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.LOGOUT}`, {
    //   method: 'POST',
    //   headers: { 'Authorization': `Bearer ${this.getToken()}` }
    // });

    localStorage.removeItem("user")
    localStorage.removeItem("token")
    window.location.href = "index.html"
  },

  // Verificar se o usuário está autenticado
  isAuthenticated() {
    const token = localStorage.getItem("token")
    const user = localStorage.getItem("user")
    return !!(token && user)
  },

  // Obter dados do usuário atual
  getCurrentUser() {
    const userStr = localStorage.getItem("user")
    return userStr ? JSON.parse(userStr) : null
  },

  // Obter token
  getToken() {
    return localStorage.getItem("token")
  },

  // Verificar permissão
  hasPermission(permission) {
    const user = this.getCurrentUser()
    if (!user) return false

    if (user.permissions.includes("all")) return true
    return user.permissions.includes(permission)
  },

  // Proteger páginas
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = "index.html"
      return false
    }
    return true
  },
}

// Utilitário para requisições autenticadas ao Spring Boot
const ApiClient = {
  async request(endpoint, options = {}) {
    const token = Auth.getToken()
    const headers = {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    }

    try {
      const response = await fetch(`${AUTH_CONFIG.API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
      })

      if (response.status === 401) {
        Auth.logout()
        throw new Error("Sessão expirada")
      }

      const data = await response.json()
      return { success: response.ok, data, status: response.status }
    } catch (error) {
      return { success: false, error: error.message }
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" })
  },

  post(endpoint, body) {
    return this.request(endpoint, { method: "POST", body: JSON.stringify(body) })
  },

  put(endpoint, body) {
    return this.request(endpoint, { method: "PUT", body: JSON.stringify(body) })
  },

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" })
  },
}
