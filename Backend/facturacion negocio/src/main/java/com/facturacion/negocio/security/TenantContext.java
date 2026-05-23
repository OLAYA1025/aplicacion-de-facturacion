package com.facturacion.negocio.security;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setNegocioId(Long negocioId) {
        CURRENT_TENANT.set(negocioId);
    }

    public static Long getNegocioId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
