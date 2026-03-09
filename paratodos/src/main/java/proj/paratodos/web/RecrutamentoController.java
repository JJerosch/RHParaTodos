package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.*;
import proj.paratodos.security.UserPrincipal;
import proj.paratodos.service.RecrutamentoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment")
public class RecrutamentoController {

    private final RecrutamentoService recrutamentoService;

    public RecrutamentoController(RecrutamentoService recrutamentoService) {
        this.recrutamentoService = recrutamentoService;
    }

    // ── Vagas ──

    @GetMapping("/vagas")
    public List<VagaResponse> listVagas(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return recrutamentoService.findVagasByStatus(status);
        }
        return recrutamentoService.findAllVagas();
    }

    @GetMapping("/vagas/{id}")
    public VagaResponse getVaga(@PathVariable Long id) {
        return recrutamentoService.findVagaById(id);
    }

    @PostMapping("/vagas")
    public ResponseEntity<VagaResponse> createVaga(@Valid @RequestBody VagaRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        VagaResponse created = recrutamentoService.createVaga(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/vagas/{id}")
    public VagaResponse updateVaga(@PathVariable Long id, @Valid @RequestBody VagaRequest request) {
        return recrutamentoService.updateVaga(id, request);
    }

    @PutMapping("/vagas/{id}/approve")
    public VagaResponse approveVaga(@PathVariable Long id) {
        return recrutamentoService.approveVaga(id);
    }

    @PutMapping("/vagas/{id}/cancel")
    public VagaResponse cancelVaga(@PathVariable Long id) {
        return recrutamentoService.cancelVaga(id);
    }

    // ── Candidatos ──

    @GetMapping("/candidatos")
    public List<CandidatoResponse> listCandidatos() {
        return recrutamentoService.findAllCandidatos();
    }

    @PostMapping("/candidatos")
    public ResponseEntity<CandidatoResponse> createCandidato(@Valid @RequestBody CandidatoRequest request) {
        CandidatoResponse created = recrutamentoService.createCandidato(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── Candidaturas ──

    @GetMapping("/vagas/{vagaId}/candidaturas")
    public List<CandidaturaResponse> listCandidaturas(@PathVariable Long vagaId) {
        return recrutamentoService.findCandidaturasByVaga(vagaId);
    }

    @PostMapping("/candidaturas")
    public ResponseEntity<CandidaturaResponse> createCandidatura(@RequestBody Map<String, Long> body) {
        Long vagaId = body.get("vagaId");
        Long candidatoId = body.get("candidatoId");
        if (vagaId == null || candidatoId == null) {
            throw new IllegalArgumentException("vagaId e candidatoId sao obrigatorios");
        }
        CandidaturaResponse created = recrutamentoService.createCandidatura(vagaId, candidatoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/candidaturas/{id}/etapa")
    public CandidaturaResponse updateEtapa(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        String etapa = body.get("etapa");
        if (etapa == null || etapa.isBlank()) {
            throw new IllegalArgumentException("Etapa e obrigatoria");
        }
        return recrutamentoService.updateEtapa(id, etapa, body.get("observacoes"), body.get("motivoRejeicao"), principal.getId());
    }

    @GetMapping("/stats")
    public RecrutamentoService.RecrutamentoStatsResponse stats() {
        return recrutamentoService.getStats();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
