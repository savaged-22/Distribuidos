package com.acme.biblio.ga.service;

import com.acme.biblio.ga.domain.GaIdempotency;
import com.acme.biblio.ga.domain.Prestamo;
import com.acme.biblio.ga.repository.GaIdempotencyRepository;
import com.acme.biblio.ga.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RenovacionService {

    private final PrestamoRepository prestamoRepo;
    private final GaIdempotencyRepository idempotencyRepo;

    public RenovacionService(PrestamoRepository prestamoRepo,
                             GaIdempotencyRepository idempotencyRepo) {
        this.prestamoRepo = prestamoRepo;
        this.idempotencyRepo = idempotencyRepo;
    }

    /**
     * Procesa la renovación de un préstamo existente.
     *
     * @param usuarioId  ID del usuario que tiene el préstamo
     * @param libroId    ID del libro prestado
     * @param sedeOrigen Sede desde donde se hace la operación (A/B).
     *                   Por ahora no afecta stock, pero se mantiene para trazabilidad/simetría.
     */
   @Transactional
public Prestamo procesarRenovacion(String usuarioId, String libroId, String sedeOrigen) {

    String key = "RENOV-" + usuarioId + "-" + libroId;
    if (idempotencyRepo.existsById(key)) {
        throw new IllegalStateException("Comando de renovación ya fue procesado previamente");
    }

    Prestamo prestamo = prestamoRepo
            .findTopByUsuarioUsuarioIdAndLibroLibroIdAndEstadoOrderByFechaInicioDesc(
                    usuarioId, libroId, "ACTIVO"
            )
            .orElseThrow(() ->
                    new IllegalArgumentException("No existe un préstamo activo para ese usuario y libro")
            );

    if (prestamo.getRenovaciones() >= 2) {
        throw new IllegalStateException("No se permiten más de 2 renovaciones para este préstamo");
    }

    prestamo.setFechaEntrega(prestamo.getFechaEntrega().plusDays(7));
    prestamo.setRenovaciones(prestamo.getRenovaciones() + 1);
    prestamoRepo.save(prestamo);

    idempotencyRepo.save(new GaIdempotency(
            key,
            LocalDate.now(),
            "Renovación aplicada"
    ));

    return prestamo;
}


}
