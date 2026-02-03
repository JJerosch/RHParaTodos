// Configuração de autenticação - Compatível com Spring Boot
// Este arquivo deve ser carregado ANTES dos outros scripts

const AUTH_CONFIG = {
  // URL base do backend Spring Boot
  // Em desenvolvimento, ajuste conforme necessário
  API_BASE_URL: "http://localhost:8080/api/v1",
  ENDPOINTS: {
    LOGIN: "/auth/login",
    LOGOUT: "/auth/logout",
    REFRESH: "/auth/refresh",
    VALIDATE: "/auth/validate",
  },
};

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
  "funcionario": {
    name: "Funcionário",
    permissions: [],
    dashboard: "employee-dashboard.html",
  },
};

// Funções de autenticação
const Auth = {
  // Login do usuário (usando email como identificador)
  async login(email, password, profile) {
    try {
      // Tenta fazer login no backend Spring Boot
      const response = await fetch(`${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.LOGIN}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        // Backend aceita tanto "email" quanto "username" via @JsonAlias
        body: JSON.stringify({ email, password, ...(profile ? { profile } : {}) })
      });

      if (response.ok) {
        const data = await response.json();
        // Salvar dados do usuário no localStorage
        localStorage.setItem("user", JSON.stringify(data.user));
        localStorage.setItem("token", data.token);
        return { success: true, data: data };
      } else {
        const errorData = await response.json().catch(() => ({}));
        return { success: false, message: errorData.message || "Credenciais inválidas" };
      }
    } catch (error) {
      console.warn("Backend não disponível, usando modo simulado:", error.message);
      
      // Fallback: Simulação de login para desenvolvimento sem backend
      return this.loginSimulado(email, password, profile);
    }
  },

  // Login simulado para desenvolvimento
  loginSimulado(email, password, profile) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const selectedProfile = profile && USER_PROFILES[profile] ? profile : 'funcionario';
      if (email && password && USER_PROFILES[selectedProfile]) {
          const userData = {
            id: Date.now(),
            username: email, // Usando email como username
            email: email,
            profile: selectedProfile,
            profileName: USER_PROFILES[selectedProfile].name,
            permissions: USER_PROFILES[selectedProfile].permissions,
            token: "jwt_token_" + Math.random().toString(36).substr(2, 9),
            refreshToken: "refresh_token_" + Math.random().toString(36).substr(2, 9),
            loginTime: new Date().toISOString(),
          };

          // Salvar dados do usuário no localStorage
          localStorage.setItem("user", JSON.stringify(userData));
          localStorage.setItem("token", userData.token);

          resolve({ success: true, data: userData });
        } else {
          resolve({ success: false, message: "Credenciais inválidas" });
        }
      }, 500);
    });
  },

  // Logout do usuário
  async logout() {
    try {
      // Tenta fazer logout no backend
      await fetch(`${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.LOGOUT}`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${this.getToken()}`,
          'Content-Type': 'application/json'
        }
      });
    } catch (error) {
      console.warn("Erro ao fazer logout no servidor:", error.message);
    }
    
    // Limpa dados locais independente do resultado
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    window.location.href = "index.html";
  },

  // Verificar se o usuário está autenticado
  isAuthenticated() {
    const token = localStorage.getItem("token");
    const user = localStorage.getItem("user");
    return !!(token && user);
  },

  // Obter dados do usuário atual
  getCurrentUser() {
    const userStr = localStorage.getItem("user");
    return userStr ? JSON.parse(userStr) : null;
  },

  // Obter token
  getToken() {
    return localStorage.getItem("token");
  },

  // Verificar permissão
  hasPermission(permission) {
    const user = this.getCurrentUser();
    if (!user) return false;

    if (user.permissions.includes("all")) return true;
    return user.permissions.includes(permission);
  },

  // Proteger páginas
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = "index.html";
      return false;
    }
    return true;
  },
};

// Utilitário para requisições autenticadas ao Spring Boot
const ApiClient = {
  async request(endpoint, options = {}) {
    const token = Auth.getToken();
    const headers = {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    try {
      const response = await fetch(`${AUTH_CONFIG.API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
      });

      if (response.status === 401) {
        Auth.logout();
        throw new Error("Sessão expirada");
      }

      const data = await response.json();
      return { success: response.ok, data, status: response.status };
    } catch (error) {
      console.error("Erro na requisição:", error);
      return { success: false, error: error.message };
    }
  },

  get(endpoint) {
    return this.request(endpoint, { method: "GET" });
  },

  post(endpoint, body) {
    return this.request(endpoint, { method: "POST", body: JSON.stringify(body) });
  },

  put(endpoint, body) {
    return this.request(endpoint, { method: "PUT", body: JSON.stringify(body) });
  },

  delete(endpoint) {
    return this.request(endpoint, { method: "DELETE" });
  },
};

// Expor globalmente para uso em outros scripts
window.Auth = Auth;
window.ApiClient = ApiClient;
window.USER_PROFILES = USER_PROFILES;
window.AUTH_CONFIG = AUTH_CONFIG;
