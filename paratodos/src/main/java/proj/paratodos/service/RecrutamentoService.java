package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.*;
import proj.paratodos.dto.*;
import proj.paratodos.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecrutamentoService {

    private final VagaRepository vagaRepository;
    private final CandidatoRepository candidatoRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CargoRepository cargoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;

    public RecrutamentoService(VagaRepository vagaRepository,
                               CandidatoRepository candidatoRepository,
                               CandidaturaRepository candidaturaRepository,
                               DepartamentoRepository departamentoRepository,
                               CargoRepository cargoRepository,
                               UsuarioRepository usuarioRepository,
                               FuncionarioRepository funcionarioRepository) {
        this.vagaRepository = vagaRepository;
        this.candidatoRepository = candidatoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    // ── Vagas ──

    public List<VagaResponse> findAllVagas() {
        return vagaRepository.findAllWithDetails().stream()
                .map(VagaResponse::fromEntity)
                .toList();
    }

    public List<VagaResponse> findVagasByStatus(String status) {
        return vagaRepository.findByStatusWithDetails(status).stream()
                .map(VagaResponse::fromEntity)
                .toList();
    }

    public VagaResponse findVagaById(Long id) {
        Vaga v = vagaRepository.findByIdWithDetails(id);
        if (v == null) {
            throw new IllegalArgumentException("Vaga nao encontrada: " + id);
        }
        return VagaResponse.fromEntity(v);
    }

    @Transactional
    public VagaResponse createVaga(VagaRequest request, Long criadoPorId) {
        // Verifica headcount do departamento
        if (request.departamentoId() != null) {
            Departamento dept = departamentoRepository.findById(request.departamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento nao encontrado"));

            if (dept.getHeadcountLimite() != null && dept.getHeadcountLimite() > 0) {
                long funcionariosAtuais = departamentoRepository.countFuncionariosByDepartamentoId(dept.getId());
                long vagasAbertas = vagaRepository.countActiveByDepartamentoId(dept.getId());
                if (funcionariosAtuais + vagasAbertas >= dept.getHeadcountLimite()) {
                    throw new IllegalArgumentException("Limite de headcount atingido para o departamento " + dept.getNome());
                }
            }
        }

        Vaga v = new Vaga();
        v.setTitulo(request.titulo());
        v.setDescricao(request.descricao());
        v.setQuantidade(request.quantidade() != null ? request.quantidade() : 1);
        v.setPrioridade(request.prioridade() != null ? request.prioridade() : "MEDIA");
        v.setSalarioMin(request.salarioMin());
        v.setSalarioMax(request.salarioMax());
        v.setTipoContrato(request.tipoContrato());
        v.setLocalTrabalho(request.localTrabalho());
        v.setModeloTrabalho(request.modeloTrabalho() != null ? request.modeloTrabalho() : "PRESENCIAL");
        v.setRequisitos(request.requisitos());
        v.setStatus(request.status() != null ? request.status() : "RASCUNHO");

        if (request.departamentoId() != null) {
            v.setDepartamento(departamentoRepository.findById(request.departamentoId()).orElse(null));
        }
        if (request.cargoId() != null) {
            v.setCargo(cargoRepository.findById(request.cargoId()).orElse(null));
        }

        Usuario criador = usuarioRepository.findById(criadoPorId).orElse(null);
        v.setCriadoPor(criador);

        v = vagaRepository.save(v);
        return VagaResponse.fromEntity(v);
    }

    @Transactional
    public VagaResponse updateVaga(Long id, VagaRequest request) {
        Vaga v = vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga nao encontrada: " + id));

        v.setTitulo(request.titulo());
        v.setDescricao(request.descricao());
        if (request.quantidade() != null) v.setQuantidade(request.quantidade());
        if (request.prioridade() != null) v.setPrioridade(request.prioridade());
        v.setSalarioMin(request.salarioMin());
        v.setSalarioMax(request.salarioMax());
        v.setTipoContrato(request.tipoContrato());
        v.setLocalTrabalho(request.localTrabalho());
        if (request.modeloTrabalho() != null) v.setModeloTrabalho(request.modeloTrabalho());
        v.setRequisitos(request.requisitos());

        if (request.status() != null) {
            String oldStatus = v.getStatus();
            v.setStatus(request.status());
            if ("ABERTA".equals(request.status()) && !"ABERTA".equals(oldStatus)) {
                v.setPublicadaEm(LocalDateTime.now());
            }
            if ("ENCERRADA".equals(request.status()) || "CANCELADA".equals(request.status())) {
                v.setEncerradaEm(LocalDateTime.now());
            }
        }

        if (request.departamentoId() != null) {
            v.setDepartamento(departamentoRepository.findById(request.departamentoId()).orElse(null));
        }
        if (request.cargoId() != null) {
            v.setCargo(cargoRepository.findById(request.cargoId()).orElse(null));
        }

        v = vagaRepository.save(v);
        return VagaResponse.fromEntity(v);
    }

    @Transactional
    public VagaResponse approveVaga(Long id) {
        Vaga v = vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga nao encontrada: " + id));

        if (!"RASCUNHO".equals(v.getStatus())) {
            throw new IllegalArgumentException("Apenas vagas em rascunho podem ser aprovadas");
        }

        v.setStatus("ABERTA");
        v.setPublicadaEm(LocalDateTime.now());
        v = vagaRepository.save(v);
        return VagaResponse.fromEntity(v);
    }

    // ── Candidatos ──

    @Transactional
    public CandidatoResponse createCandidato(CandidatoRequest request) {
        Candidato c = new Candidato();
        c.setNomeCompleto(request.nomeCompleto());
        c.setEmail(request.email());
        c.setTelefone(request.telefone());
        c.setLinkedinUrl(request.linkedinUrl());
        c.setCurriculoUrl(request.curriculoUrl());
        c.setCpf(request.cpf());
        c.setDataNascimento(request.dataNascimento());
        c.setCidade(request.cidade());
        c.setEstado(request.estado());
        c.setPretensaoSalarial(request.pretensaoSalarial());
        c.setObservacoes(request.observacoes());

        c = candidatoRepository.save(c);
        return CandidatoResponse.fromEntity(c);
    }

    public List<CandidatoResponse> findAllCandidatos() {
        return candidatoRepository.findAll().stream()
                .map(CandidatoResponse::fromEntity)
                .toList();
    }

    // ── Candidaturas ──

    @Transactional
    public CandidaturaResponse createCandidatura(Long vagaId, Long candidatoId) {
        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new IllegalArgumentException("Vaga nao encontrada: " + vagaId));
        Candidato candidato = candidatoRepository.findById(candidatoId)
                .orElseThrow(() -> new IllegalArgumentException("Candidato nao encontrado: " + candidatoId));

        if (candidaturaRepository.existsByVagaIdAndCandidatoId(vagaId, candidatoId)) {
            throw new IllegalArgumentException("Candidato ja inscrito nesta vaga");
        }

        Candidatura c = new Candidatura();
        c.setVaga(vaga);
        c.setCandidato(candidato);
        c.setEtapa("TRIAGEM");

        c = candidaturaRepository.save(c);
        return CandidaturaResponse.fromEntity(c);
    }

    public List<CandidaturaResponse> findCandidaturasByVaga(Long vagaId) {
        return candidaturaRepository.findByVagaIdWithDetails(vagaId).stream()
                .map(CandidaturaResponse::fromEntity)
                .toList();
    }

    @Transactional
    public CandidaturaResponse updateEtapa(Long candidaturaId, String novaEtapa, String observacoes, String motivoRejeicao) {
        Candidatura c = candidaturaRepository.findByIdWithDetails(candidaturaId);
        if (c == null) {
            throw new IllegalArgumentException("Candidatura nao encontrada: " + candidaturaId);
        }

        c.setEtapa(novaEtapa);
        if (observacoes != null) c.setObservacoes(observacoes);
        if (motivoRejeicao != null) c.setMotivoRejeicao(motivoRejeicao);

        // Se contratado, cria funcionario sem departamento
        if ("CONTRATADO".equals(novaEtapa)) {
            createFuncionarioFromCandidato(c);
        }

        c = candidaturaRepository.save(c);
        return CandidaturaResponse.fromEntity(c);
    }

    private void createFuncionarioFromCandidato(Candidatura candidatura) {
        Candidato cand = candidatura.getCandidato();

        // Gera matricula unica
        String matricula = "NEW-" + System.currentTimeMillis() % 100000;

        Funcionario f = new Funcionario();
        f.setNomeCompleto(cand.getNomeCompleto());
        f.setEmailPessoal(cand.getEmail());
        f.setTelefone(cand.getTelefone());
        f.setCpf(cand.getCpf() != null ? cand.getCpf() : "");
        f.setMatricula(matricula);
        f.setDataNascimento(cand.getDataNascimento() != null ? cand.getDataNascimento() : LocalDate.of(2000, 1, 1));
        f.setDataAdmissao(LocalDate.now());
        f.setStatus("ATIVO");
        f.setTipoContrato(candidatura.getVaga().getTipoContrato());
        f.setSalarioAtual(cand.getPretensaoSalarial());
        // Sem departamento e sem cargo - precisa de promocao para atribuir
        f.setDepartamento(null);
        f.setCargo(null);

        funcionarioRepository.save(f);
    }

    public RecrutamentoStatsResponse getStats() {
        long totalVagas = vagaRepository.count();
        long vagasAbertas = vagaRepository.countByStatus("ABERTA") + vagaRepository.countByStatus("EM_ANDAMENTO");
        long totalCandidatos = candidatoRepository.count();
        long contratados = candidaturaRepository.countByEtapa("CONTRATADO");
        return new RecrutamentoStatsResponse(totalVagas, vagasAbertas, totalCandidatos, contratados);
    }

    public record RecrutamentoStatsResponse(long totalVagas, long vagasAbertas, long totalCandidatos, long contratados) {}
}
