package proj.paratodos.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.security.UserPrincipal;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class TwoFactorService {

    private final TwoFactorCodeStore store;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    private final SecureRandom random = new SecureRandom();

    public TwoFactorService(TwoFactorCodeStore store, PasswordEncoder encoder, EmailService emailService) {
        this.store = store;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    public boolean needsTwoFactor(UserPrincipal p) {
        return p != null && p.isTwoFactorEnabled();
    }

    @Transactional
    public String startCodeChallenge(UserPrincipal p) {
        String code = String.format("%06d", random.nextInt(1_000_000));

        store.invalidateUnusedCodes(p.getId());
        String hash = encoder.encode(code);
        store.insertCode(p.getId(), hash, LocalDateTime.now().plusMinutes(5));

        // Envia por email
        try {
            emailService.send2FACode(p.getUsername(), code);
            System.out.println("[2FA] Codigo enviado por email para " + p.getUsername());
        } catch (Exception e) {
            // Log no console como fallback caso o email falhe
            System.out.println("[2FA] Falha ao enviar email para " + p.getUsername()
                    + " - codigo=" + code + " (erro: " + e.getMessage() + ")");
        }

        return code;
    }

    @Transactional
    public boolean verify(UserPrincipal p, String inputCode) {
        String latestHash = store.findLatestValidHash(p.getId());
        if (latestHash == null) return false;

        boolean ok = encoder.matches(inputCode, latestHash);
        if (ok) store.markUsed(p.getId(), latestHash);

        return ok;
    }
}
