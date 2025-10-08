package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Event")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DevolucionRecibidaEvt.class,   name = "DevolucionRecibidaEvt"),
  @JsonSubTypes.Type(value = RenovacionSolicitadaEvt.class, name = "RenovacionSolicitadaEvt")
})
public sealed interface Event extends Message permits DevolucionRecibidaEvt, RenovacionSolicitadaEvt {
  Topic topic();
}
