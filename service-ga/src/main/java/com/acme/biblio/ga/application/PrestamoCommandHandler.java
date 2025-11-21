package com.acme.biblio.ga.application;

import com.acme.biblio.contracts.*;
import com.acme.biblio.ga.service.PrestamoService;
import com.acme.biblio.ga.util.SedeMapper;
import com.acme.biblio.ga.util.JsonEventMapper;
import org.springframework.stereotype.Component;

@Component
public class PrestamoCommandHandler {

    private final PrestamoService prestamoService;

    public PrestamoCommandHandler(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    public Response handle(PrestamoCmd cmd) {
        MessageHeaders h = cmd.headers();

        // Normalizar sede
        String sede = SedeMapper.normalize(h.sedeOrigen());

        try {
            // Delegar al servicio
            var prestamo = prestamoService.procesarPrestamo(
                    h.usuarioId(),
                    h.libroId(),
                    sede
            );

            // Respuesta exitosa → PrestamoGranted
            return new PrestamoGranted(
                    h,
                    prestamo.getFechaEntrega()
            );

        } catch (Exception ex) {
            // Respuesta negativa → PrestamoDenied
            return new PrestamoDenied(
                    h,
                    ex.getMessage()
            );
        }
    }
}
