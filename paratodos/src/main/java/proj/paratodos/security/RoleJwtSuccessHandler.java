package proj.paratodos.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import proj.paratodos.service.JwtService;

import java.io.IOException;

@Component
public class RoleJwtSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    public RoleJwtSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void issueJwtCookie(UserPrincipal principal, HttpServletResponse response) {
        String role = principal.getRole();
        String token = jwtService.createToken(principal.getUsername(), role);

        Cookie cookie = new Cookie(JwtCookieFilter.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1 dia
        // cookie.setSecure(true); // habilitar em produção com HTTPS
        response.addCookie(cookie);
    }

    public String resolveRedirect(UserPrincipal principal) {
        String role = principal.getRole().toUpperCase();
        return switch (role) {
            case "ADMIN", "RH_CHEFE", "RH_ASSISTENTE", "DP_CHEFE", "DP_ASSISTENTE" -> "/dashboard";
            default -> "/employee-dashboard";
        };
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // Este handler não é usado diretamente no formLogin
        // A lógica de emissão pós-2FA está no TwoFactorLoginSuccessHandler e TwoFactorController
    }
}