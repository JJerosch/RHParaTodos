package proj.paratodos.dto;

public record TimesheetAdminRowResponse(
        Long funcionarioId,
        String funcionarioNome,
        String data,
        String horasTrabalhadas,
        String horasExtras,
        String horasFaltantes,
        String status
) {
}