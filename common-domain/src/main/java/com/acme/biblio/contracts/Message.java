package com.acme.biblio.contracts;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
public sealed interface Message permits Command, Event, Response {
  MessageHeaders headers();
}
