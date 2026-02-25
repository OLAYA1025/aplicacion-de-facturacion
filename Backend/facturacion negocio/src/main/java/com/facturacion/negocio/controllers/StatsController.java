package com.facturacion.negocio.controllers;

import com.facturacion.negocio.dto.response.ResumenEstadisticasDTO;
import com.facturacion.negocio.dto.response.EstadisticasDashboardDTO;
import com.facturacion.negocio.dto.response.TopProductosVendidosDTO;
import com.facturacion.negocio.services.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final ReporteService reporteService;

    public StatsController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/utilidad")
    public ResponseEntity<ResumenEstadisticasDTO> obtenerUtilidad(
            @RequestParam String modo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta) {
        return ResponseEntity.ok(
                reporteService.obtenerUtilidad(modo, fecha, anio, desde, hasta)
        );
    }

    @GetMapping("/top-productos")
    public ResponseEntity<TopProductosVendidosDTO> obtenerTopProductos(
            @RequestParam String modo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta,
            @RequestParam(defaultValue = "5") Integer limite) {
        return ResponseEntity.ok(
                reporteService.obtenerTopProductosVendidos(modo, fecha, anio, desde, hasta, limite)
        );
    }

    @GetMapping("/dashboard")
    public ResponseEntity<EstadisticasDashboardDTO> obtenerDashboard(
            @RequestParam String modo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer desde,
            @RequestParam(required = false) Integer hasta,
            @RequestParam(defaultValue = "5") Integer limiteTopProductos,
            @RequestParam(defaultValue = "20") Integer limiteRetiros,
            @RequestParam(defaultValue = "0") Integer paginaCajas,
            @RequestParam(defaultValue = "20") Integer tamanioCajas,
            @RequestParam(defaultValue = "0") Integer paginaRetiros,
            @RequestParam(required = false) Integer tamanioRetiros) {
        return ResponseEntity.ok(
                reporteService.obtenerDashboardEstadisticas(
                        modo,
                        fecha,
                        anio,
                        desde,
                        hasta,
                        limiteTopProductos,
                        limiteRetiros,
                        paginaCajas,
                        tamanioCajas,
                        paginaRetiros,
                        tamanioRetiros
                )
        );
    }
}
