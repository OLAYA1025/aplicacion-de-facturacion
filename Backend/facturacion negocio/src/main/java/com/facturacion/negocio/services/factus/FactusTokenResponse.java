package com.facturacion.negocio.services.factus;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FactusTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;          // <-- antes long

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}