package sistema.rhparatodos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import sistema.rhparatodos.dto.LoginRequest;
import sistema.rhparatodos.dto.LoginResponse;
import sistema.rhparatodos.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // CORS adicional para este controller
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativa de login: usuário={}, perfil={}", request.getUsername(), request.getProfile());
        
        try {
            LoginResponse response = authService.login(request);
            log.info("Login realizado com sucesso: usuário={}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Falha no login: usuário={}, motivo={}", request.getUsername(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("Erro no login: usuário={}", request.getUsername(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Internal Server Error",
                            "message", "Erro ao processar login"
                    ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Em uma implementação completa, você pode:
        // 1. Invalidar o token em um blacklist/cache
        // 2. Registrar o logout em auditoria
        log.info("Logout realizado");
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Refresh token é obrigatório"));
        }

        // TODO: Implementar refresh token
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("message", "Refresh token não implementado ainda"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        // Se chegou aqui, o token é válido (passou pelo JwtAuthenticationFilter)
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Token válido"
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service"
        ));
    }
}
