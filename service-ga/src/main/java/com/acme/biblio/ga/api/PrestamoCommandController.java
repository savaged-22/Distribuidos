package com.acme.biblio.ga.api;

import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.Response;
import com.acme.biblio.contracts.PrestamoDenied;
import com.acme.biblio.ga.messaging.handler.PrestamoCommandHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commands")
public class PrestamoCommandController {

    private final PrestamoCommandHandler handler;

    public PrestamoCommandController(PrestamoCommandHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/prestamo")
    public ResponseEntity<Response> procesarPrestamo(@RequestBody @Valid PrestamoCmd cmd) {
        Response resp = handler.handle(cmd);

        // Si fue denegado, devolvemos 409 (o 400, según como quieran manejarlo)
        if (resp instanceof PrestamoDenied) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
        }

        // Si fue concedido → 200 OK
        return ResponseEntity.ok(resp);
    }
}
