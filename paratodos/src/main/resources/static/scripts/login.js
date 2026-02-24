// ==========================================
// LOGIN.JS — Login page utilities
// The login form submits directly to Spring Security (/login POST).
// This file is kept for any future client-side enhancements.
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
  const passwordInput = document.getElementById('password');
  const toggleBtn = document.querySelector('.password-toggle');

  if (toggleBtn && passwordInput) {
    toggleBtn.addEventListener('click', () => {
      if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleBtn.textContent = 'Hide';
      } else {
        passwordInput.type = 'password';
        toggleBtn.textContent = 'Show';
      }
    });
  }
});
