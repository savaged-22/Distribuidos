package com.acme.biblio.infra;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class ZmqSubscriber implements AutoCloseable {
  private final ZMQ.Socket sub;
  private final ExecutorService loop = Executors.newSingleThreadExecutor();
  private volatile boolean running = false;

  public ZmqSubscriber(ZContext ctx, String host, int port, String topic){
    sub = ctx.createSocket(ZMQ.SUB);
    sub.connect("tcp://" + host + ":" + port);
    sub.subscribe(topic.getBytes(StandardCharsets.UTF_8));
  }

  public void start(SubscriberCallback cb){
    running = true;
    loop.submit(() -> {
      while(running && !Thread.currentThread().isInterrupted()){
        String t = sub.recvStr(); // topic
        String p = sub.recvStr(); // payload
        if(t!=null && p!=null) cb.onMessage(t, p);
      }
    });
  }

  @Override public void close(){
    running = false;
    loop.shutdownNow();
    sub.close();
  }
}
