package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.FeriasLog;
import proj.paratodos.domain.FeriasPeriodoAquisitivo;
import proj.paratodos.domain.FeriasSolicitacao;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.domain.PontoOcorrencia;
import proj.paratodos.domain.Usuario;
import proj.paratodos.dto.FeriasFuncionarioRowResponse;
import proj.paratodos.dto.FeriasPageResponse;
import proj.paratodos.dto.FeriasRequest;
import proj.paratodos.dto.FeriasResumoResponse;
import proj.paratodos.repository.FeriasLogRepository;
import proj.paratodos.repository.FeriasPeriodoAquisitivoRepository;
import proj.paratodos.repository.FeriasSolicitacaoRepository;
import proj.paratodos.repository.FuncionarioRepository;
import proj.paratodos.repository.PontoOcorrenciaRepository;
import proj.paratodos.repository.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeriasService {

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final FeriasSolicitacaoRepository solicitacaoRepository;
    private final FeriasLogRepository logRepository;
    private final PontoOcorrenciaRepository pontoOcorrenciaRepository;
    private final FeriasPeriodoAquisitivoRepository feriasPeriodoAquisitivoRepository;

    public FeriasService(
            FuncionarioRepository funcionarioRepository,
            UsuarioRepository usuarioRepository,
            FeriasSolicitacaoRepository solicitacaoRepository,
            FeriasLogRepository logRepository,
            PontoOcorrenciaRepository pontoOcorrenciaRepository,
            FeriasPeriodoAquisitivoRepository feriasPeriodoAquisitivoRepository
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.logRepository = logRepository;
        this.pontoOcorrenciaRepository = pontoOcorrenciaRepository;
        this.feriasPeriodoAquisitivoRepository = feriasPeriodoAquisitivoRepository;
    }

    @Transactional(readOnly = true)
    public FeriasPageResponse listarFuncionarios() {
        LocalDate hoje = LocalDate.now();

        List<Funcionario> funcionarios = funcionarioRepository.findAtivosParaFerias();
        List<PontoOcorrencia> feriasAtivas = pontoOcorrenciaRepository.findFeriasAtivasNaData(hoje);

        Map<Long, PontoOcorrencia> mapaAtivos = new HashMap<>();
        for (PontoOcorrencia ocorrencia : feriasAtivas) {
            mapaAtivos.put(ocorrencia.getFuncionario().getId(), ocorrencia);
        }

        List<FeriasFuncionarioRowResponse> rows = funcionarios.stream()
                .map(f -> {
                    PontoOcorrencia ativa = mapaAtivos.get(f.getId());
                    boolean emFerias = ativa != null;

                    return new FeriasFuncionarioRowResponse(
                            f.getId(),
                            f.getMatricula(),
                            f.getNomeCompleto(),
                            f.getCargo() != null ? f.getCargo().getTitulo() : "-",
                            f.getDepartamento() != null ? f.getDepartamento().getNome() : "-",
                            emFerias ? "EM_FERIAS" : "DISPONIVEL",
                            emFerias ? ativa.getDataInicio().toString() : null,
                            emFerias ? ativa.getDataFim().toString() : null
                    );
                })
                .toList();

        long total = rows.size();
        long emFerias = rows.stream().filter(r -> "EM_FERIAS".equals(r.statusFerias())).count();

        return new FeriasPageResponse(
                new FeriasResumoResponse(total, emFerias, total - emFerias),
                rows
        );
    }

    @Transactional
    public void solicitarFeriasAtivas(Long usuarioId, FeriasRequest request) {
        LocalDate hoje = LocalDate.now();

        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        if (request.dataInicio().isAfter(hoje) || request.dataFim().isBefore(hoje)) {
            throw new IllegalArgumentException("Nesta versão simples, as férias precisam estar ativas hoje.");
        }

        Funcionario funcionario = funcionarioRepository.findById(request.funcionarioId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado."));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado."));

        if (!"ATIVO".equalsIgnoreCase(funcionario.getStatus())) {
            throw new IllegalArgumentException("Só é possível solicitar férias para funcionários ativos.");
        }

        boolean jaEmFeriasHoje = pontoOcorrenciaRepository.findPrimeiraAtivaNoDia(funcionario.getId(), hoje)
                .filter(o -> "FERIAS".equalsIgnoreCase(o.getTipo()))
                .isPresent();

        if (jaEmFeriasHoje) {
            throw new IllegalArgumentException("Este funcionário já está em férias hoje.");
        }

        boolean jaExisteSobreposicao = !pontoOcorrenciaRepository.findSobrepostas(
                funcionario.getId(),
                "FERIAS",
                request.dataInicio(),
                request.dataFim()
        ).isEmpty();

        if (jaExisteSobreposicao) {
            throw new IllegalArgumentException("Já existe um período de férias sobrepondo este intervalo.");
        }

        int dias = (int) ChronoUnit.DAYS.between(request.dataInicio(), request.dataFim()) + 1;

        Long periodoAquisitivoId = buscarOuCriarPeriodoAquisitivo(funcionario);

        FeriasSolicitacao solicitacao = new FeriasSolicitacao();
        solicitacao.setFuncionario(funcionario);
        solicitacao.setPeriodoAquisitivoId(periodoAquisitivoId);
        solicitacao.setDataInicio(request.dataInicio());
        solicitacao.setDataFim(request.dataFim());
        solicitacao.setDiasSolicitados(dias);
        solicitacao.setStatus("APROVADA");
        solicitacao.setAprovadoPor(usuario);
        solicitacao.setAprovadoEm(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);

        PontoOcorrencia ocorrencia = new PontoOcorrencia();
        ocorrencia.setFuncionario(funcionario);
        ocorrencia.setDataInicio(request.dataInicio());
        ocorrencia.setDataFim(request.dataFim());
        ocorrencia.setTipo("FERIAS");
        ocorrencia.setObservacao(request.observacao());
        ocorrencia.setAbonaDia(true);
        ocorrencia.setBloqueiaMarcacao(true);
        pontoOcorrenciaRepository.save(ocorrencia);

        FeriasLog log = new FeriasLog();
        log.setFuncionarioId(funcionario.getId());
        log.setUsuarioId(usuario.getId());
        log.setAcao("CRIACAO_FERIAS_ATIVAS");
        log.setDetalhe(
                "Férias criadas de " + request.dataInicio()
                        + " até " + request.dataFim()
                        + (request.observacao() != null && !request.observacao().isBlank()
                        ? " | Obs: " + request.observacao().trim()
                        : "")
        );
        logRepository.save(log);
    }

    private Long buscarOuCriarPeriodoAquisitivo(Funcionario funcionario) {
        return feriasPeriodoAquisitivoRepository
                .findTopByFuncionarioIdOrderByDataFimDesc(funcionario.getId())
                .map(FeriasPeriodoAquisitivo::getId)
                .orElseGet(() -> {
                    LocalDate admissao = funcionario.getDataAdmissao() != null
                            ? funcionario.getDataAdmissao()
                            : LocalDate.now().minusYears(1);

                    FeriasPeriodoAquisitivo periodo = new FeriasPeriodoAquisitivo();
                    periodo.setFuncionarioId(funcionario.getId());
                    periodo.setDataInicio(admissao);
                    periodo.setDataFim(admissao.plusYears(1).minusDays(1));
                    periodo.setDiasDireito(30);

                    return feriasPeriodoAquisitivoRepository.save(periodo).getId();
                });
    }
}