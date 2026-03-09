package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.domain.PontoOcorrencia;
import proj.paratodos.dto.PontoOcorrenciaRequest;
import proj.paratodos.dto.PontoOcorrenciaResponse;
import proj.paratodos.repository.FuncionarioRepository;
import proj.paratodos.repository.PontoOcorrenciaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PontoOcorrenciaService {

    private final PontoOcorrenciaRepository repository;
    private final FuncionarioRepository funcionarioRepository;

    public PontoOcorrenciaService(
            PontoOcorrenciaRepository repository,
            FuncionarioRepository funcionarioRepository
    ) {
        this.repository = repository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public Optional<PontoOcorrencia> buscarOcorrenciaAtiva(Long funcionarioId, LocalDate data) {
        return repository.findPrimeiraAtivaNoDia(funcionarioId, data);
    }

    @Transactional(readOnly = true)
    public String obterTipoOcorrencia(Long funcionarioId, LocalDate data) {
        return buscarOcorrenciaAtiva(funcionarioId, data)
                .map(PontoOcorrencia::getTipo)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean abonaDia(Long funcionarioId, LocalDate data) {
        return buscarOcorrenciaAtiva(funcionarioId, data)
                .map(o -> Boolean.TRUE.equals(o.getAbonaDia()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean bloqueiaMarcacao(Long funcionarioId, LocalDate data) {
        return buscarOcorrenciaAtiva(funcionarioId, data)
                .map(o -> Boolean.TRUE.equals(o.getBloqueiaMarcacao()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public String mensagemBloqueio(Long funcionarioId, LocalDate data) {
        return buscarOcorrenciaAtiva(funcionarioId, data)
                .map(o -> switch (o.getTipo()) {
                    case "FERIAS" -> "Funcionário está em férias neste período.";
                    case "ATESTADO" -> "Funcionário está com atestado neste período.";
                    case "LICENCA" -> "Funcionário está em licença neste período.";
                    case "FALTA_JUSTIFICADA" -> "Há uma falta justificada cadastrada para este dia.";
                    default -> "Registro de ponto indisponível para este período.";
                })
                .orElse("Registro de ponto indisponível para este período.");
    }

    @Transactional
    public PontoOcorrenciaResponse criar(PontoOcorrenciaRequest request) {
        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        Funcionario funcionario = funcionarioRepository.findById(request.funcionarioId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));

        PontoOcorrencia o = new PontoOcorrencia();
        o.setFuncionario(funcionario);
        o.setDataInicio(request.dataInicio());
        o.setDataFim(request.dataFim());
        o.setTipo(normalizarTipo(request.tipo()));
        o.setObservacao(request.observacao());
        o.setAbonaDia(request.abonaDia() != null ? request.abonaDia() : Boolean.TRUE);
        o.setBloqueiaMarcacao(request.bloqueiaMarcacao() != null ? request.bloqueiaMarcacao() : Boolean.TRUE);

        return PontoOcorrenciaResponse.fromEntity(repository.save(o));
    }

    @Transactional(readOnly = true)
    public List<PontoOcorrenciaResponse> listar(Long funcionarioId, LocalDate inicio, LocalDate fim) {
        return repository.listarPorPeriodo(funcionarioId, inicio, fim)
                .stream()
                .map(PontoOcorrenciaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Ocorrência não encontrada.");
        }
        repository.deleteById(id);
    }

    private String normalizarTipo(String tipo) {
        String valor = tipo == null ? "" : tipo.trim().toUpperCase();
        return switch (valor) {
            case "FERIAS", "ATESTADO", "LICENCA", "FALTA_JUSTIFICADA" -> valor;
            default -> throw new IllegalArgumentException(
                    "Tipo inválido. Use FERIAS, ATESTADO, LICENCA ou FALTA_JUSTIFICADA."
            );
        };
    }
}