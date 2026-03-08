package proj.paratodos.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.MeuPontoResumoResponse;
import proj.paratodos.dto.PontoSemanaItemResponse;
import proj.paratodos.dto.RegistrarPontoResponse;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.service.PontoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/my-timesheet")
public class MeuPontoController {

    private final PontoService pontoService;

    public MeuPontoController(PontoService pontoService) {
        this.pontoService = pontoService;
    }

    @GetMapping("/summary")
    public MeuPontoResumoResponse summary(@AuthenticationPrincipal UserPrincipal user) {
        return pontoService.getResumoMeuPonto(user.getId());
    }

    @GetMapping("/week")
    public List<PontoSemanaItemResponse> week(@AuthenticationPrincipal UserPrincipal user) {
        return pontoService.getHistoricoSemana(user.getId());
    }

    @PostMapping("/punch")
    public RegistrarPontoResponse punch(
            @AuthenticationPrincipal UserPrincipal user,
            HttpServletRequest request
    ) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return pontoService.registrarBatida(user.getId(), ip);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}