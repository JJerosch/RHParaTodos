package proj.paratodos.dto;

public record RegistrarPontoResponse(
        String mensagem,
        String tipoRegistrado,
        String proximaAcao
) {
}