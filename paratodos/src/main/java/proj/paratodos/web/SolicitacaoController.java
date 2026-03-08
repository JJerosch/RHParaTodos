package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.SolicitacaoRequest;
import proj.paratodos.dto.SolicitacaoResponse;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.service.SolicitacaoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    private final SolicitacaoService service;

    public SolicitacaoController(SolicitacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<SolicitacaoResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo) {

        if (status != null && !status.isBlank() && tipo != null && !tipo.isBlank()) {
            return service.findByStatusAndTipo(status, tipo);
        }
        if (status != null && !status.isBlank()) {
            return service.findByStatus(status);
        }
        if (tipo != null && !tipo.isBlank()) {
            return service.findByTipo(tipo);
        }
        return service.findAll();
    }

    @GetMapping("/stats")
    public SolicitacaoService.SolicitacaoStatsResponse stats() {
        return service.getStats();
    }

    @GetMapping("/{id}")
    public SolicitacaoResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public SolicitacaoResponse create(
            @Valid @RequestBody SolicitacaoRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return service.create(request, user.getId());
    }

    @PutMapping("/{id}/approve")
    public SolicitacaoResponse approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal user) {
        String observacao = (body != null) ? body.get("observacao") : null;
        return service.approve(id, user.getId(), observacao);
    }

    @PutMapping("/{id}/reject")
    public SolicitacaoResponse reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal user) {
        String observacao = (body != null) ? body.get("observacao") : null;
        return service.reject(id, user.getId(), observacao);
    }

    @PutMapping("/{id}/cancel")
    public SolicitacaoResponse cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        return service.cancel(id, user.getId());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
