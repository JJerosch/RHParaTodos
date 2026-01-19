/* ===========================================
   MODALS JS
   =========================================== */

const Modal = {
    activeModal: null,

    init() {
        this.bindGlobalEvents();
        this.initDeleteModals();
    },

    bindGlobalEvents() {
        // Close buttons
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-modal-close]') || e.target.closest('[data-modal-close]')) {
                const modal = e.target.closest('.modal');
                if (modal) {
                    this.close(modal);
                }
            }
        });

        // Open buttons
        document.addEventListener('click', (e) => {
            const trigger = e.target.closest('[data-modal-open]');
            if (trigger) {
                const modalId = trigger.dataset.modalOpen;
                const modal = document.getElementById(modalId);
                if (modal) {
                    this.open(modal);
                }
            }
        });

        // Escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.activeModal) {
                this.close(this.activeModal);
            }
        });
    },

    open(modal) {
        if (typeof modal === 'string') {
            modal = document.getElementById(modal);
        }
        
        if (!modal) return;

        // Close any open modal
        if (this.activeModal) {
            this.close(this.activeModal, false);
        }

        modal.classList.add('open');
        document.body.style.overflow = 'hidden';
        this.activeModal = modal;

        // Focus first focusable element
        const focusable = modal.querySelector('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
        if (focusable) {
            setTimeout(() => focusable.focus(), 100);
        }

        // Dispatch event
        modal.dispatchEvent(new CustomEvent('modal:open'));
    },

    close(modal, restoreScroll = true) {
        if (typeof modal === 'string') {
            modal = document.getElementById(modal);
        }
        
        if (!modal) return;

        modal.classList.remove('open');
        
        if (restoreScroll) {
            document.body.style.overflow = '';
        }
        
        if (this.activeModal === modal) {
            this.activeModal = null;
        }

        // Dispatch event
        modal.dispatchEvent(new CustomEvent('modal:close'));
    },

    confirm(options = {}) {
        return new Promise((resolve) => {
            const {
                title = 'Confirmar',
                message = 'Tem certeza que deseja continuar?',
                confirmText = 'Confirmar',
                cancelText = 'Cancelar',
                confirmVariant = 'primary',
                icon = 'alert-triangle'
            } = options;

            // Create modal element
            const modalHtml = `
                <div class="modal modal-confirm" id="confirmModal" role="alertdialog" aria-modal="true">
                    <div class="modal-backdrop" data-modal-close></div>
                    <div class="modal-dialog modal-dialog--sm">
                        <div class="modal-content">
                            <div class="modal-header">
                                <div class="modal-icon modal-icon--warning">
                                    <img src="/images/icons/${icon}.png" alt="" class="icon icon-lg">
                                </div>
                                <h2 class="modal-title">${Utils.escapeHtml(title)}</h2>
                                <button type="button" class="modal-close" data-modal-close aria-label="Fechar">
                                    <img src="/images/icons/x.png" alt="" class="icon">
                                </button>
                            </div>
                            <div class="modal-body">
                                <p class="modal-message">${Utils.escapeHtml(message)}</p>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn--secondary" data-modal-close>
                                    ${Utils.escapeHtml(cancelText)}
                                </button>
                                <button type="button" class="btn btn--${confirmVariant}" data-modal-confirm>
                                    ${Utils.escapeHtml(confirmText)}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            const modal = Utils.parseHtml(modalHtml);
            document.body.appendChild(modal);

            const confirmBtn = modal.querySelector('[data-modal-confirm]');
            const closeHandler = () => {
                modal.remove();
                resolve(false);
            };

            confirmBtn.addEventListener('click', () => {
                modal.remove();
                document.body.style.overflow = '';
                resolve(true);
            });

            modal.addEventListener('modal:close', closeHandler);

            this.open(modal);
        });
    },

    alert(options = {}) {
        return new Promise((resolve) => {
            const {
                title = 'Aviso',
                message = '',
                type = 'info',
                buttonText = 'OK'
            } = options;

            const iconMap = {
                success: 'check-circle',
                error: 'x-circle',
                warning: 'alert-triangle',
                info: 'info'
            };

            const icon = iconMap[type] || 'info';

            const modalHtml = `
                <div class="modal modal-${type}" id="alertModal" role="alertdialog" aria-modal="true">
                    <div class="modal-backdrop" data-modal-close></div>
                    <div class="modal-dialog modal-dialog--sm">
                        <div class="modal-content">
                            <div class="modal-header modal-header--center">
                                <div class="modal-icon modal-icon--${type}">
                                    <img src="/images/icons/${icon}.png" alt="" class="icon icon-xl">
                                </div>
                            </div>
                            <div class="modal-body modal-body--center">
                                <h2 class="modal-title">${Utils.escapeHtml(title)}</h2>
                                <p class="modal-message">${Utils.escapeHtml(message)}</p>
                            </div>
                            <div class="modal-footer modal-footer--center">
                                <button type="button" class="btn btn--primary" data-modal-close>
                                    ${Utils.escapeHtml(buttonText)}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            const modal = Utils.parseHtml(modalHtml);
            document.body.appendChild(modal);

            modal.addEventListener('modal:close', () => {
                modal.remove();
                resolve();
            });

            this.open(modal);
        });
    },

    initDeleteModals() {
        const deleteModal = document.getElementById('deleteModal');
        if (!deleteModal) return;

        const deleteForm = document.getElementById('deleteForm');
        const deleteItemName = document.getElementById('deleteItemName');

        document.addEventListener('click', (e) => {
            const deleteBtn = e.target.closest('[data-action="delete"]');
            if (!deleteBtn) return;

            e.preventDefault();
            
            const id = deleteBtn.dataset.id;
            const name = deleteBtn.dataset.name || 'este item';
            const url = deleteBtn.dataset.url || `/api/${deleteBtn.closest('[data-entity]')?.dataset.entity || 'items'}/${id}`;

            deleteItemName.textContent = name;
            deleteForm.action = url;

            this.open(deleteModal);
        });
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Modal.init();
});

// Export
window.Modal = Modal;
