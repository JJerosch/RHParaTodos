package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.CargoRequest;
import proj.paratodos.dto.CargoResponse;
import proj.paratodos.repository.DepartamentoRepository;
import proj.paratodos.service.CargoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
public class CargoController {

    private final CargoService cargoService;
    private final DepartamentoRepository departamentoRepository;

    public CargoController(CargoService cargoService, DepartamentoRepository departamentoRepository) {
        this.cargoService = cargoService;
        this.departamentoRepository = departamentoRepository;
    }

    /** GET /api/positions?search=&departamentoId=&nivel= */
    @GetMapping
    public List<CargoResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String nivel) {
        return cargoService.search(search, departamentoId, nivel);
    }

    @GetMapping("/{id}")
    public CargoResponse getById(@PathVariable Long id) {
        return cargoService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CargoResponse> create(@Valid @RequestBody CargoRequest request) {
        CargoResponse created = cargoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CargoResponse update(@PathVariable Long id, @Valid @RequestBody CargoRequest request) {
        return cargoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cargoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/positions/departamentos - lista para dropdown */
    @GetMapping("/departamentos")
    public List<Map<String, Object>> listDepartamentos() {
        return departamentoRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "nome", d.getNome()))
                .toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
