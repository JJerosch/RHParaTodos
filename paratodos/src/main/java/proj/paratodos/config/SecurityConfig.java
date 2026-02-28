package proj.paratodos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import proj.paratodos.repository.UsuarioRepository;
import proj.paratodos.security.*;
import proj.paratodos.service.JwtService;

@Configuration
public class SecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtCookieFilter jwtCookieFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        return new JwtCookieFilter(jwtService, usuarioRepository);
    }

    @Bean
    public TwoFactorEnforcementFilter twoFactorEnforcementFilter() {
        return new TwoFactorEnforcementFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DatabaseAuthenticationProvider databaseAuthenticationProvider,
            JwtCookieFilter jwtCookieFilter,
            TwoFactorEnforcementFilter twoFactorEnforcementFilter,
            TwoFactorLoginSuccessHandler twoFactorLoginSuccessHandler) throws Exception {

        http
                .authenticationProvider(databaseAuthenticationProvider)
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(twoFactorEnforcementFilter, JwtCookieFilter.class)

                // Desabilita CSRF para endpoints REST /api/** (chamadas fetch/AJAX com JSON)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/2fa", "/error",
                                "/styles/**", "/scripts/**", "/assets/**", "/favicon.ico"
                        ).permitAll()

                        // Configurações: apenas ADMIN
                        .requestMatchers("/settings/**")
                            .hasRole("ADMIN")

                        // Folha de pagamento: DP
                        .requestMatchers("/payroll/**")
                            .hasAnyRole("ADMIN", "DP_CHEFE", "DP_ASSISTENTE")

                        // Férias e benefícios: DP + RH_CHEFE (aprova políticas)
                        .requestMatchers("/benefits/**", "/api/benefits/**", "/vacation/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE", "DP_CHEFE", "DP_ASSISTENTE")

                        // Funcionários, recrutamento, treinamentos: RH
                        .requestMatchers("/employees/**", "/api/employees/**", "/recruitment/**", "/training/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE")

                        // Departamentos e cargos: somente chefia RH
                        .requestMatchers("/departments/**", "/api/departments/**", "/positions/**", "/api/positions/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE")

                        // Avaliações de desempenho: chefia RH
                        .requestMatchers("/performance/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE")

                        // Relatórios: estratégicos + operacionais
                        .requestMatchers("/reports/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE", "DP_CHEFE")

                        // Ponto: todos os perfis de gestão
                        .requestMatchers("/timesheet/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE", "DP_CHEFE", "DP_ASSISTENTE")

                        // Self-service funcionário (legado mantido para compatibilidade)
                        .requestMatchers("/employee-dashboard/**", "/payslips/**")
                            .hasAnyRole("ADMIN", "EMPLOYEE")

                        // Minha Área (individual): todos os perfis
                        .requestMatchers("/meu-ponto/**", "/meus-holerites/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE", "DP_CHEFE", "DP_ASSISTENTE", "EMPLOYEE")

                        // Dashboard unificado: todos os perfis
                        .requestMatchers("/dashboard/**")
                            .hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE", "DP_CHEFE", "DP_ASSISTENTE", "EMPLOYEE")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .successHandler(twoFactorLoginSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies(JwtCookieFilter.COOKIE_NAME)
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }
}