package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.*;
import proj.paratodos.dto.PromocaoRequest;
import proj.paratodos.dto.PromocaoResponse;
import proj.paratodos.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromocaoService {

    private final PromocaoRepository promocaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public PromocaoService(PromocaoRepository promocaoRepository,
                           FuncionarioRepository funcionarioRepository,
                           CargoRepository cargoRepository,
                           DepartamentoRepository departamentoRepository,
                           UsuarioRepository usuarioRepository) {
        this.promocaoRepository = promocaoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<PromocaoResponse> findAll() {
        return promocaoRepository.findAllWithDetails().stream()
                .map(PromocaoResponse::fromEntity)
                .toList();
    }

    public List<PromocaoResponse> findByStatus(String status) {
        return promocaoRepository.findByStatusWithDetails(status).stream()
                .map(PromocaoResponse::fromEntity)
                .toList();
    }

    public PromocaoResponse findById(Long id) {
        Promocao p = promocaoRepository.findByIdWithDetails(id);
        if (p == null) {
            throw new IllegalArgumentException("Promocao nao encontrada: " + id);
        }
        return PromocaoResponse.fromEntity(p);
    }

    @Transactional
    public PromocaoResponse create(PromocaoRequest request, Long solicitanteId) {
        Funcionario funcionario = funcionarioRepository.findById(request.funcionarioId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionario nao encontrado: " + request.funcionarioId()));

        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante nao encontrado"));

        Promocao p = new Promocao();
        p.setFuncionario(funcionario);
        p.setSolicitante(solicitante);
        p.setMotivo(request.motivo());
        p.setTipo(request.tipo() != null ? request.tipo() : "PROMOCAO");

        // Preenche dados atuais do funcionario
        p.setCargoAtual(funcionario.getCargo());
        p.setDepartamentoAtual(funcionario.getDepartamento());
        p.setSalarioAtual(funcionario.getSalarioAtual());

        // Preenche dados novos
        if (request.cargoNovoId() != null) {
            Cargo cargoNovo = cargoRepository.findById(request.cargoNovoId())
                    .orElseThrow(() -> new IllegalArgumentException("Cargo nao encontrado: " + request.cargoNovoId()));
            p.setCargoNovo(cargoNovo);
        }

        if (request.departamentoNovoId() != null) {
            Departamento depNovo = departamentoRepository.findById(request.departamentoNovoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento nao encontrado: " + request.departamentoNovoId()));
            p.setDepartamentoNovo(depNovo);
        }

        p.setSalarioNovo(request.salarioNovo());
        p.setStatus("PENDENTE");

        p = promocaoRepository.save(p);
        return PromocaoResponse.fromEntity(p);
    }

    @Transactional
    public PromocaoResponse approve(Long id, Long aprovadorId, String observacao) {
        Promocao p = promocaoRepository.findByIdWithDetails(id);
        if (p == null) {
            throw new IllegalArgumentException("Promocao nao encontrada: " + id);
        }

        if (!"PENDENTE".equals(p.getStatus())) {
            throw new IllegalArgumentException("Apenas promocoes pendentes podem ser aprovadas");
        }

        Usuario aprovador = usuarioRepository.findById(aprovadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario aprovador nao encontrado"));

        p.setAprovador(aprovador);
        p.setStatus("APROVADA");
        p.setDataDecisao(LocalDateTime.now());
        p.setObservacaoAprovador(observacao);

        // Aplica mudancas no funcionario
        Funcionario f = p.getFuncionario();

        if (p.getCargoNovo() != null) {
            f.setCargo(p.getCargoNovo());
            f.setCargoDesde(LocalDate.now());
        }

        if (p.getDepartamentoNovo() != null) {
            f.setDepartamento(p.getDepartamentoNovo());
        }

        if (p.getSalarioNovo() != null) {
            f.setSalarioAtual(p.getSalarioNovo());
        }

        funcionarioRepository.save(f);
        p = promocaoRepository.save(p);
        return PromocaoResponse.fromEntity(p);
    }

    @Transactional
    public PromocaoResponse reject(Long id, Long aprovadorId, String observacao) {
        Promocao p = promocaoRepository.findByIdWithDetails(id);
        if (p == null) {
            throw new IllegalArgumentException("Promocao nao encontrada: " + id);
        }

        if (!"PENDENTE".equals(p.getStatus())) {
            throw new IllegalArgumentException("Apenas promocoes pendentes podem ser rejeitadas");
        }

        Usuario aprovador = usuarioRepository.findById(aprovadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario aprovador nao encontrado"));

        p.setAprovador(aprovador);
        p.setStatus("REJEITADA");
        p.setDataDecisao(LocalDateTime.now());
        p.setObservacaoAprovador(observacao);

        p = promocaoRepository.save(p);
        return PromocaoResponse.fromEntity(p);
    }

    public PromocaoStatsResponse getStats() {
        long total = promocaoRepository.count();
        long pendentes = promocaoRepository.countByStatus("PENDENTE");
        long aprovadas = promocaoRepository.countByStatus("APROVADA");
        long rejeitadas = promocaoRepository.countByStatus("REJEITADA");
        return new PromocaoStatsResponse(total, pendentes, aprovadas, rejeitadas);
    }

    public record PromocaoStatsResponse(long total, long pendentes, long aprovadas, long rejeitadas) {}
}
