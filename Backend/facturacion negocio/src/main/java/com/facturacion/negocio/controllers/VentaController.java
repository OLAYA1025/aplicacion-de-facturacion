package com.facturacion.negocio.controllers;

import jakarta.persistence.Id;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/venta")
public class VentaController {

    @PostMapping
    public ResponseEntity<?> cobrarPedido(){
        return ResponseEntity.ok().build();
    }
}
