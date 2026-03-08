package proj.paratodos.dto;

public record TimesheetAdminSummaryResponse(
        long totalRegistros,
        long totalFuncionarios,
        String totalHorasTrabalhadas,
        String totalHorasExtras,
        String totalHorasFaltantes
) {
}