package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.PontoMarcacao;

import java.time.LocalDateTime;
import java.util.List;

public interface PontoMarcacaoRepository extends JpaRepository<PontoMarcacao, Long> {

    List<PontoMarcacao> findByFuncionarioIdAndDataHoraBetweenOrderByDataHoraAsc(
            Long funcionarioId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    List<PontoMarcacao> findByFuncionarioIdAndDataHoraBetweenOrderByDataHoraDesc(
            Long funcionarioId,
            LocalDateTime inicio,
            LocalDateTime fim
    );
}