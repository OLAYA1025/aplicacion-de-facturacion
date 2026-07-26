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
                .requestMatchers("/usuario/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/caja/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO", "CAJERO", "MESERO", "COCINERO")
                .requestMatchers("/cliente/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO","CAJERO")
                .requestMatchers("/compra/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
                .requestMatchers("/documentos-fiscales/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO","CAJERO")
                .requestMatchers("/movimientos/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO","CAJERO")
                .requestMatchers("/movimientos/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO","CAJERO")
                .requestMatchers("/producto/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO","MESERO", "CAJERO", "COCINERO")
                .requestMatchers("/proveedor/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
                .requestMatchers("/stats/**").hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
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
