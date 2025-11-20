package com.acme.biblio.ga.messaging.handler;

import com.acme.biblio.contracts.PrestamoCmd;
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
     * Maneja el comando PrestamoCmd y llama a la capa de dominio.
     */
    public void handle(PrestamoCmd cmd) {
        String usuarioId = cmd.headers().usuarioId();
        String libroId = cmd.headers().libroId();
        String sede = SedeMapper.normalize(cmd.headers().sedeOrigen());

        service.procesarPrestamo(usuarioId, libroId, sede);

        // 🔜 En el futuro produciremos respuesta PrestamoGranted y evento a GA_OUTBOX
    }
}
