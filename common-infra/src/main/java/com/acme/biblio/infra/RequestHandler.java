package com.acme.biblio.infra;
@FunctionalInterface
public interface RequestHandler {
  String handle(String payload) throws Exception;
}
