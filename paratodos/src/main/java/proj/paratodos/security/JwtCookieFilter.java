package proj.paratodos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import proj.paratodos.repository.UsuarioRepository;
import proj.paratodos.service.JwtService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// SEM @Component
public class JwtCookieFilter extends OncePerRequestFilter {

    public static final String COOKIE_NAME = "ACCESS_TOKEN";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtCookieFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtService.isValid(token)) {
            String email = jwtService.getEmail(token);
            String role = jwtService.getRole(token);

            usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
                if (Boolean.TRUE.equals(usuario.getAtivo())) {
                    UserPrincipal principal = new UserPrincipal(usuario);
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    var session = request.getSession(false);
                    if (session != null) {
                        session.setAttribute(TwoFactorLoginSuccessHandler.SESSION_2FA_OK, true);
                    }
                }
            });
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}