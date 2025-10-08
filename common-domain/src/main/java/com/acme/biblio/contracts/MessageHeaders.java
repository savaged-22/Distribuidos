package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageHeaders(
    @NotBlank String correlationId,
    @NotBlank String idempotencyKey,
    @NotBlank String usuarioId,
    @NotBlank String libroId,
    @NotBlank String sedeOrigen,
    @NotNull  Instant timestamp,
    @NotBlank String schemaVersion
) {}
