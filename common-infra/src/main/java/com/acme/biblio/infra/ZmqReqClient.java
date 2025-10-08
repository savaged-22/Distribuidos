package com.acme.biblio.infra;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class ZmqReqClient implements AutoCloseable {
  private final ZMQ.Socket req;

  public ZmqReqClient(ZContext ctx, String host, int port, int rcvTimeoutMs){
    req = ctx.createSocket(ZMQ.REQ);
    req.setReceiveTimeOut(rcvTimeoutMs);
    req.connect("tcp://" + host + ":" + port);
  }

  public Optional<String> request(String payload){
    req.send(payload.getBytes(StandardCharsets.UTF_8), 0);
    byte[] resp = req.recv();
    if(resp == null) return Optional.empty();
    return Optional.of(new String(resp, StandardCharsets.UTF_8));
  }

  @Override public void close(){ req.close(); }
}
