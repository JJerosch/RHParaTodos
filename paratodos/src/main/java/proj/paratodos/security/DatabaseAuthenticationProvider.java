package proj.paratodos.security;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Usuario;
import proj.paratodos.repository.UsuarioRepository;

import java.time.LocalDateTime;

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private static final int MAX_TENTATIVAS = 5;
    private static final int BLOQUEIO_MINUTOS = 15;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseAuthenticationProvider(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String senha = (String) authentication.getCredentials();

        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!Boolean.TRUE.equals(u.getAtivo())) {
            throw new DisabledException("Usuário inativo");
        }

        LocalDateTime agora = LocalDateTime.now();
        if (u.getBloqueadoAte() != null && u.getBloqueadoAte().isAfter(agora)) {
            throw new LockedException("Usuário bloqueado até " + u.getBloqueadoAte());
        }

        boolean ok = passwordEncoder.matches(senha, u.getSenhaHash());
        if (!ok) {
            int tentativas = (u.getTentativasLogin() == null ? 0 : u.getTentativasLogin()) + 1;
            u.setTentativasLogin(tentativas);

            if (tentativas >= MAX_TENTATIVAS) {
                u.setBloqueadoAte(agora.plusMinutes(BLOQUEIO_MINUTOS));
                u.setTentativasLogin(0);
            }

            usuarioRepository.save(u);
            throw new BadCredentialsException("Credenciais inválidas");
        }

        // sucesso
        u.setTentativasLogin(0);
        u.setBloqueadoAte(null);
        u.setUltimoLogin(agora);
        usuarioRepository.save(u);

        UserPrincipal principal = new UserPrincipal(u);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
