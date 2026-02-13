package proj.paratodos.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;

@Service
public class TotpService {

    private final TimeBasedOneTimePasswordGenerator totp;

    public TotpService() throws Exception {
        this.totp = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30));
    }

    public boolean verify(String base32Secret, String code) {
        if (base32Secret == null || base32Secret.isBlank()) return false;
        if (code == null || code.isBlank()) return false;

        String cleaned = code.replaceAll("\\s+", "");
        if (!cleaned.matches("\\d{6}")) return false;

        try {
            byte[] keyBytes = new Base32().decode(base32Secret);
            SecretKey key = new SecretKeySpec(keyBytes, totp.getAlgorithm());

            Instant now = Instant.now();
            for (int i = -1; i <= 1; i++) {
                Instant t = now.plusSeconds(30L * i);

                int otp = totp.generateOneTimePassword(key, t); // aqui pode lançar exceção
                String expected = String.format("%06d", otp);

                if (expected.equals(cleaned)) return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
