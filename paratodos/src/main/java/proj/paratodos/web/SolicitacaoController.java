package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    private final SolicitacaoService solicitacaoService;

    public SolicitacaoController(SolicitacaoService solicitacaoService) {
        this.solicitacaoService = solicitacaoService;
    }

    @GetMapping
    public List<SolicitacaoResponse> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return solicitacaoService.findByStatus(status);
        }
        return solicitacaoService.findAll();
    }

    @GetMapping("/stats")
    public SolicitacaoService.SolicitacaoStatsResponse stats() {
        return solicitacaoService.getStats();
    }

    @GetMapping("/{id}")
    public SolicitacaoResponse getById(@PathVariable Long id) {
        return solicitacaoService.findById(id);
    }

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> create(@Valid @RequestBody SolicitacaoRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        SolicitacaoResponse created = solicitacaoService.create(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/approve")
    public SolicitacaoResponse approve(@PathVariable Long id,
                                       @RequestBody(required = false) Map<String, String> body,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        String observacao = body != null ? body.get("observacao") : null;
        return solicitacaoService.approve(id, principal.getId(), observacao);
    }

    @PutMapping("/{id}/reject")
    public SolicitacaoResponse reject(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, String> body,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        String observacao = body != null ? body.get("observacao") : null;
        return solicitacaoService.reject(id, principal.getId(), observacao);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
