package com.acme.biblio.ga.messaging.handler;

import com.acme.biblio.contracts.MessageHeaders;
import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.PrestamoDenied;
import com.acme.biblio.contracts.PrestamoGranted;
import com.acme.biblio.contracts.Response;
import com.acme.biblio.ga.domain.Prestamo;
import com.acme.biblio.ga.service.PrestamoService;
import org.springframework.stereotype.Component;

@Component
public class PrestamoCommandHandler {

    private final PrestamoService service;

    public PrestamoCommandHandler(PrestamoService service) {
        this.service = service;
    }

    /**
     * Maneja el comando PrestamoCmd, delega al dominio
     * y construye la Response (Granted / Denied).
     */
    public Response handle(PrestamoCmd cmd) {

        MessageHeaders h = cmd.headers();

        try {
            // El servicio ya usa todo el cmd (usuario, libro, sede, idempotencia, etc.)
            Prestamo prestamo = service.procesarPrestamo(cmd);

            // Éxito → PrestamoGranted con la fechaEntrega del dominio
            return new PrestamoGranted(
                    h,
                    prestamo.getFechaEntrega()
            );

        } catch (Exception ex) {
            // Error de negocio → PrestamoDenied con el motivo
            return new PrestamoDenied(
                    h,
                    ex.getMessage()
            );
        }
    }
}
