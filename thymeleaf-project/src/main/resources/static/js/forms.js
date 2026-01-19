/* ===========================================
   FORMS JS
   =========================================== */

const Forms = {
    init() {
        this.initPasswordToggle();
        this.initPhotoUpload();
        this.initCepSearch();
        this.initFormValidation();
        this.initCharCounter();
    },

    initPasswordToggle() {
        const toggleButtons = document.querySelectorAll('.password-toggle');
        
        toggleButtons.forEach(button => {
            button.addEventListener('click', () => {
                const input = button.previousElementSibling;
                const isPassword = input.type === 'password';
                
                input.type = isPassword ? 'text' : 'password';
                button.classList.toggle('active', !isPassword);
            });
        });
    },

    initPhotoUpload() {
        const photoInputs = document.querySelectorAll('.photo-input');
        
        photoInputs.forEach(input => {
            const preview = document.getElementById('photoPreview');
            const removeBtn = document.getElementById('removePhoto');
            
            input.addEventListener('change', (e) => {
                const file = e.target.files[0];
                
                if (!file) return;

                // Validate file type
                if (!file.type.startsWith('image/')) {
                    Toast.error('Por favor, selecione uma imagem válida.');
                    input.value = '';
                    return;
                }

                // Validate file size (2MB max)
                if (file.size > 2 * 1024 * 1024) {
                    Toast.error('A imagem deve ter no máximo 2MB.');
                    input.value = '';
                    return;
                }

                // Preview image
                const reader = new FileReader();
                reader.onload = (e) => {
                    if (preview) {
                        preview.src = e.target.result;
                    }
                    if (removeBtn) {
                        removeBtn.classList.remove('hidden');
                    }
                };
                reader.readAsDataURL(file);
            });

            if (removeBtn) {
                removeBtn.addEventListener('click', () => {
                    input.value = '';
                    if (preview) {
                        preview.src = '/images/icons/default-avatar.png';
                    }
                    removeBtn.classList.add('hidden');
                });
            }
        });
    },

    initCepSearch() {
        const cepInputs = document.querySelectorAll('[data-cep-search]');
        
        cepInputs.forEach(input => {
            const searchBtn = document.getElementById('searchCep');
            
            const searchCep = async () => {
                const cep = input.value.replace(/\D/g, '');
                
                if (cep.length !== 8) {
                    Toast.warning('Digite um CEP válido com 8 dígitos.');
                    return;
                }

                try {
                    const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
                    const data = await response.json();
                    
                    if (data.erro) {
                        Toast.error('CEP não encontrado.');
                        return;
                    }

                    // Fill address fields
                    this.fillAddressFields(data);
                    Toast.success('Endereço preenchido com sucesso!');
                } catch (error) {
                    console.error('Erro ao buscar CEP:', error);
                    Toast.error('Erro ao buscar CEP. Tente novamente.');
                }
            };

            if (searchBtn) {
                searchBtn.addEventListener('click', searchCep);
            }

            // Search on blur if CEP is complete
            input.addEventListener('blur', () => {
                const cep = input.value.replace(/\D/g, '');
                if (cep.length === 8) {
                    searchCep();
                }
            });
        });
    },

    fillAddressFields(data) {
        const fieldMap = {
            logradouro: 'logradouro',
            bairro: 'bairro',
            localidade: 'cidade',
            uf: 'estado'
        };

        Object.entries(fieldMap).forEach(([apiField, inputId]) => {
            const input = document.getElementById(inputId);
            if (input && data[apiField]) {
                input.value = data[apiField];
            }
        });

        // Focus on number field
        const numeroInput = document.getElementById('numero');
        if (numeroInput) {
            numeroInput.focus();
        }
    },

    initFormValidation() {
        const forms = document.querySelectorAll('form[data-validate]');
        
        forms.forEach(form => {
            form.addEventListener('submit', (e) => {
                if (!form.checkValidity()) {
                    e.preventDefault();
                    e.stopPropagation();
                    
                    // Show first invalid field
                    const firstInvalid = form.querySelector(':invalid');
                    if (firstInvalid) {
                        firstInvalid.focus();
                        firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                }
                
                form.classList.add('was-validated');
            });

            // Real-time validation
            const inputs = form.querySelectorAll('input, select, textarea');
            inputs.forEach(input => {
                input.addEventListener('blur', () => {
                    this.validateField(input);
                });

                input.addEventListener('input', () => {
                    if (input.classList.contains('is-invalid')) {
                        this.validateField(input);
                    }
                });
            });
        });
    },

    validateField(input) {
        const formGroup = input.closest('.form-group');
        if (!formGroup) return;

        const isValid = input.checkValidity();
        
        if (isValid) {
            formGroup.classList.remove('has-error');
            input.classList.remove('is-invalid');
            input.classList.add('is-valid');
        } else {
            formGroup.classList.add('has-error');
            input.classList.add('is-invalid');
            input.classList.remove('is-valid');
        }
    },

    initCharCounter() {
        const textareas = document.querySelectorAll('textarea[maxlength]');
        
        textareas.forEach(textarea => {
            const maxLength = parseInt(textarea.getAttribute('maxlength'));
            const counter = document.createElement('span');
            counter.className = 'char-counter';
            counter.style.cssText = 'font-size: 12px; color: var(--color-text-muted); text-align: right; display: block; margin-top: 4px;';
            
            const updateCounter = () => {
                const remaining = maxLength - textarea.value.length;
                counter.textContent = `${remaining} caracteres restantes`;
                
                if (remaining < 20) {
                    counter.style.color = 'var(--color-danger-600)';
                } else {
                    counter.style.color = 'var(--color-text-muted)';
                }
            };

            textarea.parentNode.appendChild(counter);
            textarea.addEventListener('input', updateCounter);
            updateCounter();
        });
    },

    // Utility: Get form data as object
    getFormData(form) {
        const formData = new FormData(form);
        const data = {};
        
        for (const [key, value] of formData.entries()) {
            // Handle arrays (checkboxes with same name)
            if (data[key]) {
                if (!Array.isArray(data[key])) {
                    data[key] = [data[key]];
                }
                data[key].push(value);
            } else {
                data[key] = value;
            }
        }
        
        return data;
    },

    // Utility: Populate form from object
    populateForm(form, data) {
        Object.entries(data).forEach(([key, value]) => {
            const input = form.querySelector(`[name="${key}"]`);
            if (!input) return;

            if (input.type === 'checkbox') {
                input.checked = !!value;
            } else if (input.type === 'radio') {
                const radio = form.querySelector(`[name="${key}"][value="${value}"]`);
                if (radio) radio.checked = true;
            } else {
                input.value = value;
            }
        });
    },

    // Utility: Reset form
    resetForm(form) {
        form.reset();
        form.classList.remove('was-validated');
        
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.classList.remove('is-valid', 'is-invalid');
            input.closest('.form-group')?.classList.remove('has-error');
        });
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Forms.init();
});

// Export
window.Forms = Forms;
