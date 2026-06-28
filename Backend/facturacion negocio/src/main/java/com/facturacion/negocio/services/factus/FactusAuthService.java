package com.facturacion.negocio.services.factus;

import com.facturacion.negocio.errors.FactusSinConexionException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class FactusAuthService {

    private final ConcurrentMap<String, TokenCache> tokensByConfig = new ConcurrentHashMap<>();

    /**
     * Devuelve un access token válido. Si está expirado, lo renueva.
     */
    public String getValidAccessToken(FactusTenantConfig config) {
        String cacheKey = cacheKey(config);
        TokenCache cache = tokensByConfig.computeIfAbsent(cacheKey, key -> new TokenCache());

        synchronized (cache) {
            return getValidAccessToken(config, cache);
        }
    }

    private String getValidAccessToken(FactusTenantConfig config, TokenCache cache) {
        // margen de seguridad para no expirar en mitad de una request
        if (cache.accessToken != null && cache.expiresAt != null
                && Instant.now().isBefore(cache.expiresAt.minusSeconds(30))) {
            return cache.accessToken;
        }

        // 1) intenta refresh si existe
        if (cache.refreshToken != null && !cache.refreshToken.isBlank()) {
            try {
                FactusTokenResponse refreshed = refreshWithRefreshToken(config, cache.refreshToken);
                cacheTokens(cache, refreshed);
                return cache.accessToken;
            } catch (Exception ignored) {
                // si refresh falla, hacemos login normal
            }
        }

        // 2) login con password
        FactusTokenResponse token = loginWithPassword(config);
        cacheTokens(cache, token);
        return cache.accessToken;
    }

    private FactusTokenResponse loginWithPassword(FactusTenantConfig config) {

        try {
            FactusTokenResponse r = webClient(config).post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("grant_type", "password")
                            .with("client_id", config.clientId())
                            .with("client_secret", config.clientSecret())
                            .with("username", config.username())
                            .with("password", config.password())
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

    private FactusTokenResponse refreshWithRefreshToken(FactusTenantConfig config, String refreshToken) {
        try {
            FactusTokenResponse r = webClient(config).post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("grant_type", "refresh_token")
                            .with("client_id", config.clientId())
                            .with("client_secret", config.clientSecret())
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

    private void cacheTokens(TokenCache cache, FactusTokenResponse r) {
        cache.accessToken = r.getAccessToken();
        cache.refreshToken = r.getRefreshToken();
        cache.expiresAt = Instant.now().plusSeconds(r.getExpiresIn());
    }

    private WebClient webClient(FactusTenantConfig config) {
        return WebClient.builder()
                .baseUrl(config.baseUrl())
                .build();
    }

    private String cacheKey(FactusTenantConfig config) {
        return config.id() + "|" + config.baseUrl() + "|" + config.username() + "|" + config.clientId();
    }

    private static class TokenCache {
        private volatile String accessToken;
        private volatile String refreshToken;
        private volatile Instant expiresAt;
    }
}
