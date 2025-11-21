package com.acme.biblio.ga.service;

import com.acme.biblio.ga.domain.GaIdempotency;
import com.acme.biblio.ga.domain.Libro;
import com.acme.biblio.ga.domain.Prestamo;
import com.acme.biblio.ga.repository.GaIdempotencyRepository;
import com.acme.biblio.ga.repository.LibroRepository;
import com.acme.biblio.ga.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DevolucionService {

    private final PrestamoRepository prestamoRepo;
    private final LibroRepository libroRepo;
    private final GaIdempotencyRepository idempotencyRepo;

    public DevolucionService(PrestamoRepository prestamoRepo,
                             LibroRepository libroRepo,
                             GaIdempotencyRepository idempotencyRepo) {
        this.prestamoRepo = prestamoRepo;
        this.libroRepo = libroRepo;
        this.idempotencyRepo = idempotencyRepo;
    }

    /**
     * Procesa la devolución de un préstamo:
     * - Marca el préstamo como DEV.
     * - Incrementa el stock en la sede correspondiente.
     * - Registra idempotencia para evitar reprocesos.
     */
    @Transactional
public Prestamo procesarDevolucion(String usuarioId, String libroId, String sedeOrigen) {
    String key = "DEV-" + usuarioId + "-" + libroId;
    if (idempotencyRepo.existsById(key)) {
        throw new IllegalStateException("Comando de devolución ya fue procesado anteriormente");
    }

    Prestamo prestamo = prestamoRepo
            .findTopByUsuarioUsuarioIdAndLibroLibroIdAndEstadoOrderByFechaInicioDesc(
                    usuarioId, libroId, "ACTIVO"
            )
            .orElseThrow(() ->
                    new IllegalArgumentException("No existe un préstamo activo para ese usuario y libro")
            );

    prestamo.setEstado("DEV");
    prestamoRepo.save(prestamo);

    Libro libro = prestamo.getLibro();
    ...
    libroRepo.save(libro);

    idempotencyRepo.save(new GaIdempotency(
            key,
            LocalDate.now(),
            "Devolución aplicada"
    ));

    return prestamo;
}

}
