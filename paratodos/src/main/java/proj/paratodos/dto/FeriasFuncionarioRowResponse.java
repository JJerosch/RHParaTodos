package proj.paratodos.dto;

public record FeriasFuncionarioRowResponse(
        Long funcionarioId,
        String matricula,
        String nomeCompleto,
        String cargo,
        String departamento,
        String statusFerias,
        String dataInicioFerias,
        String dataFimFerias
) {
}