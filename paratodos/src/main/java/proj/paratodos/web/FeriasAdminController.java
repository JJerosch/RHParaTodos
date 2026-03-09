package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.FeriasPageResponse;
import proj.paratodos.dto.FeriasRequest;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.service.FeriasService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ferias")
public class FeriasAdminController {

    private final FeriasService feriasService;

    public FeriasAdminController(FeriasService feriasService) {
        this.feriasService = feriasService;
    }

    @GetMapping("/funcionarios")
    @PreAuthorize("hasAnyRole('ADMIN','RH_CHEFE','RH_ASSISTENTE')")
    public FeriasPageResponse listarFuncionarios() {
        return feriasService.listarFuncionarios();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH_CHEFE','RH_ASSISTENTE')")
    public ResponseEntity<Map<String, String>> solicitar(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody FeriasRequest request
    ) {
        feriasService.solicitarFeriasAtivas(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Férias registradas com sucesso."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}