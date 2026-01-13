package sistema.rhparatodos.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sistema.rhparatodos.entity.Perfil;
import sistema.rhparatodos.entity.Usuario;
import sistema.rhparatodos.repository.PerfilRepository;
import sistema.rhparatodos.repository.UsuarioRepository;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Criar perfis se não existirem
            createPerfilIfNotExists("ADMIN", "Administrador do Sistema - Acesso total");
            createPerfilIfNotExists("RH_CHEFE", "Chefe de Recursos Humanos");
            createPerfilIfNotExists("RH_ASSISTENTE", "Assistente de Recursos Humanos");
            createPerfilIfNotExists("DP_CHEFE", "Chefe de Departamento Pessoal");
            createPerfilIfNotExists("DP_ASSISTENTE", "Assistente de Departamento Pessoal");

            // Verificar se já existem usuários
            if (usuarioRepository.count() == 0) {
                log.info("Nenhum usuário encontrado. Criando usuários padrão...");
                
                createDefaultUser("admin@rhparatodos.com.br", "admin123", "ADMIN");
                createDefaultUser("maria.costa@rhparatodos.com.br", "admin123", "RH_CHEFE");
                createDefaultUser("joao.silva@rhparatodos.com.br", "admin123", "RH_ASSISTENTE");
                createDefaultUser("carlos.santos@rhparatodos.com.br", "admin123", "DP_CHEFE");
                createDefaultUser("ana.oliveira@rhparatodos.com.br", "admin123", "DP_ASSISTENTE");
                
                log.info("Usuários padrão criados com sucesso!");
            }

            // Exibir informações de login
            log.info("=".repeat(60));
            log.info("USUÁRIOS DE TESTE (senha: admin123):");
            log.info("  - admin@rhparatodos.com.br (Administrador)");
            log.info("  - maria.costa@rhparatodos.com.br (Chefe de RH)");
            log.info("  - joao.silva@rhparatodos.com.br (Assistente de RH)");
            log.info("  - carlos.santos@rhparatodos.com.br (Chefe do DP)");
            log.info("  - ana.oliveira@rhparatodos.com.br (Assistente do DP)");
            log.info("=".repeat(60));
            log.info("Total de usuários no banco: {}", usuarioRepository.count());
        };
    }

    private void createPerfilIfNotExists(String nome, String descricao) {
        if (!perfilRepository.existsByNome(nome)) {
            Perfil perfil = Perfil.builder()
                    .nome(nome)
                    .descricao(descricao)
                    .build();
            perfilRepository.save(perfil);
            log.debug("Perfil criado: {}", nome);
        }
    }

    private void createDefaultUser(String email, String senha, String perfilNome) {
        if (!usuarioRepository.existsByEmail(email)) {
            Perfil perfil = perfilRepository.findByNome(perfilNome)
                    .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + perfilNome));
            
            Usuario usuario = Usuario.builder()
                    .email(email)
                    .senhaHash(passwordEncoder.encode(senha))
                    .perfil(perfil)
                    .ativo(true)
                    .autenticacao2fa(false)
                    .build();
            usuarioRepository.save(usuario);
            log.debug("Usuário criado: {}", email);
        }
    }
}
