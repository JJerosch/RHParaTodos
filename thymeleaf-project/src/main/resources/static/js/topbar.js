/* ===========================================
   TOPBAR JS
   =========================================== */

const Topbar = {
    dropdowns: [],
    
    init() {
        this.initDropdowns();
        this.initSearch();
        this.initThemeToggle();
    },

    initDropdowns() {
        const dropdownToggles = document.querySelectorAll('[data-dropdown-toggle]');
        
        dropdownToggles.forEach(toggle => {
            const targetId = toggle.dataset.dropdownToggle;
            const dropdown = document.getElementById(targetId);
            
            if (!dropdown) return;
            
            this.dropdowns.push({ toggle, dropdown });
            
            toggle.addEventListener('click', (e) => {
                e.stopPropagation();
                this.toggleDropdown(toggle, dropdown);
            });
        });

        // Close dropdowns when clicking outside
        document.addEventListener('click', (e) => {
            this.dropdowns.forEach(({ toggle, dropdown }) => {
                if (!toggle.contains(e.target) && !dropdown.contains(e.target)) {
                    this.closeDropdown(dropdown, toggle);
                }
            });
        });

        // Close dropdowns on escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeAllDropdowns();
            }
        });
    },

    toggleDropdown(toggle, dropdown) {
        const isOpen = dropdown.classList.contains('open');
        
        // Close all other dropdowns
        this.closeAllDropdowns();
        
        if (!isOpen) {
            dropdown.classList.add('open');
            toggle.closest('.action-dropdown, .user-menu')?.classList.add('open');
        }
    },

    closeDropdown(dropdown, toggle) {
        dropdown.classList.remove('open');
        toggle?.closest('.action-dropdown, .user-menu')?.classList.remove('open');
    },

    closeAllDropdowns() {
        this.dropdowns.forEach(({ toggle, dropdown }) => {
            this.closeDropdown(dropdown, toggle);
        });
    },

    initSearch() {
        const searchInput = document.querySelector('.search-input');
        
        if (!searchInput) return;

        // Keyboard shortcut (Ctrl+K)
        document.addEventListener('keydown', (e) => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                searchInput.focus();
            }
        });

        // Search form submission
        const searchForm = searchInput.closest('form');
        if (searchForm) {
            searchForm.addEventListener('submit', (e) => {
                if (!searchInput.value.trim()) {
                    e.preventDefault();
                }
            });
        }
    },

    initThemeToggle() {
        const themeToggle = document.getElementById('themeToggle');
        
        if (!themeToggle) return;

        // Restore saved theme
        const savedTheme = localStorage.getItem('theme') || 'light';
        document.documentElement.setAttribute('data-theme', savedTheme);

        themeToggle.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
        });
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Topbar.init();
});

// Export
window.Topbar = Topbar;
