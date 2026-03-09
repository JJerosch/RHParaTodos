package proj.paratodos.service;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.*;
import proj.paratodos.dto.SolicitacaoRequest;
import proj.paratodos.dto.SolicitacaoResponse;
import proj.paratodos.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ObjectMapper objectMapper;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              UsuarioRepository usuarioRepository,
                              FuncionarioRepository funcionarioRepository,
                              CargoRepository cargoRepository,
                              DepartamentoRepository departamentoRepository,
                              ObjectMapper objectMapper) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.objectMapper = objectMapper;
    }

    // ── Listagem ──────────────────────────────────────────────────

    public List<SolicitacaoResponse> findAll() {
        return solicitacaoRepository.findAllWithDetails().stream()
                .map(s -> SolicitacaoResponse.fromEntity(s, resolveDescricao(s)))
                .toList();
    }

    public List<SolicitacaoResponse> findByStatus(String status) {
        StatusSolicitacao st = StatusSolicitacao.valueOf(status);
        return solicitacaoRepository.findByStatusWithDetails(st).stream()
                .map(s -> SolicitacaoResponse.fromEntity(s, resolveDescricao(s)))
                .toList();
    }

    public List<SolicitacaoResponse> findByTipo(String tipo) {
        TipoSolicitacao tp = TipoSolicitacao.valueOf(tipo);
        return solicitacaoRepository.findByTipoWithDetails(tp).stream()
                .map(s -> SolicitacaoResponse.fromEntity(s, resolveDescricao(s)))
                .toList();
    }

    public List<SolicitacaoResponse> findByStatusAndTipo(String status, String tipo) {
        StatusSolicitacao st = StatusSolicitacao.valueOf(status);
        TipoSolicitacao tp = TipoSolicitacao.valueOf(tipo);
        return solicitacaoRepository.findByStatusAndTipoWithDetails(st, tp).stream()
                .map(s -> SolicitacaoResponse.fromEntity(s, resolveDescricao(s)))
                .toList();
    }

    public SolicitacaoResponse findById(Long id) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitação não encontrada: " + id);
        }
        return SolicitacaoResponse.fromEntity(s, resolveDescricao(s));
    }

    // ── Estatísticas ──────────────────────────────────────────────

    public SolicitacaoStatsResponse getStats() {
        long total = solicitacaoRepository.count();
        long pendentes = solicitacaoRepository.countByStatus(StatusSolicitacao.PENDENTE);
        long aprovadas = solicitacaoRepository.countByStatus(StatusSolicitacao.APROVADA);
        long rejeitadas = solicitacaoRepository.countByStatus(StatusSolicitacao.REJEITADA);
        long canceladas = solicitacaoRepository.countByStatus(StatusSolicitacao.CANCELADA);
        return new SolicitacaoStatsResponse(total, pendentes, aprovadas, rejeitadas, canceladas);
    }

    public record SolicitacaoStatsResponse(long total, long pendentes, long aprovadas, long rejeitadas, long canceladas) {}

    // ── Criação ───────────────────────────────────────────────────

    @Transactional
    public SolicitacaoResponse create(SolicitacaoRequest request, Long solicitanteId) {
        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado"));

        Solicitacao s = new Solicitacao();
        s.setTipo(TipoSolicitacao.valueOf(request.tipo()));
        s.setStatus(StatusSolicitacao.PENDENTE);
        s.setSolicitante(solicitante);
        s.setMotivo(request.motivo());

        if (request.referenciaTipo() != null && !request.referenciaTipo().isBlank()) {
            s.setReferenciaTipo(ReferenciaTipo.valueOf(request.referenciaTipo()));
        }
        s.setReferenciaId(request.referenciaId());
        s.setDadosAntes(request.dadosAntes());
        s.setDadosDepois(request.dadosDepois());

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s, resolveDescricao(s));
    }

    // ── Aprovação ─────────────────────────────────────────────────

    @Transactional
    public SolicitacaoResponse approve(Long id, Long aprovadorId, String observacao) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitação não encontrada: " + id);
        }
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Apenas solicitações pendentes podem ser aprovadas");
        }

        Usuario aprovador = usuarioRepository.findById(aprovadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário aprovador não encontrado"));

        s.setAprovador(aprovador);
        s.setStatus(StatusSolicitacao.APROVADA);
        s.setDecididoEm(LocalDateTime.now());
        s.setObservacao(observacao);

        // Executa a ação real conforme o tipo
        executarAcao(s);

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s, resolveDescricao(s));
    }

    // ── Rejeição ──────────────────────────────────────────────────

    @Transactional
    public SolicitacaoResponse reject(Long id, Long aprovadorId, String observacao) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitação não encontrada: " + id);
        }
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Apenas solicitações pendentes podem ser rejeitadas");
        }

        Usuario aprovador = usuarioRepository.findById(aprovadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário aprovador não encontrado"));

        s.setAprovador(aprovador);
        s.setStatus(StatusSolicitacao.REJEITADA);
        s.setDecididoEm(LocalDateTime.now());
        s.setObservacao(observacao);

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s, resolveDescricao(s));
    }

    // ── Cancelamento ──────────────────────────────────────────────

    @Transactional
    public SolicitacaoResponse cancel(Long id, Long solicitanteId) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitação não encontrada: " + id);
        }
        if (s.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Apenas solicitações pendentes podem ser canceladas");
        }
        if (!s.getSolicitante().getId().equals(solicitanteId)) {
            throw new IllegalArgumentException("Apenas o solicitante pode cancelar a solicitação");
        }

        s.setStatus(StatusSolicitacao.CANCELADA);
        s.setDecididoEm(LocalDateTime.now());

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s, resolveDescricao(s));
    }

    // ── Execução da ação ao aprovar ───────────────────────────────

    private void executarAcao(Solicitacao s) {
        switch (s.getTipo()) {
            case DESATIVACAO_FUNCIONARIO -> executarDesativacaoFuncionario(s);
            case ATIVACAO_FUNCIONARIO -> executarAtivacaoFuncionario(s);
            case ALTERACAO_FUNCIONARIO -> executarAlteracaoFuncionario(s);
            case DESATIVACAO_DEPARTAMENTO -> executarDesativacaoDepartamento(s);
            case DESATIVACAO_CARGO -> executarDesativacaoCargo(s);
            case CRIACAO_DEPARTAMENTO -> executarCriacaoDepartamento(s);
            case CRIACAO_CARGO -> executarCriacaoCargo(s);
            case EDICAO_DEPARTAMENTO -> executarEdicaoDepartamento(s);
            case EDICAO_CARGO -> executarEdicaoCargo(s);
            // Tipos que não possuem execução automática por enquanto
            case EXCLUSAO_FUNCIONARIO, EXCLUSAO_DEPARTAMENTO, EXCLUSAO_CARGO,
                 ABERTURA_VAGA, PROMOCAO, TRANSFERENCIA, REAJUSTE_SALARIAL -> {
                // Será implementado em fases futuras
            }
        }
    }

    private void executarDesativacaoFuncionario(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Funcionario f = funcionarioRepository.findById(s.getReferenciaId()).orElse(null);
        if (f != null) {
            f.setStatus("DESLIGADO");
            funcionarioRepository.save(f);
        }
    }

    private void executarAtivacaoFuncionario(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Funcionario f = funcionarioRepository.findById(s.getReferenciaId()).orElse(null);
        if (f != null) {
            f.setStatus("ATIVO");
            funcionarioRepository.save(f);
        }
    }

    private void executarAlteracaoFuncionario(Solicitacao s) {
        if (s.getReferenciaId() == null || s.getDadosDepois() == null) return;
        Funcionario f = funcionarioRepository.findById(s.getReferenciaId()).orElse(null);
        if (f == null) return;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);

            if (dados.containsKey("nomeCompleto")) f.setNomeCompleto((String) dados.get("nomeCompleto"));
            if (dados.containsKey("emailPessoal")) f.setEmailPessoal((String) dados.get("emailPessoal"));
            if (dados.containsKey("emailCorporativo")) f.setEmailCorporativo((String) dados.get("emailCorporativo"));
            if (dados.containsKey("telefone")) f.setTelefone((String) dados.get("telefone"));
            if (dados.containsKey("celular")) f.setCelular((String) dados.get("celular"));
            if (dados.containsKey("cep")) f.setCep((String) dados.get("cep"));
            if (dados.containsKey("logradouro")) f.setLogradouro((String) dados.get("logradouro"));
            if (dados.containsKey("numero")) f.setNumero((String) dados.get("numero"));
            if (dados.containsKey("complemento")) f.setComplemento((String) dados.get("complemento"));
            if (dados.containsKey("bairro")) f.setBairro((String) dados.get("bairro"));
            if (dados.containsKey("cidade")) f.setCidade((String) dados.get("cidade"));
            if (dados.containsKey("estado")) f.setEstado((String) dados.get("estado"));

            funcionarioRepository.save(f);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao aplicar alteração do funcionário: " + e.getMessage(), e);
        }
    }

    private void executarDesativacaoDepartamento(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Departamento d = departamentoRepository.findById(s.getReferenciaId()).orElse(null);
        if (d != null) {
            d.setAtivo(false);
            departamentoRepository.save(d);
        }
    }

    private void executarDesativacaoCargo(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Cargo c = cargoRepository.findById(s.getReferenciaId()).orElse(null);
        if (c != null) {
            c.setAtivo(false);
            cargoRepository.save(c);
        }
    }

    private void executarCriacaoDepartamento(Solicitacao s) {
        if (s.getDadosDepois() == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);
            Departamento d = new Departamento();
            d.setNome((String) dados.get("nome"));
            d.setDescricao((String) dados.get("descricao"));
            d.setAtivo(true);
            if (dados.containsKey("headcountLimite") && dados.get("headcountLimite") != null) {
                d.setHeadcountLimite(((Number) dados.get("headcountLimite")).intValue());
            }
            d = departamentoRepository.save(d);
            // Atualiza a referência com o id criado
            s.setReferenciaId(d.getId());
            s.setReferenciaTipo(ReferenciaTipo.DEPARTAMENTO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar departamento: " + e.getMessage(), e);
        }
    }

    private void executarCriacaoCargo(Solicitacao s) {
        if (s.getDadosDepois() == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);
            Cargo c = new Cargo();
            c.setTitulo((String) dados.get("titulo"));
            c.setDescricao((String) dados.get("descricao"));
            c.setNivel((String) dados.get("nivel"));
            c.setAtivo(true);
            if (dados.containsKey("departamentoId") && dados.get("departamentoId") != null) {
                Long depId = ((Number) dados.get("departamentoId")).longValue();
                departamentoRepository.findById(depId).ifPresent(c::setDepartamento);
            }
            if (dados.containsKey("salarioMinimo") && dados.get("salarioMinimo") != null) {
                c.setSalarioMinimo(new java.math.BigDecimal(dados.get("salarioMinimo").toString()));
            }
            if (dados.containsKey("salarioMaximo") && dados.get("salarioMaximo") != null) {
                c.setSalarioMaximo(new java.math.BigDecimal(dados.get("salarioMaximo").toString()));
            }
            c = cargoRepository.save(c);
            s.setReferenciaId(c.getId());
            s.setReferenciaTipo(ReferenciaTipo.CARGO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar cargo: " + e.getMessage(), e);
        }
    }

    private void executarEdicaoDepartamento(Solicitacao s) {
        if (s.getReferenciaId() == null || s.getDadosDepois() == null) return;
        Departamento d = departamentoRepository.findById(s.getReferenciaId()).orElse(null);
        if (d == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);
            if (dados.containsKey("nome")) d.setNome((String) dados.get("nome"));
            if (dados.containsKey("descricao")) d.setDescricao((String) dados.get("descricao"));
            if (dados.containsKey("headcountLimite") && dados.get("headcountLimite") != null) {
                d.setHeadcountLimite(((Number) dados.get("headcountLimite")).intValue());
            }
            departamentoRepository.save(d);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao editar departamento: " + e.getMessage(), e);
        }
    }

    private void executarEdicaoCargo(Solicitacao s) {
        if (s.getReferenciaId() == null || s.getDadosDepois() == null) return;
        Cargo c = cargoRepository.findById(s.getReferenciaId()).orElse(null);
        if (c == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);
            if (dados.containsKey("titulo")) c.setTitulo((String) dados.get("titulo"));
            if (dados.containsKey("descricao")) c.setDescricao((String) dados.get("descricao"));
            if (dados.containsKey("nivel")) c.setNivel((String) dados.get("nivel"));
            if (dados.containsKey("departamentoId") && dados.get("departamentoId") != null) {
                Long depId = ((Number) dados.get("departamentoId")).longValue();
                departamentoRepository.findById(depId).ifPresent(c::setDepartamento);
            }
            if (dados.containsKey("salarioMinimo") && dados.get("salarioMinimo") != null) {
                c.setSalarioMinimo(new java.math.BigDecimal(dados.get("salarioMinimo").toString()));
            }
            if (dados.containsKey("salarioMaximo") && dados.get("salarioMaximo") != null) {
                c.setSalarioMaximo(new java.math.BigDecimal(dados.get("salarioMaximo").toString()));
            }
            cargoRepository.save(c);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao editar cargo: " + e.getMessage(), e);
        }
    }

    // ── Resolução de descrição da referência ──────────────────────

    private String resolveDescricao(Solicitacao s) {
        if (s.getReferenciaTipo() == null || s.getReferenciaId() == null) return null;

        return switch (s.getReferenciaTipo()) {
            case FUNCIONARIO -> funcionarioRepository.findById(s.getReferenciaId())
                    .map(Funcionario::getNomeCompleto).orElse("Funcionário #" + s.getReferenciaId());
            case CARGO -> cargoRepository.findById(s.getReferenciaId())
                    .map(Cargo::getTitulo).orElse("Cargo #" + s.getReferenciaId());
            case DEPARTAMENTO -> departamentoRepository.findById(s.getReferenciaId())
                    .map(Departamento::getNome).orElse("Departamento #" + s.getReferenciaId());
            case VAGA -> "Vaga #" + s.getReferenciaId();
        };
    }
}
