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

        // 1️⃣ Idempotencia básica (puedes mejorar luego usando idempotencyKey del header)
        String key = "RENOV-" + usuarioId + "-" + libroId;
        if (idempotencyRepo.existsById(key)) {
            throw new IllegalStateException("Comando de renovación ya fue procesado previamente");
        }

        // 2️⃣ Buscar préstamo ACTIVO para ese usuario y libro
        Prestamo prestamo = prestamoRepo
                .findTopByUsuarioUsuarioIdAndLibroLibroIdAndEstadoOrderByFechaInicioDesc(
                        usuarioId, libroId, "ACTIVO"
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("No existe un préstamo activo para ese usuario y libro")
                );

        // 3️⃣ Validar número máximo de renovaciones (ejemplo: máximo 2)
        if (prestamo.getRenovaciones() >= 2) {
            throw new IllegalStateException("No se permiten más de 2 renovaciones para este préstamo");
        }

        // 4️⃣ Actualizar fecha de entrega y contador de renovaciones
        //    Aquí asumimos +7 días; ajústalo si el enunciado define otra cosa.
        prestamo.setFechaEntrega(prestamo.getFechaEntrega().plusDays(7));
        prestamo.setRenovaciones(prestamo.getRenovaciones() + 1);

        prestamoRepo.save(prestamo);

        // 5️⃣ Registrar idempotencia de la operación
        idempotencyRepo.save(new GaIdempotency(
                key,
                LocalDate.now(),
                "Renovación aplicada"
        ));

        // 6️⃣ Futuro: aquí podrías publicar un evento a GA_OUTBOX (RenovacionSolicitadaEvt)

        return prestamo;
    }
}
