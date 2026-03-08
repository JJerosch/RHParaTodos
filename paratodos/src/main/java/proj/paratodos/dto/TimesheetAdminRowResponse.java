package proj.paratodos.dto;

public record TimesheetAdminRowResponse(
        Long funcionarioId,
        String funcionarioNome,
        String data,
        String entrada,
        String saidaAlmoco,
        String retornoAlmoco,
        String saida,
        String horasTrabalhadas,
        String horasExtras,
        String horasFaltantes,
        String status
) {
}