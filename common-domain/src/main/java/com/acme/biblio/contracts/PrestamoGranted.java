package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PrestamoGranted(
    @NotNull MessageHeaders headers,
    @NotNull LocalDate fechaEntrega
) implements Response { }
