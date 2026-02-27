package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.DepartamentoRequest;
import proj.paratodos.dto.DepartamentoResponse;
import proj.paratodos.dto.DepartamentoStatsResponse;
import proj.paratodos.service.DepartamentoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping
    public List<DepartamentoResponse> list() {
        return departamentoService.findAll();
    }

    @GetMapping("/stats")
    public DepartamentoStatsResponse stats() {
        return departamentoService.getStats();
    }

    @GetMapping("/{id}")
    public DepartamentoResponse getById(@PathVariable Long id) {
        return departamentoService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DepartamentoResponse> create(@Valid @RequestBody DepartamentoRequest request) {
        DepartamentoResponse created = departamentoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public DepartamentoResponse update(@PathVariable Long id, @Valid @RequestBody DepartamentoRequest request) {
        return departamentoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departamentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
