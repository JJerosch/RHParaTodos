package proj.paratodos.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proj.paratodos.domain.*;
import proj.paratodos.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final FuncionarioRepository funcionarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final VagaRepository vagaRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    public DashboardController(FuncionarioRepository funcionarioRepository,
                               DepartamentoRepository departamentoRepository,
                               VagaRepository vagaRepository,
                               SolicitacaoRepository solicitacaoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.departamentoRepository = departamentoRepository;
        this.vagaRepository = vagaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @GetMapping
    public Map<String, Object> getDashboardData() {
        long totalFuncionarios = funcionarioRepository.count();
        long funcionariosAtivos = funcionarioRepository.countByStatus("ATIVO");

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        long contratadosMes = funcionarioRepository.countContratadosDesde(inicioMes);

        long vagasAbertas = vagaRepository.countByStatus("ABERTA");
        long vagasEmAndamento = vagaRepository.countByStatus("EM_ANDAMENTO");

        // Solicitações pendentes (substitui promocoesPendentes)
        long solicitacoesPendentes = solicitacaoRepository.countByStatus(StatusSolicitacao.PENDENTE);

        // Detalhamento por categoria de solicitação pendente
        List<Map<String, Object>> pendenciasPorTipo = buildPendenciasPorTipo();

        // Contratações recentes: últimos 30 dias, max 5
        LocalDate umMesAtras = LocalDate.now().minusDays(30);
        List<Funcionario> contratacoes = funcionarioRepository.findContratadosRecentes(umMesAtras);
        List<Map<String, Object>> contratacaoList = contratacoes.stream()
                .limit(5)
                .map(f -> Map.<String, Object>of(
                        "nome", f.getNomeCompleto(),
                        "cargo", f.getCargo() != null ? f.getCargo().getTitulo() : "-",
                        "departamento", f.getDepartamento() != null ? f.getDepartamento().getNome() : "-",
                        "dataAdmissao", f.getDataAdmissao().toString(),
                        "status", f.getStatus()
                ))
                .toList();

        // Aniversariantes do mês
        int mesAtual = LocalDate.now().getMonthValue();
        List<Funcionario> aniversariantes = funcionarioRepository.findAniversariantesMes(mesAtual);
        List<Map<String, Object>> aniversarianteList = aniversariantes.stream()
                .map(f -> Map.<String, Object>of(
                        "nome", f.getNomeCompleto(),
                        "dia", f.getDataNascimento().getDayOfMonth(),
                        "cargo", f.getCargo() != null ? f.getCargo().getTitulo() : "-"
                ))
                .toList();

        // Distribuição por departamento
        List<Departamento> departamentos = departamentoRepository.findByAtivoTrueOrderByNomeAsc();
        List<Map<String, Object>> distribuicao = departamentos.stream()
                .map(d -> {
                    long count = departamentoRepository.countFuncionariosByDepartamentoId(d.getId());
                    return Map.<String, Object>of(
                            "nome", d.getNome(),
                            "count", count
                    );
                })
                .filter(m -> (long) m.get("count") > 0)
                .sorted((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")))
                .toList();

        // Porcentagem de ativos
        double percentAtivos = totalFuncionarios > 0
                ? Math.round((double) funcionariosAtivos / totalFuncionarios * 1000.0) / 10.0
                : 0;

        return Map.ofEntries(
                Map.entry("totalFuncionarios", totalFuncionarios),
                Map.entry("funcionariosAtivos", funcionariosAtivos),
                Map.entry("percentAtivos", percentAtivos),
                Map.entry("contratadosMes", contratadosMes),
                Map.entry("vagasAbertas", vagasAbertas),
                Map.entry("vagasEmAndamento", vagasEmAndamento),
                Map.entry("solicitacoesPendentes", solicitacoesPendentes),
                Map.entry("pendenciasPorTipo", pendenciasPorTipo),
                Map.entry("contratacaoRecentes", contratacaoList),
                Map.entry("aniversariantes", aniversarianteList),
                Map.entry("distribuicaoDepartamento", distribuicao)
        );
    }

    private List<Map<String, Object>> buildPendenciasPorTipo() {
        List<Map<String, Object>> pendencias = new ArrayList<>();

        // Grupo: Funcionários (alteração, ativação, desativação, exclusão)
        long funcPendentes = countPendentes(
                TipoSolicitacao.ALTERACAO_FUNCIONARIO,
                TipoSolicitacao.ATIVACAO_FUNCIONARIO,
                TipoSolicitacao.DESATIVACAO_FUNCIONARIO,
                TipoSolicitacao.EXCLUSAO_FUNCIONARIO
        );
        if (funcPendentes > 0) {
            pendencias.add(Map.of(
                    "titulo", "Solicitações de Funcionários",
                    "count", funcPendentes,
                    "link", "/solicitacoes?tipo=funcionario"
            ));
        }

        // Grupo: Departamentos
        long deptPendentes = countPendentes(
                TipoSolicitacao.CRIACAO_DEPARTAMENTO,
                TipoSolicitacao.EDICAO_DEPARTAMENTO,
                TipoSolicitacao.DESATIVACAO_DEPARTAMENTO,
                TipoSolicitacao.EXCLUSAO_DEPARTAMENTO
        );
        if (deptPendentes > 0) {
            pendencias.add(Map.of(
                    "titulo", "Solicitações de Departamentos",
                    "count", deptPendentes,
                    "link", "/solicitacoes?tipo=departamento"
            ));
        }

        // Grupo: Cargos
        long cargoPendentes = countPendentes(
                TipoSolicitacao.CRIACAO_CARGO,
                TipoSolicitacao.EDICAO_CARGO,
                TipoSolicitacao.DESATIVACAO_CARGO,
                TipoSolicitacao.EXCLUSAO_CARGO
        );
        if (cargoPendentes > 0) {
            pendencias.add(Map.of(
                    "titulo", "Solicitações de Cargos",
                    "count", cargoPendentes,
                    "link", "/solicitacoes?tipo=cargo"
            ));
        }

        // Grupo: Movimentações (promoção, transferência, reajuste)
        long movPendentes = countPendentes(
                TipoSolicitacao.PROMOCAO,
                TipoSolicitacao.TRANSFERENCIA,
                TipoSolicitacao.REAJUSTE_SALARIAL
        );
        if (movPendentes > 0) {
            pendencias.add(Map.of(
                    "titulo", "Movimentações de Pessoal",
                    "count", movPendentes,
                    "link", "/solicitacoes?tipo=movimentacao"
            ));
        }

        // Grupo: Recrutamento (abertura de vaga)
        long recPendentes = countPendentes(TipoSolicitacao.ABERTURA_VAGA);
        if (recPendentes > 0) {
            pendencias.add(Map.of(
                    "titulo", "Abertura de Vagas",
                    "count", recPendentes,
                    "link", "/solicitacoes?tipo=recrutamento"
            ));
        }

        return pendencias;
    }

    private long countPendentes(TipoSolicitacao... tipos) {
        long total = 0;
        for (TipoSolicitacao tipo : tipos) {
            total += solicitacaoRepository.countByStatusAndTipo(StatusSolicitacao.PENDENTE, tipo);
        }
        return total;
    }
}
