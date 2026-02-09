// Módulo de Autenticação de Dois Fatores (2FA)
// Este arquivo fornece funcionalidades complementares e utilitários para 2FA

const TwoFactorAuth = {
  // Configuração de timeouts
  config: {
    codeTimeout: 10 * 60 * 1000, // 10 minutos
    maxAttempts: 5,
    resendDelay: 30, // 30 segundos
  },

  // Validar formato do código
  isValidCode(code) {
    return /^\d{6}$/.test(code);
  },

  // Obter tempo restante para código expirar
  getCodeTimeRemaining() {
    const pending = Auth.get2FAPending();
    if (!pending || !pending.timestamp) return 0;

    const elapsed = Date.now() - pending.timestamp;
    const remaining = Math.max(0, this.config.codeTimeout - elapsed);
    return Math.ceil(remaining / 1000); // retornar em segundos
  },

  // Verificar se o código expirou
  isCodeExpired() {
    return this.getCodeTimeRemaining() <= 0;
  },

  // Formatar tempo para exibição
  formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  },

  // Registrar tentativa de verificação
  recordAttempt() {
    const pending = Auth.get2FAPending();
    if (!pending) return;

    const attempts = pending.attempts || 0;
    pending.attempts = attempts + 1;
    Auth.set2FAPending(pending.email, pending.tempToken);
  },

  // Obter número de tentativas
  getAttempts() {
    const pending = Auth.get2FAPending();
    return pending?.attempts || 0;
  },

  // Verificar se ultrapassou máximo de tentativas
  hasExceededMaxAttempts() {
    return this.getAttempts() >= this.config.maxAttempts;
  },

  // Limpar tentativas
  resetAttempts() {
    const pending = Auth.get2FAPending();
    if (!pending) return;

    pending.attempts = 0;
    Auth.set2FAPending(pending.email, pending.tempToken);
  },
};

// Expor globalmente
window.TwoFactorAuth = TwoFactorAuth;
