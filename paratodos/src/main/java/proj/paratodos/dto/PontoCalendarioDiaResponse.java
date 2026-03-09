package proj.paratodos.dto;

public record PontoCalendarioDiaResponse(
        String data,
        String tipo,
        String descricao,
        boolean fimDeSemana
) {
}