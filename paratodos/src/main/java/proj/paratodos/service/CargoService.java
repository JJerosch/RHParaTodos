package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Cargo;
import proj.paratodos.domain.Departamento;
import proj.paratodos.dto.CargoRequest;
import proj.paratodos.dto.CargoResponse;
import proj.paratodos.repository.CargoRepository;
import proj.paratodos.repository.DepartamentoRepository;

import java.util.List;

@Service
public class CargoService {

    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;

    public CargoService(CargoRepository cargoRepository, DepartamentoRepository departamentoRepository) {
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
    }

    @Transactional(readOnly = true)
    public List<CargoResponse> search(String search, Long departamentoId, String nivel) {
        String searchParam = (search != null && !search.isBlank()) ? search.trim() : null;
        String nivelParam = (nivel != null && !nivel.isBlank()) ? nivel.trim() : null;

        return cargoRepository.search(searchParam, departamentoId, nivelParam).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CargoResponse findById(Long id) {
        Cargo c = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo nao encontrado: " + id));
        return toResponse(c);
    }

    @Transactional
    public CargoResponse create(CargoRequest request) {
        Cargo c = new Cargo();
        mapRequestToEntity(request, c);
        c = cargoRepository.save(c);
        return toResponse(c);
    }

    @Transactional
    public CargoResponse update(Long id, CargoRequest request) {
        Cargo c = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo nao encontrado: " + id));
        mapRequestToEntity(request, c);
        c = cargoRepository.save(c);
        return toResponse(c);
    }

    @Transactional
    public void delete(Long id) {
        Cargo c = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo nao encontrado: " + id));
        long funcionariosAtivos = cargoRepository.countFuncionariosByCargoId(id);
        if (funcionariosAtivos > 0) {
            throw new IllegalArgumentException(
                "Nao e possivel excluir o cargo '" + c.getTitulo() +
                "'. Existem " + funcionariosAtivos + " funcionario(s) vinculado(s). " +
                "Demita ou transfira o funcionario antes de excluir.");
        }
        cargoRepository.deleteById(id);
    }

    private void mapRequestToEntity(CargoRequest req, Cargo c) {
        c.setTitulo(req.titulo());
        c.setDescricao(req.descricao());
        c.setNivel(req.nivel());
        c.setSalarioMinimo(req.salarioMinimo());
        c.setSalarioMaximo(req.salarioMaximo());
        c.setAtivo(req.ativo() != null ? req.ativo() : true);

        if (req.departamentoId() != null) {
            Departamento dep = departamentoRepository.findById(req.departamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento nao encontrado: " + req.departamentoId()));
            c.setDepartamento(dep);
        } else {
            c.setDepartamento(null);
        }
    }

    private CargoResponse toResponse(Cargo c) {
        long ocupantes = cargoRepository.countFuncionariosByCargoId(c.getId());
        return CargoResponse.fromEntity(c, ocupantes);
    }
}
