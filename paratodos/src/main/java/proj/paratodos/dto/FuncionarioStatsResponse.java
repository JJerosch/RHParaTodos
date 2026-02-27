package proj.paratodos.dto;

public record FuncionarioStatsResponse(
        long total,
        long ativos,
        long ferias,
        long desligadosMes
) {}
