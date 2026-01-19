/* ===========================================
   SIDEBAR JS
   =========================================== */

const Sidebar = {
    sidebar: null,
    toggleBtn: null,
    mobileToggleBtn: null,
    backdrop: null,

    init() {
        this.sidebar = document.getElementById('sidebar');
        this.toggleBtn = document.getElementById('sidebarToggle');
        this.mobileToggleBtn = document.getElementById('mobileMenuToggle');
        
        if (!this.sidebar) return;

        this.createBackdrop();
        this.bindEvents();
        this.restoreState();
        this.initSubmenus();
    },

    createBackdrop() {
        this.backdrop = document.createElement('div');
        this.backdrop.className = 'sidebar-backdrop';
        this.sidebar.parentNode.insertBefore(this.backdrop, this.sidebar.nextSibling);
    },

    bindEvents() {
        // Desktop toggle
        if (this.toggleBtn) {
            this.toggleBtn.addEventListener('click', () => this.toggle());
        }

        // Mobile toggle
        if (this.mobileToggleBtn) {
            this.mobileToggleBtn.addEventListener('click', () => this.toggleMobile());
        }

        // Backdrop click
        if (this.backdrop) {
            this.backdrop.addEventListener('click', () => this.closeMobile());
        }

        // Keyboard shortcut
        document.addEventListener('keydown', (e) => {
            // Ctrl/Cmd + B to toggle sidebar
            if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
                e.preventDefault();
                this.toggle();
            }
            // Escape to close mobile sidebar
            if (e.key === 'Escape' && this.sidebar.classList.contains('mobile-open')) {
                this.closeMobile();
            }
        });

        // Window resize
        window.addEventListener('resize', Utils.debounce(() => {
            if (window.innerWidth > 1024) {
                this.closeMobile();
            }
        }, 250));
    },

    toggle() {
        document.body.classList.toggle('sidebar-collapsed');
        this.saveState();
    },

    toggleMobile() {
        this.sidebar.classList.toggle('mobile-open');
        document.body.style.overflow = this.sidebar.classList.contains('mobile-open') ? 'hidden' : '';
    },

    closeMobile() {
        this.sidebar.classList.remove('mobile-open');
        document.body.style.overflow = '';
    },

    saveState() {
        const collapsed = document.body.classList.contains('sidebar-collapsed');
        localStorage.setItem('sidebar-collapsed', collapsed);
    },

    restoreState() {
        const collapsed = localStorage.getItem('sidebar-collapsed') === 'true';
        if (collapsed) {
            document.body.classList.add('sidebar-collapsed');
        }
    },

    initSubmenus() {
        const submenuToggles = this.sidebar.querySelectorAll('.submenu-toggle');
        
        submenuToggles.forEach(toggle => {
            toggle.addEventListener('click', (e) => {
                e.preventDefault();
                const parent = toggle.closest('.has-submenu');
                
                // Close other submenus (accordion behavior)
                submenuToggles.forEach(otherToggle => {
                    const otherParent = otherToggle.closest('.has-submenu');
                    if (otherParent !== parent && otherParent.classList.contains('open')) {
                        otherParent.classList.remove('open');
                    }
                });
                
                // Toggle current submenu
                parent.classList.toggle('open');
            });
        });

        // Open submenu if active item is inside
        const activeSubItem = this.sidebar.querySelector('.submenu-item.active');
        if (activeSubItem) {
            const parentSubmenu = activeSubItem.closest('.has-submenu');
            if (parentSubmenu) {
                parentSubmenu.classList.add('open');
            }
        }
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Sidebar.init();
});

// Export
window.Sidebar = Sidebar;
