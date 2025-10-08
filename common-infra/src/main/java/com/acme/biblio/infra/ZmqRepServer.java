package com.acme.biblio.infra;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public final class ZmqRepServer implements AutoCloseable {
  private final ZMQ.Socket rep;
  private final ExecutorService loop = Executors.newSingleThreadExecutor();
  private volatile boolean running = false;

  public ZmqRepServer(ZContext ctx, int port){
    rep = ctx.createSocket(ZMQ.REP);
    rep.bind("tcp://*:" + port);
  }

  public void start(RequestHandler handler){
    running = true;
    loop.submit(() -> {
      while(running && !Thread.currentThread().isInterrupted()){
        byte[] reqBytes = rep.recv();
        if(reqBytes == null) continue;
        String payload = new String(reqBytes, StandardCharsets.UTF_8);
        String response;
        try { response = handler.handle(payload); }
        catch(Exception e){ response = "{\"status\":\"ERROR\",\"msg\":\""+e.getMessage()+"\"}"; }
        rep.send(response.getBytes(StandardCharsets.UTF_8), 0);
      }
    });
  }

  @Override public void close(){
    running = false;
    loop.shutdownNow();
    rep.close();
  }
}
