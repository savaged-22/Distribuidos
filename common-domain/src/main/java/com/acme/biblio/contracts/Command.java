package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Command")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DevolucionCmd.class, name = "DevolucionCmd"),
  @JsonSubTypes.Type(value = RenovacionCmd.class,   name = "RenovacionCmd"),
  @JsonSubTypes.Type(value = PrestamoCmd.class,     name = "PrestamoCmd")
})
public sealed interface Command extends Message permits DevolucionCmd, RenovacionCmd, PrestamoCmd {
  Operation operation();
}
