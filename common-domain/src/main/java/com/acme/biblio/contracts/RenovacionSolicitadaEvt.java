package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RenovacionSolicitadaEvt(
    @NotNull MessageHeaders headers
) implements Event {
  @Override public Topic topic() { return Topic.RENOVACION; }
}
