package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.BeneficioStatsResponse;
import proj.paratodos.dto.TipoBeneficioRequest;
import proj.paratodos.dto.TipoBeneficioResponse;
import proj.paratodos.service.TipoBeneficioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/benefits")
public class TipoBeneficioController {

    private final TipoBeneficioService tipoBeneficioService;

    public TipoBeneficioController(TipoBeneficioService tipoBeneficioService) {
        this.tipoBeneficioService = tipoBeneficioService;
    }

    @GetMapping
    public List<TipoBeneficioResponse> list() {
        return tipoBeneficioService.findAll();
    }

    @GetMapping("/stats")
    public BeneficioStatsResponse stats() {
        return tipoBeneficioService.getStats();
    }

    @GetMapping("/{id}")
    public TipoBeneficioResponse getById(@PathVariable Long id) {
        return tipoBeneficioService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TipoBeneficioResponse> create(@Valid @RequestBody TipoBeneficioRequest request) {
        TipoBeneficioResponse created = tipoBeneficioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TipoBeneficioResponse update(@PathVariable Long id, @Valid @RequestBody TipoBeneficioRequest request) {
        return tipoBeneficioService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tipoBeneficioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
