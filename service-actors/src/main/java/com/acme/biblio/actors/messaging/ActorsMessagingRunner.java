package com.acme.biblio.actors.messaging;

import com.acme.biblio.contracts.*;
import com.acme.biblio.infra.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ActorsMessagingRunner {
  private static final Logger log = LoggerFactory.getLogger(ActorsMessagingRunner.class);

  @Value("${gc.host}")               private String gcHost;
  @Value("${zmq.pub.dev.port}")      private int devPort;
  @Value("${zmq.pub.ren.port}")      private int renPort;

  @Value("${ga.host}")               private String gaHost;
  @Value("${ga.rep.port}")           private int gaPort;
  @Value("${ga.timeoutMs}")          private int gaTimeout;

  private ZContextManager zcm;
  private ZmqSubscriber devSub;
  private ZmqSubscriber renSub;
  private ZmqReqClient  gaDevClient;
  private ZmqReqClient  gaRenClient;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    this.zcm = new ZContextManager();
    devSub = new ZmqSubscriber(zcm.context(), gcHost, devPort, Topic.DEVOLUCION.value());
    renSub = new ZmqSubscriber(zcm.context(), gcHost, renPort, Topic.RENOVACION.value());

    // Un REQ client por tipo de evento (evita contención)
    gaDevClient = new ZmqReqClient(zcm.context(), gaHost, gaPort, gaTimeout);
    gaRenClient = new ZmqReqClient(zcm.context(), gaHost, gaPort, gaTimeout);

    devSub.start((topic, payload) -> {
      DevolucionRecibidaEvt evt = JsonCodec.fromJson(payload, DevolucionRecibidaEvt.class);
      log.info("Actor-Devolucion consume corrId={} libroId={}", evt.headers().correlationId(), evt.headers().libroId());
      Optional<String> r = gaDevClient.request(payload);
      log.info("GA resp DEV corrId={} -> {}", evt.headers().correlationId(), r.orElse("<timeout>"));
    });

    renSub.start((topic, payload) -> {
      RenovacionSolicitadaEvt evt = JsonCodec.fromJson(payload, RenovacionSolicitadaEvt.class);
      log.info("Actor-Renovacion consume corrId={} libroId={}", evt.headers().correlationId(), evt.headers().libroId());
      Optional<String> r = gaRenClient.request(payload);
      log.info("GA resp REN corrId={} -> {}", evt.headers().correlationId(), r.orElse("<timeout>"));
    });

    log.info("Actores SUB: dev {}:{}, ren {}:{}; GA at {}:{}", gcHost, devPort, gcHost, renPort, gaHost, gaPort);
  }
}
