package com.facturacion.negocio.scheduler;

import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final String nombre;
    private final String username;
    private final String password;
    private final String email;
    private final Rol rol = Rol.ADMINISTRADOR;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           UsuarioService usuarioService,
                           @Value("${admin.nombre}") String nombre,
                           @Value("${admin.username}") String username,
                           @Value("${admin.password}") String password,
                           @Value("${admin.email}") String email){
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Override
    public void run(String[] args){

        if(usuarioRepository.count()==0){
            usuarioService.crearUsuario(nombre, username, password, email, rol);
            System.out.println("Creando usuario admin...");
        }


    }
}
