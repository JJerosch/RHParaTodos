package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proj.paratodos.dto.CargoRequest;
import proj.paratodos.dto.CargoResponse;
import proj.paratodos.repository.DepartamentoRepository;
import proj.paratodos.service.CargoService;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import proj.paratodos.domain.Departamento;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.repository.FuncionarioRepository;

@RestController
@RequestMapping("/api/positions")
public class CargoController {

    private final CargoService cargoService;
    private final DepartamentoRepository departamentoRepository;
    private final FuncionarioRepository funcionarioRepository;

    public CargoController(CargoService cargoService,
                           DepartamentoRepository departamentoRepository,
                           FuncionarioRepository funcionarioRepository) {
        this.cargoService = cargoService;
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'RH_CHEFE', 'RH_ASSISTENTE')")
    public ResponseEntity<CargoResponse> create(@Valid @RequestBody CargoRequest request) {
        CargoResponse created = cargoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH_CHEFE', 'RH_ASSISTENTE')")
    public CargoResponse update(@PathVariable Long id, @Valid @RequestBody CargoRequest request) {
        return cargoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH_CHEFE', 'RH_ASSISTENTE')")
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

    /** GET /api/positions/by-department - departamentos com cargos e funcionários */
    @GetMapping("/by-department")
    public List<Map<String, Object>> listByDepartment() {
        List<Departamento> departamentos = departamentoRepository.findByAtivoTrueOrderByNomeAsc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Departamento dept : departamentos) {
            List<Funcionario> funcionarios = funcionarioRepository.findAtivosByDepartamentoId(dept.getId());

            // Agrupar por cargo
            Map<String, List<Map<String, Object>>> cargoMap = new LinkedHashMap<>();
            for (Funcionario f : funcionarios) {
                String cargoKey = f.getCargo() != null ? f.getCargo().getTitulo() : "Sem cargo";
                cargoMap.computeIfAbsent(cargoKey, k -> new ArrayList<>());

                Map<String, Object> funcData = new LinkedHashMap<>();
                funcData.put("id", f.getId());
                funcData.put("nome", f.getNomeCompleto());
                funcData.put("salario", f.getSalarioAtual());
                funcData.put("dataAdmissao", f.getDataAdmissao() != null ? f.getDataAdmissao().toString() : null);
                cargoMap.get(cargoKey).add(funcData);
            }

            // Também incluir cargos sem funcionários
            var cargos = cargoService.search(null, dept.getId(), null);
            for (var cargo : cargos) {
                if (!cargoMap.containsKey(cargo.titulo())) {
                    cargoMap.put(cargo.titulo(), new ArrayList<>());
                }
            }

            List<Map<String, Object>> cargosList = new ArrayList<>();
            for (var entry : cargoMap.entrySet()) {
                Map<String, Object> cargoData = new LinkedHashMap<>();
                cargoData.put("titulo", entry.getKey());
                cargoData.put("funcionarios", entry.getValue());
                cargosList.add(cargoData);
            }

            Map<String, Object> deptData = new LinkedHashMap<>();
            deptData.put("id", dept.getId());
            deptData.put("nome", dept.getNome());
            deptData.put("totalFuncionarios", funcionarios.size());
            deptData.put("cargos", cargosList);
            result.add(deptData);
        }
        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
