package com.facturacion.negocio.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.services.UsuarioService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final String nombre;
    private final String username;
    private final String password;
    private final String email;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           @Value("${superadmin.nombre}") String nombre,
                           @Value("${superadmin.username}") String username,
                           @Value("${superadmin.password}") String password,
                           @Value("${superadmin.email}") String email) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public void run(String[] args) {
        if (usuarioRepository.existsByRol(Rol.SUPER_ADMIN)) {
            return;
        }

        usuarioService.crearSuperAdmin(nombre, username, password, email);
        System.out.println("SUPER_ADMIN creado automaticamente: " + username);
    }
}
