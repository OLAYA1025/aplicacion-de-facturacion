package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.dto.response.PedidoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    private static final String EVENTO_PEDIDO_ACTUALIZADO = "nuevo-pedido";

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emisoresPorNegocio = new ConcurrentHashMap<>();
    private final TenantService tenantService;

    public SseService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public SseEmitter agregarEmitter() {
        Long negocioId = tenantService.getNegocioIdActual();
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emisoresPorNegocio
                .computeIfAbsent(negocioId, ignored -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removerEmitter(negocioId, emitter));
        emitter.onTimeout(() -> removerEmitter(negocioId, emitter));
        emitter.onError((error) -> removerEmitter(negocioId, emitter));

        return emitter;
    }

    public void emitirActualizacionPedido(Pedido pedido) {
        emitirActualizacionPedido(pedido, new ArrayList<>(pedido.getDetallePedidos()));
    }

    public void emitirActualizacionPedido(Pedido pedido, List<DetallePedido> detalles) {
        enviarEventoPedido(PedidoResponseDTO.from(pedido, detalles));
    }

    private void enviarEventoPedido(PedidoResponseDTO dto) {
        Long negocioId = tenantService.getNegocioIdActual();
        List<SseEmitter> emisores = emisoresPorNegocio.get(negocioId);
        if (emisores == null || emisores.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emisores) {
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENTO_PEDIDO_ACTUALIZADO)
                        .data(dto));
            } catch (Exception e) {
                removerEmitter(negocioId, emitter);
            }
        }
    }

    private void removerEmitter(Long negocioId, SseEmitter emitter) {
        List<SseEmitter> emisores = emisoresPorNegocio.get(negocioId);
        if (emisores == null) {
            return;
        }
        emisores.remove(emitter);
        if (emisores.isEmpty()) {
            emisoresPorNegocio.remove(negocioId, emisores);
        }
    }
}
