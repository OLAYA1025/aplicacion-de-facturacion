package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.ConfiguracionFacturacionElectronica;
import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.factura.FactusFactElectronicaRequestDTO;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.MetodoPago;
import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.enumerated.TipoDocumento;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.facturacion.negocio.mock.ProveedorTecnologicoClientFactus;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.CategoriaRepository;
import com.facturacion.negocio.repository.ClienteRepository;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import com.facturacion.negocio.repository.NegocioRepository;
import com.facturacion.negocio.repository.PedidoRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.services.factus.FactusTenantConfig;
import com.facturacion.negocio.services.factus.FactusTenantConfigResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.task.scheduling.enabled=false",
        "factus.base-url=http://localhost",
        "factus.client-id=***REMOVED***",
        "factus.client-secret=***REMOVED***",
        "factus.username=***REMOVED***",
        "factus.password=***REMOVED***",
        "establecimiento.nombre=***REMOVED***",
        "establecimiento.nit=***REMOVED***",
        "establecimiento.direccion=***REMOVED***",
        "establecimiento.telefono=***REMOVED***",
        "establecimiento.email=***REMOVED***",
        "establecimiento.municipaly=***REMOVED***"
})
class DocumentoFiscalServiceConcurrencyTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("facturacion_test")
            .withUsername("facturacion")
            .withPassword("facturacion");

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    @DynamicPropertySource
    static void registrarPropiedadesPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    private DocumentoFiscalService documentoFiscalService;

    @Autowired
    private DocumentoFiscalRepository documentoFiscalRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private NegocioRepository negocioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProveedorTecnologicoClientFactus proveedorTecnologicoClient;

    @MockitoBean
    private FactusTenantConfigResolver factusTenantConfigResolver;

    @Test
    void debeInvocarProveedorUnaSolaVezCuandoDosHilosEnvianSimultaneamente() throws Exception {
        DocumentoFiscal documento = crearDocumentoFiscalPendienteEnvio();
        Long docId = documento.getId();

        when(proveedorTecnologicoClient.validarFactura(any(FactusTenantConfig.class), any(FactusFactElectronicaRequestDTO.class)))
                .thenReturn(respuestaExitosaFactus(docId));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch hilosListos = new CountDownLatch(2);
        CountDownLatch inicioSimultaneo = new CountDownLatch(1);

        Future<?> f1 = executor.submit(() -> {
            ejecutarEnvioSincronizado(docId, hilosListos, inicioSimultaneo);
            return null;
        });
        Future<?> f2 = executor.submit(() -> {
            ejecutarEnvioSincronizado(docId, hilosListos, inicioSimultaneo);
            return null;
        });

        assertThat(hilosListos.await(5, TimeUnit.SECONDS)).isTrue();
        inicioSimultaneo.countDown();

        esperarFinalizacion(f1);
        esperarFinalizacion(f2);
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        verify(proveedorTecnologicoClient, times(1))
                .validarFactura(any(FactusTenantConfig.class), any(FactusFactElectronicaRequestDTO.class));

        DocumentoFiscal actualizado = documentoFiscalRepository.findById(docId).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoDocumentoFiscal.ACEPTADO_DIAN);
        assertThat(actualizado.getNumeroDocumento()).isEqualTo("SETP99000" + docId);
        assertThat(actualizado.getCufe()).isEqualTo("CUFE-" + docId);
        assertThat(actualizado.getQr()).isEqualTo("QR-IMAGE-" + docId);
    }

    @Test
    void noDebeReinvocarProveedorCuandoSegundoIntentoLlegaMientrasPrimeroEstaEnviando() throws Exception {
        DocumentoFiscal documento = crearDocumentoFiscalPendienteEnvio();
        Long docId = documento.getId();

        CountDownLatch proveedorIniciado = new CountDownLatch(1);
        CountDownLatch permitirRespuestaProveedor = new CountDownLatch(1);

        when(proveedorTecnologicoClient.validarFactura(any(FactusTenantConfig.class), any(FactusFactElectronicaRequestDTO.class)))
                .thenAnswer(invocation -> {
                    proveedorIniciado.countDown();
                    if (!permitirRespuestaProveedor.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout esperando liberacion del proveedor mock");
                    }
                    return respuestaExitosaFactus(docId);
                });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> primerIntento = executor.submit(() -> documentoFiscalService.enviarAlProveedor(docId));

        assertThat(proveedorIniciado.await(5, TimeUnit.SECONDS)).isTrue();

        DocumentoFiscal enProceso = documentoFiscalRepository.findById(docId).orElseThrow();
        assertThat(enProceso.getEstado()).isEqualTo(EstadoDocumentoFiscal.ENVIADO_PT);

        documentoFiscalService.enviarAlProveedor(docId);

        verify(proveedorTecnologicoClient, times(1))
                .validarFactura(any(FactusTenantConfig.class), any(FactusFactElectronicaRequestDTO.class));

        permitirRespuestaProveedor.countDown();
        esperarFinalizacion(primerIntento);
        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

        DocumentoFiscal actualizado = documentoFiscalRepository.findById(docId).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoDocumentoFiscal.ACEPTADO_DIAN);
        assertThat(actualizado.getNumeroDocumento()).isEqualTo("SETP99000" + docId);
    }

    @Test
    void cuandoProveedorResponde409DebeQuedarEnviadoPtYNoRechazado() throws Exception {
        DocumentoFiscal documento = crearDocumentoFiscalPendienteEnvio();
        Long docId = documento.getId();

        HttpClientErrorException conflictoDuplicado = HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "Conflict",
                HttpHeaders.EMPTY,
                "{\"message\":\"Duplicado reference_code\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(proveedorTecnologicoClient.validarFactura(any(FactusTenantConfig.class), any(FactusFactElectronicaRequestDTO.class)))
                .thenThrow(conflictoDuplicado);

        assertThatCode(() -> documentoFiscalService.enviarAlProveedor(docId))
                .doesNotThrowAnyException();

        verify(proveedorTecnologicoClient, times(1))
                .validarFactura(any(FactusTenantConfig.class), any(FactusFactElectronicaRequestDTO.class));

        DocumentoFiscal actualizado = documentoFiscalRepository.findById(docId).orElseThrow();
        assertThat(actualizado.getEstado()).isEqualTo(EstadoDocumentoFiscal.ENVIADO_PT);
        assertThat(actualizado.getEstado()).isNotEqualTo(EstadoDocumentoFiscal.RECHAZADO);
        assertThat(actualizado.getErrorCodigo()).isEqualTo("409_EN_PROCESO");
        assertThat(actualizado.getErrorMensaje()).contains("Duplicado reference_code");
    }

    private void ejecutarEnvioSincronizado(Long docId, CountDownLatch hilosListos, CountDownLatch inicioSimultaneo)
            throws InterruptedException {
        hilosListos.countDown();
        assertThat(inicioSimultaneo.await(5, TimeUnit.SECONDS)).isTrue();
        documentoFiscalService.enviarAlProveedor(docId);
    }

    private void esperarFinalizacion(Future<?> future)
            throws InterruptedException, ExecutionException, TimeoutException {
        future.get(5, TimeUnit.SECONDS);
    }

    private DocumentoFiscal crearDocumentoFiscalPendienteEnvio() {
        long n = SEQUENCE.getAndIncrement();
        Negocio negocio = negocioRepository.save(new Negocio(
                "Negocio " + n,
                "90000000" + n,
                "Direccion " + n,
                "300000000" + n,
                true
        ));

        Usuario usuario = new Usuario(
                "Usuario " + n,
                "user" + n,
                "pass-" + n,
                "user" + n + "@mail.test",
                Rol.CAJERO
        );
        usuario.setNegocio(negocio);
        usuario = usuarioRepository.save(usuario);

        Caja caja = cajaRepository.save(new Caja(new BigDecimal("100000.00"), usuario));
        Categoria categoria = new Categoria("Categoria-" + n);
        categoria.setNegocio(negocio);
        categoria = categoriaRepository.save(categoria);
        Producto producto = productoRepository.save(new Producto(
                "Producto-" + n,
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                "Producto de prueba",
                categoria
        ));

        Pedido pedido = new Pedido(
                "Pedido-" + n,
                List.of(new DetallePedido(producto, 2)),
                caja
        );
        pedido.pagar(usuario, MetodoPago.EFECTIVO);
        pedido = pedidoRepository.saveAndFlush(pedido);

        Cliente cliente = new Cliente(
                TipoDocumento.CC,
                "10" + String.format("%010d", n),
                "Cliente " + n,
                "cliente" + n + "@mail.test"
        );
        cliente.setNegocio(negocio);
        cliente = clienteRepository.save(cliente);

        DocumentoFiscal documentoFiscal = DocumentoFiscal.crearBorrador(pedido, TipoDocumentoFiscal.FEV, "PED");
        documentoFiscal.setCliente(cliente);
        documentoFiscal.marcarPendienteEnvio();
        configurarFactusMock(documentoFiscal, n);

        return documentoFiscalRepository.saveAndFlush(documentoFiscal);
    }

    private void configurarFactusMock(DocumentoFiscal documentoFiscal, long n) {
        ConfiguracionFacturacionElectronica config = new ConfiguracionFacturacionElectronica(
                ProveedorFacturacion.FACTUS,
                "http://localhost",
                "tenant-" + n,
                "password-" + n,
                "client-" + n,
                "secret-" + n,
                "PED"
        );
        config.setNegocio(documentoFiscal.getNegocio());

        FactusTenantConfig factusConfig = new FactusTenantConfig(
                n,
                "http://localhost",
                "tenant-" + n,
                "password-" + n,
                "client-" + n,
                "secret-" + n
        );

        when(factusTenantConfigResolver.factusConfigPara(any(DocumentoFiscal.class))).thenReturn(factusConfig);
        when(factusTenantConfigResolver.configuracionPara(any(DocumentoFiscal.class))).thenReturn(config);
    }

    private JsonNode respuestaExitosaFactus(Long docId) throws Exception {
        String payload = """
                {
                  "data": {
                    "bill": {
                      "number": "SETP99000%s",
                      "cufe": "CUFE-%s",
                      "qr_image": "QR-IMAGE-%s"
                    }
                  }
                }
                """.formatted(docId, docId, docId);

        return objectMapper.readTree(payload);
    }


}
