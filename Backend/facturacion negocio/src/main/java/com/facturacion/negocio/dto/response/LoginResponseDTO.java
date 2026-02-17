package com.facturacion.negocio.dto.response;

public class LoginResponseDTO {
    private String token;
    private String tipo;
    private Long expiracionEpochMillis;
    private UsuarioResponseDTO usuario;

    public static LoginResponseDTO of(String token, long expiracionEpochMillis, UsuarioResponseDTO usuario) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setTipo("Bearer");
        response.setExpiracionEpochMillis(expiracionEpochMillis);
        response.setUsuario(usuario);
        return response;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getExpiracionEpochMillis() {
        return expiracionEpochMillis;
    }

    public void setExpiracionEpochMillis(Long expiracionEpochMillis) {
        this.expiracionEpochMillis = expiracionEpochMillis;
    }

    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponseDTO usuario) {
        this.usuario = usuario;
    }
}
