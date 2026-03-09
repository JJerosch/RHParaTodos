package proj.paratodos.dto;

import java.util.List;

public record FeriasPageResponse(
        FeriasResumoResponse summary,
        List<FeriasFuncionarioRowResponse> rows
) {
}