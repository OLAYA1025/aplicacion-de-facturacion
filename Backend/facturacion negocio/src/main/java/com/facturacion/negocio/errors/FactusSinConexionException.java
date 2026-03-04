package com.facturacion.negocio.errors;

public class FactusSinConexionException extends RuntimeException{
    public FactusSinConexionException(String msg, Throwable cause) { super(msg, cause); }
}
