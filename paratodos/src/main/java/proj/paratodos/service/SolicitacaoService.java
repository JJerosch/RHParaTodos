package proj.paratodos.service;

import jakarta.persistence.EntityManager;
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
    private final VagaRepository vagaRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              UsuarioRepository usuarioRepository,
                              FuncionarioRepository funcionarioRepository,
                              CargoRepository cargoRepository,
                              DepartamentoRepository departamentoRepository,
                              VagaRepository vagaRepository,
                              ObjectMapper objectMapper,
                              EntityManager entityManager) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.vagaRepository = vagaRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
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
            case PROMOCAO -> executarPromocao(s);
            case EXCLUSAO_CARGO -> executarExclusaoCargo(s);
            case EXCLUSAO_FUNCIONARIO -> executarExclusaoFuncionario(s);
            case EXCLUSAO_DEPARTAMENTO -> executarExclusaoDepartamento(s);
            case ABERTURA_VAGA -> executarAberturaVaga(s);
            // Tipos que não possuem execução automática por enquanto
            case TRANSFERENCIA, REAJUSTE_SALARIAL -> {
            }
        }
    }

    private void executarDesativacaoFuncionario(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Funcionario f = funcionarioRepository.findById(s.getReferenciaId()).orElse(null);
        if (f != null) {
            f.setStatus("INATIVO");
            f.setDataDesligamento(java.time.LocalDate.now());
            // Remove o vínculo com o cargo (torna o cargo "Vago")
            f.setCargo(null);
            f.setCargoDesde(null);
            f.setDepartamento(null);
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

    private void executarPromocao(Solicitacao s) {
        if (s.getDadosDepois() == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);

            Long funcionarioId = s.getReferenciaId();
            if (funcionarioId == null && dados.containsKey("funcionarioId") && dados.get("funcionarioId") != null) {
                funcionarioId = ((Number) dados.get("funcionarioId")).longValue();
            }
            if (funcionarioId == null) return;

            Funcionario f = funcionarioRepository.findById(funcionarioId).orElse(null);
            if (f == null) return;

            // Move o funcionário para o novo cargo/departamento
            if (dados.containsKey("cargoNovoId") && dados.get("cargoNovoId") != null) {
                Long novoCargoId = ((Number) dados.get("cargoNovoId")).longValue();
                Cargo novoCargo = cargoRepository.findById(novoCargoId).orElse(null);
                if (novoCargo != null) {
                    f.setCargo(novoCargo);
                    f.setCargoDesde(java.time.LocalDate.now());
                }
            }

            if (dados.containsKey("departamentoNovoId") && dados.get("departamentoNovoId") != null) {
                Long novoDeptId = ((Number) dados.get("departamentoNovoId")).longValue();
                Departamento novoDept = departamentoRepository.findById(novoDeptId).orElse(null);
                if (novoDept != null) {
                    f.setDepartamento(novoDept);
                }
            }

            if (dados.containsKey("salarioNovo") && dados.get("salarioNovo") != null) {
                f.setSalarioAtual(new java.math.BigDecimal(dados.get("salarioNovo").toString()));
            }

            funcionarioRepository.save(f);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar promoção: " + e.getMessage(), e);
        }
    }

    private void executarExclusaoCargo(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Cargo c = cargoRepository.findById(s.getReferenciaId()).orElse(null);
        if (c == null) return;

        // Impedir exclusão de cargos com funcionários ativos
        long funcionariosAtivos = cargoRepository.countFuncionariosByCargoId(c.getId());
        if (funcionariosAtivos > 0) {
            throw new IllegalArgumentException(
                "Não é possível excluir o cargo '" + c.getTitulo() +
                "'. Existem " + funcionariosAtivos + " funcionário(s) vinculado(s). " +
                "Demita ou transfira o funcionário antes de excluir.");
        }

        cargoRepository.delete(c);
    }

    private void executarExclusaoFuncionario(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Long funcId = s.getReferenciaId();

        Funcionario f = funcionarioRepository.findById(funcId).orElse(null);
        if (f == null) return;

        Long usuarioId = f.getUsuario() != null ? f.getUsuario().getId() : null;

        // Remove todas as referências FK usando queries nativas
        entityManager.createNativeQuery("UPDATE funcionarios SET gestor_id = NULL WHERE gestor_id = :fid")
                .setParameter("fid", funcId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM promocoes WHERE funcionario_id = :fid")
                .setParameter("fid", funcId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM ponto_marcacoes WHERE funcionario_id = :fid")
                .setParameter("fid", funcId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM ponto_apuracao_diaria WHERE funcionario_id = :fid")
                .setParameter("fid", funcId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM funcionarios_beneficios WHERE funcionario_id = :fid")
                .setParameter("fid", funcId).executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // Recarrega e deleta
        f = funcionarioRepository.findById(funcId).orElse(null);
        if (f != null) {
            funcionarioRepository.delete(f);
            funcionarioRepository.flush();
        }

        // Desativa o usuário associado
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(u -> {
                u.setAtivo(false);
                usuarioRepository.save(u);
            });
        }
    }

    private void executarExclusaoDepartamento(Solicitacao s) {
        if (s.getReferenciaId() == null) return;
        Departamento d = departamentoRepository.findById(s.getReferenciaId()).orElse(null);
        if (d == null) return;

        long funcionariosAtivos = departamentoRepository.countFuncionariosByDepartamentoId(d.getId());
        if (funcionariosAtivos > 0) {
            throw new IllegalArgumentException(
                "Não é possível excluir o departamento '" + d.getNome() +
                "'. Existem " + funcionariosAtivos + " funcionário(s) vinculado(s).");
        }
        departamentoRepository.delete(d);
    }

    private void executarAberturaVaga(Solicitacao s) {
        if (s.getDadosDepois() == null) return;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> dados = objectMapper.readValue(s.getDadosDepois(), Map.class);

            Vaga v = new Vaga();
            v.setTitulo((String) dados.get("titulo"));
            v.setDescricao((String) dados.get("descricao"));
            v.setQuantidade(dados.containsKey("quantidade") && dados.get("quantidade") != null
                    ? ((Number) dados.get("quantidade")).intValue() : 1);
            v.setPrioridade(dados.containsKey("prioridade") ? (String) dados.get("prioridade") : "MEDIA");
            v.setTipoContrato(dados.containsKey("tipoContrato") ? (String) dados.get("tipoContrato") : "CLT");
            v.setLocalTrabalho((String) dados.get("localTrabalho"));
            v.setModeloTrabalho(dados.containsKey("modeloTrabalho") ? (String) dados.get("modeloTrabalho") : "PRESENCIAL");
            v.setRequisitos((String) dados.get("requisitos"));
            v.setStatus("ABERTA");
            v.setPublicadaEm(LocalDateTime.now());

            if (dados.containsKey("salarioMin") && dados.get("salarioMin") != null) {
                v.setSalarioMin(new java.math.BigDecimal(dados.get("salarioMin").toString()));
            }
            if (dados.containsKey("salarioMax") && dados.get("salarioMax") != null) {
                v.setSalarioMax(new java.math.BigDecimal(dados.get("salarioMax").toString()));
            }
            if (dados.containsKey("departamentoId") && dados.get("departamentoId") != null) {
                Long depId = ((Number) dados.get("departamentoId")).longValue();
                departamentoRepository.findById(depId).ifPresent(v::setDepartamento);
            }
            if (dados.containsKey("cargoId") && dados.get("cargoId") != null) {
                Long cargoId = ((Number) dados.get("cargoId")).longValue();
                cargoRepository.findById(cargoId).ifPresent(v::setCargo);
            }

            // Set the user who requested it as the creator
            v.setCriadoPor(s.getSolicitante());

            v = vagaRepository.save(v);
            s.setReferenciaId(v.getId());
            s.setReferenciaTipo(ReferenciaTipo.VAGA);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar vaga: " + e.getMessage(), e);
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
