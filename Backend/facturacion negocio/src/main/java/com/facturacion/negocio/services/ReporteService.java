package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.EstadisticasDashboardDTO;
import com.facturacion.negocio.dto.response.HistorialCajaCerradaDTO;
import com.facturacion.negocio.dto.response.HistorialRetiroDTO;
import com.facturacion.negocio.dto.response.PaginacionDTO;
import com.facturacion.negocio.dto.response.ResumenEstadisticasDTO;
import com.facturacion.negocio.dto.response.TopProductoVendidoDTO;
import com.facturacion.negocio.dto.response.TopProductosVendidosDTO;
import com.facturacion.negocio.enumerated.EstadoCaja;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.DetallePedidoRepository;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import com.facturacion.negocio.repository.TopProductoVendidoProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class ReporteService {

    private static final List<String> LABELS_SEMANA = List.of("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom");
    private static final List<String> LABELS_MESES = List.of("Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");

    private final CajaRepository cajaRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ZoneId zonaAplicacion;

    public ReporteService(CajaRepository cajaRepository,
                          DetallePedidoRepository detallePedidoRepository,
                          MovimientoCajaRepository movimientoCajaRepository,
                          @Value("${app.time-zone:America/Bogota}") String zonaHoraria) {
        this.cajaRepository = cajaRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.zonaAplicacion = ZoneId.of(zonaHoraria);
    }

    public ResumenEstadisticasDTO obtenerUtilidad(String modo,
                                                  LocalDate fecha,
                                                  Integer anio,
                                                  Integer desde,
                                                  Integer hasta) {
        if (modo == null || modo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modo es requerido");
        }

        String modoNormalizado = modo.trim().toLowerCase(Locale.ROOT);
        return switch (modoNormalizado) {
            case "semana" -> construirResumenSemana(fecha);
            case "mes" -> construirResumenMes(anio);
            case "anio", "año" -> construirResumenAnio(desde, hasta);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "modo invalido. Valores permitidos: semana, mes, anio"
            );
        };
    }

    public EstadisticasDashboardDTO obtenerDashboardEstadisticas(String modo,
                                                                 LocalDate fecha,
                                                                 Integer anio,
                                                                 Integer desde,
                                                                 Integer hasta,
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

        Periodo periodo = resolverPeriodoPorModo(modoNormalizado, fecha, anio, desde, hasta);
        LocalDate fechaFin = periodo.finExclusivo().minusDays(1);

        ResumenEstadisticasDTO graficoUtilidades = obtenerUtilidad(modoNormalizado, fecha, anio, desde, hasta);
        TopProductosVendidosDTO graficoTopProductos = obtenerTopProductosVendidos(
                modoNormalizado,
                fecha,
                anio,
                desde,
                hasta,
                limiteTop
        );

        Page<Caja> paginaHistorialCajasCerradas = cajaRepository
                .findByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanOrderByFechaCierreDesc(
                EstadoCaja.CERRADA,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                PageRequest.of(paginaHistorialCajas, tamanioHistorialCajas)
        );

        List<HistorialCajaCerradaDTO> historialCajasCerradas = construirHistorialCajasCerradas(
                paginaHistorialCajasCerradas.getContent()
        );

        long totalCajasCerradas = cajaRepository.countByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThan(
                EstadoCaja.CERRADA,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo())
        );

        PaginacionDTO paginacionCajasCerradas = construirPaginacion(paginaHistorialCajasCerradas);

        BigDecimal totalRetiros = valorSeguro(movimientoCajaRepository.calcularTotalByMovimientoAndFechaRango(
                TipoMovimiento.RETIRO,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo())
        ));

        Page<MovimientoCaja> paginaHistorialRetirosMovimientos = movimientoCajaRepository
                .findByTipoAndFechaGreaterThanEqualAndFechaLessThanOrderByFechaDesc(
                TipoMovimiento.RETIRO,
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                PageRequest.of(paginaHistorialRetiros, tamanioHistorialRetiros)
        );

        List<HistorialRetiroDTO> historialRetiros = construirHistorialRetiros(
                paginaHistorialRetirosMovimientos.getContent()
        );

        PaginacionDTO paginacionRetiros = construirPaginacion(paginaHistorialRetirosMovimientos);

        return new EstadisticasDashboardDTO(
                modoNormalizado,
                periodo.inicio(),
                fechaFin,
                valorSeguro(graficoUtilidades.getTotal()),
                totalCajasCerradas,
                totalRetiros,
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
                                                               Integer limite) {
        if (modo == null || modo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modo es requerido");
        }

        int limiteNormalizado = limite == null ? 5 : limite;
        if (limiteNormalizado <= 0 || limiteNormalizado > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limite invalido");
        }

        String modoNormalizado = normalizarModo(modo);
        Periodo periodo = resolverPeriodoPorModo(modoNormalizado, fecha, anio, desde, hasta);

        List<TopProductoVendidoProjection> topProductos = detallePedidoRepository.findTopProductosVendidos(
                inicioDia(periodo.inicio()),
                inicioDia(periodo.finExclusivo()),
                PageRequest.of(0, limiteNormalizado)
        );

        List<TopProductoVendidoDTO> productos = new ArrayList<>(topProductos.size());
        List<String> labels = new ArrayList<>(topProductos.size());
        List<Long> values = new ArrayList<>(topProductos.size());

        long totalVendidos = 0L;
        for (TopProductoVendidoProjection item : topProductos) {
            long cantidad = valorSeguro(item.getCantidadVendida());
            productos.add(new TopProductoVendidoDTO(
                    item.getProductoCodigo(),
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
        List<Caja> cajas = buscarCajasCerradas(inicioSemana, finSemanaExclusivo);
        for (Caja caja : cajas) {
            if (caja.getFechaCierre() == null) {
                continue;
            }
            int index = caja.getFechaCierre().atZone(zonaAplicacion).getDayOfWeek().getValue() - 1;
            valores[index] = valores[index].add(valorSeguro(caja.getUtilidad()));
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
        List<Caja> cajas = buscarCajasCerradas(inicioAnio, finAnioExclusivo);
        for (Caja caja : cajas) {
            if (caja.getFechaCierre() == null) {
                continue;
            }
            int index = caja.getFechaCierre().atZone(zonaAplicacion).getMonthValue() - 1;
            valores[index] = valores[index].add(valorSeguro(caja.getUtilidad()));
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
        List<Caja> cajas = buscarCajasCerradas(inicio, finExclusivo);
        for (Caja caja : cajas) {
            if (caja.getFechaCierre() == null) {
                continue;
            }
            int index = caja.getFechaCierre().atZone(zonaAplicacion).getYear() - desde;
            if (index >= 0 && index < cantidadAnios) {
                valores[index] = valores[index].add(valorSeguro(caja.getUtilidad()));
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

    private List<Caja> buscarCajasCerradas(LocalDate desdeInclusive, LocalDate hastaExclusive) {
        return cajaRepository.findByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThan(
                EstadoCaja.CERRADA,
                inicioDia(desdeInclusive),
                inicioDia(hastaExclusive)
        );
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
        if (!List.of("semana", "mes", "anio").contains(modoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "modo invalido. Valores permitidos: semana, mes, anio"
            );
        }
        return modoNormalizado;
    }

    private Periodo resolverPeriodoPorModo(String modo,
                                           LocalDate fecha,
                                           Integer anio,
                                           Integer desde,
                                           Integer hasta) {
        return switch (modo) {
            case "semana" -> resolverPeriodoSemana(fecha);
            case "mes" -> resolverPeriodoMes(anio);
            case "anio" -> resolverPeriodoAnio(desde, hasta);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "modo invalido. Valores permitidos: semana, mes, anio"
            );
        };
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
