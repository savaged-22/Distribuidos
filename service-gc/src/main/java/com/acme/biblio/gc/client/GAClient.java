package com.acme.biblio.gc.client;

import com.acme.biblio.contracts.PrestamoCmd;
import com.acme.biblio.contracts.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GAClient {

    private final RestClient restClient;

    public GAClient(@Value("${ga.base-url}") String gaBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(gaBaseUrl)
                .build();
    }

    public Response solicitarPrestamo(PrestamoCmd cmd) {
        return restClient.post()
                .uri("/api/commands/prestamo")
                .contentType(MediaType.APPLICATION_JSON)
                .body(cmd)
                .retrieve()
                .body(Response.class);
    }

    // Más adelante podrás agregar:
    // - public void registrarDevolucion(DevolucionCmd cmd)
    // - public void registrarRenovacion(RenovacionCmd cmd)
}
