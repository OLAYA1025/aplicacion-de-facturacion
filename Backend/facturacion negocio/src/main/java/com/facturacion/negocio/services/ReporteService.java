package com.facturacion.negocio.services;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.EstadisticasDashboardDTO;
import com.facturacion.negocio.dto.response.HistorialCajaCerradaDTO;
import com.facturacion.negocio.dto.response.HistorialRetiroDTO;
import com.facturacion.negocio.dto.response.PaginacionDTO;
import com.facturacion.negocio.dto.response.ResumenEstadisticasDTO;
import com.facturacion.negocio.dto.response.TopProductoVendidoDTO;
import com.facturacion.negocio.dto.response.TopProductosVendidosDTO;
import com.facturacion.negocio.enumerated.EstadoCaja;
import com.facturacion.negocio.enumerated.EstadoCompra;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.CompraRepository;
import com.facturacion.negocio.repository.DetallePedidoRepository;
import com.facturacion.negocio.repository.IngresosPorMetodoProjection;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import com.facturacion.negocio.repository.TopProductoVendidoProjection;
import com.facturacion.negocio.repository.TotalesMovimientoPorTipoProjection;
import com.facturacion.negocio.repository.UtilidadDiariaProjection;

@Service
public class ReporteService {

    private static final List<String> LABELS_SEMANA = List.of("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom");
    private static final List<String> LABELS_MESES = List.of("Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");

    private final CajaRepository cajaRepository;
    private final CompraRepository compraRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ZoneId zonaAplicacion;
    private final TenantService tenantService;

    public ReporteService(CajaRepository cajaRepository,
                          CompraRepository compraRepository,
                          DetallePedidoRepository detallePedidoRepository,
                          MovimientoCajaRepository movimientoCajaRepository,
                          TenantService tenantService,
                          @Value("${app.time-zone:America/Bogota}") String zonaHoraria) {
        this.cajaRepository = cajaRepository;
        this.compraRepository = compraRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.tenantService = tenantService;
        this.zonaAplicacion = ZoneId.of(zonaHoraria);
    }

    public ResumenEstadisticasDTO obtenerUtilidad(String modo,
                                                  LocalDate fecha,
                                                  Integer anio,
                                                  Integer desde,
                                                  Integer hasta,
                                                  LocalDate fechaDesde,
                                                  LocalDate fechaHasta) {
        if (modo == null || modo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modo es requerido");
        }

        String modoNormalizado = modo.trim().toLowerCase(Locale.ROOT);
        return switch (modoNormalizado) {
            case "semana" -> construirResumenSemana(fecha);
            case "mes" -> construirResumenMes(anio);
            case "anio", "año" -> construirResumenAnio(desde, hasta);
            case "personalizado" -> construirResumenPersonalizado(fechaDesde, fechaHasta);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "modo invalido. Valores permitidos: semana, mes, anio, personalizado"
            );
        };
    }

    public EstadisticasDashboardDTO obtenerDashboardEstadisticas(String modo,
                                                                 LocalDate fecha,
                                                                 Integer anio,
                                                                 Integer desde,
                                                                 Integer hasta,
                                                                 LocalDate fechaDesde,
                                                                 LocalDate fechaHasta,
                                                                 Integer limiteTopProductos,
                                                                 Integer limiteRetiros,
                                                                 Integer paginaCajas,
                                                                 Integer tamanioCajas,
                                                                 Integer paginaRetiros,
                                                                 Integer tamanioRetiros) {
        String modoNormalizado = normalizarModo(modo);

        int limiteTop = limiteTopProductos == null ? 5 : limiteTopProductos;
        if (limiteTop <= 0 || limiteTop > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limiteTopProductos invalido");
        }

        int paginaHistorialCajas = paginaCajas == null ? 0 : paginaCajas;
        int tamanioHistorialCajas = tamanioCajas == null ? 20 : tamanioCajas;
        int paginaHistorialRetiros = paginaRetiros == null ? 0 : paginaRetiros;
        int tamanioHistorialRetiros = tamanioRetiros != null
                ? tamanioRetiros
                : (limiteRetiros == null ? 20 : limiteRetiros);

        validarPaginacion("paginaCajas", paginaHistorialCajas);
        validarPaginacion("paginaRetiros", paginaHistorialRetiros);
        validarTamanio("tamanioCajas", tamanioHistorialCajas);
        validarTamanio("tamanioRetiros", tamanioHistorialRetiros);

        Periodo periodo = resolverPeriodoPorModo(modoNormalizado, fecha, anio, desde, hasta, fechaDesde, fechaHasta);
        LocalDate fechaFin = periodo.finExclusivo().minusDays(1);
        Negocio negocio = tenantService.getNegocioActual();

        ResumenEstadisticasDTO graficoUtilidades = obtenerUtilidad(modoNormalizado, fecha, anio, desde, hasta, fechaDesde, fechaHasta);
        TopProductosVendidosDTO graficoTopProductos = obtenerTopProductosVendidos(
                modoNormalizado,
                fecha,
                anio,
                desde,
                hasta,
            fechaDesde,
            fechaHasta,
                limiteTop
        );

        Page<Caja> paginaHistorialCajasCerradas = cajaRepository
                .findByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanAndNegocioOrderByFechaCierreDesc(
                EstadoCaja.CERRADA,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                negocio,
                PageRequest.of(paginaHistorialCajas, tamanioHistorialCajas)
        );

        List<HistorialCajaCerradaDTO> historialCajasCerradas = construirHistorialCajasCerradas(
                paginaHistorialCajasCerradas.getContent()
        );

        long totalCajasCerradas = cajaRepository.countByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanAndNegocio(
                EstadoCaja.CERRADA,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                negocio
        );

        PaginacionDTO paginacionCajasCerradas = construirPaginacion(paginaHistorialCajasCerradas);

        Map<TipoMovimiento, BigDecimal> totalesMovimientos = new LinkedHashMap<>();
        for (TotalesMovimientoPorTipoProjection item : movimientoCajaRepository.calcularTotalesPorTipoYFechaRango(
            List.of(TipoMovimiento.RETIRO, TipoMovimiento.EGRESO),
            negocio,
            inicioDia(periodo.inicio()),
            inicioDia(periodo.finExclusivo())
        )) {
            totalesMovimientos.put(item.getTipo(), valorSeguro(item.getTotal()));
        }

        BigDecimal totalRetiros = totalesMovimientos.getOrDefault(TipoMovimiento.RETIRO, BigDecimal.ZERO);
        BigDecimal totalEgresos = totalesMovimientos.getOrDefault(TipoMovimiento.EGRESO, BigDecimal.ZERO);

        BigDecimal totalGastos = valorSeguro(compraRepository.calcularTotalConfirmadasPorFechaConfirmacionRango(
            EstadoCompra.CONFIRMADA,
            negocio,
            inicioDia(periodo.inicio()),
            inicioDia(periodo.finExclusivo())
        ));

        Page<MovimientoCaja> paginaHistorialRetirosMovimientos = movimientoCajaRepository
                .findByTipoAndFechaGreaterThanEqualAndFechaLessThanAndNegocioOrderByFechaDesc(
                TipoMovimiento.RETIRO,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                negocio,
                PageRequest.of(paginaHistorialRetiros, tamanioHistorialRetiros)
        );

        List<HistorialRetiroDTO> historialRetiros = construirHistorialRetiros(
                paginaHistorialRetirosMovimientos.getContent()
        );

        PaginacionDTO paginacionRetiros = construirPaginacion(paginaHistorialRetirosMovimientos);

        IngresosPorMetodoProjection ingresosPorMetodo = detallePedidoRepository.findIngresosTransferenciaYTarjeta(
            inicioDia(periodo.inicio()),
            inicioDia(periodo.finExclusivo()),
            negocio
        );

        BigDecimal ingresosTransferencia = valorSeguro(
            ingresosPorMetodo != null ? ingresosPorMetodo.getIngresosTransferencia() : null
        );
        BigDecimal ingresosTarjeta = valorSeguro(
            ingresosPorMetodo != null ? ingresosPorMetodo.getIngresosTarjeta() : null
        );

        return new EstadisticasDashboardDTO(
                modoNormalizado,
                periodo.inicio(),
                fechaFin,
                valorSeguro(graficoUtilidades.getTotal()),
                totalCajasCerradas,
                totalRetiros,
                totalEgresos,
                totalGastos,
                ingresosTransferencia,
                ingresosTarjeta,
                graficoUtilidades,
                graficoTopProductos,
                historialCajasCerradas,
                paginacionCajasCerradas,
                historialRetiros,
                paginacionRetiros
        );
    }

    public TopProductosVendidosDTO obtenerTopProductosVendidos(String modo,
                                                               LocalDate fecha,
                                                               Integer anio,
                                                               Integer desde,
                                                               Integer hasta,
                                                               LocalDate fechaDesde,
                                                               LocalDate fechaHasta,
                                                               Integer limite) {
        if (modo == null || modo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modo es requerido");
        }

        int limiteNormalizado = limite == null ? 5 : limite;
        if (limiteNormalizado <= 0 || limiteNormalizado > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limite invalido");
        }

        String modoNormalizado = normalizarModo(modo);
        Periodo periodo = resolverPeriodoPorModo(modoNormalizado, fecha, anio, desde, hasta, fechaDesde, fechaHasta);
        Negocio negocio = tenantService.getNegocioActual();

        List<TopProductoVendidoProjection> topProductos = detallePedidoRepository.findTopProductosVendidos(
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                negocio,
                PageRequest.of(0, limiteNormalizado)
        );

        List<TopProductoVendidoDTO> productos = new ArrayList<>(topProductos.size());
        List<String> labels = new ArrayList<>(topProductos.size());
        List<Long> values = new ArrayList<>(topProductos.size());

        long totalVendidos = 0L;
        for (TopProductoVendidoProjection item : topProductos) {
            long cantidad = valorSeguro(item.getCantidadVendida());
            productos.add(new TopProductoVendidoDTO(
                    item.getProductoId(),
                    item.getProductoNombre(),
                    cantidad
            ));
            labels.add(item.getProductoNombre());
            values.add(cantidad);
            totalVendidos += cantidad;
        }

        return new TopProductosVendidosDTO(
                modoNormalizado,
                periodo.inicio(),
                periodo.finExclusivo().minusDays(1),
                labels,
                values,
                totalVendidos,
                productos
        );
    }

    private ResumenEstadisticasDTO construirResumenSemana(LocalDate fecha) {
        Periodo periodo = resolverPeriodoSemana(fecha);
        LocalDate inicioSemana = periodo.inicio();
        LocalDate finSemanaExclusivo = periodo.finExclusivo();

        BigDecimal[] valores = inicializarEnCero(7);
        List<UtilidadDiariaProjection> utilidades = cajaRepository.calcularUtilidadDiariaEnRango(
                tenantService.getNegocioActual().getId(),
                inicioDia(inicioSemana),
                inicioDia(finSemanaExclusivo)
        );
        for (UtilidadDiariaProjection item : utilidades) {
            if (item.getDia() == null) {
                continue;
            }
            int index = item.getDia().getDayOfWeek().getValue() - 1;
            if (index >= 0 && index < valores.length) {
                valores[index] = valores[index].add(valorSeguro(item.getTotal()));
            }
        }

        List<BigDecimal> valoresLista = Arrays.asList(valores);
        return new ResumenEstadisticasDTO(
                "semana",
                inicioSemana,
                finSemanaExclusivo.minusDays(1),
                LABELS_SEMANA,
                valoresLista,
                sumar(valoresLista)
        );
    }

    private ResumenEstadisticasDTO construirResumenMes(Integer anio) {
        Periodo periodo = resolverPeriodoMes(anio);
        LocalDate inicioAnio = periodo.inicio();
        LocalDate finAnioExclusivo = periodo.finExclusivo();

        BigDecimal[] valores = inicializarEnCero(12);
        List<UtilidadDiariaProjection> utilidades = cajaRepository.calcularUtilidadDiariaEnRango(
                tenantService.getNegocioActual().getId(),
                inicioDia(inicioAnio),
                inicioDia(finAnioExclusivo)
        );
        for (UtilidadDiariaProjection item : utilidades) {
            if (item.getDia() == null) {
                continue;
            }
            int index = item.getDia().getMonthValue() - 1;
            if (index >= 0 && index < valores.length) {
                valores[index] = valores[index].add(valorSeguro(item.getTotal()));
            }
        }

        List<BigDecimal> valoresLista = Arrays.asList(valores);
        return new ResumenEstadisticasDTO(
                "mes",
                inicioAnio,
                finAnioExclusivo.minusDays(1),
                LABELS_MESES,
                valoresLista,
                sumar(valoresLista)
        );
    }

    private ResumenEstadisticasDTO construirResumenAnio(Integer desde, Integer hasta) {
        Periodo periodo = resolverPeriodoAnio(desde, hasta);
        LocalDate inicio = periodo.inicio();
        LocalDate finExclusivo = periodo.finExclusivo();

        int cantidadAnios = (hasta - desde) + 1;
        BigDecimal[] valores = inicializarEnCero(cantidadAnios);
        List<UtilidadDiariaProjection> utilidades = cajaRepository.calcularUtilidadDiariaEnRango(
                tenantService.getNegocioActual().getId(),
                inicioDia(inicio),
                inicioDia(finExclusivo)
        );
        for (UtilidadDiariaProjection item : utilidades) {
            if (item.getDia() == null) {
                continue;
            }
            int index = item.getDia().getYear() - desde;
            if (index >= 0 && index < cantidadAnios) {
                valores[index] = valores[index].add(valorSeguro(item.getTotal()));
            }
        }

        List<String> labels = new ArrayList<>(cantidadAnios);
        for (int anioActual = desde; anioActual <= hasta; anioActual++) {
            labels.add(String.valueOf(anioActual));
        }

        List<BigDecimal> valoresLista = Arrays.asList(valores);
        return new ResumenEstadisticasDTO(
                "anio",
                inicio,
                finExclusivo.minusDays(1),
                labels,
                valoresLista,
                sumar(valoresLista)
        );
    }

    private ResumenEstadisticasDTO construirResumenPersonalizado(LocalDate fechaDesde, LocalDate fechaHasta) {
        Periodo periodo = resolverPeriodoPersonalizado(fechaDesde, fechaHasta);
        LocalDate inicio = periodo.inicio();
        LocalDate finExclusivo = periodo.finExclusivo();

        long dias = ChronoUnit.DAYS.between(inicio, finExclusivo);
        if (dias <= 0 || dias > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rango de fechas invalido");
        }

        Map<LocalDate, BigDecimal> utilidadesPorDia = new LinkedHashMap<>();
        List<UtilidadDiariaProjection> utilidades = cajaRepository.calcularUtilidadDiariaEnRango(
                tenantService.getNegocioActual().getId(),
                inicioDia(inicio),
                inicioDia(finExclusivo)
        );

        for (UtilidadDiariaProjection item : utilidades) {
            if (item.getDia() != null) {
                utilidadesPorDia.put(item.getDia(), valorSeguro(item.getTotal()));
            }
        }

        List<String> labels = new ArrayList<>((int) dias);
        List<BigDecimal> valores = new ArrayList<>((int) dias);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate fechaActual = inicio; !fechaActual.isEqual(finExclusivo); fechaActual = fechaActual.plusDays(1)) {
            labels.add(fechaActual.format(formatter));
            valores.add(utilidadesPorDia.getOrDefault(fechaActual, BigDecimal.ZERO));
        }

        return new ResumenEstadisticasDTO(
                "personalizado",
                inicio,
                finExclusivo.minusDays(1),
                labels,
                valores,
                sumar(valores)
        );
    }

    private List<HistorialCajaCerradaDTO> construirHistorialCajasCerradas(List<Caja> cajasCerradas) {
        List<HistorialCajaCerradaDTO> historial = new ArrayList<>(cajasCerradas.size());
        for (Caja caja : cajasCerradas) {
            LocalDate fechaCaja = caja.getFechaCierre() != null
                    ? caja.getFechaCierre().atZone(zonaAplicacion).toLocalDate()
                    : (caja.getFechaApertura() != null ? caja.getFechaApertura().atZone(zonaAplicacion).toLocalDate() : null);

            historial.add(new HistorialCajaCerradaDTO(
                    caja.getId(),
                    obtenerNombreUsuario(caja.getUsuarioApertura()),
                    obtenerNombreUsuario(caja.getUsuarioCierre()),
                    caja.getFechaApertura(),
                    caja.getFechaCierre(),
                    fechaCaja,
                    valorSeguro(caja.getUtilidad())
            ));
        }
        return historial;
    }

    private List<HistorialRetiroDTO> construirHistorialRetiros(List<MovimientoCaja> retiros) {
        List<HistorialRetiroDTO> historial = new ArrayList<>(retiros.size());
        for (MovimientoCaja retiro : retiros) {
            Long cajaId = retiro.getCaja() != null ? retiro.getCaja().getId() : null;
            historial.add(new HistorialRetiroDTO(
                    retiro.getId(),
                    cajaId,
                    obtenerNombreUsuario(retiro.getUsuario()),
                    retiro.getMotivo(),
                    retiro.getFecha(),
                    valorSeguro(retiro.getMonto())
            ));
        }
        return historial;
    }

    private PaginacionDTO construirPaginacion(Page<?> pagina) {
        return new PaginacionDTO(
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast(),
                pagina.hasNext(),
                pagina.hasPrevious()
        );
    }

    private String obtenerNombreUsuario(Usuario usuario) {
        if (usuario == null) {
            return "Sin usuario";
        }
        if (usuario.getNombre() != null && !usuario.getNombre().isBlank()) {
            return usuario.getNombre();
        }
        if (usuario.getUsername() != null && !usuario.getUsername().isBlank()) {
            return usuario.getUsername();
        }
        return "Sin nombre";
    }

    private Instant inicioDia(LocalDate fecha) {
        return fecha.atStartOfDay(zonaAplicacion).toInstant();
    }

    private Periodo resolverPeriodoSemana(LocalDate fecha) {
        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fecha es requerida cuando modo=semana");
        }
        LocalDate inicioSemana = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new Periodo(inicioSemana, inicioSemana.plusDays(7));
    }

    private Periodo resolverPeriodoMes(Integer anio) {
        if (anio == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "anio es requerido cuando modo=mes");
        }
        if (anio < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "anio invalido");
        }
        LocalDate inicioAnio = LocalDate.of(anio, 1, 1);
        return new Periodo(inicioAnio, inicioAnio.plusYears(1));
    }

    private Periodo resolverPeriodoAnio(Integer desde, Integer hasta) {
        if (desde == null || hasta == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "desde y hasta son requeridos cuando modo=anio");
        }
        if (desde < 1 || hasta < 1 || desde > hasta) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rango de anios invalido");
        }
        return new Periodo(
                LocalDate.of(desde, 1, 1),
                LocalDate.of(hasta + 1, 1, 1)
        );
    }

    private String normalizarModo(String modo) {
        if (modo == null || modo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modo es requerido");
        }
        String modoNormalizado = modo.trim().toLowerCase(Locale.ROOT);
        if ("año".equals(modoNormalizado)) {
            return "anio";
        }
        if (!List.of("semana", "mes", "anio", "personalizado").contains(modoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                "modo invalido. Valores permitidos: semana, mes, anio, personalizado"
            );
        }
        return modoNormalizado;
    }

    private Periodo resolverPeriodoPorModo(String modo,
                                           LocalDate fecha,
                                           Integer anio,
                                           Integer desde,
                                           Integer hasta,
                                           LocalDate fechaDesde,
                                           LocalDate fechaHasta) {
        return switch (modo) {
            case "semana" -> resolverPeriodoSemana(fecha);
            case "mes" -> resolverPeriodoMes(anio);
            case "anio" -> resolverPeriodoAnio(desde, hasta);
            case "personalizado" -> resolverPeriodoPersonalizado(fechaDesde, fechaHasta);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "modo invalido. Valores permitidos: semana, mes, anio, personalizado"
            );
        };
    }

    private Periodo resolverPeriodoPersonalizado(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (fechaDesde == null || fechaHasta == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDesde y fechaHasta son requeridas cuando modo=personalizado");
        }
        if (fechaDesde.isAfter(fechaHasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fechaDesde no puede ser mayor a fechaHasta");
        }
        return new Periodo(fechaDesde, fechaHasta.plusDays(1));
    }

    private void validarPaginacion(String campo, int pagina) {
        if (pagina < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " invalida");
        }
    }

    private void validarTamanio(String campo, int tamanio) {
        if (tamanio <= 0 || tamanio > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " invalido");
        }
    }

    private BigDecimal[] inicializarEnCero(int tamanio) {
        BigDecimal[] valores = new BigDecimal[tamanio];
        Arrays.fill(valores, BigDecimal.ZERO);
        return valores;
    }

    private BigDecimal sumar(List<BigDecimal> valores) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal valor : valores) {
            total = total.add(valorSeguro(valor));
        }
        return total;
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private long valorSeguro(Long valor) {
        return valor == null ? 0L : valor;
    }

    private record Periodo(LocalDate inicio, LocalDate finExclusivo) {
    }
}
