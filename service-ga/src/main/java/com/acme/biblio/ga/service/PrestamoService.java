package com.acme.biblio.ga.service;

import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.PrestamoDenied;
import com.acme.biblio.contracts.PrestamoGranted;
import com.acme.biblio.contracts.Response;
import com.acme.biblio.ga.domain.*;
import com.acme.biblio.ga.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import com.acme.biblio.ga.util.JsonEventMapper;


@Service
public class PrestamoService {

    private final LibroRepository libroRepo;
    private final UsuarioRepository usuarioRepo;
    private final PrestamoRepository prestamoRepo;
    private final GaIdempotencyRepository idempotencyRepo;
    private final GaOutboxRepository outboxRepo;

    public PrestamoService(
            LibroRepository libroRepo,
            UsuarioRepository usuarioRepo,
            PrestamoRepository prestamoRepo,
            GaIdempotencyRepository idempotencyRepo,
            GaOutboxRepository outboxRepo
    ) {
        this.libroRepo = libroRepo;
        this.usuarioRepo = usuarioRepo;
        this.prestamoRepo = prestamoRepo;
        this.idempotencyRepo = idempotencyRepo;
        this.outboxRepo = outboxRepo;
    }


    @Transactional
    public Response procesarPrestamo(PrestamoCmd cmd) {

        var h = cmd.headers();

        // 1️⃣ Idempotencia usando la key del mensaje
        if (idempotencyRepo.existsById(h.idempotencyKey())) {
            return new PrestamoDenied(h, "Duplicate command");
        }

        // 2️⃣ Validar usuario
        Usuario usuario = usuarioRepo.findById(h.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe: " + h.usuarioId()));

        // 3️⃣ Validar libro
        Libro libro = libroRepo.findById(h.libroId())
                .orElseThrow(() -> new IllegalArgumentException("Libro no existe: " + h.libroId()));

        // 4️⃣ Stock según sede origen
        int stock = h.sedeOrigen().equalsIgnoreCase("A")
                ? libro.getStockSedeA()
                : libro.getStockSedeB();

        if (stock <= 0) {
            return new PrestamoDenied(h, "No hay stock disponible en sede " + h.sedeOrigen());
        }

        // 5️⃣ Crear préstamo
        Prestamo p = new Prestamo();
        p.setUsuario(usuario);
        p.setLibro(libro);
        p.setFechaInicio(LocalDate.now());
        p.setFechaEntrega(LocalDate.now().plusDays(7));
        p.setRenovaciones(0);
        p.setEstado("ACTIVO");

        prestamoRepo.save(p);

        // 6️⃣ Actualizar stock
        if (h.sedeOrigen().equalsIgnoreCase("A")) {
            libro.setStockSedeA(stock - 1);
        } else {
            libro.setStockSedeB(stock - 1);
        }
        libroRepo.save(libro);

        // 7️⃣ Guardar registro de idempotencia
        idempotencyRepo.save(new GaIdempotency(
                h.idempotencyKey(),
                LocalDate.now(),
                "PRESTAMO"
        ));

        // 8️⃣ Crear evento de salida
        PrestamoGranted evt = new PrestamoGranted(h, p.getFechaEntrega());

       // Convertir a JSON usando el mapper centralizado
String payload = JsonEventMapper.toJson(evt);

outboxRepo.save(new GaOutbox(
        null,
        evt.getClass().getSimpleName(), // o evt.topic().value() si tu evento tiene topic
        payload,
        LocalDate.now(),
        null
));

return evt;

    }
}
