package com.acme.biblio.ga.messaging.handler;

import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.Response;
import com.acme.biblio.ga.service.PrestamoService;
import org.springframework.stereotype.Component;

@Component
public class PrestamoCommandHandler {

    private final PrestamoService prestamoService;

    public PrestamoCommandHandler(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /**
     * Maneja el comando PrestamoCmd delegando la lógica al PrestamoService.
     * El servicio devuelve un Response:
     *  - PrestamoGranted si se otorga el préstamo
     *  - PrestamoDenied si hay algún problema (stock, usuario/libro inexistente, etc.)
     */
    public Response handle(PrestamoCmd cmd) {
        return prestamoService.procesarPrestamo(cmd);
    }
}
