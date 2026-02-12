package proj.paratodos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class TwoFactorEnforcementFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOW_PREFIX = Set.of(
            "/login", "/2fa", "/error", "/css/", "/js/", "/images/", "/webjars/", "/logout"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return ALLOW_PREFIX.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            Object principal = auth.getPrincipal();
            if (principal instanceof UserPrincipal p && p.isTwoFactorEnabled()) {

                HttpSession session = request.getSession(false);
                boolean ok = session != null && Boolean.TRUE.equals(session.getAttribute(TwoFactorLoginSuccessHandler.SESSION_2FA_OK));

                if (!ok) {
                    response.sendRedirect("/2fa");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
