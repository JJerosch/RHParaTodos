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

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/2fa", "/error",
                                "/styles/**", "/scripts/**", "/assets/**", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(
                                "/dashboard/**", "/employees/**", "/departments/**",
                                "/positions/**", "/recruitment/**", "/training/**",
                                "/performance/**", "/payroll/**", "/benefits/**",
                                "/vacation/**", "/timesheet/**", "/reports/**",
                                "/settings/**"
                        ).hasAnyRole("ADMIN", "RH_CHEFE", "RH_ASSISTENTE", "DP_CHEFE", "DP_ASSISTENTE")
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