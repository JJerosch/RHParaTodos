package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.Cargo;

import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    List<Cargo> findByAtivoTrueOrderByTituloAsc();

    List<Cargo> findByDepartamentoIdAndAtivoTrueOrderByTituloAsc(Long departamentoId);
}
