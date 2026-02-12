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
    private final TotpService totpService;

    private final SecureRandom random = new SecureRandom();

    public TwoFactorService(TwoFactorCodeStore store, PasswordEncoder encoder, TotpService totpService) {
        this.store = store;
        this.encoder = encoder;
        this.totpService = totpService;
    }

    public boolean needsTwoFactor(UserPrincipal p) {
        return p != null && p.isTwoFactorEnabled();
    }

    @Transactional
    public String startCodeChallenge(UserPrincipal p) {
        // 6 dígitos
        String code = String.format("%06d", random.nextInt(1_000_000));

        store.invalidateUnusedCodes(p.getId());
        String hash = encoder.encode(code);
        store.insertCode(p.getId(), hash, LocalDateTime.now().plusMinutes(5));

        return code; // por enquanto, vamos logar no console (depois integra email/sms)
    }

    @Transactional
    public boolean verify(UserPrincipal p, String inputCode) {
        String tipo = (p.getTwoFactorType() == null ? "CODIGO" : p.getTwoFactorType().toUpperCase());

        if ("TOTP".equals(tipo)) {
            return totpService.verify(p.getTwoFactorSecret(), inputCode);
        }

        String latestHash = store.findLatestValidHash(p.getId());
        if (latestHash == null) return false;

        boolean ok = encoder.matches(inputCode, latestHash);
        if (ok) store.markUsed(p.getId(), latestHash);

        return ok;
    }
}
