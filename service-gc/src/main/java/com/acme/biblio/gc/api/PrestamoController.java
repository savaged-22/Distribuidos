package com.acme.biblio.gc.api;

import com.acme.biblio.contracts.MessageHeaders;
import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.PrestamoDenied;
import com.acme.biblio.contracts.Response;
import com.acme.biblio.gc.api.dto.SolicitudPrestamoDto;
import com.acme.biblio.gc.client.GAClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/gc/prestamos")
public class PrestamoController {

    private final GAClient gaClient;

    public PrestamoController(GAClient gaClient) {
        this.gaClient = gaClient;
    }

    @PostMapping
    public ResponseEntity<Response> solicitarPrestamo(@RequestBody SolicitudPrestamoDto dto) {

        // Construimos los headers del contrato
        MessageHeaders headers = new MessageHeaders(
                UUID.randomUUID().toString(),        // correlationId
                "gc-" + UUID.randomUUID(),           // idempotencyKey
                dto.usuarioId(),
                dto.libroId(),
                dto.sedeOrigen(),
                Instant.now(),
                "1.0.0"                               // schemaVersion
        );

        PrestamoCmd cmd = new PrestamoCmd(headers);

        Response resp = gaClient.solicitarPrestamo(cmd);

        if (resp instanceof PrestamoDenied denied) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(denied);
        }

        return ResponseEntity.ok(resp);
    }
}
