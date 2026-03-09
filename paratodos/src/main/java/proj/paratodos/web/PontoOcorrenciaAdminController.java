package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.PontoOcorrenciaRequest;
import proj.paratodos.dto.PontoOcorrenciaResponse;
import proj.paratodos.service.PontoOcorrenciaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ponto-ocorrencias")
public class PontoOcorrenciaAdminController {

    private final PontoOcorrenciaService service;

    public PontoOcorrenciaAdminController(PontoOcorrenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontoOcorrenciaResponse> listar(
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        return service.listar(funcionarioId, inicio, fim);
    }

    @PostMapping
    public PontoOcorrenciaResponse criar(@Valid @RequestBody PontoOcorrenciaRequest request) {
        return service.criar(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}