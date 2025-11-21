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

        // 1️⃣ Idempotencia básica por operación + usuario + libro
        String key = "DEV-" + usuarioId + "-" + libroId;
        if (idempotencyRepo.existsById(key)) {
            throw new IllegalStateException("Comando de devolución ya fue procesado anteriormente");
        }

        // 2️⃣ Buscar préstamo ACTIVO para ese usuario y libro
        Prestamo prestamo = prestamoRepo
                .findTopByUsuarioUsuarioIdAndLibroLibroIdAndEstadoOrderByFechaInicioDesc(
                        usuarioId, libroId, "ACTIVO"
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("No existe un préstamo activo para ese usuario y libro")
                );

        // 3️⃣ Marcar el préstamo como devuelto
        prestamo.setEstado("DEV");
        prestamoRepo.save(prestamo);

        // 4️⃣ Actualizar stock en LIBRO según sede de origen
        Libro libro = prestamo.getLibro();
        if (sedeOrigen == null) {
            throw new IllegalArgumentException("La sede de origen no puede ser nula en una devolución");
        }

        if ("A".equalsIgnoreCase(sedeOrigen)) {
            libro.setStockSedeA(libro.getStockSedeA() + 1);
        } else if ("B".equalsIgnoreCase(sedeOrigen)) {
            libro.setStockSedeB(libro.getStockSedeB() + 1);
        } else {
            throw new IllegalArgumentException("Sede de origen inválida: " + sedeOrigen);
        }

        libroRepo.save(libro);

        // 5️⃣ Registrar idempotencia de la operación
        idempotencyRepo.save(new GaIdempotency(
                key,
                LocalDate.now(),
                "Devolución aplicada"
        ));

        // 6️⃣ Futuro: aquí podríamos registrar un evento DevolucionRecibidaEvt en GA_OUTBOX

        return prestamo;
    }
}
