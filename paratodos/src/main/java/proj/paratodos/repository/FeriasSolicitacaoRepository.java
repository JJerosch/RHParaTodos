package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.FeriasSolicitacao;

public interface FeriasSolicitacaoRepository extends JpaRepository<FeriasSolicitacao, Long> {
}