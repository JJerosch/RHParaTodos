package proj.paratodos.dto;

import java.util.List;

public record MeuPontoResumoResponse(
        String statusAtual,
        String proximaAcao,
        String horasHoje,
        String horasSemana,
        String horasMes,
        String bancoHoras,
        List<PontoMarcacaoResponse> marcacoesHoje
) {
}