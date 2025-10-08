package com.acme.biblio.ps.run;

import com.acme.biblio.contracts.*;
import com.acme.biblio.infra.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class SendOnceRunner implements CommandLineRunner {
  private static final Logger log = LoggerFactory.getLogger(SendOnceRunner.class);

  @Value("${gc.sedeA.host}")       private String host;
  @Value("${gc.sedeA.reqrepPort}") private int port;

  @Override
  public void run(String... args) {
    // Construye headers base
    MessageHeaders base = new MessageHeaders(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        "usuario-001",
        "libro-123",
        "A",
        Instant.now(),
        ContractsVersion.CURRENT
    );

    var dev = new DevolucionCmd(base);
    var ren = new RenovacionCmd(new MessageHeaders(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        "usuario-002",
        "libro-456",
        "A",
        Instant.now(),
        ContractsVersion.CURRENT
    ));

    String devJson = JsonCodec.toJson(dev);
    String renJson = JsonCodec.toJson(ren);

    try (ZContextManager zcm = new ZContextManager();
         ZmqReqClient client = new ZmqReqClient(zcm.context(), host, port, 2000)) {

      Optional<String> r1 = client.request(devJson);
      log.info("ACK Devolucion -> {}", r1.orElse("<timeout>"));

      Optional<String> r2 = client.request(renJson);
      log.info("ACK Renovacion -> {}", r2.orElse("<timeout>"));
    }
  }
}
