package proj.paratodos.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.domain.Usuario;
import proj.paratodos.dto.FuncionarioResponse;
import proj.paratodos.repository.FuncionarioRepository;
import proj.paratodos.repository.UsuarioRepository;
import proj.paratodos.security.UserPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(FuncionarioRepository funcionarioRepository,
                             UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder) {
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** GET /api/me — retorna dados do funcionário vinculado ao usuário logado */
    @GetMapping
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return funcionarioRepository.findByUsuarioId(principal.getId())
                .map(f -> ResponseEntity.ok(FuncionarioResponse.fromEntity(f)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** PUT /api/me — atualiza campos editáveis do perfil */
    @PutMapping
    public ResponseEntity<?> updateMyProfile(@AuthenticationPrincipal UserPrincipal principal,
                                             @RequestBody Map<String, String> body) {
        Funcionario f = funcionarioRepository.findByUsuarioId(principal.getId())
                .orElse(null);
        if (f == null) {
            return ResponseEntity.notFound().build();
        }

        // Apenas campos que o próprio funcionário pode editar
        if (body.containsKey("telefone"))       f.setTelefone(body.get("telefone"));
        if (body.containsKey("celular"))        f.setCelular(body.get("celular"));
        if (body.containsKey("emailPessoal"))   f.setEmailPessoal(body.get("emailPessoal"));
        if (body.containsKey("cep"))            f.setCep(body.get("cep"));
        if (body.containsKey("logradouro"))     f.setLogradouro(body.get("logradouro"));
        if (body.containsKey("numero"))         f.setNumero(body.get("numero"));
        if (body.containsKey("complemento"))    f.setComplemento(body.get("complemento"));
        if (body.containsKey("bairro"))         f.setBairro(body.get("bairro"));
        if (body.containsKey("cidade"))         f.setCidade(body.get("cidade"));
        if (body.containsKey("estado"))         f.setEstado(body.get("estado"));

        funcionarioRepository.save(f);
        return ResponseEntity.ok(FuncionarioResponse.fromEntity(f));
    }

    /** PUT /api/me/password — altera a senha do usuário logado */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                            @RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Senha atual e nova senha sao obrigatorias"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "A nova senha deve ter pelo menos 6 caracteres"));
        }

        Usuario usuario = usuarioRepository.findById(principal.getId())
                .orElseThrow();

        if (!passwordEncoder.matches(currentPassword, usuario.getSenhaHash())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Senha atual incorreta"));
        }

        usuario.setSenhaHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }
}
