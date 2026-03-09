package proj.paratodos.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

import proj.paratodos.domain.Cargo;
import proj.paratodos.domain.Departamento;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.domain.TipoBeneficio;
import proj.paratodos.repository.CargoRepository;
import proj.paratodos.repository.FuncionarioRepository;
import proj.paratodos.repository.TipoBeneficioRepository;

@RestController
@RequestMapping("/api/positions")
public class CargoController {

    private final CargoService cargoService;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoBeneficioRepository tipoBeneficioRepository;

    public CargoController(CargoService cargoService,
                           CargoRepository cargoRepository,
                           DepartamentoRepository departamentoRepository,
                           FuncionarioRepository funcionarioRepository,
                           TipoBeneficioRepository tipoBeneficioRepository) {
        this.cargoService = cargoService;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.tipoBeneficioRepository = tipoBeneficioRepository;
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
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listByDepartment() {
        List<Departamento> departamentos = departamentoRepository.findByAtivoTrueOrderByNomeAsc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Departamento dept : departamentos) {
            // Buscar cargos deste departamento diretamente
            List<Cargo> cargos = cargoRepository.findByDepartamentoIdAndAtivoTrueOrderByTituloAsc(dept.getId());

            // Buscar funcionários ativos do departamento
            List<Funcionario> funcionarios;
            try {
                funcionarios = funcionarioRepository.findAtivosByDepartamentoId(dept.getId());
            } catch (Exception e) {
                funcionarios = List.of();
            }

            // Mapear funcionários por cargo id
            Map<Long, List<Map<String, Object>>> funcPorCargo = new LinkedHashMap<>();
            for (Funcionario f : funcionarios) {
                Long cargoId = f.getCargo() != null ? f.getCargo().getId() : null;
                if (cargoId != null) {
                    funcPorCargo.computeIfAbsent(cargoId, k -> new ArrayList<>());
                    Map<String, Object> funcData = new LinkedHashMap<>();
                    funcData.put("id", f.getId());
                    funcData.put("nome", f.getNomeCompleto());
                    funcData.put("salario", f.getSalarioAtual());
                    funcData.put("dataAdmissao", f.getDataAdmissao() != null ? f.getDataAdmissao().toString() : null);
                    funcPorCargo.get(cargoId).add(funcData);
                }
            }

            List<Map<String, Object>> cargosList = new ArrayList<>();
            for (Cargo cargo : cargos) {
                Map<String, Object> cargoData = new LinkedHashMap<>();
                cargoData.put("id", cargo.getId());
                cargoData.put("titulo", cargo.getTitulo());
                cargoData.put("nivel", cargo.getNivel());
                cargoData.put("salarioMinimo", cargo.getSalarioMinimo());
                cargoData.put("salarioMaximo", cargo.getSalarioMaximo());
                cargoData.put("ativo", cargo.getAtivo());

                List<Map<String, Object>> funcs = funcPorCargo.getOrDefault(cargo.getId(), List.of());
                cargoData.put("funcionarios", funcs);
                cargoData.put("ocupantes", funcs.size());

                // Benefícios vinculados ao cargo
                try {
                    List<String> beneficioNomes = tipoBeneficioRepository.findByCargoId(cargo.getId())
                            .stream().map(TipoBeneficio::getNome).toList();
                    cargoData.put("beneficios", beneficioNomes);
                } catch (Exception e) {
                    cargoData.put("beneficios", List.of());
                }

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
