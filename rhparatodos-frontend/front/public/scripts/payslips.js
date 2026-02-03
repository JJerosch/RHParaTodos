document.addEventListener('DOMContentLoaded', initPayslips);

function initPayslips() {
  if (!Auth.requireAuth()) return;
  const user = Auth.getCurrentUser();
  if (!user) { Auth.logout(); return; }

  // UI refs
  const tbody = document.getElementById('payslipsTbody');
  const totalCount = document.getElementById('totalCount');
  const searchInput = document.getElementById('searchInput');
  const refreshBtn = document.getElementById('btnRefreshPayslips');

  // render user info
  const avatarEl = document.getElementById('userAvatar');
  if (avatarEl) {
    const nameParts = (user.username || user.email || '').split(' ').filter(Boolean);
    avatarEl.textContent = (nameParts.length ? (nameParts[0][0] + (nameParts[1] ? nameParts[1][0] : '')) : 'U').toUpperCase();
  }
  const userNameEl = document.getElementById('userName'); if (userNameEl) userNameEl.textContent = user.username || user.email;
  const userFullNameEl = document.getElementById('userFullName'); if (userFullNameEl) userFullNameEl.textContent = user.username || user.email || 'Usuário';
  const userProfileEl = document.getElementById('userProfile'); if (userProfileEl) userProfileEl.textContent = user.profileName || user.profile;

  // logout handler
  const logoutBtn = document.getElementById('logoutButton');
  if (logoutBtn) logoutBtn.addEventListener('click', (e) => { e.preventDefault(); if (confirm('Deseja realmente sair do sistema?')) Auth.logout(); });

  // sample/process data store (global processed list)
  function processedKey() { return 'processed_payslips'; }
  function ensureData() {
    let list = JSON.parse(localStorage.getItem(processedKey()) || '[]');
    if (!list.length) {
      // generate sample processed payslips for last 4 months for a few users
      const now = new Date();
      const customers = [ { id: user.id || user.email, name: user.username || user.email } ];
      if (user.permissions && user.permissions.includes('all')) {
        customers.push({ id: 'user2', name: 'Maria Santos' }, { id: 'user3', name: 'João Silva' });
      }

      list = [];
      customers.forEach((c, idx) => {
        for (let m = 0; m < 4; m++) {
          const d = new Date(now.getFullYear(), now.getMonth() - m, 1);
          list.push({ id: `hp_${c.id}_${d.getFullYear()}${String(d.getMonth()+1).padStart(2,'0')}`, employeeId: c.id, employeeName: c.name, month: d.getMonth()+1, year: d.getFullYear(), gross: 2000 + Math.floor(Math.random()*1200), net: 1500 + Math.floor(Math.random()*900), processedAt: new Date(d.getFullYear(), d.getMonth(), Math.floor(Math.random()*25)+1).toISOString() });
        }
      });
      localStorage.setItem(processedKey(), JSON.stringify(list));
    }
  }

  function loadProcessed() { try { return JSON.parse(localStorage.getItem(processedKey()) || '[]'); } catch (e) { return []; } }

  function filterAndRender() {
    let list = loadProcessed();
    // if not admin/all, show only own
    if (!user.permissions || !user.permissions.includes('all')) {
      list = list.filter(p => p.employeeId === (user.id || user.email));
    }

    const q = searchInput.value.trim().toLowerCase();
    if (q) {
      list = list.filter(p => `${p.employeeName}`.toLowerCase().includes(q) || (`${p.month}/${p.year}`).toLowerCase().includes(q) || p.id.toLowerCase().includes(q));
    }

    list.sort((a,b) => new Date(b.processedAt) - new Date(a.processedAt));

    renderTable(list);
  }

  function renderTable(list) {
    tbody.innerHTML = '';
    if (!list.length) { tbody.innerHTML = '<tr><td colspan="7">Nenhum holerite processado.</td></tr>'; totalCount.textContent = '0'; return; }
    totalCount.textContent = String(list.length);

    list.forEach(p => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${p.id}</td><td>${escapeHtml(p.employeeName)}</td><td>${String(p.month).padStart(2,'0')}/${p.year}</td><td>R$ ${p.gross.toFixed(2)}</td><td>R$ ${p.net.toFixed(2)}</td><td>${new Date(p.processedAt).toLocaleDateString('pt-BR')}</td><td><button class="btn btn-primary" data-id="${p.id}">Baixar</button></td>`;
      tbody.appendChild(tr);
    });

    tbody.querySelectorAll('button[data-id]').forEach(b => b.addEventListener('click', (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      const p = loadProcessed().find(x => x.id === id);
      if (p) downloadPayslip(p);
    }));
  }

  function downloadPayslip(p) {
    const content = `Holerite Processado - ${String(p.month).padStart(2,'0')}/${p.year}\nFuncionário: ${p.employeeName}\nValor Líquido: R$ ${p.net.toFixed(2)}\nValor Bruto: R$ ${p.gross.toFixed(2)}\nProcessado em: ${new Date(p.processedAt).toLocaleDateString('pt-BR')}`;
    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `holerite_${p.id}.txt`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  }

  function escapeHtml(s) { return String(s).replace(/[&<>"']/g, (c) => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

  // init
  ensureData();
  filterAndRender();

  // events
  searchInput.addEventListener('input', filterAndRender);
  refreshBtn.addEventListener('click', () => { ensureData(); filterAndRender(); showAlert('success', 'Dados atualizados'); });
}
