package proj.paratodos.dto;

public record DepartamentoStatsResponse(
        long total,
        long ativos,
        long funcionarios,
        long media
) {}
