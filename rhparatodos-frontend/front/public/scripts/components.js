// ==========================================
// COMPONENTES E UTILITÁRIOS JAVASCRIPT
// ==========================================

// Toast Notifications
const Toast = {
  container: null,

  init() {
    if (!this.container) {
      this.container = document.createElement('div');
      this.container.className = 'toast-container';
      document.body.appendChild(this.container);
    }
  },

  show(message, type = 'info', duration = 4000) {
    this.init();
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <span>${message}</span>
      <button class="toast-close">&times;</button>
    `;
    
    toast.querySelector('.toast-close').onclick = () => this.hide(toast);
    this.container.appendChild(toast);
    
    if (duration > 0) {
      setTimeout(() => this.hide(toast), duration);
    }
    
    return toast;
  },

  hide(toast) {
    toast.style.animation = 'toastSlideIn 0.3s ease reverse';
    setTimeout(() => toast.remove(), 300);
  },

  success(message) { return this.show(message, 'success'); },
  error(message) { return this.show(message, 'error'); },
  warning(message) { return this.show(message, 'warning'); },
  info(message) { return this.show(message, 'info'); }
};

// Modal Manager
const Modal = {
  open(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('active');
      document.body.style.overflow = 'hidden';
    }
  },

  close(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('active');
      document.body.style.overflow = '';
    }
  },

  closeAll() {
    document.querySelectorAll('.modal.active').forEach(modal => {
      modal.classList.remove('active');
    });
    document.body.style.overflow = '';
  },

  init() {
    // Fechar modal ao clicar fora
    document.addEventListener('click', (e) => {
      if (e.target.classList.contains('modal')) {
        this.closeAll();
      }
    });

    // Fechar modal com ESC
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        this.closeAll();
      }
    });

    // Botões de fechar
    document.querySelectorAll('.modal-close, [data-modal-close]').forEach(btn => {
      btn.addEventListener('click', () => this.closeAll());
    });
  }
};

// Tabs Component
const Tabs = {
  init() {
    document.querySelectorAll('.tabs').forEach(tabContainer => {
      const buttons = tabContainer.querySelectorAll('.tab-btn');
      const contentId = tabContainer.dataset.tabContent;
      
      buttons.forEach(btn => {
        btn.addEventListener('click', () => {
          // Remove active from all buttons
          buttons.forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
          
          // Show corresponding content
          const target = btn.dataset.tab;
          if (contentId) {
            const container = document.getElementById(contentId);
            container.querySelectorAll('.tab-content').forEach(content => {
              content.classList.remove('active');
            });
            container.querySelector(`[data-tab-content="${target}"]`)?.classList.add('active');
          }
        });
      });
    });
  }
};

// Table Utilities
const DataTable = {
  sort(tableId, columnIndex, type = 'string') {
    const table = document.getElementById(tableId);
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));
    const header = table.querySelectorAll('th')[columnIndex];
    const isAsc = header.classList.contains('asc');
    
    // Reset all headers
    table.querySelectorAll('th').forEach(th => {
      th.classList.remove('asc', 'desc');
    });
    
    header.classList.add(isAsc ? 'desc' : 'asc');
    
    rows.sort((a, b) => {
      let aVal = a.cells[columnIndex].textContent.trim();
      let bVal = b.cells[columnIndex].textContent.trim();
      
      if (type === 'number') {
        aVal = parseFloat(aVal.replace(/[^\d.-]/g, '')) || 0;
        bVal = parseFloat(bVal.replace(/[^\d.-]/g, '')) || 0;
      } else if (type === 'date') {
        aVal = new Date(aVal.split('/').reverse().join('-'));
        bVal = new Date(bVal.split('/').reverse().join('-'));
      }
      
      if (aVal < bVal) return isAsc ? 1 : -1;
      if (aVal > bVal) return isAsc ? -1 : 1;
      return 0;
    });
    
    rows.forEach(row => tbody.appendChild(row));
  },

  filter(tableId, searchValue, columns = []) {
    const table = document.getElementById(tableId);
    const rows = table.querySelectorAll('tbody tr');
    const search = searchValue.toLowerCase();
    
    rows.forEach(row => {
      const cells = columns.length > 0 
        ? columns.map(i => row.cells[i]?.textContent || '')
        : Array.from(row.cells).map(c => c.textContent);
      
      const match = cells.some(text => text.toLowerCase().includes(search));
      row.style.display = match ? '' : 'none';
    });
  }
};

// Form Utilities
const Form = {
  serialize(formId) {
    const form = document.getElementById(formId);
    const formData = new FormData(form);
    const data = {};
    
    formData.forEach((value, key) => {
      if (data[key]) {
        if (!Array.isArray(data[key])) {
          data[key] = [data[key]];
        }
        data[key].push(value);
      } else {
        data[key] = value;
      }
    });
    
    return data;
  },

  populate(formId, data) {
    const form = document.getElementById(formId);
    
    Object.entries(data).forEach(([key, value]) => {
      const field = form.querySelector(`[name="${key}"]`);
      if (field) {
        if (field.type === 'checkbox') {
          field.checked = Boolean(value);
        } else if (field.type === 'radio') {
          const radio = form.querySelector(`[name="${key}"][value="${value}"]`);
          if (radio) radio.checked = true;
        } else {
          field.value = value;
        }
      }
    });
  },

  reset(formId) {
    document.getElementById(formId)?.reset();
  },

  validate(formId) {
    const form = document.getElementById(formId);
    const requiredFields = form.querySelectorAll('[required]');
    let valid = true;
    
    requiredFields.forEach(field => {
      if (!field.value.trim()) {
        field.classList.add('error');
        valid = false;
      } else {
        field.classList.remove('error');
      }
    });
    
    return valid;
  }
};

// Format Utilities
const Format = {
  currency(value) {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  },

  date(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR');
  },

  dateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('pt-BR');
  },

  cpf(cpf) {
    return cpf?.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4') || '';
  },

  phone(phone) {
    if (!phone) return '';
    const cleaned = phone.replace(/\D/g, '');
    if (cleaned.length === 11) {
      return cleaned.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    return cleaned.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  },

  cep(cep) {
    return cep?.replace(/(\d{5})(\d{3})/, '$1-$2') || '';
  }
};

// Input Masks
const Mask = {
  apply(input, type) {
    input.addEventListener('input', (e) => {
      let value = e.target.value.replace(/\D/g, '');
      
      switch (type) {
        case 'cpf':
          value = value.substring(0, 11);
          value = value.replace(/(\d{3})(\d)/, '$1.$2');
          value = value.replace(/(\d{3})(\d)/, '$1.$2');
          value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
          break;
        case 'phone':
          value = value.substring(0, 11);
          value = value.replace(/(\d{2})(\d)/, '($1) $2');
          value = value.replace(/(\d{5})(\d)/, '$1-$2');
          break;
        case 'cep':
          value = value.substring(0, 8);
          value = value.replace(/(\d{5})(\d)/, '$1-$2');
          break;
        case 'currency':
          value = (parseInt(value) / 100).toFixed(2);
          value = value.replace('.', ',');
          value = 'R$ ' + value.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
          break;
        case 'date':
          value = value.substring(0, 8);
          value = value.replace(/(\d{2})(\d)/, '$1/$2');
          value = value.replace(/(\d{2})(\d)/, '$1/$2');
          break;
      }
      
      e.target.value = value;
    });
  },

  initAll() {
    document.querySelectorAll('[data-mask]').forEach(input => {
      this.apply(input, input.dataset.mask);
    });
  }
};

// Confirmation Dialog
const Confirm = {
  show(message, onConfirm, onCancel) {
    const modal = document.createElement('div');
    modal.className = 'modal active';
    modal.innerHTML = `
      <div class="modal-dialog modal-sm">
        <div class="modal-header">
          <h3 class="modal-title">Confirmar</h3>
          <button class="modal-close" data-action="cancel">&times;</button>
        </div>
        <div class="modal-body">
          <p>${message}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" data-action="cancel">Cancelar</button>
          <button class="btn btn-danger" data-action="confirm">Confirmar</button>
        </div>
      </div>
    `;
    
    document.body.appendChild(modal);
    document.body.style.overflow = 'hidden';
    
    modal.addEventListener('click', (e) => {
      const action = e.target.dataset.action;
      if (action === 'confirm') {
        onConfirm?.();
      } else if (action === 'cancel' || e.target === modal) {
        onCancel?.();
      }
      
      if (action) {
        modal.remove();
        document.body.style.overflow = '';
      }
    });
  }
};

// Quick Actions Dropdown
const QuickActions = {
  init() {
    document.querySelectorAll('.quick-actions').forEach(container => {
      const trigger = container.querySelector('[data-quick-actions-trigger]');
      const menu = container.querySelector('.quick-actions-menu');
      
      if (trigger && menu) {
        trigger.addEventListener('click', (e) => {
          e.stopPropagation();
          menu.classList.toggle('active');
        });
      }
    });
    
    // Close on click outside
    document.addEventListener('click', () => {
      document.querySelectorAll('.quick-actions-menu.active').forEach(menu => {
        menu.classList.remove('active');
      });
    });
  }
};

// Initialize all components on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  Modal.init();
  Tabs.init();
  Mask.initAll();
  QuickActions.init();
});

// Export for global use
window.Toast = Toast;
window.Modal = Modal;
window.Tabs = Tabs;
window.DataTable = DataTable;
window.Form = Form;
window.Format = Format;
window.Mask = Mask;
window.Confirm = Confirm;
