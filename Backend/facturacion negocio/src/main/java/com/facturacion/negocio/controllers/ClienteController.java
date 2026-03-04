package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.dto.request.ActualizarClienteRequestDTO;
import com.facturacion.negocio.dto.response.ClienteResponseDTO;
import com.facturacion.negocio.services.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/{tipoIdentificacion}/{numeroIdentificacion}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorIdentificacion(@PathVariable String tipoIdentificacion,
                                                                       @PathVariable String numeroIdentificacion) {
        Cliente cliente = clienteService.obtenerPorTipoYNumero(tipoIdentificacion, numeroIdentificacion);
        return ResponseEntity.ok(ClienteResponseDTO.from(cliente));
    }

    @PutMapping("/{tipoIdentificacion}/{numeroIdentificacion}")
    public ResponseEntity<ClienteResponseDTO> editarPorIdentificacion(@PathVariable String tipoIdentificacion,
                                                                      @PathVariable String numeroIdentificacion,
                                                                      @RequestBody ActualizarClienteRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }

        Cliente cliente = clienteService.editarPorTipoYNumero(
                tipoIdentificacion,
                numeroIdentificacion,
                request.getNombre(),
                request.getEmail()
        );
        return ResponseEntity.ok(ClienteResponseDTO.from(cliente));
    }
}
