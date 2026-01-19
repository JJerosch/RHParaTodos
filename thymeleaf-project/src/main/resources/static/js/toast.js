/* ===========================================
   TOAST JS - Notificações
   =========================================== */

const Toast = {
    container: null,
    queue: [],
    maxToasts: 5,

    init() {
        this.container = document.getElementById('toast-container');
        if (!this.container) {
            this.createContainer();
        }
    },

    createContainer() {
        this.container = document.createElement('div');
        this.container.id = 'toast-container';
        this.container.className = 'toast-container';
        document.body.appendChild(this.container);
    },

    show(options = {}) {
        if (!this.container) {
            this.init();
        }

        const {
            title = '',
            message = '',
            type = 'info',
            duration = 5000,
            dismissible = true
        } = options;

        const iconMap = {
            success: 'check-circle',
            error: 'x-circle',
            warning: 'alert-triangle',
            info: 'info'
        };

        const icon = iconMap[type] || 'info';
        const id = Utils.generateId();

        const toastHtml = `
            <div class="toast toast--${type}" id="toast-${id}" role="alert">
                <div class="toast-icon">
                    <img src="/images/icons/${icon}.png" alt="" class="icon">
                </div>
                <div class="toast-content">
                    ${title ? `<h4 class="toast-title">${Utils.escapeHtml(title)}</h4>` : ''}
                    <p class="toast-message">${Utils.escapeHtml(message)}</p>
                </div>
                ${dismissible ? `
                    <button type="button" class="toast-close" aria-label="Fechar">
                        <img src="/images/icons/x.png" alt="" class="icon icon-sm">
                    </button>
                ` : ''}
            </div>
        `;

        const toast = Utils.parseHtml(toastHtml);
        
        // Remove old toasts if at max
        const toasts = this.container.querySelectorAll('.toast');
        if (toasts.length >= this.maxToasts) {
            this.dismiss(toasts[0]);
        }

        this.container.appendChild(toast);

        // Show animation
        requestAnimationFrame(() => {
            toast.classList.add('show');
        });

        // Auto dismiss
        if (duration > 0) {
            setTimeout(() => {
                this.dismiss(toast);
            }, duration);
        }

        // Close button
        if (dismissible) {
            const closeBtn = toast.querySelector('.toast-close');
            closeBtn?.addEventListener('click', () => {
                this.dismiss(toast);
            });
        }

        return toast;
    },

    dismiss(toast) {
        if (!toast || !toast.parentNode) return;

        toast.classList.remove('show');
        
        setTimeout(() => {
            toast.remove();
        }, 300);
    },

    success(message, title = 'Sucesso') {
        return this.show({ title, message, type: 'success' });
    },

    error(message, title = 'Erro') {
        return this.show({ title, message, type: 'error', duration: 8000 });
    },

    warning(message, title = 'Atenção') {
        return this.show({ title, message, type: 'warning' });
    },

    info(message, title = '') {
        return this.show({ title, message, type: 'info' });
    },

    clear() {
        if (!this.container) return;
        
        const toasts = this.container.querySelectorAll('.toast');
        toasts.forEach(toast => this.dismiss(toast));
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Toast.init();

    // Show flash messages from server
    const flashMessages = document.querySelectorAll('[data-flash-message]');
    flashMessages.forEach(el => {
        const type = el.dataset.flashType || 'info';
        const message = el.dataset.flashMessage;
        const title = el.dataset.flashTitle || '';
        
        Toast.show({ type, message, title });
        el.remove();
    });
});

// Export
window.Toast = Toast;
