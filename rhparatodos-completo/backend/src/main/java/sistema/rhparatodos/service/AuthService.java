package sistema.rhparatodos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sistema.rhparatodos.dto.LoginRequest;
import sistema.rhparatodos.dto.LoginResponse;
import sistema.rhparatodos.entity.Perfil;
import sistema.rhparatodos.entity.Usuario;
import sistema.rhparatodos.repository.PerfilRepository;
import sistema.rhparatodos.repository.UsuarioRepository;
import sistema.rhparatodos.security.JwtService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para: {}", request.getEmail());

        // Converter perfil do frontend para nome no banco
        // Frontend envia: "admin", "rh-chefe", etc.
        // Banco tem: "ADMIN", "RH_CHEFE", etc.
        String perfilNome = convertFrontendProfileToDbName(request.getProfile());

        // Buscar usuário pelo email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos"));

        // Verificar se o perfil do usuário corresponde ao solicitado
        if (!usuario.getPerfil().getNome().equalsIgnoreCase(perfilNome)) {
            log.warn("Perfil não corresponde. Esperado: {}, Recebido: {}", 
                    usuario.getPerfil().getNome(), perfilNome);
            throw new BadCredentialsException("Perfil de acesso não autorizado para este usuário");
        }

        // Verificar se o usuário está ativo
        if (!usuario.getAtivo()) {
            log.warn("Tentativa de login de usuário inativo: {}", request.getEmail());
            throw new BadCredentialsException("Usuário inativo");
        }

        // Autenticar (valida a senha)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.warn("Falha na autenticação para: {}", request.getEmail());
            throw new BadCredentialsException("Email ou senha inválidos");
        }

        // Atualizar último acesso
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Gerar tokens
        String jwtToken = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        log.info("Login bem-sucedido para: {}", request.getEmail());

        // Construir resposta
        return LoginResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .user(LoginResponse.UserDTO.builder()
                        .id(usuario.getId())
                        .username(usuario.getEmail()) // Usando email como username
                        .email(usuario.getEmail())
                        .profile(usuario.getPerfil().toFrontendFormat())
                        .profileName(usuario.getPerfil().getNomeExibicao())
                        .permissions(usuario.getPerfil().getPermissoes())
                        .loginTime(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build())
                .build();
    }

    @Transactional
    public Usuario createUsuario(String email, String senha, Perfil perfil) {
        // Verificar se email já existe
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado: " + email);
        }

        Usuario usuario = Usuario.builder()
                .email(email)
                .senhaHash(passwordEncoder.encode(senha))
                .perfil(perfil)
                .ativo(true)
                .autenticacao2fa(false)
                .build();

        return usuarioRepository.save(usuario);
    }

    /**
     * Converte o formato do frontend para o nome do perfil no banco
     * Ex: "admin" -> "ADMIN", "rh-chefe" -> "RH_CHEFE"
     */
    private String convertFrontendProfileToDbName(String frontendProfile) {
        if (frontendProfile == null) {
            throw new BadCredentialsException("Perfil é obrigatório");
        }
        return frontendProfile.toUpperCase().replace("-", "_");
    }

    /**
     * Gera token de recuperação de senha
     */
    @Transactional
    public void gerarTokenRecuperacao(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email não encontrado"));

        String token = java.util.UUID.randomUUID().toString();
        usuario.setTokenRecuperacao(token);
        usuario.setTokenExpiracao(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);

        // TODO: Enviar email com link de recuperação
        log.info("Token de recuperação gerado para: {}", email);
    }

    /**
     * Valida token de recuperação e altera a senha
     */
    @Transactional
    public void recuperarSenha(String token, String novaSenha) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacao(token)
                .orElseThrow(() -> new BadCredentialsException("Token inválido"));

        if (!usuario.hasValidRecoveryToken()) {
            throw new BadCredentialsException("Token expirado");
        }

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setTokenRecuperacao(null);
        usuario.setTokenExpiracao(null);
        usuarioRepository.save(usuario);

        log.info("Senha alterada com sucesso para: {}", usuario.getEmail());
    }
}
