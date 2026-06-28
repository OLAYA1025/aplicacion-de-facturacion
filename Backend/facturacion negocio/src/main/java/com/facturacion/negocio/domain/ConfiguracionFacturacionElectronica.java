package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import com.facturacion.negocio.security.EncryptionService;
import com.facturacion.negocio.utils.Normalizador;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "configuracion_facturacion_electronica",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_config_fe_negocio", columnNames = {"negocio_id"})
        }
)
public class ConfiguracionFacturacionElectronica extends BaseTenantEntity{
    /*
    factus.base-url=https://api-sandbox.factus.com.co
    factus.username=***REMOVED***
    factus.password=***REMOVED***
    factus.client-id=***REMOVED***
    factus.client-secret=***REMOVED***
    factus.prefijo-code=***REMOVED***
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProveedorFacturacion proveedor;

    @Column(nullable = false)
    private String baseUrl;


    @Column(nullable = false)
    private String username;


    @Column(nullable = false, length = 1024)
    private String password;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false, length = 1024)
    private String clientSecret;

    //este es el prefijo, con el que se envian las facturas a factus
    private String prefijoCode;

    @OneToMany(
            mappedBy = "configuracion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<NumberingRange> numberingRanges = new ArrayList<>();

    protected ConfiguracionFacturacionElectronica() {
    }






    public ConfiguracionFacturacionElectronica(
            ProveedorFacturacion proveedor,
            String baseUrl,
            String username,
            String password,
            String clientId,
            String clientSecret,
            String prefijoCode
    ) {
        this.proveedor = proveedor;
        this.baseUrl = baseUrl;
        this.username = Normalizador.normalizar(username);
        this.password = Normalizador.normalizar(password);
        this.clientId = Normalizador.normalizar(clientId);
        this.clientSecret = Normalizador.normalizar(clientSecret);
        this.prefijoCode = prefijoCode != null ? prefijoCode : "PED-";
    }

    public void cifrarCredenciales(EncryptionService encryptionService) {
        this.password = encryptionService.encrypt(password);
        this.clientSecret = encryptionService.encrypt(clientSecret);
    }

    public void addRange(NumberingRange range) {
        range.setConfiguracion(this);
        this.numberingRanges.add(range);
    }


    public void actualizar(
            EncryptionService encryptionService,
            ProveedorFacturacion proveedor,
            String baseUrl,
            String username,
            String password,
            String clientId,
            String clientSecret,
            String prefijoCode
    ) {

        if (proveedor == null
                || baseUrl == null
                || username == null
                || password == null
                || clientId == null
                || clientSecret == null) {

            throw new IllegalArgumentException(
                    "credenciales invalidas"
            );
        }

        if (prefijoCode == null || prefijoCode.isBlank()) {
            prefijoCode = "PED-";
        }

        this.proveedor = proveedor;
        this.baseUrl = Normalizador.normalizar(baseUrl);
        this.username = Normalizador.normalizar(username);
        this.password = Normalizador.normalizar(password);
        this.clientId = Normalizador.normalizar(clientId);
        this.clientSecret = Normalizador.normalizar(clientSecret);
        this.prefijoCode = Normalizador.normalizar(prefijoCode);

        cifrarCredenciales(encryptionService);
    }




    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPrefijoCode() {
        return prefijoCode;
    }

    public void setPrefijoCode(String pefijoCode) {
        this.prefijoCode = pefijoCode;
    }

    public Long getId() {
        return id;
    }

    public ProveedorFacturacion getProveedor() {
        return proveedor;
    }
}
