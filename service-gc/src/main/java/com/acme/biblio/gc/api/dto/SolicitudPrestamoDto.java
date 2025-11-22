package com.acme.biblio.gc.api.dto;

public record SolicitudPrestamoDto(
        String usuarioId,
        String libroId,
        String sedeOrigen
) {}
