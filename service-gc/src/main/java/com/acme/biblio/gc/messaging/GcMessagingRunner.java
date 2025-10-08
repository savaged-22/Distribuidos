package com.acme.biblio.gc.messaging;

import com.acme.biblio.contracts.*;
import com.acme.biblio.infra.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;

@Component
public class GcMessagingRunner {
  private static final Logger log = LoggerFactory.getLogger(GcMessagingRunner.class);

  @Value("${zmq.gc.reqrep.port}") private int repPort;
  @Value("${zmq.pub.dev.port}")   private int devPubPort;
  @Value("${zmq.pub.ren.port}")   private int renPubPort;

  private ZContextManager zcm;
  private ZmqRepServer repServer;
  private ZmqPublisher devPub;
  private ZmqPublisher renPub;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    this.zcm = new ZContextManager();
    ZContext ctx = zcm.context();
    this.devPub = new ZmqPublisher(ctx, devPubPort);
    this.renPub = new ZmqPublisher(ctx, renPubPort);
    this.repServer = new ZmqRepServer(ctx, repPort);

    log.info("GC ready: REP on {}, PUB dev={}, ren={}", repPort, devPubPort, renPubPort);

    repServer.start(payload -> {
      // Llega un JSON de Command
      Message msg = JsonCodec.fromJson(payload, Message.class);
      if(!(msg instanceof Command cmd)) {
        log.warn("Payload no es Command: {}", payload);
        return "{\"status\":\"ERROR\",\"msg\":\"not a Command\"}";
      }
      if(cmd instanceof DevolucionCmd dc){
        // Publicar evento Devolución
        var evt = new DevolucionRecibidaEvt(dc.headers());
        devPub.publishJson(Topic.DEVOLUCION.value(), evt);
        log.info("ACK DEV corrId={}", dc.headers().correlationId());
        return "{\"status\":\"ACK\",\"op\":\"DEVOLUCION\"}";
      }
      if(cmd instanceof RenovacionCmd rc){
        var evt = new RenovacionSolicitadaEvt(rc.headers());
        renPub.publishJson(Topic.RENOVACION.value(), evt);
        log.info("ACK REN corrId={}", rc.headers().correlationId());
        return "{\"status\":\"ACK\",\"op\":\"RENOVACION\"}";
      }
      if(cmd instanceof PrestamoCmd){
        // Para E1 no se requiere: lo dejamos como no implementado
        log.info("PRESTAMO recibido (stub) corrId={}", cmd.headers().correlationId());
        return "{\"status\":\"ERROR\",\"msg\":\"PRESTAMO not wired yet\"}";
      }
      return "{\"status\":\"ERROR\",\"msg\":\"unknown command\"}";
    });
  }

  @Override protected void finalize() throws Throwable {
    if(repServer!=null) repServer.close();
    if(devPub!=null) devPub.close();
    if(renPub!=null) renPub.close();
    if(zcm!=null) zcm.close();
    super.finalize();
  }
}
