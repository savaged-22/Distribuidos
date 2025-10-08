package com.acme.biblio.ga.messaging;

import com.acme.biblio.contracts.*;
import com.acme.biblio.ga.store.GaStore;
import com.acme.biblio.infra.JsonCodec;
import com.acme.biblio.infra.ZContextManager;
import com.acme.biblio.infra.ZmqRepServer;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;

@Component
public class GaRepRunner {
  private static final Logger log = LoggerFactory.getLogger(GaRepRunner.class);

  @Value("${ga.server.rep.port}") private int repPort;
  @Value("${ga.local.path}")      private String basePath;

  private ZContextManager zcm;
  private ZmqRepServer rep;
  private GaStore store;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    store = new GaStore(Path.of(basePath));
    store.init();

    zcm = new ZContextManager();
    rep = new ZmqRepServer(zcm.context(), repPort);
    log.info("GA REP listening on {}", repPort);

    rep.start(payload -> {
      // Espera un JSON con 'kind' de Event
      Message msg = JsonCodec.fromJson(payload, Message.class);
      if(!(msg instanceof Event evt)) {
        log.warn("Payload no es Event: {}", payload);
        return JsonCodec.toJson(Map.of("status","ERROR","msg","not an Event"));
      }
      String idem = evt.headers().idempotencyKey();
      if (store.isApplied(idem)) {
        return JsonCodec.toJson(Map.of("status","DUPLICATE","idempotencyKey", idem));
      }
      // Persistencia simple: solo registrar el evento (E1)
      store.appendEvent(evt);
      store.markApplied(idem);
      log.info("APPLIED {} corrId={} idem={}", evt.topic(), evt.headers().correlationId(), idem);
      return JsonCodec.toJson(Map.of("status","APPLIED","topic", evt.topic().value()));
    });
  }

  @Bean
  public org.springframework.boot.actuate.health.HealthIndicator gaHealth() {
    return () -> store!=null && store.writable()
        ? org.springframework.boot.actuate.health.Health.up().withDetail("path", basePath).build()
        : org.springframework.boot.actuate.health.Health.down().withDetail("path", basePath).build();
  }
}
