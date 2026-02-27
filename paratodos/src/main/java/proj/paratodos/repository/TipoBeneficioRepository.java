package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proj.paratodos.domain.TipoBeneficio;

import java.util.List;

public interface TipoBeneficioRepository extends JpaRepository<TipoBeneficio, Long> {

    List<TipoBeneficio> findAllByOrderByNomeAsc();

    List<TipoBeneficio> findByAtivoTrueOrderByNomeAsc();

    @Query("SELECT COUNT(DISTINCT fb.funcionarioId) FROM FuncionarioBeneficio fb WHERE fb.tipoBeneficioId = :tipoId AND fb.ativo = true")
    long countBeneficiariosByTipoId(@Param("tipoId") Long tipoId);

    @Query("SELECT COUNT(DISTINCT fb.funcionarioId) FROM FuncionarioBeneficio fb WHERE fb.ativo = true")
    long countTotalBeneficiarios();

    @Query("SELECT COALESCE(SUM(fb.valor), 0) FROM FuncionarioBeneficio fb WHERE fb.ativo = true")
    java.math.BigDecimal sumCustoMensal();
}
