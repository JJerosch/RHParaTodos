// ==========================================
// AUTH.JS — Compatível com JWT HttpOnly Cookie
// Os dados do usuário são injetados pelo Thymeleaf
// via window.__CURRENT_USER__ no template.
// ==========================================

const ROLE_DISPLAY = {
  ADMIN:          "Administrador do Sistema",
  RH_CHEFE:       "Chefe de RH",
  RH_ASSISTENTE:  "Assistente de RH",
  DP_CHEFE:       "Chefe de DP",
  DP_ASSISTENTE:  "Assistente de DP",
  EMPLOYEE:       "Funcionário",
};

function normalizeRole(role) {
  return String(role || "").toUpperCase().trim();
}

function parseRolesAttr(value) {
  return String(value || "")
    .split(",")
    .map((s) => normalizeRole(s))
    .filter(Boolean);
}

const Auth = {
  // ---- Dados do usuário (injetados pelo backend via Thymeleaf) ----
  getCurrentUser() {
    return window.__CURRENT_USER__ || null;
  },

  getCurrentRole() {
    const user = this.getCurrentUser();
    return user ? normalizeRole(user.role) : null;
  },

  isAuthenticated() {
    return !!this.getCurrentUser();
  },

  getProfileDisplayName(user) {
    const u = user || this.getCurrentUser();
    if (!u) return "";
    return ROLE_DISPLAY[normalizeRole(u.role)] || u.role || "—";
  },

  // ---- Proteção de páginas ----
  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = "/login";
      return false;
    }
    return true;
  },

  hasAnyRole(allowedRoles = []) {
    const role = this.getCurrentRole();
    if (!role) return false;
    return allowedRoles.map(normalizeRole).includes(role);
  },

  requireRole(allowedRoles = [], redirectTo = "/meu-ponto") {
    if (!this.requireAuth()) return false;
    if (!this.hasAnyRole(allowedRoles)) {
      window.location.href = redirectTo;
      return false;
    }
    return true;
  },

  // ---- Logout ----
  logout() {
    // Chama o endpoint de logout do Spring Security (limpa o cookie)
    const form = document.createElement("form");
    form.method = "POST";
    form.action = "/logout";

    // CSRF token (Spring Security exige)
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    if (csrfMeta) {
      const input = document.createElement("input");
      input.type = "hidden";
      input.name = csrfMeta.getAttribute("name") || "_csrf";
      input.value = csrfMeta.getAttribute("content");
      form.appendChild(input);
    }

    document.body.appendChild(form);
    form.submit();
  },

  // ---- Visibilidade por role (data-roles="ADMIN,RH_CHEFE,...") ----
  applyRoleVisibility(root = document) {
    const role = this.getCurrentRole();
    if (!role) return;

    root.querySelectorAll("[data-roles]").forEach((el) => {
      const allowed = parseRolesAttr(el.getAttribute("data-roles"));
      el.style.display = allowed.includes(role) ? "" : "none";
    });

    // Oculta seções do sidebar sem itens visíveis
    root.querySelectorAll(".sidebar-nav .nav-section").forEach((section) => {
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

  // ---- Visibilidade de ações por role (data-action-roles="...") ----
  // Usado para esconder botões como "Excluir" de quem não tem permissão
  applyActionVisibility(root = document) {
    const role = this.getCurrentRole();
    if (!role) return;

    root.querySelectorAll("[data-action-roles]").forEach((el) => {
      const allowed = parseRolesAttr(el.getAttribute("data-action-roles"));
      if (!allowed.includes(role)) {
        el.remove(); // Remove do DOM, não só esconde
      }
    });
  },

  // ---- Redirecionamento após login (usado pelo backend, mantido para compatibilidade) ----
  redirectAfterLogin(user) {
    const role = normalizeRole(user?.role) || this.getCurrentRole();
    return role === "EMPLOYEE" ? "/meu-ponto" : "/dashboard";
  },

  // ---- Compatibilidade legada (não utiliza mais localStorage) ----
  getToken() { return null; }, // JWT está no HttpOnly cookie, inacessível via JS
  hasPermission() { return true; }, // Permissões agora são por role
};

// Utilitário de requisições autenticadas
// O cookie é enviado automaticamente pelo browser
const ApiClient = {
  async request(endpoint, options = {}) {
    try {
      const response = await fetch(`/api/v1${endpoint}`, {
        ...options,
        credentials: "same-origin", // envia o cookie JWT
        headers: {
          "Content-Type": "application/json",
          ...options.headers,
        },
      });

      if (response.status === 401) {
        window.location.href = "/login";
        throw new Error("Sessão expirada");
      }

      const data = await response.json().catch(() => ({}));
      return { success: response.ok, data, status: response.status };
    } catch (error) {
      console.error("Erro na requisição:", error);
      return { success: false, error: error.message };
    }
  },

  get(endpoint)         { return this.request(endpoint, { method: "GET" }); },
  post(endpoint, body)  { return this.request(endpoint, { method: "POST",   body: JSON.stringify(body) }); },
  put(endpoint, body)   { return this.request(endpoint, { method: "PUT",    body: JSON.stringify(body) }); },
  delete(endpoint)      { return this.request(endpoint, { method: "DELETE" }); },
};

window.Auth = Auth;
window.ApiClient = ApiClient;
window.ROLE_DISPLAY = ROLE_DISPLAY;