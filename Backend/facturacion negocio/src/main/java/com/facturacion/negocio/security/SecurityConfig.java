package com.facturacion.negocio.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(Customizer.withDefaults());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/platform/negocios/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/usuario/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/caja/**").hasAnyRole("ADMINISTRADOR", "CAJERO", "MESERO", "COCINERO")
                .requestMatchers("/cliente/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                .requestMatchers("/compra/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/documentos-fiscales/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                .requestMatchers("/movimientos/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                .requestMatchers("/movimientos/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                .requestMatchers("/producto/**").hasAnyRole("ADMINISTRADOR", "MESERO", "CAJERO", "COCINERO")
                .requestMatchers("/proveedor/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/stats/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated()
        );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
