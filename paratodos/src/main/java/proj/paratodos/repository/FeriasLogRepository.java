package proj.paratodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proj.paratodos.domain.FeriasLog;

public interface FeriasLogRepository extends JpaRepository<FeriasLog, Long> {
}