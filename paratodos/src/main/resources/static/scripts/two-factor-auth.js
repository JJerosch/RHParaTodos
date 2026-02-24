// ==========================================
// TWO-FACTOR-AUTH.JS — 2FA page utilities
// The 2FA form submits directly to Spring Security (/2fa POST).
// This file provides client-side code validation helpers.
// ==========================================

const TwoFactorAuth = {
  config: {
    codeLength: 6,
  },

  isValidCode(code) {
    return new RegExp(`^\\d{${this.config.codeLength}}$`).test(code);
  },

  formatCode(value) {
    return value.replace(/[^0-9]/g, '').slice(0, this.config.codeLength);
  },
};

window.TwoFactorAuth = TwoFactorAuth;
