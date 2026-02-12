package proj.paratodos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "autenticacao_2fa")
    private Boolean autenticacao2fa;

    @Column(name = "tipo_2fa")
    private String tipo2fa; // ex: "TOTP" ou "CODIGO"

    @Column(name = "segredo_2fa")
    private String segredo2fa;

    @Column(name = "tentativas_login")
    private Integer tentativasLogin;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "role")
    private String role; // ex: "ADMIN"
}