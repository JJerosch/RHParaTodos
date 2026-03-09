package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.Funcionario;
import proj.paratodos.domain.PontoApuracaoDiaria;
import proj.paratodos.domain.PontoJornada;
import proj.paratodos.domain.PontoMarcacao;
import proj.paratodos.domain.Usuario;
import proj.paratodos.dto.MeuPontoResumoResponse;
import proj.paratodos.dto.PontoMarcacaoResponse;
import proj.paratodos.dto.PontoSemanaItemResponse;
import proj.paratodos.dto.RegistrarPontoResponse;
import proj.paratodos.dto.TimesheetAdminResponse;
import proj.paratodos.dto.TimesheetAdminRowResponse;
import proj.paratodos.dto.TimesheetAdminSummaryResponse;
import proj.paratodos.repository.FuncionarioRepository;
import proj.paratodos.repository.PontoApuracaoDiariaRepository;
import proj.paratodos.repository.PontoJornadaRepository;
import proj.paratodos.repository.PontoMarcacaoRepository;
import proj.paratodos.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PontoService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FuncionarioRepository funcionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PontoMarcacaoRepository pontoMarcacaoRepository;
    private final PontoApuracaoDiariaRepository pontoApuracaoDiariaRepository;
    private final PontoJornadaRepository pontoJornadaRepository;
    private final PontoCalendarioService pontoCalendarioService;

    public PontoService(
            FuncionarioRepository funcionarioRepository,
            UsuarioRepository usuarioRepository,
            PontoMarcacaoRepository pontoMarcacaoRepository,
            PontoApuracaoDiariaRepository pontoApuracaoDiariaRepository,
            PontoJornadaRepository pontoJornadaRepository,
            PontoCalendarioService pontoCalendarioService
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.pontoMarcacaoRepository = pontoMarcacaoRepository;
        this.pontoApuracaoDiariaRepository = pontoApuracaoDiariaRepository;
        this.pontoJornadaRepository = pontoJornadaRepository;
        this.pontoCalendarioService = pontoCalendarioService;
    }

    @Transactional
    public RegistrarPontoResponse registrarBatida(Long usuarioId, String ip) {
        Funcionario funcionario = getFuncionarioLogado(usuarioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        LocalDate hoje = LocalDate.now();

        if (!isDiaUtil(hoje)) {
            throw new IllegalArgumentException("Registro de ponto indisponível para hoje.");
        }

        List<PontoMarcacao> hojeMarcacoes = buscarMarcacoesDia(funcionario.getId(), hoje);
        String proximoTipo = determinarProximoTipoParaRegistro(hojeMarcacoes);

        PontoMarcacao marcacao = new PontoMarcacao();
        marcacao.setFuncionario(funcionario);
        marcacao.setTipo(proximoTipo);
        marcacao.setDataHora(LocalDateTime.now());
        marcacao.setOrigem("WEB");
        marcacao.setIp(ip);
        marcacao.setCriadoPor(usuario);

        pontoMarcacaoRepository.save(marcacao);
        recalcularApuracao(funcionario, hoje);

        String proximaAcao = switch (proximoTipo) {
            case "ENTRADA" -> "SAIDA_ALMOCO";
            case "SAIDA_ALMOCO" -> "RETORNO_ALMOCO";
            case "RETORNO_ALMOCO" -> "SAIDA";
            default -> "FECHADO";
        };

        return new RegistrarPontoResponse(
                "Ponto registrado com sucesso.",
                proximoTipo,
                proximaAcao
        );
    }

    @Transactional(readOnly = true)
    public MeuPontoResumoResponse getResumoMeuPonto(Long usuarioId) {
        Funcionario funcionario = getFuncionarioLogado(usuarioId);
        LocalDate hoje = LocalDate.now();
        String tipoDia = pontoCalendarioService.obterTipoDia(hoje);

        List<PontoMarcacao> hojeMarcacoes = buscarMarcacoesDia(funcionario.getId(), hoje);
        List<PontoMarcacaoResponse> marcacoesHoje = hojeMarcacoes.stream()
                .sorted(Comparator.comparing(PontoMarcacao::getDataHora))
                .map(PontoMarcacaoResponse::fromEntity)
                .toList();

        String statusAtual;
        String proximaAcao;

        if (isDiaUtil(hoje)) {
            statusAtual = calcularStatusAtual(hojeMarcacoes);
            proximaAcao = determinarProximoTipo(hojeMarcacoes);
        } else {
            statusAtual = "INDISPONIVEL";
            proximaAcao = "FECHADO";
        }

        BigDecimal horasHoje = pontoApuracaoDiariaRepository
                .findByFuncionarioIdAndData(funcionario.getId(), hoje)
                .map(PontoApuracaoDiaria::getHorasTrabalhadas)
                .orElse(BigDecimal.ZERO);

        LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);
        LocalDate inicioMes = hoje.withDayOfMonth(1);

        BigDecimal horasSemana = somarHoras(funcionario.getId(), inicioSemana, hoje, "TRABALHADAS");
        BigDecimal horasMes = somarHoras(funcionario.getId(), inicioMes, hoje, "TRABALHADAS");
        BigDecimal bancoHoras = somarHoras(funcionario.getId(), inicioMes, hoje, "EXTRAS")
                .subtract(somarHoras(funcionario.getId(), inicioMes, hoje, "FALTANTES"));

        return new MeuPontoResumoResponse(
                statusAtual,
                proximaAcao,
                formatHoras(horasHoje),
                formatHoras(horasSemana),
                formatHoras(horasMes),
                formatHorasSigned(bancoHoras),
                marcacoesHoje,
                tipoDia
        );
    }

    @Transactional(readOnly = true)
    public List<PontoSemanaItemResponse> getHistoricoSemana(Long usuarioId) {
        Funcionario funcionario = getFuncionarioLogado(usuarioId);

        LocalDate inicioSemana = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate fimSemana = inicioSemana.plusDays(4);

        List<PontoSemanaItemResponse> itens = new ArrayList<>();

        for (LocalDate data = inicioSemana; !data.isAfter(fimSemana); data = data.plusDays(1)) {

            String aviso = null;

            if ("FERIAS".equalsIgnoreCase(funcionario.getStatus())) {
                aviso = "FÉRIAS";
            } else {
                String tipoDia = pontoCalendarioService.obterTipoDia(data);

                if ("RECESSO".equalsIgnoreCase(tipoDia)) {
                    aviso = "RECESSO";
                } else if ("FERIADO".equalsIgnoreCase(tipoDia)) {
                    aviso = "FERIADO";
                }
            }

            if (aviso != null) {
                itens.add(new PontoSemanaItemResponse(
                        data.format(DATA_BR),
                        aviso,
                        aviso,
                        aviso,
                        aviso,
                        aviso
                ));
                continue;
            }

            List<PontoMarcacao> marcacoes = buscarMarcacoesDia(funcionario.getId(), data);
            PontoApuracaoDiaria apuracao = pontoApuracaoDiariaRepository
                    .findByFuncionarioIdAndData(funcionario.getId(), data)
                    .orElse(null);

            itens.add(new PontoSemanaItemResponse(
                    data.format(DATA_BR),
                    getHoraPorTipo(marcacoes, "ENTRADA"),
                    getHoraPorTipo(marcacoes, "SAIDA_ALMOCO"),
                    getHoraPorTipo(marcacoes, "RETORNO_ALMOCO"),
                    getHoraPorTipo(marcacoes, "SAIDA"),
                    apuracao != null ? formatHoras(apuracao.getHorasTrabalhadas()) : "--:--"
            ));
        }

        return itens;
    }

    private Funcionario getFuncionarioLogado(Long usuarioId) {
        return funcionarioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário vinculado ao usuário não encontrado."));
    }

    private List<PontoMarcacao> buscarMarcacoesDia(Long funcionarioId, LocalDate data) {
        return pontoMarcacaoRepository.findByFuncionarioIdAndDataHoraBetweenOrderByDataHoraAsc(
                funcionarioId,
                data.atStartOfDay(),
                data.atTime(LocalTime.MAX)
        );
    }

    private boolean isDiaUtil(LocalDate data) {
        return "DIA_UTIL".equals(pontoCalendarioService.obterTipoDia(data));
    }

    private String determinarProximoTipo(List<PontoMarcacao> marcacoes) {
        int total = marcacoes.size();
        return switch (total) {
            case 0 -> "ENTRADA";
            case 1 -> "SAIDA_ALMOCO";
            case 2 -> "RETORNO_ALMOCO";
            case 3 -> "SAIDA";
            default -> "FECHADO";
        };
    }

    private String determinarProximoTipoParaRegistro(List<PontoMarcacao> marcacoes) {
        String proximoTipo = determinarProximoTipo(marcacoes);

        if ("FECHADO".equals(proximoTipo)) {
            throw new IllegalArgumentException("Expediente já encerrado para hoje.");
        }

        return proximoTipo;
    }

    private String calcularStatusAtual(List<PontoMarcacao> marcacoes) {
        return switch (marcacoes.size()) {
            case 0 -> "SEM_PONTO";
            case 1 -> "TRABALHANDO";
            case 2 -> "EM_INTERVALO";
            case 3 -> "TRABALHANDO";
            default -> "FINALIZADO";
        };
    }

    private void recalcularApuracao(Funcionario funcionario, LocalDate data) {
        List<PontoMarcacao> marcacoes = buscarMarcacoesDia(funcionario.getId(), data);

        BigDecimal horasTrabalhadas = calcularHorasTrabalhadas(marcacoes);
        BigDecimal cargaDiaria = buscarCargaHorariaDiaria(funcionario);
        BigDecimal horasExtras = BigDecimal.ZERO;
        BigDecimal horasFaltantes = BigDecimal.ZERO;

        String tipoDia = pontoCalendarioService.obterTipoDia(data);
        boolean diaUtil = "DIA_UTIL".equals(tipoDia);

        if (diaUtil) {
            if (horasTrabalhadas.compareTo(cargaDiaria) > 0) {
                horasExtras = horasTrabalhadas.subtract(cargaDiaria);
            } else {
                horasFaltantes = cargaDiaria.subtract(horasTrabalhadas);
            }
        } else {
            horasExtras = BigDecimal.ZERO;
            horasFaltantes = BigDecimal.ZERO;
        }

        boolean fechado = marcacoes.size() >= 4;

        PontoApuracaoDiaria apuracao = pontoApuracaoDiariaRepository
                .findByFuncionarioIdAndData(funcionario.getId(), data)
                .orElseGet(PontoApuracaoDiaria::new);

        apuracao.setFuncionario(funcionario);
        apuracao.setData(data);
        apuracao.setHorasTrabalhadas(horasTrabalhadas.max(BigDecimal.ZERO));
        apuracao.setHorasExtras(fechado ? horasExtras.max(BigDecimal.ZERO) : BigDecimal.ZERO);
        apuracao.setHorasFaltantes(fechado ? horasFaltantes.max(BigDecimal.ZERO) : BigDecimal.ZERO);
        apuracao.setFechado(fechado);

        pontoApuracaoDiariaRepository.save(apuracao);
    }

    private BigDecimal buscarCargaHorariaDiaria(Funcionario funcionario) {
        if (funcionario.getJornadaId() == null) {
            return new BigDecimal("8.00");
        }

        return pontoJornadaRepository.findById(funcionario.getJornadaId())
                .map(PontoJornada::getCargaHorariaDiaria)
                .orElse(new BigDecimal("8.00"));
    }

    private BigDecimal calcularHorasTrabalhadas(List<PontoMarcacao> marcacoes) {
        LocalDateTime entrada = getDataHoraPorTipo(marcacoes, "ENTRADA");
        if (entrada == null) return BigDecimal.ZERO;

        LocalDateTime saidaAlmoco = getDataHoraPorTipo(marcacoes, "SAIDA_ALMOCO");
        LocalDateTime retornoAlmoco = getDataHoraPorTipo(marcacoes, "RETORNO_ALMOCO");
        LocalDateTime saidaFinal = getDataHoraPorTipo(marcacoes, "SAIDA");

        Duration total = Duration.ZERO;

        LocalDateTime fimPrimeiroPeriodo = saidaAlmoco != null ? saidaAlmoco : LocalDateTime.now();
        if (!fimPrimeiroPeriodo.isBefore(entrada)) {
            total = total.plus(Duration.between(entrada, fimPrimeiroPeriodo));
        }

        if (retornoAlmoco != null) {
            LocalDateTime fimSegundoPeriodo = saidaFinal != null ? saidaFinal : LocalDateTime.now();
            if (!fimSegundoPeriodo.isBefore(retornoAlmoco)) {
                total = total.plus(Duration.between(retornoAlmoco, fimSegundoPeriodo));
            }
        }

        long minutos = total.toMinutes();
        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal somarHoras(Long funcionarioId, LocalDate inicio, LocalDate fim, String tipo) {
        return pontoApuracaoDiariaRepository.findByFuncionarioIdAndDataBetweenOrderByDataDesc(funcionarioId, inicio, fim)
                .stream()
                .map(a -> switch (tipo) {
                    case "EXTRAS" -> a.getHorasExtras();
                    case "FALTANTES" -> a.getHorasFaltantes();
                    default -> a.getHorasTrabalhadas();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String getHoraPorTipo(List<PontoMarcacao> marcacoes, String tipo) {
        LocalDateTime dataHora = getDataHoraPorTipo(marcacoes, tipo);
        return dataHora != null ? dataHora.toLocalTime().format(HH_MM) : "--:--";
    }

    private LocalDateTime getDataHoraPorTipo(List<PontoMarcacao> marcacoes, String tipo) {
        return marcacoes.stream()
                .filter(m -> tipo.equalsIgnoreCase(m.getTipo()))
                .map(PontoMarcacao::getDataHora)
                .findFirst()
                .orElse(null);
    }

    private String formatHoras(BigDecimal horas) {
        int totalMinutos = horas.multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        int h = totalMinutos / 60;
        int m = totalMinutos % 60;
        return String.format("%02d:%02d", h, m);
    }

    private String formatHorasSigned(BigDecimal horas) {
        String sinal = horas.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-";
        return sinal + formatHoras(horas.abs());
    }

    @Transactional(readOnly = true)
    public TimesheetAdminResponse listarTimesheetAdmin(String nome, LocalDate inicio, LocalDate fim) {
        List<PontoApuracaoDiaria> apuracoes;

        if (nome == null || nome.isBlank()) {
            apuracoes = pontoApuracaoDiariaRepository.buscarTimesheetAdmin(inicio, fim);
        } else {
            apuracoes = pontoApuracaoDiariaRepository.buscarTimesheetAdminPorNome(
                    inicio,
                    fim,
                    "%" + nome.trim() + "%"
            );
        }

        List<TimesheetAdminRowResponse> rows = apuracoes.stream()
                .map(this::mapTimesheetRow)
                .toList();

        long totalRegistros = rows.size();
        long totalFuncionarios = apuracoes.stream()
                .map(a -> a.getFuncionario().getId())
                .distinct()
                .count();

        BigDecimal totalHorasTrabalhadas = apuracoes.stream()
                .map(PontoApuracaoDiaria::getHorasTrabalhadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHorasExtras = apuracoes.stream()
                .map(PontoApuracaoDiaria::getHorasExtras)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHorasFaltantes = apuracoes.stream()
                .map(PontoApuracaoDiaria::getHorasFaltantes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TimesheetAdminSummaryResponse summary = new TimesheetAdminSummaryResponse(
                totalRegistros,
                totalFuncionarios,
                formatHoras(totalHorasTrabalhadas),
                formatHoras(totalHorasExtras),
                formatHoras(totalHorasFaltantes)
        );

        return new TimesheetAdminResponse(summary, rows);
    }

    private TimesheetAdminRowResponse mapTimesheetRow(PontoApuracaoDiaria apuracao) {
        List<PontoMarcacao> marcacoes = buscarMarcacoesDia(
                apuracao.getFuncionario().getId(),
                apuracao.getData()
        );

        return new TimesheetAdminRowResponse(
                apuracao.getFuncionario().getId(),
                apuracao.getFuncionario().getNomeCompleto(),
                apuracao.getData().format(DATA_BR),
                getHoraPorTipo(marcacoes, "ENTRADA"),
                getHoraPorTipo(marcacoes, "SAIDA_ALMOCO"),
                getHoraPorTipo(marcacoes, "RETORNO_ALMOCO"),
                getHoraPorTipo(marcacoes, "SAIDA"),
                formatHoras(apuracao.getHorasTrabalhadas()),
                formatHoras(apuracao.getHorasExtras()),
                formatHoras(apuracao.getHorasFaltantes()),
                montarStatusAdmin(apuracao)
        );
    }

    private String montarStatusAdmin(PontoApuracaoDiaria apuracao) {
        if (Boolean.TRUE.equals(apuracao.getFechado())) {
            return "FINALIZADO";
        }
        return "EM_ANDAMENTO";
    }
}