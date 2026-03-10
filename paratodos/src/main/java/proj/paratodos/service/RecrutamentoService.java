package proj.paratodos.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.*;
import proj.paratodos.dto.*;
import proj.paratodos.repository.*;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
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
    private final PromocaoRepository promocaoRepository;
    private final TipoBeneficioRepository tipoBeneficioRepository;
    private final PasswordEncoder passwordEncoder;

    public RecrutamentoService(VagaRepository vagaRepository,
                               CandidatoRepository candidatoRepository,
                               CandidaturaRepository candidaturaRepository,
                               DepartamentoRepository departamentoRepository,
                               CargoRepository cargoRepository,
                               UsuarioRepository usuarioRepository,
                               FuncionarioRepository funcionarioRepository,
                               PromocaoRepository promocaoRepository,
                               TipoBeneficioRepository tipoBeneficioRepository,
                               PasswordEncoder passwordEncoder) {
        this.vagaRepository = vagaRepository;
        this.candidatoRepository = candidatoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.departamentoRepository = departamentoRepository;
        this.cargoRepository = cargoRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.promocaoRepository = promocaoRepository;
        this.tipoBeneficioRepository = tipoBeneficioRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (request.beneficioIds() != null && !request.beneficioIds().isEmpty()) {
            v.setBeneficios(new HashSet<>(tipoBeneficioRepository.findAllById(request.beneficioIds())));
        }

        v = vagaRepository.save(v);
        return VagaResponse.fromEntity(v);
    }

    @Transactional
    public VagaResponse updateVaga(Long id, VagaRequest request) {
        Vaga v = vagaRepository.findByIdWithDetails(id);
        if (v == null) {
            throw new IllegalArgumentException("Vaga nao encontrada: " + id);
        }

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

        if (request.beneficioIds() != null) {
            v.setBeneficios(new HashSet<>(tipoBeneficioRepository.findAllById(request.beneficioIds())));
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

    @Transactional
    public VagaResponse cancelVaga(Long id) {
        Vaga v = vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga nao encontrada: " + id));

        if ("ENCERRADA".equals(v.getStatus())) {
            throw new IllegalArgumentException("Vaga já encerrada não pode ser cancelada");
        }

        v.setStatus("CANCELADA");
        v.setEncerradaEm(LocalDateTime.now());
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
    public CandidaturaResponse updateEtapa(Long candidaturaId, String novaEtapa, String observacoes, String motivoRejeicao, Long userId) {
        Candidatura c = candidaturaRepository.findByIdWithDetails(candidaturaId);
        if (c == null) {
            throw new IllegalArgumentException("Candidatura nao encontrada: " + candidaturaId);
        }

        // Impedir movimentação a partir de CONTRATADO
        if ("CONTRATADO".equals(c.getEtapa())) {
            throw new IllegalArgumentException("Candidato já foi contratado e não pode ser movido");
        }

        c.setEtapa(novaEtapa);
        if (observacoes != null) c.setObservacoes(observacoes);
        if (motivoRejeicao != null) c.setMotivoRejeicao(motivoRejeicao);

        // Salva a candidatura primeiro para garantir que o status é persistido
        c = candidaturaRepository.save(c);
        candidaturaRepository.flush();

        // Se contratado, cria funcionario e fecha vaga se necessário
        if ("CONTRATADO".equals(novaEtapa)) {
            createFuncionarioFromCandidato(c, userId);

            // Recarrega a vaga com detalhes para garantir cargo e departamento carregados
            Vaga vaga = vagaRepository.findByIdWithDetails(c.getVaga().getId());
            long contratados = candidaturaRepository.countContratadosByVagaId(vaga.getId());
            if (contratados >= vaga.getQuantidade()) {
                vaga.setStatus("ENCERRADA");
                vaga.setEncerradaEm(LocalDateTime.now());
                vagaRepository.save(vaga);
            }
        }

        return CandidaturaResponse.fromEntity(c);
    }

    private void createFuncionarioFromCandidato(Candidatura candidatura, Long userId) {
        Candidato cand = candidatura.getCandidato();
        // Recarrega a vaga com JOIN FETCH para garantir que cargo e departamento estão carregados
        Vaga vaga = vagaRepository.findByIdWithDetails(candidatura.getVaga().getId());

        String cpf = cand.getCpf();
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF do candidato é obrigatório para contratação");
        }

        // Verifica se já existe funcionário com este CPF
        if (funcionarioRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("Já existe um funcionário cadastrado com o CPF " + cpf);
        }

        // Gera matricula unica
        String matricula = "NEW-" + System.currentTimeMillis() % 100000;

        // Gera email corporativo: primeiro_nome.ultimo_nome@gruporp.com
        String emailCorp = gerarEmailCorporativo(cand.getNomeCompleto());

        // Senha automática: 6 primeiros dígitos do CPF + @Gruporp
        String cpfDigits = cpf.replaceAll("\\D", "");
        String senhaAutomatica = cpfDigits.substring(0, Math.min(6, cpfDigits.length())) + "@Gruporp";

        // Cria o Usuario com credenciais automáticas
        Usuario usuario = new Usuario();
        usuario.setEmail(emailCorp);
        usuario.setSenhaHash(passwordEncoder.encode(senhaAutomatica));
        usuario.setAtivo(true);
        usuario.setAutenticacao2fa(false);
        usuario.setTentativasLogin(0);
        usuario.setRole("EMPLOYEE");
        usuario = usuarioRepository.save(usuario);

        Funcionario f = new Funcionario();
        f.setNomeCompleto(cand.getNomeCompleto());
        f.setEmailPessoal(cand.getEmail());
        f.setEmailCorporativo(emailCorp);
        f.setTelefone(cand.getTelefone());
        f.setCpf(cpf);
        f.setMatricula(matricula);
        f.setDataNascimento(cand.getDataNascimento() != null ? cand.getDataNascimento() : LocalDate.of(2000, 1, 1));
        f.setDataAdmissao(LocalDate.now());
        f.setStatus("ATIVO");
        f.setTipoContrato(vaga.getTipoContrato() != null ? vaga.getTipoContrato() : "CLT");
        f.setSalarioAtual(cand.getPretensaoSalarial());
        f.setCidade(cand.getCidade());
        f.setEstado(cand.getEstado());
        f.setUsuario(usuario);

        // Atribui cargo e departamento da vaga diretamente
        if (vaga.getDepartamento() != null) {
            f.setDepartamento(vaga.getDepartamento());
        }
        if (vaga.getCargo() != null) {
            f.setCargo(vaga.getCargo());
            f.setCargoDesde(LocalDate.now());
        }

        funcionarioRepository.save(f);
    }

    private String gerarEmailCorporativo(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isBlank()) return "novo.usuario@gruporp.com";

        // Remove acentos
        String normalized = Normalizer.normalize(nomeCompleto.trim(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase();

        String[] partes = normalized.split("\\s+");
        String primeiro = partes[0];
        String ultimo = partes.length > 1 ? partes[partes.length - 1] : primeiro;

        String baseEmail = primeiro + "." + ultimo + "@gruporp.com";

        // Verifica unicidade, adiciona sufixo se necessário
        String email = baseEmail;
        int suffix = 1;
        while (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            email = primeiro + "." + ultimo + suffix + "@gruporp.com";
            suffix++;
        }
        return email;
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
