package com.facturacion.negocio.utils;

public class Normalizador {

    private Normalizador(){}

    public static String normalizar(String text){
        if (text == null || text.isBlank()){
            return null;
        }
        return text.trim();

    }
}
