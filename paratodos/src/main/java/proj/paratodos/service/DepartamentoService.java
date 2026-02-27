package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Departamento;
import proj.paratodos.dto.DepartamentoRequest;
import proj.paratodos.dto.DepartamentoResponse;
import proj.paratodos.dto.DepartamentoStatsResponse;
import proj.paratodos.repository.DepartamentoRepository;

import java.util.List;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<DepartamentoResponse> findAll() {
        return departamentoRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public DepartamentoResponse findById(Long id) {
        Departamento d = departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Departamento nao encontrado: " + id));
        return toResponse(d);
    }

    @Transactional
    public DepartamentoResponse create(DepartamentoRequest request) {
        Departamento d = new Departamento();
        mapRequestToEntity(request, d);
        d = departamentoRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public DepartamentoResponse update(Long id, DepartamentoRequest request) {
        Departamento d = departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Departamento nao encontrado: " + id));
        mapRequestToEntity(request, d);
        d = departamentoRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public void delete(Long id) {
        if (!departamentoRepository.existsById(id)) {
            throw new IllegalArgumentException("Departamento nao encontrado: " + id);
        }
        departamentoRepository.deleteById(id);
    }

    public DepartamentoStatsResponse getStats() {
        long total = departamentoRepository.count();
        long ativos = departamentoRepository.countByAtivoTrue();
        long funcionarios = departamentoRepository.countTotalFuncionariosComDepartamento();
        long media = total > 0 ? Math.round((double) funcionarios / total) : 0;
        return new DepartamentoStatsResponse(total, ativos, funcionarios, media);
    }

    private void mapRequestToEntity(DepartamentoRequest req, Departamento d) {
        d.setNome(req.nome());
        d.setDescricao(req.descricao());
        d.setDepartamentoPaiId(req.departamentoPaiId());
        d.setAtivo(req.ativo() != null ? req.ativo() : true);
    }

    private DepartamentoResponse toResponse(Departamento d) {
        long funcionarios = departamentoRepository.countFuncionariosByDepartamentoId(d.getId());
        long cargos = departamentoRepository.countCargosByDepartamentoId(d.getId());

        String paiNome = null;
        if (d.getDepartamentoPaiId() != null) {
            paiNome = departamentoRepository.findById(d.getDepartamentoPaiId())
                    .map(Departamento::getNome)
                    .orElse(null);
        }

        return DepartamentoResponse.fromEntity(d, funcionarios, cargos, paiNome);
    }
}
