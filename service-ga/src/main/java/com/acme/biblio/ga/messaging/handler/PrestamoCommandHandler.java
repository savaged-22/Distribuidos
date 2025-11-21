package com.acme.biblio.ga.messaging.handler;

import com.acme.biblio.contracts.MessageHeaders;
import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.PrestamoDenied;
import com.acme.biblio.contracts.PrestamoGranted;
import com.acme.biblio.contracts.Response;
import com.acme.biblio.ga.domain.Prestamo;
import com.acme.biblio.ga.service.PrestamoService;
import com.acme.biblio.ga.util.SedeMapper;
import org.springframework.stereotype.Component;

@Component
public class PrestamoCommandHandler {

    private final PrestamoService service;

    public PrestamoCommandHandler(PrestamoService service) {
        this.service = service;
    }

    /**
     * Maneja el comando PrestamoCmd:
     *  - Normaliza la sede.
     *  - Llama a la capa de dominio (PrestamoService).
     *  - Devuelve un Response de contratos:
     *      - PrestamoGranted si todo sale bien.
     *      - PrestamoDenied si hay algún error de negocio.
     */
    public Response handle(PrestamoCmd cmd) {
        MessageHeaders h = cmd.headers();

        String usuarioId = h.usuarioId();
        String libroId   = h.libroId();
        String sede      = SedeMapper.normalize(h.sedeOrigen());

        try {
            // Lógica de dominio: registra el préstamo y actualiza stock
            Prestamo prestamo = service.procesarPrestamo(usuarioId, libroId, sede);

            // Éxito → respondemos con PrestamoGranted
            return new PrestamoGranted(
                    h,
                    prestamo.getFechaEntrega()
            );

        } catch (Exception ex) {
            // Cualquier excepción de negocio → PrestamoDenied
            return new PrestamoDenied(
                    h,
                    ex.getMessage()
            );
        }
    }
}
