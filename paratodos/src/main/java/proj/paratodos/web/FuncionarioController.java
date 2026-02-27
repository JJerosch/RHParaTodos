package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.FuncionarioRequest;
import proj.paratodos.dto.FuncionarioResponse;
import proj.paratodos.dto.FuncionarioStatsResponse;
import proj.paratodos.repository.CargoRepository;
import proj.paratodos.repository.DepartamentoRepository;
import proj.paratodos.service.FuncionarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;

    public FuncionarioController(FuncionarioService funcionarioService,
                                 DepartamentoRepository departamentoRepository,
                                 CargoRepository cargoRepository) {
        this.funcionarioService = funcionarioService;
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
    }

    /** GET /api/employees?search=&departamentoId=&status=&page=0&size=10&sort=nomeCompleto,asc */
    @GetMapping
    public Page<FuncionarioResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomeCompleto,asc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return funcionarioService.search(search, departamentoId, status, pageable);
    }

    /** GET /api/employees/stats */
    @GetMapping("/stats")
    public FuncionarioStatsResponse stats() {
        return funcionarioService.getStats();
    }

    /** GET /api/employees/{id} */
    @GetMapping("/{id}")
    public FuncionarioResponse getById(@PathVariable Long id) {
        return funcionarioService.findById(id);
    }

    /** POST /api/employees */
    @PostMapping
    public ResponseEntity<FuncionarioResponse> create(@Valid @RequestBody FuncionarioRequest request) {
        FuncionarioResponse created = funcionarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/employees/{id} */
    @PutMapping("/{id}")
    public FuncionarioResponse update(@PathVariable Long id, @Valid @RequestBody FuncionarioRequest request) {
        return funcionarioService.update(id, request);
    }

    /** DELETE /api/employees/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        funcionarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/employees/departamentos - lista para dropdown */
    @GetMapping("/departamentos")
    public List<Map<String, Object>> listDepartamentos() {
        return departamentoRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(d -> Map.<String, Object>of("id", d.getId(), "nome", d.getNome()))
                .toList();
    }

    /** GET /api/employees/cargos?departamentoId= - lista para dropdown */
    @GetMapping("/cargos")
    public List<Map<String, Object>> listCargos(@RequestParam(required = false) Long departamentoId) {
        var cargos = departamentoId != null
                ? cargoRepository.findByDepartamentoIdAndAtivoTrueOrderByTituloAsc(departamentoId)
                : cargoRepository.findByAtivoTrueOrderByTituloAsc();

        return cargos.stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "titulo", c.getTitulo()))
                .toList();
    }

    /** GET /api/employees/gestores - lista de funcionarios ativos para dropdown gestor */
    @GetMapping("/gestores")
    public List<Map<String, Object>> listGestores() {
        // Retorna uma lista simplificada de todos os funcionarios ativos
        Pageable all = PageRequest.of(0, 1000, Sort.by("nomeCompleto"));
        return funcionarioService.search(null, null, "ATIVO", all)
                .getContent()
                .stream()
                .map(f -> Map.<String, Object>of("id", f.id(), "nome", f.nomeCompleto()))
                .toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
