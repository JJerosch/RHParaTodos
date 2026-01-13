package sistema.rhparatodos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    /**
     * Email do usuário para login.
     * Aceita tanto "email" quanto "username" no JSON para manter compatibilidade com o frontend.
     */
    @NotBlank(message = "Email é obrigatório")
    @JsonAlias({"username", "user"})
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @JsonAlias("senha")
    private String password;
    
    @NotBlank(message = "Perfil é obrigatório")
    @JsonAlias("perfil")
    private String profile;
}
