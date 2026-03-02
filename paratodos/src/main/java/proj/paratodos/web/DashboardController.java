package proj.paratodos.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proj.paratodos.domain.Departamento;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final FuncionarioRepository funcionarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final VagaRepository vagaRepository;
    private final PromocaoRepository promocaoRepository;

    public DashboardController(FuncionarioRepository funcionarioRepository,
                               DepartamentoRepository departamentoRepository,
                               VagaRepository vagaRepository,
                               PromocaoRepository promocaoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.departamentoRepository = departamentoRepository;
        this.vagaRepository = vagaRepository;
        this.promocaoRepository = promocaoRepository;
    }

    @GetMapping
    public Map<String, Object> getDashboardData() {
        long totalFuncionarios = funcionarioRepository.count();
        long funcionariosAtivos = funcionarioRepository.countByStatus("ATIVO");

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        long contratadosMes = funcionarioRepository.countContratadosDesde(inicioMes);

        long vagasAbertas = vagaRepository.countByStatus("ABERTA");
        long vagasEmAndamento = vagaRepository.countByStatus("EM_ANDAMENTO");

        long promocoesPendentes = promocaoRepository.countByStatus("PENDENTE");

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
                Map.entry("promocoesPendentes", promocoesPendentes),
                Map.entry("contratacaoRecentes", contratacaoList),
                Map.entry("aniversariantes", aniversarianteList),
                Map.entry("distribuicaoDepartamento", distribuicao)
        );
    }
}
