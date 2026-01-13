package sistema.rhparatodos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "perfis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    /**
     * Converte o nome do perfil para o formato do frontend
     * Ex: "ADMIN" -> "admin", "RH_CHEFE" -> "rh-chefe"
     */
    public String toFrontendFormat() {
        if (nome == null) return null;
        return nome.toLowerCase().replace("_", "-");
    }

    /**
     * Retorna as permissões baseadas no nome do perfil
     */
    public java.util.List<String> getPermissoes() {
        return switch (nome.toUpperCase()) {
            case "ADMIN" -> java.util.List.of(
                "dashboard.view", "dashboard.stats",
                "employees.view", "employees.create", "employees.edit", "employees.delete", "employees.export",
                "payroll.view", "payroll.create", "payroll.edit", "payroll.approve", "payroll.export",
                "benefits.view", "benefits.create", "benefits.edit", "benefits.delete",
                "recruitment.view", "recruitment.create", "recruitment.edit", "recruitment.delete",
                "training.view", "training.create", "training.edit", "training.delete",
                "reports.view", "reports.create", "reports.export",
                "settings.view", "settings.edit",
                "users.view", "users.create", "users.edit", "users.delete",
                "audit.view"
            );
            case "RH_CHEFE" -> java.util.List.of(
                "dashboard.view", "dashboard.stats",
                "employees.view", "employees.create", "employees.edit", "employees.export",
                "recruitment.view", "recruitment.create", "recruitment.edit", "recruitment.delete",
                "training.view", "training.create", "training.edit", "training.delete",
                "reports.view", "reports.create", "reports.export"
            );
            case "RH_ASSISTENTE" -> java.util.List.of(
                "dashboard.view",
                "employees.view", "employees.create", "employees.edit",
                "recruitment.view", "recruitment.create", "recruitment.edit",
                "training.view", "training.create"
            );
            case "DP_CHEFE" -> java.util.List.of(
                "dashboard.view", "dashboard.stats",
                "employees.view", "employees.export",
                "payroll.view", "payroll.create", "payroll.edit", "payroll.approve", "payroll.export",
                "benefits.view", "benefits.create", "benefits.edit", "benefits.delete",
                "reports.view", "reports.create", "reports.export"
            );
            case "DP_ASSISTENTE" -> java.util.List.of(
                "dashboard.view",
                "employees.view",
                "payroll.view", "payroll.create", "payroll.edit",
                "benefits.view", "benefits.create", "benefits.edit"
            );
            default -> java.util.List.of("dashboard.view");
        };
    }

    /**
     * Retorna o nome de exibição do perfil
     */
    public String getNomeExibicao() {
        return switch (nome.toUpperCase()) {
            case "ADMIN" -> "Administrador do Sistema";
            case "RH_CHEFE" -> "Chefe de RH";
            case "RH_ASSISTENTE" -> "Assistente de RH";
            case "DP_CHEFE" -> "Chefe de Departamento Pessoal";
            case "DP_ASSISTENTE" -> "Assistente de Departamento Pessoal";
            default -> nome;
        };
    }
}
