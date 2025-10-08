package com.acme.biblio.contracts;
public enum Topic {
  DEVOLUCION("Devolucion"),
  RENOVACION("Renovacion");
  private final String value;
  Topic(String v){ this.value = v; }
  public String value(){ return value; }
}
