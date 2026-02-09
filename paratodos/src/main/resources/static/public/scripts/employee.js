// Script simples para ponto eletrônico local (desenvolvimento)
(function () {
  document.addEventListener('DOMContentLoaded', init);

  function init() {
    if (!Auth.requireAuth()) return;
    const user = Auth.getCurrentUser();
    if (!user) { Auth.logout(); return; }

    // Permitir apenas EMPLOYEE (e ADMIN para debug) nesta página
    const role = Auth.getCurrentRole();
    if (!(role === 'EMPLOYEE' || role === 'ADMIN')) {
      window.location.href = Auth.redirectAfterLogin(user);
      return;
    }

    const displayName = user.username || user.email || 'Funcionário';
    const displayProfile = Auth.getProfileDisplayName(user);

    const userNameEl = document.getElementById('userName');
    if (userNameEl) userNameEl.textContent = displayName;
    const userNameInlineEl = document.getElementById('userNameInline');
    if (userNameInlineEl) userNameInlineEl.textContent = displayName;

    const userProfileEl = document.getElementById('userProfile');
    if (userProfileEl) userProfileEl.textContent = displayProfile;
    const userProfileInlineEl = document.getElementById('userProfileInline');
    if (userProfileInlineEl) userProfileInlineEl.textContent = displayProfile;

    document.getElementById('btnLogout').addEventListener('click', () => Auth.logout());
    document.getElementById('btnPunch').addEventListener('click', punch);
    document.getElementById('btnRefresh').addEventListener('click', render);

    // Sidebar / nav buttons (Home e Holerites)
    const navHome = document.getElementById('nav-timesheet');
    const navPayslips = document.getElementById('nav-payslips');
    if (navHome) navHome.addEventListener('click', (e) => {
      e.preventDefault();
      const punchEl = document.getElementById('btnPunch');
      if (punchEl) { punchEl.scrollIntoView({ behavior: 'smooth', block: 'center' }); punchEl.classList.add('pulse'); setTimeout(() => punchEl.classList.remove('pulse'), 700); }
      document.querySelectorAll('.sidebar-nav .nav-item').forEach(a => a.classList.remove('active'));
      navHome.classList.add('active');
    });
    if (navPayslips) navPayslips.addEventListener('click', (e) => { /* navegar para página de holerites processados */ });
    // Se o link for clicado naturalmente, o navegador abre payslips.html (href configurado).

    // user avatar + logout
    const avatarEl = document.getElementById('userAvatar');
    if (avatarEl) {
      const nameParts = (user.username || user.email || '').split(' ').filter(Boolean);
      avatarEl.textContent = (nameParts.length ? (nameParts[0][0] + (nameParts[1] ? nameParts[1][0] : '')) : 'U').toUpperCase();
    }
    const userFullNameEl = document.getElementById('userFullName');
    if (userFullNameEl) userFullNameEl.textContent = user.username || user.email || 'Funcionário';
    const logoutBtn = document.getElementById('logoutButton');
    if (logoutBtn) logoutBtn.addEventListener('click', (e) => { e.preventDefault(); if (confirm('Deseja realmente sair do sistema?')) Auth.logout(); });

    // User menu toggle (dropdown)
    const userMenuButton = document.getElementById('userMenuButton');
    const userDropdown = document.getElementById('userDropdown');
    if (userMenuButton && userDropdown) {
      userMenuButton.addEventListener('click', (e) => { e.stopPropagation(); userDropdown.classList.toggle('active'); });
      document.addEventListener('click', () => { userDropdown.classList.remove('active'); });
    }

    // Mobile sidebar toggle
    const sidebar = document.getElementById('sidebar');
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    if (mobileMenuBtn && sidebar) mobileMenuBtn.addEventListener('click', () => sidebar.classList.toggle('active'));

    // Close sidebar on mobile when clicking a nav item
    document.querySelectorAll('.nav-item').forEach((item) => {
      item.addEventListener('click', () => { if (window.innerWidth <= 768 && sidebar) sidebar.classList.remove('active'); });
    });

    // Modal close handlers
    const closeBtn = document.getElementById('closePayslipModal');
    if (closeBtn) closeBtn.addEventListener('click', closePayslipModal);
    const modal = document.getElementById('payslipModal');
    if (modal) modal.addEventListener('click', (e) => { if (e.target === modal) closePayslipModal(); });

    render();
  }

  function storageKey() {
    const user = Auth.getCurrentUser();
    return `punches_user_${user.id || user.email}`;
  }

  function loadPunches() {
    try {
      return JSON.parse(localStorage.getItem(storageKey()) || '[]');
    } catch (e) { return []; }
  }

  function savePunches(list) {
    localStorage.setItem(storageKey(), JSON.stringify(list));
  }

  function punch() {
    const list = loadPunches();
    const last = list[list.length - 1];
    const nextType = (last && last.type === 'in') ? 'out' : 'in';
    const now = new Date();
    const rec = { timestamp: now.toISOString(), type: nextType };
    list.push(rec);
    savePunches(list);
    showAlert('success', `Ponto registrado: ${nextType.toUpperCase()} às ${formatTime(now)}`);
    render();
  }

  function render() {
    const list = loadPunches();
    const last = list[list.length - 1];
    document.getElementById('lastPunch').textContent = last ? `${last.type.toUpperCase()} — ${formatDateTime(new Date(last.timestamp))}` : 'Nenhum registro';
    renderTable(list.filter(p => isCurrentMonth(new Date(p.timestamp))));
  }

  function renderTable(list) {
    const tbody = document.getElementById('punchTableBody');
    tbody.innerHTML = '';
    if (!list.length) {
      const tr = document.createElement('tr');
      tr.innerHTML = '<td colspan="3">Nenhum registro para o mês atual.</td>';
      tbody.appendChild(tr);
      return;
    }

    list.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    list.forEach(p => {
      const d = new Date(p.timestamp);
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${formatDate(d)}</td><td>${formatTime(d)}</td><td>${p.type.toUpperCase()}</td>`;
      tbody.appendChild(tr);
    });
  }

  function isCurrentMonth(d) {
    const now = new Date();
    return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth();
  }

  function formatDate(d) { return d.toLocaleDateString('pt-BR'); }
  function formatTime(d) { return d.toLocaleTimeString('pt-BR'); }
  function formatDateTime(d) { return `${formatDate(d)} ${formatTime(d)}`; }

  // Payslip utilities
  function getPayslipStorageKey() { const user = Auth.getCurrentUser(); return `payslips_user_${user.id || user.email}`; }
  function loadPayslips() { try { return JSON.parse(localStorage.getItem(getPayslipStorageKey()) || '[]'); } catch (e) { return []; } }
  function savePayslips(list) { localStorage.setItem(getPayslipStorageKey(), JSON.stringify(list)); }
  function ensurePayslipsData() {
    let list = loadPayslips();
    if (!list.length) {
      const now = new Date();
      list = [0,1,2].map(i => {
        const d = new Date(now.getFullYear(), now.getMonth()-i, 1);
        return { id: 'p'+d.getFullYear()+''+(d.getMonth()+1), month: d.getMonth()+1, year: d.getFullYear(), gross: (2000 + Math.floor(Math.random()*1000)), net: (1500 + Math.floor(Math.random()*800)) };
      });
      savePayslips(list);
    }
  }

  function renderPayslips(list) {
    const container = document.getElementById('payslipList');
    container.innerHTML = '';
    if (!list.length) { container.innerHTML = '<div class="muted">Nenhum holerite disponível.</div>'; return; }
    const table = document.createElement('table');
    table.style.width = '100%';
    table.innerHTML = '<thead><tr><th>Mês</th><th>Ano</th><th>Valor líquido</th><th></th></tr></thead>';
    const tb = document.createElement('tbody');
    list.forEach(p => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${String(p.month).padStart(2,'0')}</td><td>${p.year}</td><td>R$ ${p.net.toFixed(2)}</td><td><button class="btn btn-primary" data-id="${p.id}">Baixar</button></td>`;
      tb.appendChild(tr);
    });
    table.appendChild(tb);
    container.appendChild(table);
    container.querySelectorAll('button[data-id]').forEach(b => b.addEventListener('click', (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      const p = list.find(x => x.id === id);
      if (p) downloadPayslip(p);
    }));
  }

  function openPayslips() {
    ensurePayslipsData();
    const list = loadPayslips();
    renderPayslips(list);
    const modal = document.getElementById('payslipModal');
    if (modal) { modal.classList.add('active'); modal.setAttribute('aria-hidden', 'false'); }
  }

  function closePayslipModal() {
    const modal = document.getElementById('payslipModal');
    if (modal) { modal.classList.remove('active'); modal.setAttribute('aria-hidden', 'true'); }
  }

  function downloadPayslip(p) {
    const content = `Holerite - ${String(p.month).padStart(2,'0')}/${p.year}\nValor Líquido: R$ ${p.net.toFixed(2)}\nValor Bruto: R$ ${p.gross.toFixed(2)}\n\nEmitido: ${new Date().toLocaleDateString('pt-BR')}`;
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `holerite_${p.year}_${String(p.month).padStart(2,'0')}.txt`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    showAlert('success', 'Download iniciado');
  }

  function showAlert(type, msg) {
    const el = document.getElementById(type === 'error' ? 'alertError' : 'alertSuccess');
    if (!el) return;
    el.textContent = msg;
    el.style.display = 'block';
    if (type === 'success') setTimeout(() => el.style.display = 'none', 3000);
  }
})();
