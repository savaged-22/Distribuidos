package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Response")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PrestamoGranted.class, name = "PrestamoGranted"),
  @JsonSubTypes.Type(value = PrestamoDenied.class,  name = "PrestamoDenied")
})
public sealed interface Response extends Message permits PrestamoGranted, PrestamoDenied { }
