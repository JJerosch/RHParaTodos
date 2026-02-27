package proj.paratodos.dto;

import java.math.BigDecimal;

public record BeneficioStatsResponse(
        long tipos,
        long beneficiarios,
        BigDecimal custoMensal,
        BigDecimal custoPorFuncionario
) {}
