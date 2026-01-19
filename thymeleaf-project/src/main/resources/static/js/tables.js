/* ===========================================
   TABLES JS
   =========================================== */

const Tables = {
    init() {
        this.initSorting();
        this.initSearch();
        this.initRowSelection();
    },

    initSorting() {
        const sortableHeaders = document.querySelectorAll('.data-table th.sortable');
        
        sortableHeaders.forEach(header => {
            header.addEventListener('click', () => {
                const table = header.closest('table');
                const sortField = header.dataset.sort;
                const currentSort = header.classList.contains('sorted');
                const currentDesc = header.classList.contains('desc');
                
                // Remove sort from all headers
                table.querySelectorAll('th.sortable').forEach(th => {
                    th.classList.remove('sorted', 'asc', 'desc');
                });

                // Apply new sort
                let newDirection = 'asc';
                if (currentSort && !currentDesc) {
                    newDirection = 'desc';
                }

                header.classList.add('sorted', newDirection);

                // Sort table rows
                this.sortTable(table, sortField, newDirection);
            });
        });
    },

    sortTable(table, field, direction) {
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        rows.sort((a, b) => {
            const aCell = a.querySelector(`[data-sort-value="${field}"]`) || a.cells[this.getColumnIndex(table, field)];
            const bCell = b.querySelector(`[data-sort-value="${field}"]`) || b.cells[this.getColumnIndex(table, field)];

            if (!aCell || !bCell) return 0;

            let aValue = aCell.dataset.sortValue || aCell.textContent.trim();
            let bValue = bCell.dataset.sortValue || bCell.textContent.trim();

            // Try to parse as numbers
            const aNum = parseFloat(aValue.replace(/[^\d.-]/g, ''));
            const bNum = parseFloat(bValue.replace(/[^\d.-]/g, ''));

            if (!isNaN(aNum) && !isNaN(bNum)) {
                return direction === 'asc' ? aNum - bNum : bNum - aNum;
            }

            // String comparison
            aValue = aValue.toLowerCase();
            bValue = bValue.toLowerCase();

            if (aValue < bValue) return direction === 'asc' ? -1 : 1;
            if (aValue > bValue) return direction === 'asc' ? 1 : -1;
            return 0;
        });

        // Re-append rows in sorted order
        rows.forEach(row => tbody.appendChild(row));
    },

    getColumnIndex(table, field) {
        const headers = table.querySelectorAll('th');
        for (let i = 0; i < headers.length; i++) {
            if (headers[i].dataset.sort === field) {
                return i;
            }
        }
        return -1;
    },

    initSearch() {
        const searchInputs = document.querySelectorAll('.table-search-input');
        
        searchInputs.forEach(input => {
            const tableId = input.dataset.table;
            const table = document.getElementById(tableId) || input.closest('.table-container')?.querySelector('.data-table');
            
            if (!table) return;

            const debouncedSearch = Utils.debounce((term) => {
                this.filterTable(table, term);
            }, 300);

            input.addEventListener('input', (e) => {
                debouncedSearch(e.target.value);
            });
        });
    },

    filterTable(table, searchTerm) {
        const tbody = table.querySelector('tbody');
        const rows = tbody.querySelectorAll('tr');
        const term = searchTerm.toLowerCase().trim();

        rows.forEach(row => {
            if (!term) {
                row.style.display = '';
                return;
            }

            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(term) ? '' : 'none';
        });

        // Update info text
        const visibleRows = tbody.querySelectorAll('tr:not([style*="display: none"])');
        const infoEl = table.closest('.card')?.querySelector('.table-info');
        
        if (infoEl && term) {
            infoEl.innerHTML = `<span>Mostrando <strong>${visibleRows.length}</strong> resultados para "${Utils.escapeHtml(searchTerm)}"</span>`;
        }
    },

    initRowSelection() {
        const tables = document.querySelectorAll('.data-table[data-selectable]');
        
        tables.forEach(table => {
            const selectAllCheckbox = table.querySelector('thead input[type="checkbox"]');
            const rowCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]');

            if (selectAllCheckbox) {
                selectAllCheckbox.addEventListener('change', () => {
                    rowCheckboxes.forEach(checkbox => {
                        checkbox.checked = selectAllCheckbox.checked;
                        this.toggleRowSelection(checkbox);
                    });
                    this.updateSelectionCount(table);
                });
            }

            rowCheckboxes.forEach(checkbox => {
                checkbox.addEventListener('change', () => {
                    this.toggleRowSelection(checkbox);
                    this.updateSelectAllState(table);
                    this.updateSelectionCount(table);
                });
            });
        });
    },

    toggleRowSelection(checkbox) {
        const row = checkbox.closest('tr');
        if (checkbox.checked) {
            row.classList.add('selected');
        } else {
            row.classList.remove('selected');
        }
    },

    updateSelectAllState(table) {
        const selectAllCheckbox = table.querySelector('thead input[type="checkbox"]');
        const rowCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]');
        
        if (!selectAllCheckbox) return;

        const checkedCount = table.querySelectorAll('tbody input[type="checkbox"]:checked').length;
        const totalCount = rowCheckboxes.length;

        selectAllCheckbox.checked = checkedCount === totalCount;
        selectAllCheckbox.indeterminate = checkedCount > 0 && checkedCount < totalCount;
    },

    updateSelectionCount(table) {
        const selectionInfo = table.closest('.card')?.querySelector('.selection-info');
        if (!selectionInfo) return;

        const checkedCount = table.querySelectorAll('tbody input[type="checkbox"]:checked').length;
        
        if (checkedCount > 0) {
            selectionInfo.textContent = `${checkedCount} item(ns) selecionado(s)`;
            selectionInfo.style.display = '';
        } else {
            selectionInfo.style.display = 'none';
        }
    },

    getSelectedRows(tableId) {
        const table = document.getElementById(tableId);
        if (!table) return [];

        const selectedCheckboxes = table.querySelectorAll('tbody input[type="checkbox"]:checked');
        return Array.from(selectedCheckboxes).map(checkbox => {
            const row = checkbox.closest('tr');
            return row.dataset.id || row.cells[0]?.textContent;
        });
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Tables.init();
});

// Export
window.Tables = Tables;
