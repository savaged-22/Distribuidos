package com.acme.biblio.infra;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.nio.charset.StandardCharsets;

public final class ZmqPublisher implements AutoCloseable {
  private final ZMQ.Socket pub;
  public ZmqPublisher(ZContext ctx, int port) {
    pub = ctx.createSocket(ZMQ.PUB);
    pub.bind("tcp://*:" + port);
  }
  public void publish(String topic, String payload){
    pub.sendMore(topic);
    pub.send(payload.getBytes(StandardCharsets.UTF_8), 0);
  }
  public void publishJson(String topic, Object message){
    publish(topic, JsonCodec.toJson(message));
  }
  @Override public void close(){ pub.close(); }
}
