package proj.paratodos.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import proj.paratodos.domain.Usuario;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String senhaHash;
    private final boolean ativo;
    private final boolean twoFactorEnabled;
    private final String twoFactorType;   // "TOTP" | "CODIGO" | null
    private final String twoFactorSecret; // pode ser null
    private final String role;            // ex: "ADMIN"

    public UserPrincipal(Usuario u) {
        this.id = u.getId();
        this.email = u.getEmail();
        this.senhaHash = u.getSenhaHash();
        this.ativo = Boolean.TRUE.equals(u.getAtivo());
        this.twoFactorEnabled = Boolean.TRUE.equals(u.getAutenticacao2fa());
        this.twoFactorType = u.getTipo2fa();
        this.twoFactorSecret = u.getSegredo2fa();
        this.role = (u.getRole() == null || u.getRole().isBlank()) ? "USER" : u.getRole();
    }

    public Long getId() { return id; }
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public String getTwoFactorType() { return twoFactorType; }
    public String getTwoFactorSecret() { return twoFactorSecret; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() { return senhaHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; } // lock real via provider

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return ativo; }
}
