package com.facturacion.negocio.controllers;

import com.facturacion.negocio.dto.request.LoginRequestDTO;
import com.facturacion.negocio.dto.response.LoginResponseDTO;
import com.facturacion.negocio.services.AuthService;
import com.facturacion.negocio.services.dian.certificate.DianCertificateData;
import com.facturacion.negocio.services.dian.certificate.DianCertificateLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }

        LoginResponseDTO response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }


}
