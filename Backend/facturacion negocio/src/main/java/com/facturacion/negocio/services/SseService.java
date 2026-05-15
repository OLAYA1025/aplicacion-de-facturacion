package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.dto.response.PedidoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {
    private static final String EVENTO_PEDIDO_ACTUALIZADO = "nuevo-pedido";

    private final List<SseEmitter> emisores = new CopyOnWriteArrayList<>();

    public SseEmitter agregarEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emisores.add(emitter);

        emitter.onCompletion(() -> removerEmitter(emitter));
        emitter.onTimeout(() -> removerEmitter(emitter));
        emitter.onError((error) -> removerEmitter(emitter));

        return emitter;
    }

    public void emitirActualizacionPedido(Pedido pedido) {
        emitirActualizacionPedido(pedido, new ArrayList<>(pedido.getDetallePedidos()));
    }

    public void emitirActualizacionPedido(Pedido pedido, List<DetallePedido> detalles) {
        enviarEventoPedido(PedidoResponseDTO.from(pedido, detalles));
    }

    private void enviarEventoPedido(PedidoResponseDTO dto) {
        for (SseEmitter emitter : emisores) {
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENTO_PEDIDO_ACTUALIZADO)
                        .data(dto));
            } catch (Exception e) {
                removerEmitter(emitter);
            }
        }
    }

    private void removerEmitter(SseEmitter emitter) {
        emisores.remove(emitter);
    }
}
