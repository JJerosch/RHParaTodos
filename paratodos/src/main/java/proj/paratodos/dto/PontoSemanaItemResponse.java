package proj.paratodos.dto;

public record PontoSemanaItemResponse(
        String data,
        String entrada,
        String saidaAlmoco,
        String retornoAlmoco,
        String saida,
        String total
) {
}