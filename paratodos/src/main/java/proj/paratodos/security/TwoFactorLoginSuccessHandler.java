package proj.paratodos.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import proj.paratodos.service.TwoFactorService;

import java.io.IOException;

@Component
public class TwoFactorLoginSuccessHandler implements AuthenticationSuccessHandler {

    public static final String SESSION_2FA_OK = "TWO_FACTOR_VERIFIED";

    private final TwoFactorService twoFactorService;

    public TwoFactorLoginSuccessHandler(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        HttpSession session = request.getSession(true);

        if (twoFactorService.needsTwoFactor(p)) {
            session.setAttribute(SESSION_2FA_OK, false);

            String tipo = (p.getTwoFactorType() == null ? "CODIGO" : p.getTwoFactorType().toUpperCase());
            if (!"TOTP".equals(tipo)) {
                String code = twoFactorService.startCodeChallenge(p);
                System.out.println("DEBUG 2FA (usuario=" + p.getUsername() + "): código = " + code);
            }

            response.sendRedirect("/2fa");
            return;
        }

        session.setAttribute(SESSION_2FA_OK, true);
        response.sendRedirect("/dashboard");
    }
}
