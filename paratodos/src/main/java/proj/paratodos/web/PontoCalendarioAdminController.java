package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.PontoCalendarioDiaResponse;
import proj.paratodos.dto.PontoCalendarioUpdateRequest;
import proj.paratodos.service.PontoCalendarioService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ponto-calendario")
@PreAuthorize("hasRole('ADMIN')")
public class PontoCalendarioAdminController {

    private final PontoCalendarioService service;

    public PontoCalendarioAdminController(PontoCalendarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontoCalendarioDiaResponse> listarMes(
            @RequestParam int ano,
            @RequestParam int mes
    ) {
        return service.listarMes(ano, mes);
    }

    @PutMapping("/{data}")
    public ResponseEntity<Map<String, String>> salvarDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Valid @RequestBody PontoCalendarioUpdateRequest request
    ) {
        service.salvarDia(data, request);
        return ResponseEntity.ok(Map.of("message", "Dia atualizado com sucesso."));
    }

    @DeleteMapping("/{data}")
    public ResponseEntity<Map<String, String>> limparDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        service.limparDia(data);
        return ResponseEntity.ok(Map.of("message", "Configuração removida com sucesso."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}