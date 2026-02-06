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
    ME: "/me", // recomendado (retorna {id,nome,email,role})
  },
};

// Perfis locais (legado do front). Mantido para compatibilidade.
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
  funcionario: {
    name: "Funcionário",
    permissions: [],
    dashboard: "employee-dashboard.html",
  },
};

// Mapeamento entre roles do backend e perfis do front (legado)
const ROLE_TO_PROFILE = {
  ADMIN: "admin",
  RH_CHEFE: "rh-chefe",
  RH_ASSISTENTE: "rh-assistente",
  DP_CHEFE: "dp-chefe",
  DP_ASSISTENTE: "dp-assistente",
  EMPLOYEE: "funcionario",
};

const PROFILE_TO_ROLE = Object.keys(ROLE_TO_PROFILE).reduce((acc, role) => {
  acc[ROLE_TO_PROFILE[role]] = role;
  return acc;
}, {});

function normalizeRole(role) {
  return String(role || "").toUpperCase().trim();
}

function parseRolesAttr(value) {
  return String(value || "")
    .split(",")
    .map((s) => normalizeRole(s))
    .filter(Boolean);
}

// Funções de autenticação
const Auth = {
  // Login do usuário (usando email como identificador)
  async login(email, password, profile) {
    try {
      // Tenta fazer login no backend Spring Boot
      const response = await fetch(
        `${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.LOGIN}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          // Backend pode aceitar "email" ou "username" via @JsonAlias
          body: JSON.stringify({ email, password, ...(profile ? { profile } : {}) }),
        }
      );

      if (response.ok) {
        const data = await response.json();

        // Aceita formatos:
        // A) { token, user }
        // B) { token } e depois busca /me
        let user = data.user;

        if (!user && data.token) {
          // tenta buscar /me (se implementado no backend)
          try {
            const meResp = await fetch(
              `${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.ME}`,
              { headers: { Authorization: `Bearer ${data.token}` } }
            );
            if (meResp.ok) user = await meResp.json();
          } catch (_) {}
        }

        // Normaliza role/profile
        user = this._normalizeUser(user, email);

        // Salvar dados do usuário no localStorage
        localStorage.setItem("user", JSON.stringify(user));
        if (data.token) localStorage.setItem("token", data.token);

        return { success: true, data: { user, token: data.token } };
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
        // Se profile não vier, inferimos pelo email (admin/rh/dp) para não cair sempre em funcionário
        const inferredRole = this._inferRoleFromEmail(email);
        const selectedProfile =
          (profile && USER_PROFILES[profile] && profile) ||
          ROLE_TO_PROFILE[inferredRole] ||
          "funcionario";

        if (email && password && USER_PROFILES[selectedProfile]) {
          const role = PROFILE_TO_ROLE[selectedProfile] || "EMPLOYEE";
          const userData = {
            id: Date.now(),
            username: email, // Usando email como username
            email: email,
            // campos legados
            profile: selectedProfile,
            profileName: USER_PROFILES[selectedProfile].name,
            permissions: USER_PROFILES[selectedProfile].permissions,
            // campos novos (para RBAC real)
            role: role,
            token: "jwt_token_" + Math.random().toString(36).substr(2, 9),
            refreshToken: "refresh_token_" + Math.random().toString(36).substr(2, 9),
            loginTime: new Date().toISOString(),
          };

          localStorage.setItem("user", JSON.stringify(userData));
          localStorage.setItem("token", userData.token);

          resolve({ success: true, data: userData });
        } else {
          resolve({ success: false, message: "Credenciais inválidas" });
        }
      }, 300);
    });
  },

  _inferRoleFromEmail(email) {
    const e = String(email || "").toLowerCase();
    if (e.includes("admin")) return "ADMIN";
    if (e.includes("rh")) return "RH_CHEFE";
    if (e.includes("dp")) return "DP_CHEFE";
    return "EMPLOYEE";
  },

  _normalizeUser(user, fallbackEmail) {
    const u = user || {};
    const email = u.email || u.username || fallbackEmail || "";
    const role = normalizeRole(u.role) || normalizeRole(PROFILE_TO_ROLE[u.profile]) || this._inferRoleFromEmail(email);
    const profile = u.profile || ROLE_TO_PROFILE[role] || "funcionario";
    const profileName = u.profileName || USER_PROFILES[profile]?.name || role;

    return {
      ...u,
      email,
      username: u.username || email,
      role,
      profile,
      profileName,
      permissions: u.permissions || USER_PROFILES[profile]?.permissions || [],
    };
  },

  // Logout do usuário
  async logout() {
    try {
      await fetch(`${AUTH_CONFIG.API_BASE_URL}${AUTH_CONFIG.ENDPOINTS.LOGOUT}`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${this.getToken()}`,
          "Content-Type": "application/json",
        },
      });
    } catch (error) {
      console.warn("Erro ao fazer logout no servidor:", error.message);
    }

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

  getCurrentRole() {
    const user = this.getCurrentUser();
    if (!user) return null;
    return normalizeRole(user.role) || normalizeRole(PROFILE_TO_ROLE[user.profile]) || null;
  },

  getProfileDisplayName(user) {
    if (!user) return "";
    return user.profileName || USER_PROFILES[user.profile]?.name || user.profile || user.role || "—";
  },

  redirectAfterLogin(user) {
    const role = normalizeRole(user?.role) || this.getCurrentRole();

    if (role === "EMPLOYEE") return "employee-dashboard.html";
    // ADMIN / RH_* / DP_* -> dashboard principal
    return "dashboard.html";
  },

  hasAnyRole(allowedRoles = []) {
    const role = this.getCurrentRole();
    if (!role) return false;
    return allowedRoles.map(normalizeRole).includes(role);
  },

  // Proteger páginas por role
  requireRole(allowedRoles = [], redirectTo = "dashboard.html") {
    if (!this.requireAuth()) return false;
    if (!this.hasAnyRole(allowedRoles)) {
      // Usuário autenticado mas sem permissão
      window.location.href = redirectTo;
      return false;
    }
    return true;
  },

  // Verificar permissão (legado)
  hasPermission(permission) {
    const user = this.getCurrentUser();
    if (!user) return false;

    if (Array.isArray(user.permissions) && user.permissions.includes("all")) return true;
    return Array.isArray(user.permissions) && user.permissions.includes(permission);
  },

  // Proteger páginas (apenas autenticado)
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = "index.html";
      return false;
    }
    return true;
  },

  // Esconder/mostrar elementos por role usando data-roles="ADMIN,RH_CHEFE,..."
  applyRoleVisibility(root = document) {
    const role = this.getCurrentRole();
    if (!role) return;

    root.querySelectorAll("[data-roles]").forEach((el) => {
      const allowed = parseRolesAttr(el.getAttribute("data-roles"));
      el.style.display = allowed.includes(role) ? "" : "none";
    });

    // Oculta seções vazias na sidebar (quando todos os itens da seção foram escondidos)
    root.querySelectorAll(".sidebar-nav .nav-section").forEach((section) => {
      // Procura se há algum nav-item visível até a próxima seção
      let el = section.nextElementSibling;
      let hasVisible = false;

      while (el && !el.classList.contains("nav-section")) {
        if (el.classList.contains("nav-item") && el.style.display !== "none") {
          hasVisible = true;
          break;
        }
        el = el.nextElementSibling;
      }

      section.style.display = hasVisible ? "" : "none";
    });
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

      const data = await response.json().catch(() => ({}));
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
