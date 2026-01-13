package sistema.rhparatodos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sistema.rhparatodos.entity.Perfil;
import sistema.rhparatodos.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.perfil.nome = :perfilNome")
    Optional<Usuario> findByEmailAndPerfilNome(@Param("email") String email, @Param("perfilNome") String perfilNome);

    boolean existsByEmail(String email);

    List<Usuario> findByPerfil(Perfil perfil);

    List<Usuario> findByAtivoTrue();

    Optional<Usuario> findByTokenRecuperacao(String tokenRecuperacao);
}
