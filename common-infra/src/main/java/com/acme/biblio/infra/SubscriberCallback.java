package com.acme.biblio.infra;
@FunctionalInterface
public interface SubscriberCallback {
  void onMessage(String topic, String payload);
}
