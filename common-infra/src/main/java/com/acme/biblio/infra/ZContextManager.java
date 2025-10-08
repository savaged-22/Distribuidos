package com.acme.biblio.infra;
import org.zeromq.ZContext;
public final class ZContextManager implements AutoCloseable {
  private final ZContext ctx = new ZContext();
  public ZContext context(){ return ctx; }
  @Override public void close(){ ctx.close(); }
}
