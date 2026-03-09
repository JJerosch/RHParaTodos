package proj.paratodos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proj.paratodos.domain.PontoCalendario;
import proj.paratodos.dto.PontoCalendarioDiaResponse;
import proj.paratodos.dto.PontoCalendarioUpdateRequest;
import proj.paratodos.repository.PontoCalendarioRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PontoCalendarioService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE;

    private final PontoCalendarioRepository repository;

    public PontoCalendarioService(PontoCalendarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PontoCalendarioDiaResponse> listarMes(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        Map<LocalDate, PontoCalendario> mapa = repository
                .findByDataBetweenOrderByDataAsc(inicio, fim)
                .stream()
                .collect(Collectors.toMap(PontoCalendario::getData, Function.identity()));

        List<PontoCalendarioDiaResponse> itens = new ArrayList<>();

        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            boolean fimDeSemana = isFimDeSemana(data);
            PontoCalendario registro = mapa.get(data);

            String tipo = registro != null
                    ? registro.getTipo()
                    : (fimDeSemana ? "RECESSO" : "DIA_UTIL");

            String descricao = registro != null ? registro.getDescricao() : null;

            itens.add(new PontoCalendarioDiaResponse(
                    data.format(ISO),
                    tipo,
                    descricao,
                    fimDeSemana
            ));
        }

        return itens;
    }

    @Transactional
    public void salvarDia(LocalDate data, PontoCalendarioUpdateRequest request) {
        String tipo = normalizarTipo(request.tipo());

        PontoCalendario registro = repository.findByData(data)
                .orElseGet(PontoCalendario::new);

        registro.setData(data);
        registro.setTipo(tipo);
        registro.setDescricao(request.descricao());

        repository.save(registro);
    }

    @Transactional
    public void limparDia(LocalDate data) {
        repository.deleteByData(data);
    }

    @Transactional(readOnly = true)
    public String obterTipoDia(LocalDate data) {
        return repository.findByData(data)
                .map(PontoCalendario::getTipo)
                .orElseGet(() -> isFimDeSemana(data) ? "RECESSO" : "DIA_UTIL");
    }

    private boolean isFimDeSemana(LocalDate data) {
        return data.getDayOfWeek() == DayOfWeek.SATURDAY
                || data.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private String normalizarTipo(String tipo) {
        String valor = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!valor.equals("DIA_UTIL") && !valor.equals("FERIADO") && !valor.equals("RECESSO")) {
            throw new IllegalArgumentException("Tipo inválido. Use DIA_UTIL, FERIADO ou RECESSO.");
        }
        return valor;
    }
}