package proj.paratodos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.*;
import proj.paratodos.dto.SolicitacaoRequest;
import proj.paratodos.dto.SolicitacaoResponse;
import proj.paratodos.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public SolicitacaoService(SolicitacaoRepository solicitacaoRepository,
                              FuncionarioRepository funcionarioRepository,
                              UsuarioRepository usuarioRepository,
                              ObjectMapper objectMapper) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    public List<SolicitacaoResponse> findAll() {
        return solicitacaoRepository.findAllWithDetails().stream()
                .map(SolicitacaoResponse::fromEntity)
                .toList();
    }

    public List<SolicitacaoResponse> findByStatus(String status) {
        return solicitacaoRepository.findByStatusWithDetails(status).stream()
                .map(SolicitacaoResponse::fromEntity)
                .toList();
    }

    public SolicitacaoResponse findById(Long id) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitacao nao encontrada: " + id);
        }
        return SolicitacaoResponse.fromEntity(s);
    }

    @Transactional
    public SolicitacaoResponse create(SolicitacaoRequest request, Long solicitanteId) {
        Funcionario funcionario = funcionarioRepository.findById(request.funcionarioId())
                .orElseThrow(() -> new IllegalArgumentException("Funcionario nao encontrado: " + request.funcionarioId()));

        Usuario solicitante = usuarioRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante nao encontrado"));

        String tipo = request.tipo().toUpperCase();
        if (!tipo.equals("EDICAO") && !tipo.equals("DESATIVACAO") && !tipo.equals("EXCLUSAO")) {
            throw new IllegalArgumentException("Tipo de solicitacao invalido: " + tipo);
        }

        Solicitacao s = new Solicitacao();
        s.setTipo(tipo);
        s.setFuncionario(funcionario);
        s.setSolicitante(solicitante);
        s.setMotivo(request.motivo());
        s.setDadosJson(request.dadosJson());
        s.setStatus("PENDENTE");

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s);
    }

    @Transactional
    public SolicitacaoResponse approve(Long id, Long aprovadorId, String observacao) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitacao nao encontrada: " + id);
        }

        if (!"PENDENTE".equals(s.getStatus())) {
            throw new IllegalArgumentException("Apenas solicitacoes pendentes podem ser aprovadas");
        }

        Usuario aprovador = usuarioRepository.findById(aprovadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario aprovador nao encontrado"));

        s.setAprovador(aprovador);
        s.setStatus("APROVADA");
        s.setDataDecisao(LocalDateTime.now());
        s.setObservacaoAprovador(observacao);

        Funcionario f = s.getFuncionario();

        switch (s.getTipo()) {
            case "DESATIVACAO" -> {
                f.setStatus("DESLIGADO");
                f.setDataDesligamento(LocalDate.now());
                funcionarioRepository.save(f);
            }
            case "EXCLUSAO" -> {
                funcionarioRepository.delete(f);
            }
            case "EDICAO" -> {
                applyEditData(f, s.getDadosJson());
                funcionarioRepository.save(f);
            }
        }

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s);
    }

    @Transactional
    public SolicitacaoResponse reject(Long id, Long aprovadorId, String observacao) {
        Solicitacao s = solicitacaoRepository.findByIdWithDetails(id);
        if (s == null) {
            throw new IllegalArgumentException("Solicitacao nao encontrada: " + id);
        }

        if (!"PENDENTE".equals(s.getStatus())) {
            throw new IllegalArgumentException("Apenas solicitacoes pendentes podem ser rejeitadas");
        }

        Usuario aprovador = usuarioRepository.findById(aprovadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario aprovador nao encontrado"));

        s.setAprovador(aprovador);
        s.setStatus("REJEITADA");
        s.setDataDecisao(LocalDateTime.now());
        s.setObservacaoAprovador(observacao);

        s = solicitacaoRepository.save(s);
        return SolicitacaoResponse.fromEntity(s);
    }

    public SolicitacaoStatsResponse getStats() {
        long total = solicitacaoRepository.count();
        long pendentes = solicitacaoRepository.countByStatus("PENDENTE");
        long aprovadas = solicitacaoRepository.countByStatus("APROVADA");
        long rejeitadas = solicitacaoRepository.countByStatus("REJEITADA");
        return new SolicitacaoStatsResponse(total, pendentes, aprovadas, rejeitadas);
    }

    private void applyEditData(Funcionario f, String dadosJson) {
        if (dadosJson == null || dadosJson.isBlank()) return;
        try {
            JsonNode data = objectMapper.readTree(dadosJson);
            if (data.has("nomeCompleto")) f.setNomeCompleto(data.get("nomeCompleto").asText());
            if (data.has("emailPessoal")) f.setEmailPessoal(textOrNull(data, "emailPessoal"));
            if (data.has("emailCorporativo")) f.setEmailCorporativo(textOrNull(data, "emailCorporativo"));
            if (data.has("telefone")) f.setTelefone(textOrNull(data, "telefone"));
            if (data.has("celular")) f.setCelular(textOrNull(data, "celular"));
            if (data.has("cep")) f.setCep(textOrNull(data, "cep"));
            if (data.has("logradouro")) f.setLogradouro(textOrNull(data, "logradouro"));
            if (data.has("numero")) f.setNumero(textOrNull(data, "numero"));
            if (data.has("complemento")) f.setComplemento(textOrNull(data, "complemento"));
            if (data.has("bairro")) f.setBairro(textOrNull(data, "bairro"));
            if (data.has("cidade")) f.setCidade(textOrNull(data, "cidade"));
            if (data.has("estado")) f.setEstado(textOrNull(data, "estado"));
            if (data.has("banco")) f.setBanco(textOrNull(data, "banco"));
            if (data.has("agencia")) f.setAgencia(textOrNull(data, "agencia"));
            if (data.has("conta")) f.setConta(textOrNull(data, "conta"));
            if (data.has("tipoConta")) f.setTipoConta(textOrNull(data, "tipoConta"));
            if (data.has("pix")) f.setPix(textOrNull(data, "pix"));
            if (data.has("tipoContrato")) f.setTipoContrato(textOrNull(data, "tipoContrato"));
            if (data.has("status")) f.setStatus(data.get("status").asText());
            if (data.has("gestorId")) {
                if (data.get("gestorId").isNull()) {
                    f.setGestor(null);
                } else {
                    Long gestorId = data.get("gestorId").asLong();
                    funcionarioRepository.findById(gestorId).ifPresent(f::setGestor);
                }
            }
            if (data.has("emergenciaNome")) f.setEmergenciaNome(textOrNull(data, "emergenciaNome"));
            if (data.has("emergenciaParentesco")) f.setEmergenciaParentesco(textOrNull(data, "emergenciaParentesco"));
            if (data.has("emergenciaTelefone")) f.setEmergenciaTelefone(textOrNull(data, "emergenciaTelefone"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao processar dados da edicao: " + e.getMessage());
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n == null || n.isNull() || n.asText().isBlank()) ? null : n.asText();
    }

    public record SolicitacaoStatsResponse(long total, long pendentes, long aprovadas, long rejeitadas) {}
}
