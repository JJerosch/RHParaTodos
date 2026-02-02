/**
 * RH Para Todos - JavaScript Utilitário
 * Funções auxiliares para o sistema
 */

// ==================== MÁSCARAS ====================
const Mask = {
  cpf: (value) => {
    return value
      .replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})/, '$1-$2')
      .replace(/(-\d{2})\d+?$/, '$1');
  },
  
  cnpj: (value) => {
    return value
      .replace(/\D/g, '')
      .replace(/(\d{2})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1/$2')
      .replace(/(\d{4})(\d)/, '$1-$2')
      .replace(/(-\d{2})\d+?$/, '$1');
  },
  
  phone: (value) => {
    const numbers = value.replace(/\D/g, '');
    if (numbers.length <= 10) {
      return numbers
        .replace(/(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{4})(\d)/, '$1-$2')
        .replace(/(-\d{4})\d+?$/, '$1');
    }
    return numbers
      .replace(/(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{5})(\d)/, '$1-$2')
      .replace(/(-\d{4})\d+?$/, '$1');
  },
  
  cep: (value) => {
    return value
      .replace(/\D/g, '')
      .replace(/(\d{5})(\d)/, '$1-$2')
      .replace(/(-\d{3})\d+?$/, '$1');
  },
  
  currency: (value) => {
    const numbers = value.replace(/\D/g, '');
    const amount = (parseInt(numbers) / 100).toFixed(2);
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(amount);
  },
  
  date: (value) => {
    return value
      .replace(/\D/g, '')
      .replace(/(\d{2})(\d)/, '$1/$2')
      .replace(/(\d{2})(\d)/, '$1/$2')
      .replace(/(\/\d{4})\d+?$/, '$1');
  },
  
  rg: (value) => {
    return value
      .replace(/\D/g, '')
      .replace(/(\d{2})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1})/, '$1-$2')
      .replace(/(-\d{1})\d+?$/, '$1');
  },
  
  pis: (value) => {
    return value
      .replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{5})(\d)/, '$1.$2')
      .replace(/(\d{5})(\d{1,2})/, '$1-$2')
      .replace(/(-\d{2})\d+?$/, '$1');
  },
  
  // Aplicar máscara automaticamente
  apply: (input) => {
    const maskType = input.dataset.mask;
    if (!maskType || !Mask[maskType]) return;
    
    input.addEventListener('input', (e) => {
      e.target.value = Mask[maskType](e.target.value);
    });
  },
  
  // Inicializar todas as máscaras
  initAll: () => {
    document.querySelectorAll('[data-mask]').forEach(Mask.apply);
  }
};

// ==================== FORMATADORES ====================
const Format = {
  currency: (value) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value || 0);
  },
  
  date: (value, format = 'short') => {
    if (!value) return '-';
    const date = new Date(value);
    if (isNaN(date)) return value;
    
    const options = format === 'full' 
      ? { day: '2-digit', month: 'long', year: 'numeric' }
      : { day: '2-digit', month: '2-digit', year: 'numeric' };
    
    return date.toLocaleDateString('pt-BR', options);
  },
  
  dateTime: (value) => {
    if (!value) return '-';
    const date = new Date(value);
    if (isNaN(date)) return value;
    
    return date.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  },
  
  cpf: (value) => {
    if (!value) return '-';
    const numbers = value.replace(/\D/g, '');
    return Mask.cpf(numbers);
  },
  
  phone: (value) => {
    if (!value) return '-';
    const numbers = value.replace(/\D/g, '');
    return Mask.phone(numbers);
  },
  
  initials: (name) => {
    if (!name) return '?';
    const parts = name.trim().split(' ');
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  },
  
  percentage: (value, decimals = 1) => {
    return (value || 0).toFixed(decimals) + '%';
  }
};

// ==================== UTILITÁRIOS ====================
const Utils = {
  // Debounce para pesquisa
  debounce: (func, wait = 300) => {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  },
  
  // Serializar formulário
  serializeForm: (formId) => {
    const form = document.getElementById(formId);
    if (!form) return {};
    
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
  
  // Popular formulário com dados
  populateForm: (formId, data) => {
    const form = document.getElementById(formId);
    if (!form || !data) return;
    
    Object.keys(data).forEach(key => {
      const element = form.elements[key];
      if (!element) return;
      
      if (element.type === 'checkbox') {
        element.checked = Boolean(data[key]);
      } else if (element.type === 'radio') {
        const radio = form.querySelector(`input[name="${key}"][value="${data[key]}"]`);
        if (radio) radio.checked = true;
      } else {
        element.value = data[key] || '';
      }
    });
  },
  
  // Limpar formulário
  resetForm: (formId) => {
    const form = document.getElementById(formId);
    if (form) form.reset();
  },
  
  // Copiar para clipboard
  copyToClipboard: async (text) => {
    try {
      await navigator.clipboard.writeText(text);
      Toast.success('Copiado para a área de transferência!');
      return true;
    } catch (err) {
      Toast.error('Erro ao copiar');
      return false;
    }
  },
  
  // Gerar ID único
  generateId: () => {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  },
  
  // Scroll suave para elemento
  scrollTo: (elementId) => {
    const element = document.getElementById(elementId);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
};

// ==================== TOASTS ====================
const Toast = {
  container: null,
  
  init: () => {
    if (!Toast.container) {
      Toast.container = document.createElement('div');
      Toast.container.className = 'toast-container position-fixed top-0 end-0 p-3';
      Toast.container.style.zIndex = '1100';
      document.body.appendChild(Toast.container);
    }
  },
  
  show: (message, type = 'info', duration = 4000) => {
    Toast.init();
    
    const icons = {
      success: 'bi-check-circle-fill',
      error: 'bi-x-circle-fill',
      warning: 'bi-exclamation-triangle-fill',
      info: 'bi-info-circle-fill'
    };
    
    const colors = {
      success: 'text-success',
      error: 'text-danger',
      warning: 'text-warning',
      info: 'text-info'
    };
    
    const toastId = Utils.generateId();
    const toastHtml = `
      <div id="${toastId}" class="toast align-items-center border-0 show" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
          <div class="toast-body d-flex align-items-center gap-2">
            <i class="bi ${icons[type] || icons.info} ${colors[type] || colors.info}"></i>
            <span>${message}</span>
          </div>
          <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Fechar"></button>
        </div>
      </div>
    `;
    
    Toast.container.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.getElementById(toastId);
    const bsToast = new bootstrap.Toast(toastElement, { delay: duration });
    bsToast.show();
    
    toastElement.addEventListener('hidden.bs.toast', () => {
      toastElement.remove();
    });
  },
  
  success: (message, duration) => Toast.show(message, 'success', duration),
  error: (message, duration) => Toast.show(message, 'error', duration),
  warning: (message, duration) => Toast.show(message, 'warning', duration),
  info: (message, duration) => Toast.show(message, 'info', duration)
};

// ==================== CONFIRMAÇÃO ====================
const Confirm = {
  show: (options) => {
    const defaults = {
      title: 'Confirmar ação',
      message: 'Tem certeza que deseja continuar?',
      confirmText: 'Confirmar',
      cancelText: 'Cancelar',
      confirmClass: 'btn-danger',
      onConfirm: () => {},
      onCancel: () => {}
    };
    
    const config = { ...defaults, ...options };
    
    const modalId = 'confirmModal_' + Utils.generateId();
    const modalHtml = `
      <div class="modal fade" id="${modalId}" tabindex="-1" data-bs-backdrop="static">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">${config.title}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <p class="mb-0">${config.message}</p>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${config.cancelText}</button>
              <button type="button" class="btn ${config.confirmClass}" id="${modalId}_confirm">${config.confirmText}</button>
            </div>
          </div>
        </div>
      </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    const modalElement = document.getElementById(modalId);
    const modal = new bootstrap.Modal(modalElement);
    
    document.getElementById(`${modalId}_confirm`).addEventListener('click', () => {
      config.onConfirm();
      modal.hide();
    });
    
    modalElement.addEventListener('hidden.bs.modal', () => {
      modalElement.remove();
    });
    
    modal.show();
  },
  
  delete: (itemName, onConfirm) => {
    Confirm.show({
      title: 'Confirmar exclusão',
      message: `Tem certeza que deseja excluir <strong>${itemName}</strong>? Esta ação não pode ser desfeita.`,
      confirmText: 'Excluir',
      confirmClass: 'btn-danger',
      onConfirm
    });
  }
};

// ==================== BUSCA DE CEP ====================
const ViaCEP = {
  fetch: async (cep) => {
    const cleanCep = cep.replace(/\D/g, '');
    if (cleanCep.length !== 8) return null;
    
    try {
      const response = await fetch(`https://viacep.com.br/ws/${cleanCep}/json/`);
      const data = await response.json();
      
      if (data.erro) return null;
      
      return {
        logradouro: data.logradouro,
        bairro: data.bairro,
        cidade: data.localidade,
        uf: data.uf,
        complemento: data.complemento
      };
    } catch (error) {
      console.error('Erro ao buscar CEP:', error);
      return null;
    }
  },
  
  // Auto-preenchimento de endereço
  autofill: (cepInputId, fields) => {
    const cepInput = document.getElementById(cepInputId);
    if (!cepInput) return;
    
    cepInput.addEventListener('blur', async () => {
      const cep = cepInput.value;
      if (cep.replace(/\D/g, '').length !== 8) return;
      
      const data = await ViaCEP.fetch(cep);
      if (!data) {
        Toast.warning('CEP não encontrado');
        return;
      }
      
      // Preencher campos
      const fieldMap = {
        logradouro: fields.logradouro || 'logradouro',
        bairro: fields.bairro || 'bairro',
        cidade: fields.cidade || 'cidade',
        uf: fields.uf || 'uf'
      };
      
      Object.keys(fieldMap).forEach(key => {
        const element = document.getElementById(fieldMap[key]) || 
                       document.querySelector(`[name="${fieldMap[key]}"]`);
        if (element && data[key]) {
          element.value = data[key];
        }
      });
      
      Toast.success('Endereço preenchido automaticamente');
    });
  }
};

// ==================== VALIDAÇÃO ====================
const Validator = {
  cpf: (cpf) => {
    cpf = cpf.replace(/\D/g, '');
    if (cpf.length !== 11) return false;
    if (/^(\d)\1+$/.test(cpf)) return false;
    
    let sum = 0;
    for (let i = 0; i < 9; i++) {
      sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let digit = 11 - (sum % 11);
    if (digit > 9) digit = 0;
    if (parseInt(cpf.charAt(9)) !== digit) return false;
    
    sum = 0;
    for (let i = 0; i < 10; i++) {
      sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    digit = 11 - (sum % 11);
    if (digit > 9) digit = 0;
    return parseInt(cpf.charAt(10)) === digit;
  },
  
  cnpj: (cnpj) => {
    cnpj = cnpj.replace(/\D/g, '');
    if (cnpj.length !== 14) return false;
    if (/^(\d)\1+$/.test(cnpj)) return false;
    
    let size = cnpj.length - 2;
    let numbers = cnpj.substring(0, size);
    const digits = cnpj.substring(size);
    let sum = 0;
    let pos = size - 7;
    
    for (let i = size; i >= 1; i--) {
      sum += numbers.charAt(size - i) * pos--;
      if (pos < 2) pos = 9;
    }
    
    let result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    if (result !== parseInt(digits.charAt(0))) return false;
    
    size++;
    numbers = cnpj.substring(0, size);
    sum = 0;
    pos = size - 7;
    
    for (let i = size; i >= 1; i--) {
      sum += numbers.charAt(size - i) * pos--;
      if (pos < 2) pos = 9;
    }
    
    result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    return result === parseInt(digits.charAt(1));
  },
  
  email: (email) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  },
  
  phone: (phone) => {
    const numbers = phone.replace(/\D/g, '');
    return numbers.length >= 10 && numbers.length <= 11;
  },
  
  required: (value) => {
    return value !== null && value !== undefined && value.toString().trim() !== '';
  },
  
  minLength: (value, min) => {
    return value && value.length >= min;
  },
  
  maxLength: (value, max) => {
    return !value || value.length <= max;
  }
};

// ==================== LOADING ====================
const Loading = {
  show: (target = 'body') => {
    const element = target === 'body' ? document.body : document.querySelector(target);
    if (!element) return;
    
    const overlay = document.createElement('div');
    overlay.className = 'loading-overlay';
    overlay.innerHTML = `
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
    `;
    overlay.style.cssText = `
      position: ${target === 'body' ? 'fixed' : 'absolute'};
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(255, 255, 255, 0.8);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    `;
    
    if (target !== 'body') {
      element.style.position = 'relative';
    }
    
    element.appendChild(overlay);
  },
  
  hide: (target = 'body') => {
    const element = target === 'body' ? document.body : document.querySelector(target);
    if (!element) return;
    
    const overlay = element.querySelector('.loading-overlay');
    if (overlay) overlay.remove();
  }
};

// ==================== SIDEBAR ====================
const Sidebar = {
  toggle: () => {
    const sidebar = document.querySelector('.sidebar');
    const mainContent = document.querySelector('.main-content');
    const overlay = document.querySelector('.sidebar-overlay');
    
    if (window.innerWidth <= 991) {
      sidebar.classList.toggle('show');
      overlay?.classList.toggle('show');
    } else {
      sidebar.classList.toggle('collapsed');
      mainContent?.classList.toggle('sidebar-collapsed');
    }
  },
  
  init: () => {
    // Criar overlay para mobile
    if (!document.querySelector('.sidebar-overlay')) {
      const overlay = document.createElement('div');
      overlay.className = 'sidebar-overlay';
      overlay.addEventListener('click', Sidebar.toggle);
      document.body.appendChild(overlay);
    }
    
    // Fechar sidebar ao clicar em link (mobile)
    document.querySelectorAll('.sidebar .nav-item').forEach(item => {
      item.addEventListener('click', () => {
        if (window.innerWidth <= 991) {
          Sidebar.toggle();
        }
      });
    });
  }
};

// ==================== TABELA ====================
const DataTable = {
  sort: (tableId, columnIndex, sortKey, data, renderCallback) => {
    const table = document.getElementById(tableId);
    if (!table) return;
    
    const headers = table.querySelectorAll('thead th');
    const header = headers[columnIndex];
    const isAsc = header.classList.contains('asc');
    
    headers.forEach(h => h.classList.remove('asc', 'desc'));
    header.classList.add(isAsc ? 'desc' : 'asc');
    
    data.sort((a, b) => {
      let aVal = a[sortKey] || '';
      let bVal = b[sortKey] || '';
      
      if (sortKey.includes('data') || sortKey.includes('date')) {
        aVal = new Date(aVal);
        bVal = new Date(bVal);
      }
      
      if (aVal < bVal) return isAsc ? 1 : -1;
      if (aVal > bVal) return isAsc ? -1 : 1;
      return 0;
    });
    
    if (renderCallback) renderCallback();
    return data;
  },
  
  paginate: (data, page, perPage) => {
    const start = (page - 1) * perPage;
    const end = start + perPage;
    return {
      items: data.slice(start, end),
      totalPages: Math.ceil(data.length / perPage),
      totalItems: data.length,
      currentPage: page,
      start: start + 1,
      end: Math.min(end, data.length)
    };
  }
};

// ==================== INICIALIZAÇÃO ====================
document.addEventListener('DOMContentLoaded', () => {
  // Inicializar máscaras
  Mask.initAll();
  
  // Inicializar sidebar
  Sidebar.init();
  
  // Inicializar tooltips do Bootstrap
  const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
  tooltips.forEach(el => new bootstrap.Tooltip(el));
  
  // Inicializar popovers do Bootstrap
  const popovers = document.querySelectorAll('[data-bs-toggle="popover"]');
  popovers.forEach(el => new bootstrap.Popover(el));
  
  // Toggle de senha
  document.querySelectorAll('.password-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.parentElement.querySelector('input');
      const icon = btn.querySelector('i');
      
      if (input.type === 'password') {
        input.type = 'text';
        icon.classList.replace('bi-eye', 'bi-eye-slash');
      } else {
        input.type = 'password';
        icon.classList.replace('bi-eye-slash', 'bi-eye');
      }
    });
  });
  
  // Validação de formulários Bootstrap
  document.querySelectorAll('.needs-validation').forEach(form => {
    form.addEventListener('submit', event => {
      if (!form.checkValidity()) {
        event.preventDefault();
        event.stopPropagation();
      }
      form.classList.add('was-validated');
    });
  });
});
