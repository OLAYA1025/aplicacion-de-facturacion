package com.facturacion.negocio.mock;



import com.facturacion.negocio.dto.factura.FactusFactElectronicaRequestDTO;
import com.facturacion.negocio.dto.factura.response.ApiResponse;
import com.facturacion.negocio.dto.factura.response.PdfData;
import com.facturacion.negocio.errors.FactusSinConexionException;
import com.facturacion.negocio.services.factus.FactusAuthService;
import com.facturacion.negocio.services.factus.FactusTenantConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Base64;
import java.util.Collections;


@Component
public class ProveedorTecnologicoClientFactus {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FactusAuthService authService;


    public ProveedorTecnologicoClientFactus(RestTemplate restTemplate,
                                            ObjectMapper objectMapper,
                                            FactusAuthService authService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.authService = authService;
    }

    public JsonNode validarFactura(FactusTenantConfig config, FactusFactElectronicaRequestDTO dto) throws JsonProcessingException {
        String token = authService.getValidAccessToken(config);
        String endpoint = config.baseUrl() + "/v1/bills/validate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        HttpEntity<FactusFactElectronicaRequestDTO> request = new HttpEntity<>(dto, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Factus validate: respuesta vacía");
            }

            return objectMapper.readTree(response.getBody());

        }catch (ResourceAccessException e){
            throw new FactusSinConexionException(
                    "No se pudo conectar a Factus (DNS / sin internet) al refrescar token.",
                    e
            );

        } catch (HttpClientErrorException e) {
            throw e;

        } catch (Exception e) {
            throw e;
        }
    }



    public byte[] obtenerPdf(FactusTenantConfig config, String numeroDocumento){

        String token = authService.getValidAccessToken(config);
        String endpoint = config.baseUrl() + "/v1/bills/download-pdf/{numeroDocumento}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try{
            ResponseEntity<ApiResponse<PdfData>> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<ApiResponse<PdfData>>() {},
                    numeroDocumento);

            ApiResponse<PdfData> body = response.getBody();
            if (body == null || body.getData() == null || body.getData().getPdfBase64Encoded() == null) {
                throw new IllegalStateException("Respuesta inválida: no viene data.pdf_base_64_encoded");
            }

            String b64 = body.getData().getPdfBase64Encoded();

            // por si viene con prefijo "data:application/pdf;base64,"
            b64 = b64.replace("data:application/pdf;base64,", "").trim();

            return Base64.getDecoder().decode(b64);


        }catch (ResourceAccessException e){
            throw e;

        } catch (HttpClientErrorException e){
            throw e;
        }catch (Exception e){
            throw e;

        }

    }
}









