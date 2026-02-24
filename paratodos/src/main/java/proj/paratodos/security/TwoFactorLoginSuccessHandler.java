package proj.paratodos.security;

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
    private final RoleJwtSuccessHandler roleJwtSuccessHandler;

    public TwoFactorLoginSuccessHandler(TwoFactorService twoFactorService,
                                        RoleJwtSuccessHandler roleJwtSuccessHandler) {
        this.twoFactorService = twoFactorService;
        this.roleJwtSuccessHandler = roleJwtSuccessHandler;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        HttpSession session = request.getSession(true);

        if (twoFactorService.needsTwoFactor(principal)) {
            // Nao emite JWT ainda — gera codigo e envia por email
            session.setAttribute(SESSION_2FA_OK, false);
            twoFactorService.startCodeChallenge(principal);
            response.sendRedirect("/2fa");
            return;
        }

        // Sem 2FA: emite JWT e redireciona
        session.setAttribute(SESSION_2FA_OK, true);
        roleJwtSuccessHandler.issueJwtCookie(principal, response);
        response.sendRedirect(roleJwtSuccessHandler.resolveRedirect(principal));
    }
}
