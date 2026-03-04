package com.facturacion.negocio.services.factus;

import com.facturacion.negocio.errors.FactusSinConexionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;

@Service
public class FactusAuthService {

    private final WebClient webClient;

    private final String clientId;
    private final String clientSecret;
    private final String username;
    private final String password;

    // cache en memoria
    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile Instant expiresAt;

    public FactusAuthService(
            @Value("${factus.base-url}") String baseUrl,
            @Value("${factus.client-id}") String clientId,
            @Value("${factus.client-secret}") String clientSecret,
            @Value("${factus.username}") String username,
            @Value("${factus.password}") String password
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
    }

    /**
     * Devuelve un access token válido. Si está expirado, lo renueva.
     */
    public synchronized String getValidAccessToken() {
        // margen de seguridad para no expirar en mitad de una request
        if (accessToken != null && expiresAt != null && Instant.now().isBefore(expiresAt.minusSeconds(30))) {
            return accessToken;
        }

        // 1) intenta refresh si existe
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                FactusTokenResponse refreshed = refreshWithRefreshToken(refreshToken);
                cacheTokens(refreshed);
                return accessToken;
            } catch (Exception ignored) {
                // si refresh falla, hacemos login normal
            }
        }

        // 2) login con password
        FactusTokenResponse token = loginWithPassword();
        cacheTokens(token);
        return accessToken;
    }

    private FactusTokenResponse loginWithPassword() {

        try {
            FactusTokenResponse r = webClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("grant_type", "password")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("username", username)
                            .with("password", password)
                    )
                    .retrieve()
                    .bodyToMono(FactusTokenResponse.class)
                    .block();

            if (r == null || r.getAccessToken() == null || r.getAccessToken().isBlank()) {
                throw new IllegalStateException("FactusAuthService: respuesta inválida en loginWithPassword()");
            }

            return r;

        }catch (WebClientRequestException ex) {
            // DNS / sin internet / conexión / timeout
            throw new FactusSinConexionException(
                    "No se pudo conectar a Factus (DNS / sin internet).",
                    ex
            );

        } catch (WebClientResponseException ex) {
            // Factus respondió 4xx/5xx (credenciales malas, etc.)
            throw new IllegalStateException(
                    "FactusAuthService: error HTTP obteniendo token: " + ex.getStatusCode().value(),
                    ex
            );
        }

    }

    private FactusTokenResponse refreshWithRefreshToken(String refreshToken) {
        try {
            FactusTokenResponse r = webClient.post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("grant_type", "refresh_token")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("refresh_token", refreshToken)
                    )
                    .retrieve()
                    .bodyToMono(FactusTokenResponse.class)
                    .block();

            if (r == null || r.getAccessToken() == null || r.getAccessToken().isBlank()) {
                throw new IllegalStateException("FactusAuthService: respuesta inválida en refreshWithRefreshToken()");
            }

            return r;
        }catch (WebClientRequestException ex) {
            throw new FactusSinConexionException(
                    "No se pudo conectar a Factus (DNS / sin internet) al refrescar token.",
                    ex
            );
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException(
                    "FactusAuthService: error HTTP refrescando token: " + ex.getStatusCode().value(),
                    ex
            );
        }
    }

    private void cacheTokens(FactusTokenResponse r) {
        this.accessToken = r.getAccessToken();
        this.refreshToken = r.getRefreshToken();
        this.expiresAt = Instant.now().plusSeconds(r.getExpiresIn());
    }
}